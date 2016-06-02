package au.com.feedbacker.controllers

//import play.api.http.Writeable

import au.com.feedbacker.model.FeedbackStatus.FeedbackStatus
import play.api.http.Writeable
import play.api.libs.json._
import au.com.feedbacker.model._
import org.joda.time.DateTime

/**
 * Created by lachlang on 09/05/2016.
 */
class Feedback extends AuthenticatedController {

  def getPendingFeedbackActions = AuthenticatedAction { person =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(Nomination.getPendingNominationsForUser(person.credentials.email) .map {
      case Nomination(id, Some(from), _, status, lastUpdated, _, shared) =>
        val managerName: String = Person.findByEmail(from.managerEmail) match {
          case Some(p) => p.name
          case None => from.managerEmail
        }
        SummaryItem(id, status.toString, from.name, from.role, managerName, lastUpdated, shared) } )))
  }

  def updateFeedbackItem(nominationId: Long) = AuthenticatedRequestAction { (person, json) =>
    validateWritePermission(nominationId, person) match {
      case None => Forbidden
      case Some(n) => {
        val submittedJson: JsResult[Boolean] = json.validate[Boolean]((JsPath \ "body" \ "submit").read[Boolean])
        val questionsJson: JsResult[Seq[QuestionResponse]] = json.validate[Seq[QuestionResponse]]((JsPath \ "body" \ "questions").read[Seq[QuestionResponse]])
        (questionsJson, submittedJson) match {
          case (questions: JsSuccess[Seq[QuestionResponse]], submitted: JsSuccess[Boolean]) =>
            if (Nomination.submitFeedback(nominationId, questions.get, submitted.get)) Ok else BadRequest
          case _ => BadRequest
        }
      }
    }
  }

  def getFeedbackItem(nominationId: Long) = AuthenticatedAction { person =>
    validateReadPermission(nominationId, person) match {
      case None => NotFound(Json.obj("message" -> "Could not find feedback detail."))
      case Some(n) => Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(DetailItem.detailFromNomination(n))))
    }
  }

  def getCurrentFeedbackItemsForSelf = AuthenticatedAction { person =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(Nomination.getCurrentFeedbackForUser(person.credentials.email))))
  }

  def getFeedbackHistoryForSelf = AuthenticatedAction { person  =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(Nomination.getHistoryFeedbackForUser(person.credentials.email))))
  }

  def getCurrentFeedbackItemsForUser(id: Long) = AuthenticatedAction { person =>
    ???
  }

  def getFeedbackHistoryForUser(id: Long) = AuthenticatedAction { person =>
    ???
  }

  private val nominationWriteFilter: (Nomination, String) => Boolean = (n, email) => n.to.map(_.credentials.email == email).getOrElse(false)
//  private val nominationReadFilter: (Nomination, String) => Boolean = (n, email) => n.from.map(_.credentials.email == email).getOrElse(false)
  private val nominationReadFilter: (Option[Person], String) => Boolean = (person, email) => person match {
    case None => false
    case Some(fromPerson) if fromPerson.credentials.email == email => true
    case Some(fromPerson) => nominationReadFilter(Person.findByEmail(fromPerson.managerEmail), email)
  }

  private def validateWritePermission(nominationId: Long, toPerson: Person): Option[Nomination] =
    Nomination.getDetail(nominationId).filter(nominationWriteFilter(_, toPerson.credentials.email))

  private def validateReadPermission(nominationId: Long, fromPerson: Person): Option[Nomination] =
    Nomination.getDetail(nominationId).filter( n =>
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
                      shareFeedback: Boolean)

object DetailItem {
  implicit val writes: Writes[DetailItem] = Json.writes[DetailItem]

  def detailFromNomination(nomination: Nomination): Option[DetailItem] = nomination match {
    case Nomination(Some(id), Some(fromPerson), Some(toPerson), status, _, questions, shared) =>
      Some(DetailItem(id, status, fromPerson.name, toPerson.name, None, fromPerson.managerEmail, questions, shared))
    case _ => None
  }
}

class Nominations extends AuthenticatedController {

  def getCurrentNominations = AuthenticatedAction { person =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(Nomination.getCurrentNominationsFromUser(person.credentials.email))))
  }

  def getNomineeCandidates = AuthenticatedAction { person =>
    Ok(Json.obj("body" -> Json.toJson(Nominee.findNomineeCandidates)))
  }

  def createNomination = AuthenticatedRequestAction { (fromUser, json) =>
    val toUsernameJson: JsResult[String] = json.validate[String]((JsPath \ "body" \ "username").read[String](Reads.email))
    val cycleIdJson: JsResult[Long] = json.validate[Long]((JsPath \ "body" \ "cycleId").read[Long])
    (toUsernameJson, cycleIdJson) match {
      case (JsSuccess(toUsername, _), JsSuccess(cycleId, _)) => {
        if (!FeedbackCycle.validateCycle(cycleId))
          BadRequest(Json.obj("message" -> "Invalid feedback cycle selected."))
        else {
          Person.findByEmail(toUsername) match {
            case Some(toUser) => wrapEither(Nomination.createNomination(fromUser.credentials.email, toUser.credentials.email, cycleId))
            case None => wrapEither(createNominatedUser(toUsername).right
              .map(id => Nomination.createNomination(fromUser.credentials.email, toUsername, cycleId)))
          }
        }
      }
      case _ => BadRequest
    }
  }

  private def createNominatedUser(username: String): Either[Throwable, Long] = Person.createNominee(username)

  def cancelNomination(id: Long) = AuthenticatedAction { person =>
    val genericFail: JsValue = Json.obj("message" -> "Could not cancel nomination.")
    Nomination.getSummary(id) match {
      case Some(Nomination(_, _, _,status, _, _, _)) if status != FeedbackStatus.New && status != FeedbackStatus.Pending =>
        BadRequest(Json.obj("message" -> "Can only cancel nominations with a 'New' or 'Pending' status."))
      case Some(Nomination(_, Some(p), _, _, _, _, _))
        if p.credentials.email == person.credentials.email => if (Nomination.cancelNomination(id)) Ok else BadRequest(genericFail)
      case None => BadRequest(genericFail)
    }
  }

  def getActiveFeedbackCycles = AuthenticatedAction { person =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(FeedbackCycle.findActiveCycles)))
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