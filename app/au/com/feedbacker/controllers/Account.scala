package au.com.feedbacker.controllers

import play.api.libs.functional.syntax._
import play.api.libs.json._
//import play.api.libs.json.Format._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import au.com.feedbacker.model._

import scala.concurrent.Future

/**
 * Created by lachlang on 09/05/2016.
 */


class Registration extends Controller {

  def translateResult(result: Either[Throwable, Person], errorMessage: String) : Result = result match {
    case Left(e) => println(e.getMessage); BadRequest("{ \"body\": { \"message\": \"" + errorMessage + "\"}} ")
    case Right(p) => Ok(Json.toJson(p))
  }

  def register: Action[JsValue] = Action(parse.json(maxLength = 2000)) { request =>
    val errorMessage = "Could not create user."

    request.body.validate[RegistrationContent].asOpt match {
      case None => BadRequest("{ \"body\": { \"message\": \"Could not parse request.\"}} ")
      case Some(body) => Credentials.findStatusByEmail(body.email) match {
        case Some((id, CredentialStatus.Nominated)) => translateResult(Person.update(Person(Some(id), body.name, body.role, Credentials(body.email, Authentication.hash(body.password), CredentialStatus.Inactive), body.managerEmail)), errorMessage)
        case Some((_, _)) => Conflict("{ \"body\": { \"message\": \"User is already registered.\"}} ")
        case None => translateResult(Person.create(Person(None, body.name, body.role, Credentials(body.email, Authentication.hash(body.password), CredentialStatus.Inactive), body.managerEmail)), errorMessage)
      }
    }
  }
}

object Registration {
  def validateEmailFormat = ???
}

class Account extends AuthenticatedController {

  def getUser = Action { request =>
    Authentication.getUser(request) match {
      case Some(user) => Ok(Json.obj("body" -> Json.toJson(user)))
      case _ => Forbidden
    }
  }

  // TODO: add update functions here
}

case class RegistrationContent(name: String, role: String, email: String, password: String, managerEmail: String)

object RegistrationContent {

  implicit val format: Format[RegistrationContent] = (
    (JsPath \ "body" \ "name").format[String] and
    (JsPath \ "body" \ "role").format[String] and
    (JsPath \ "body" \ "email").format[String] and
    (JsPath \ "body" \ "password").format[String] and
    (JsPath \ "body" \ "managerEmail").format[String]
  )(RegistrationContent.apply, unlift(RegistrationContent.unapply))
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