package au.com.feedbacker.controllers

//import play.api.http.Writeable

import play.api.http.Writeable
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import au.com.feedbacker.model._
import org.joda.time.DateTime

import scala.concurrent.Future

/**
 * Created by lachlang on 09/05/2016.
 */
class Feedback extends AuthenticatedController {

  def getPendingFeedbackActions = AuthenticatedAction { person =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(Nomination.getPendingNominationsForUser(person.credentials.email) .map {
      case Nomination(id, _, Some(to), status, lastUpdated, _, shared) =>
        val managerName: String = Person.findByEmail(to.managerEmail) match {
          case Some(p) => p.name
          case None => to.managerEmail
        }
        SummaryItem(id, status.toString, to.name, to.role, managerName, lastUpdated, shared) } )))
  }

  def updateFeedbackItem(id: Long) = AuthenticatedRequestAction { (person, request) =>
    ???
  }

  def getFeedbackItem(id: Long) = Action { person =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(Nomination.getDetail(id))))
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

}

object Feedback {
  def checkPermission(user: Person, targetId: Long) : Boolean = ??? //if (user.id.get == targetId) true else Person.findBy
}

case class SummaryItem(id: Option[Long], status: String, name: String, role: String, managerName: String, lastUpdated: Option[DateTime], shared: Boolean)

object SummaryItem {

  implicit val format: Format[SummaryItem] = Json.format[SummaryItem]

}

class Nominations extends AuthenticatedController {

  def getCurrentNominations = AuthenticatedAction { person =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(Nomination.getCurrentNominationsFromUser(person.credentials.email))))
  }

  def getNomineeCandidates = AuthenticatedAction { person =>
    Ok(Json.obj("body" -> Json.toJson(Nominee.findNomineeCandidates)))
  }

  def createNomination = AuthenticatedRequestAction { (person, json) =>
    val username: JsResult[String] = json.validate[String]((JsPath \ "body" \ "username").read[String](Reads.email))
    val cycleId: JsResult[Long] = json.validate[Long]((JsPath \ "body" \ "cycleId").read[Long])
    (person.id, username, cycleId) match {
      case (Some(id), name: JsSuccess[String], cycle: JsSuccess[Long]) =>
        if (person.credentials.email == name.get) {
          BadRequest(Json.obj("message" -> "You cannot nominate yourself."))
        } else {
          Nomination.createNomination(id, name.get, cycle.get) match {
            case Left(e) => BadRequest(Json.obj("message" -> e.getMessage))
            case Right(n) => Created
          }
        }
      case _ => BadRequest
    }
  }

  def cancelNomination(id: Long) = AuthenticatedAction { person =>
    val genericFail: JsValue = Json.obj("message" -> "Could not cancel nomination.")
    Nomination.getSummary(id) match {
      case Some(Nomination(_, _, _,status, _, _, _)) if status == FeedbackStatus.New || status == FeedbackStatus.Pending =>
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