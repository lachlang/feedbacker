package au.com.feedbacker.controllers

import au.com.feedbacker.model.FeedbackStatus.FeedbackStatus
import au.com.feedbacker.util.AuthenticationUtil
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format, Json}
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
    AuthenticationUtil.getUser(request.session) match {
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

    Ok
  }

  def getCurrentFeedbackItemsForUser(id: Long) = Action.async { request =>

    Future(Ok)
  }

  def getCurrentFeedbackItemsForSelf = Action.async { request =>

    Future(Ok)
  }

  def getFeedbackHistoryForUser(id: Long) = Action.async { request =>

    Future(Ok)
  }

  def getFeedbackHistoryForSelf = Action.async { request =>

    Future(Ok)
  }

}

case class SummaryItem(id: Option[Long], status: String, name: String, role: String, managerName: String, lastUpdated: DateTime, shared: Boolean)

object SummaryItem {
  implicit val format: Format[SummaryItem] = (
      (JsPath \ "id").formatNullable[Long] and
      (JsPath \ "status").format[String] and
      (JsPath \ "name").format[String] and
      (JsPath \ "role").format[String] and
      (JsPath \ "managerName").format[String] and
      (JsPath \ "lastUpdated").format[DateTime] and
      (JsPath \ "shared").format[Boolean]
    )(SummaryItem.apply, unlift(SummaryItem.unapply))

}