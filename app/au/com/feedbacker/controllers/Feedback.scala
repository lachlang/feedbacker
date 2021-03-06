package au.com.feedbacker.controllers

import javax.inject.Inject

import au.com.feedbacker.model.FeedbackStatus.FeedbackStatus
import au.com.feedbacker.util.Emailer
import play.api.libs.functional.syntax._
import play.api.libs.json._
import au.com.feedbacker.model._
import org.joda.time.DateTime
import play.api.mvc.Result

/**
 * Created by lachlang on 09/05/2016.
 */
class Feedback @Inject() (person: PersonDao,
                          nomination: NominationDao,
                          feedbackCycle: FeedbackCycleDao,
                          adHocFeedback: AdHocFeedbackDao,
                          emailer: Emailer,
                          sessionManager: SessionManager) extends AuthenticatedController(person, sessionManager) {

  def getPendingFeedbackActions = AuthenticatedAction { user =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(nomination.getPendingNominationsForUser(user.credentials.email) .map {
      case Nomination(id, Some(from), _, status, lastUpdated, _, shared, _, _) =>
        val managerName: String = person.findByEmail(from.managerEmail) match {
          case Some(p) => p.name
          case None => from.managerEmail
        }
        SummaryItem(id, status.toString, from.name, from.role, managerName, lastUpdated, shared) } )))
  }

  def updateFeedbackItem(nominationId: Long) = AuthenticatedRequestAction { (user, json) =>
    validateWritePermission(nominationId, user) match {
      case None => Forbidden
      case Some(n) => {
        val submittedJson: JsResult[Boolean] = json.validate[Boolean]((JsPath \ "body" \ "submit").read[Boolean])
        val shareFeedbackJson: JsResult[Boolean] = json.validate[Boolean]((JsPath \ "body" \ "shareFeedback").read[Boolean])
        val questionsJson: JsResult[Seq[QuestionResponse]] = json.validate[Seq[QuestionResponse]]((JsPath \ "body" \ "questions").read[Seq[QuestionResponse]])
        (questionsJson, submittedJson, shareFeedbackJson) match {
          case (JsSuccess(questions,_), JsSuccess(submitted,_), JsSuccess(shareFeedback,_)) =>
            if (nomination.submitFeedback(nominationId, questions, submitted, feedbackCycle.getSharingSettingsForNomination(nominationId,shareFeedback))) {
              nomination.getDetail(nominationId) match {
                case None => BadRequest
                case Some(updatedNomination) => Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(DetailItem.detailFromNomination(updatedNomination, feedbackCycle.findById(n.cycleId)))))
              }
            }
            else BadRequest
          case _ => BadRequest
        }
      }
    }
  }

  def getFeedbackItem(nominationId: Long) = AuthenticatedAction { user =>
    validateReadPermission(nominationId, user) match {
      case None => NotFound(Json.obj("message" -> "Could not find feedback detail."))
      case Some(n) => Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(DetailItem.detailFromNomination(n, feedbackCycle.findById(n.cycleId)))))
    }
  }

  def getCurrentFeedbackItemsForSelf = AuthenticatedAction { user =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(nomination.getPendingFeedbackItemsForUser(user.credentials.email))))
  }

  def getFeedbackHistoryForSelf = AuthenticatedAction { user  =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(nomination.getHistoryFeedbackForUser(user.credentials.email))))
  }

  // TODO: suspect this is duplicate code with Account#ReportFile#isInReportingLine
  private val nominationWriteFilter: (Nomination, String) => Boolean = (n, email) => n.to.map(_.credentials.email == email).getOrElse(false)

  private val nominationReadFilter: (Option[Person], String) => Boolean = (user, email) => user match {
    case None => false
    case Some(fromPerson) if fromPerson.credentials.email == email => true
    case Some(fromPerson) => nominationReadFilter(person.findByEmail(fromPerson.managerEmail), email)
  }

  private def validateWritePermission(nominationId: Long, toPerson: Person): Option[Nomination] =
    nomination.getDetail(nominationId).filter(nominationWriteFilter(_, toPerson.credentials.email))

  private def validateReadPermission(nominationId: Long, fromPerson: Person): Option[Nomination] =
    nomination.getDetail(nominationId).filter( n =>
      nominationReadFilter(n.from, fromPerson.credentials.email) || nominationWriteFilter(n, fromPerson.credentials.email))

  def createAdHocFeedback = AuthenticatedRequestAction { (user , json) =>
    json.validate[AdHocFeedbackRequest].asOpt.flatMap{request => person.findByEmail(request.toEmail).map{to =>
      (AdHocFeedback(None, user.credentials.email, user.name, user.role, request.toEmail, to.name, to.role, DateTime.now, request.message, request.publishToRecipient), to)}} match {
      case None => BadRequest(Json.obj("message" -> "Invalid request."))
      case Some((feedback, recipient)) =>
        if (feedback.toEmail == feedback.fromEmail) {
          BadRequest(Json.obj("message" -> "Can't send feedback to yourself."))
        } else {
          adHocFeedback.createAdHocFeedback(feedback) match {
            case None => InternalServerError(Json.obj("message" -> "Could not create ad-hoc feedback."))
            case Some(savedFeedback) => emailer.sendAdHocFeedbackEmail(savedFeedback, person.findByEmail(recipient.managerEmail), recipient.managerEmail);
              Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(savedFeedback)))
          }
        }
    }
  }

  def getAdHocFeedbackForSelf = AuthenticatedAction { user =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(adHocFeedback.getAdHocFeedbackForSelf(user.credentials.email))))
  }

  def getAdHocFeedbackForUser(username: String) = AuthenticatedAction { user =>
    val personOpt = person.findByEmail(username)
    if (username == user.credentials.email || !isInReportingLine(user.credentials.email, personOpt)) {
      Forbidden
    } else {
      Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(adHocFeedback.getAdHocFeedbackForReport(username))))
    }
  }

  def getAdHocFeedbackFromSelf = AuthenticatedAction { user =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(adHocFeedback.getAdHocFeedbackFromSelf(user.credentials.email))))
  }
}

case class SummaryItem(id: Option[Long], status: String, name: String, role: String, managerName: String, lastUpdated: Option[DateTime], shared: Boolean)

object SummaryItem {

  implicit val format: Format[SummaryItem] = Json.format[SummaryItem]

}

case class DetailItem(id: Long,
                      status: FeedbackStatus,
                      fromName: String,
                      toName: String,
                      bossName: Option[String],
                      bossEmail: String,
                      message: Option[String],
                      questions: Seq[QuestionResponse],
                      shareFeedback: Boolean,
                      cycleLabel: String,
                      cycleEndDate: DateTime,
                      hasForcedSharing: Boolean = false,
                      hasOptionalSharing: Boolean = true,
                      helpLinkText: Option[String] = None,
                      helpLinkUrl: Option[String] = None)

object DetailItem {
  implicit val format: Format[DetailItem] = Json.format[DetailItem]

  def detailFromNomination(nomination: Nomination, cycle: Option[FeedbackCycle]): Option[DetailItem] = (nomination, cycle) match {
    case (Nomination(Some(id), Some(fromPerson), Some(toPerson), status, _, questions, shared, _, message),
          Some(FeedbackCycle(_, label, _, endDate, _, _, hasOptionalSharing, hasForcedSharing, _, helpLinkText, helpLinkUrl))) =>
      Some(DetailItem(id, status, fromPerson.name, toPerson.name, None, fromPerson.managerEmail, message, questions,
        shared, label, endDate, hasForcedSharing = hasForcedSharing, hasOptionalSharing = hasOptionalSharing,
        helpLinkText = helpLinkText, helpLinkUrl = helpLinkUrl))
    case _ => None
  }
}

case class AdHocFeedbackRequest(toEmail: String, message: String, publishToRecipient: Boolean)
object AdHocFeedbackRequest {

  implicit val format: Format[AdHocFeedbackRequest] = (
    (JsPath \ "body" \ "recipientEmail").format[String](Reads.email) and
      (JsPath \ "body" \ "message").format[String] and
      (JsPath \ "body" \ "publishToRecipient").format[Boolean]
    )(AdHocFeedbackRequest.apply, unlift(AdHocFeedbackRequest.unapply))

}

class Nominations @Inject() (emailer: Emailer,
                             person: PersonDao,
                             nomination: NominationDao,
                             feedbackCycle: FeedbackCycleDao,
                             sessionManager: SessionManager) extends AuthenticatedController(person, sessionManager) {

  def getCurrentNominations = AuthenticatedAction { person =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(nomination.getCurrentNominationsFromUser(person.credentials.email))))
  }

  def createNomination = AuthenticatedRequestAction { (fromUser, json) =>
    val toUsernameJson: JsResult[String] = json.validate[String]((JsPath \ "body" \ "username").read[String](Reads.email)).map(_.toLowerCase)
    val cycleIdJson: JsResult[Long] = json.validate[Long]((JsPath \ "body" \ "cycleId").read[Long])
    val message: Option[String] = (json \ "body" \ "message").asOpt[String]
    (toUsernameJson, cycleIdJson) match {
      case (JsSuccess(toUsername, _), JsSuccess(cycleId, _)) => {
        if (!feedbackCycle.validateCycle(cycleId))
          BadRequest(Json.obj("message" -> "Invalid feedback cycle selected."))
        else {
          person.findByEmail(toUsername) match {
            case Some(toUser) =>
              if (toUser.credentials.email == fromUser.credentials.email)
                BadRequest(Json.obj("message" -> "Cannot nominate yourself"))
              else
                wrapEither(nomination.createNomination(fromUser.credentials.email, toUser.credentials.email, cycleId, message),emailer.sendNominationNotificationEmail)
            case None => wrapEither(createNominatedUser(toUsername).right.flatMap(id => nomination.createNomination(fromUser.credentials.email, toUsername, cycleId, message)), emailer.sendNominationNotificationEmail)
          }
        }
      }
      case _ => BadRequest(Json.obj("message" -> "Invalid request parameters."))
    }
  }

  def wrapEither[A]: (Either[Throwable, A], A => Unit) => Result = (either, sideEffect) =>
    either match {
      case Left(e) => BadRequest(Json.obj("message" -> e.getMessage))
      case Right(r) => sideEffect(r); Created
    }

  private def createNominatedUser(username: String): Either[Throwable, Long] = person.createNominee(username)

  def cancelNomination(id: Long) = AuthenticatedAction { person =>
    val genericFail: JsValue = Json.obj("message" -> "Could not cancel nomination.")

    nomination.getSummary(id) match {
      case Some(Nomination(_, Some(p), _, FeedbackStatus.New, _, _, _, _, _))
        if p.credentials.email == person.credentials.email => if (nomination.cancelNomination(id)) Ok else BadRequest(genericFail)
      case Some(Nomination(_, _, _,status, _, _, _, _, _)) if status != FeedbackStatus.New =>
        BadRequest(Json.obj("message" -> "Can only cancel nominations with a 'New' status."))
      case None => BadRequest(genericFail)
    }
  }

  def getCycleReports = AuthenticatedAction { user =>
    val reports = nomination.getFeedbackForUsers(person.findDirectReports(user.credentials.email).map { _.credentials.email })
    Ok(Json.obj("body" -> Json.toJson(reports)))
  }

}

class FeedbackCycleController @Inject() (person: PersonDao,
                               feedbackCycle: FeedbackCycleDao,
                               sessionManager: SessionManager) extends AuthenticatedController(person, sessionManager) {

  def getFeedbackCycleDetails(cycleId: Long) = AuthenticatedAdminAction { user =>
    feedbackCycle.findDetailsById(cycleId) match {
      case Some(cycle) => Ok(Json.obj("body" -> Json.toJson(cycle)))
      case None => BadRequest
    }
  }

  def getActiveFeedbackCycles = AuthenticatedAction { person =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(feedbackCycle.findActiveCycles)))
  }

  def getAllFeedbackCycles = AuthenticatedAdminAction { person =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(feedbackCycle.findAllCycles)))
  }

  def createFeedbackCycle = AuthenticatedAdminRequestAction { json =>
    (json \ "body").validate[FeedbackCycle].asOpt match {
      case None => BadRequest
      case Some(FeedbackCycle(Some(_),_,_,_,_,_,_,_,_,_,_)) => BadRequest
      case Some(cycle) => feedbackCycle.createCycle(cycle) match {
        case Right(newCycle) => Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(newCycle)))
        case Left(e) => println(e.getMessage);e.printStackTrace;InternalServerError(Json.obj("message" -> "Could not create new feedback cycle."))
      }
    }
  }

  def updateFeedbackCycle(id: Long) = AuthenticatedAdminRequestAction { json =>
    (json \ "body").validate[FeedbackCycle].asOpt match {
      case None => BadRequest
      case Some(FeedbackCycle(None,_,_,_,_,_,_,_,_,_,_)) => BadRequest
      case Some(cycle) => feedbackCycle.findById(id) map { _ =>
        feedbackCycle.updateCycle(id, cycle) match {
          case Left(e) => println(e.getMessage);e.printStackTrace;InternalServerError(Json.obj("message" -> s"Could not update feedback cycle with id='$id'."))
          case Right(newCycle) => Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(newCycle)))
        }
      } getOrElse(BadRequest(Json.obj("message" -> s"Cannot update feedback cycle with id='$id'.  It does not exist.")))
    }
  }

  def updateFeedbackCycle360Status(id: Long) = AuthenticatedAdminRequestAction { request =>
    ???
  }
}
//case class Response[T : Writes](apiVersion: String, body: T)
//
//object Response {
//  implicit def writes[T: Writes] = Json.writes[Response[T]]
//
//  implicit def writable[Response](implicit codec: Codec): Writeable[Response] =
//    Writeable(a => codec.encode(Json.stringify(Json.toJson(a))))
//}
