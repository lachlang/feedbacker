package au.com.feedbacker.controllers

import play.api.libs.json.Json
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import au.com.feedbacker.model._

import scala.concurrent.Future

/**
 * Created by lachlang on 09/05/2016.
 */
class Registration extends Controller {

  def register = Action { request =>

    val name : String = ??? //request.body.asJson
    val role : String = ???
    val email : String = ???
    val password : String = ???
    val managerEmail : String = ???
    Person.create(Person(None, name, role, Credentials(email, password, ""), managerEmail)) match {
      case Left(e) => BadRequest
      case Right => Ok
    }
  }
}

object Registration {
  def validateEmailFormat = ???
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