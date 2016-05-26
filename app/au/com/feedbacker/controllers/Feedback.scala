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
class Feedback extends Controller {

  def getPendingFeedbackActions = Action { request =>
    Authentication.getUser(request) match {
      case Some(Person(Some(id), _, _, _, _)) => {
        Ok(Json.toJson(Nomination.getPendingNominationsForUser(id) .map {
          case Nomination(id, _, Some(to), status, lastUpdated, _, shared) =>
            val managerName: String = Person.findByEmail(to.managerEmail) match {
              case Some(p) => p.name
              case None => to.managerEmail
            }
            SummaryItem(id, status.toString, to.name, to.role, managerName, lastUpdated, shared) } ))
      }
      case _ => Forbidden
    }
  }

  def updateFeedbackItem(id: Long) = Action.async { request =>

    Future(Ok)
  }

  def getFeedbackItem(id: Long) = Action { request =>
    Authentication.getUser(request) match {
      case Some(_) => Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(Nomination.getDetail(id))))
      case _ => Forbidden
    }
  }

  def getCurrentFeedbackItemsForSelf = Action { request =>
    Authentication.getUser(request) match {
      case Some(Person(Some(id), _, _, _, _)) => Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(Nomination.getCurrentFeedbackForUser(id))))
      case _ => Forbidden
    }
  }

  def AuthAction(body: (Person) => Result) = Action { request =>
    Authentication.getUser(request) match {
      case Some(person) => body(person)
      case _ => Forbidden
    }
  }

    def getFeedbackHistoryForSelf2 = AuthAction { person  =>
       Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(Nomination.getHistoryFeedbackForUser(person.id.getOrElse(0)))))
    }

  def getFeedbackHistoryForSelf = Action { request =>
    Authentication.getUser(request) match {
      case Some(Person(Some(id), _, _, _, _)) => Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(Nomination.getHistoryFeedbackForUser(id))))
      case _ => Forbidden
    }
  }

  def getCurrentFeedbackItemsForUser(id: Long) = Action { request =>
    ???
  }

  def getFeedbackHistoryForUser(id: Long) = Action { request =>
    ???
  }

}

case class SummaryItem(id: Option[Long], status: String, name: String, role: String, managerName: String, lastUpdated: DateTime, shared: Boolean)

object SummaryItem {

  implicit val format: Format[SummaryItem] = Json.format[SummaryItem]

}

class Nominations extends Controller {

  def getCurrentNominations = Action { request =>
    Ok
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