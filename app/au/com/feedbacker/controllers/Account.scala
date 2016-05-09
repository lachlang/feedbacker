package au.com.feedbacker.controllers

import play.api.libs.json.Json
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
 * Created by lachlang on 09/05/2016.
 */
class Registration extends Controller {

  def register = Action.async { request => 
    Future(Ok)
  }
}

class Activation extends Controller {

  def activate = Action.async { request => 

    Future(Ok)
  }

  def sendActivationEmail = Action.async { request =>
    Future(Ok)
  }
}

class ResetPassword extends Controller {

  def resetPassword = Action.async { request =>
    Future(Ok)
  }

  def sendPasswordResetEmail = Action.async { request => 
    Future(Ok)
  }
}