package au.com.feedbacker.controllers

import javax.inject.Inject

import au.com.feedbacker.util.Emailer
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._

import au.com.feedbacker.model._

/**
 * Created by lachlang on 09/05/2016.
 */


class Registration @Inject() (emailer: Emailer,
                              person: PersonDao,
                              credentials: CredentialsDao,
                              activation: ActivationDao) extends Controller {

  def translateResultAndActivate(result: Either[Throwable, Person], errorMessage: String) : Result = result match {
    case Left(e) => BadRequest("{ \"body\": { \"message\": \"" + errorMessage + "\"}} ")
    case Right(p) => activation.createActivationToken(p.credentials.email) match {
      case None => BadRequest("{ \"body\": { \"message\": \"Could not send activation email.\"}} ")
      case Some(st) => emailer.sendActivationEmail(p.name, st); Ok(Json.toJson(p))
    }
  }

  def register: Action[JsValue] = Action(parse.json(maxLength = 2000)) { request =>
    val errorMessage = "Could not create user."

    request.body.validate[RegistrationContent].asOpt match {
      case None => BadRequest("{ \"body\": { \"message\": \"Could not parse request.\"}} ")
      case Some(body) => credentials.findStatusByEmail(body.email) match {
        case Some((id, CredentialStatus.Nominated)) => translateResultAndActivate(person.update(Person(Some(id), body.name, body.role, Credentials(body.email, Authentication.hash(body.password), CredentialStatus.Inactive), body.managerEmail)), errorMessage)
        case Some((_, _)) => Conflict("{ \"body\": { \"message\": \"User is already registered.\"}} ")
        case None => translateResultAndActivate(person.create(Person(None, body.name, body.role, Credentials(body.email, Authentication.hash(body.password), CredentialStatus.Inactive), body.managerEmail)), errorMessage)
      }
    }
  }
}

class Account @Inject() (person: PersonDao) extends AuthenticatedController(person) {

  def getUser = AuthenticatedAction { user =>
     Ok(Json.obj("body" -> Json.toJson(user)))
  }

  def getReports = AuthenticatedAction { user =>
    Ok(Json.obj("body" -> Json.toJson(person.findDirectReports(user.credentials.email))))
  }

  // TODO: add update functions here
}

case class RegistrationContent(name: String, role: String, email: String, password: String, managerEmail: String)

object RegistrationContent {

  implicit val format: Format[RegistrationContent] = (
    (JsPath \ "body" \ "name").format[String] and
    (JsPath \ "body" \ "role").format[String] and
    (JsPath \ "body" \ "email").format[String](Reads.email) and
    (JsPath \ "body" \ "password").format[String] and
    (JsPath \ "body" \ "managerEmail").format[String](Reads.email)
  )(RegistrationContent.apply, unlift(RegistrationContent.unapply))
}

class ActivationCtrl @Inject() (emailer: Emailer, person: PersonDao, activation: ActivationDao) extends Controller {

  def activate = LoggingAction { request =>
    request.getQueryString("username").flatMap{username =>
      request.getQueryString("token").map{token => SessionToken(username, token.replaceAll(" ", "+"))}}
    match {
      case None => BadRequest
      case Some(st) => if (!activation.validateToken(st)) Forbidden else if (activation.activate(st)) st.signIn(Redirect("/#/list")) else BadRequest
    }
  }

  def sendActivationEmail = LoggingAction { request =>
    request.body.asJson.flatMap ( json => (json \ "body" \ "username").asOpt[String].flatMap(person.findByEmail(_))) match {
      case None => BadRequest
      case Some(person) => activation.createActivationToken(person.credentials.email) match {
        case None => BadRequest
        case Some(st) => emailer.sendActivationEmail(person.name, st); Ok
      }
    }
  }

}

class ResetPassword @Inject() (emailer: Emailer,
                               person: PersonDao,
                               activation: ActivationDao) extends Controller {

  def resetPassword = LoggingAction(parse.json(maxLength = 200)) { request =>

    request.body.validate[ResetPasswordContent].asOpt match {
      case Some(content) =>
        val st = SessionToken(content.username, content.token.replaceAll(" ", "+"))
        if (!activation.validateToken(st)) Forbidden
        else {
          person.findByEmail(st.username) match {
            case None => BadRequest
            case Some(p) => person.update(p.setNewHash(Authentication.hash(content.password))) match {
                  case Left(e) => BadRequest(Json.obj("message" -> e.getMessage))
                  case Right(_) => activation.expireToken(st.token);Ok
                }
          }
        }
      case _ => Forbidden
    }
  }

  def sendPasswordResetEmail = LoggingAction { request =>
    request.body.asJson
      .flatMap { json => (json \ "body" \ "email").asOpt[String](Reads.email) } match {
      case None => BadRequest
      case Some(username) => (person.findByEmail(username),activation.createActivationToken(username)) match {
        case (Some(p),Some(st)) => emailer.sendPasswordResetEmail(p.name, st); Ok
        case _ => BadRequest
      }
    }
  }
}

case class ResetPasswordContent(password: String, username: String, token: String)

object ResetPasswordContent {

  implicit val format: Format[ResetPasswordContent] = (
      (JsPath \ "body" \ "password").format[String] and
      (JsPath \ "body" \ "username").format[String](Reads.email) and
      (JsPath \ "body" \ "token").format[String]
    )(ResetPasswordContent.apply, unlift(ResetPasswordContent.unapply))
}

