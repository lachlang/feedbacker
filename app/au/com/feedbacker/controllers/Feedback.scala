package au.com.feedbacker.controllers

import play.api.libs.json.Json
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
 * Created by lachlang on 09/05/2016.
 */
class Feedback extends Controller {

  def getPendingFeedbackActions = Action.async { request =>

    Future(Ok)
  }

  def updateFeedbackItem(id: Long) = Action.async { request =>

    Future(Ok)
  }

  def getFeedbackItem(id: Long) = Action.async { request =>

    Future(Ok)
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