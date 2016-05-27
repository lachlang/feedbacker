package au.com.feedbacker.controllers

//import play.api.http.Writeable
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

  def updateFeedbackItem(id: Long) = Action { request =>
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

case class SummaryItem(id: Option[Long], status: String, name: String, role: String, managerName: String, lastUpdated: DateTime, shared: Boolean)

object SummaryItem {

  implicit val format: Format[SummaryItem] = Json.format[SummaryItem]

}

class Nominations extends AuthenticatedController {

  def getCurrentNominations = AuthenticatedAction { person =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(Nomination.getCurrentNominationsFromUser(person.credentials.email))))
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