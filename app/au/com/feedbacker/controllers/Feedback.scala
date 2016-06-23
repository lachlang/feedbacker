package au.com.feedbacker.controllers

//import play.api.http.Writeable

import javax.inject.Inject

import au.com.feedbacker.model.FeedbackStatus.FeedbackStatus
import au.com.feedbacker.util.Emailer
import play.api.http.Writeable
import play.api.libs.json._
import au.com.feedbacker.model._
import org.joda.time.DateTime

/**
 * Created by lachlang on 09/05/2016.
 */
class Feedback @Inject() (person: PersonDao, nomination: NominationDao, feedbackCycle: FeedbackCycleDao) extends AuthenticatedController(person) {

  def getPendingFeedbackActions = AuthenticatedAction { user =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(nomination.getPendingNominationsForUser(user.credentials.email) .map {
      case Nomination(id, Some(from), _, status, lastUpdated, _, shared, _) =>
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
        val questionsJson: JsResult[Seq[QuestionResponse]] = json.validate[Seq[QuestionResponse]]((JsPath \ "body" \ "questions").read[Seq[QuestionResponse]])
        (questionsJson, submittedJson) match {
          case (JsSuccess(questions,_), JsSuccess(submitted,_)) =>
            if (nomination.submitFeedback(nominationId, questions, submitted)) Ok else BadRequest
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
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(nomination.getCurrentFeedbackForUser(user.credentials.email))))
  }

  def getFeedbackHistoryForSelf = AuthenticatedAction { user  =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(nomination.getHistoryFeedbackForUser(user.credentials.email))))
  }

  def getCurrentFeedbackItemsForUser(id: Long) = AuthenticatedAction { user =>
    ???
  }

  def getFeedbackHistoryForUser(id: Long) = AuthenticatedAction { user =>
    ???
  }

  def getCycleForFeedback(cycleId: Long) = AuthenticatedAction { user =>
    feedbackCycle.findById(cycleId) match {
      case Some(cycle) => Ok(Json.obj("body" -> Json.toJson(cycle)))
      case None => BadRequest
    }
  }

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

}

object Feedback {
  def checkPermission(user: Person, targetId: Long) : Boolean = ??? //if (user.id.get == targetId) true else Person.findBy
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
                      questions: Seq[QuestionResponse],
                      shareFeedback: Boolean,
                      cycleLabel: Option[String],
                      cycleEndDate: Option[DateTime])

object DetailItem {
  implicit val writes: Writes[DetailItem] = Json.writes[DetailItem]

  def detailFromNomination(nomination: Nomination, cycle: Option[FeedbackCycle]): Option[DetailItem] = nomination match {
    case Nomination(Some(id), Some(fromPerson), Some(toPerson), status, _, questions, shared, _) =>
      Some(DetailItem(id, status, fromPerson.name, toPerson.name, None, fromPerson.managerEmail, questions, shared, cycle.map(_.label), cycle.map(_.end_date)))
    case _ => None
  }
}

class Nominations @Inject() (emailer: Emailer,
                             person: PersonDao,
                             nomination: NominationDao,
                             feedbackCycle: FeedbackCycleDao,
                             nominee: NomineeDao) extends AuthenticatedController(person) {

  def getCurrentNominations = AuthenticatedAction { person =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(nomination.getCurrentNominationsFromUser(person.credentials.email))))
  }

  def getNomineeCandidates = AuthenticatedAction { person =>
    Ok(Json.obj("body" -> Json.toJson(nominee.findNomineeCandidates)))
  }

  def createNomination = AuthenticatedRequestAction { (fromUser, json) =>
    val toUsernameJson: JsResult[String] = json.validate[String]((JsPath \ "body" \ "username").read[String](Reads.email)).map(_.toLowerCase)
    val cycleIdJson: JsResult[Long] = json.validate[Long]((JsPath \ "body" \ "cycleId").read[Long])
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
                wrapEither(nomination.createNomination(fromUser.credentials.email, toUser.credentials.email, cycleId),emailer.sendNominationNotificationEmail)
            case None => wrapEither(createNominatedUser(toUsername).right.flatMap(id => nomination.createNomination(fromUser.credentials.email, toUsername, cycleId)), emailer.sendNominationNotificationEmail)
          }
        }
      }
      case _ => BadRequest
    }
  }

  private def createNominatedUser(username: String): Either[Throwable, Long] = person.createNominee(username)

  def cancelNomination(id: Long) = AuthenticatedAction { person =>
    val genericFail: JsValue = Json.obj("message" -> "Could not cancel nomination.")

    nomination.getSummary(id) match {
      case Some(Nomination(_, Some(p), _, FeedbackStatus.New, _, _, _, _))
        if p.credentials.email == person.credentials.email => if (nomination.cancelNomination(id)) Ok else BadRequest(genericFail)
      case Some(Nomination(_, _, _,status, _, _, _, _)) if status != FeedbackStatus.New =>
        BadRequest(Json.obj("message" -> "Can only cancel nominations with a 'New' status."))
      case None => BadRequest(genericFail)
    }
  }

  def getActiveFeedbackCycles = AuthenticatedAction { person =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(feedbackCycle.findActiveCycles)))
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