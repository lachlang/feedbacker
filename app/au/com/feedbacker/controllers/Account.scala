package au.com.feedbacker.controllers

import javax.inject.Inject

import au.com.feedbacker.util.Emailer
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

  def translateResultAndActivate(result: Either[Throwable, Person], errorMessage: String) : Result = result match {
    case Left(e) => println(e.getMessage);BadRequest("{ \"body\": { \"message\": \"" + errorMessage + "\"}} ")
    case Right(p) => ActivationCtrl.generateActivationEmail(p); Ok(Json.toJson(p))
  }

  def register: Action[JsValue] = Action(parse.json(maxLength = 2000)) { request =>
    val errorMessage = "Could not create user."

    request.body.validate[RegistrationContent].asOpt match {
      case None => BadRequest("{ \"body\": { \"message\": \"Could not parse request.\"}} ")
      case Some(body) => Credentials.findStatusByEmail(body.email) match {
        case Some((id, CredentialStatus.Nominated)) => translateResultAndActivate(Person.update(Person(Some(id), body.name, body.role, Credentials(body.email, Authentication.hash(body.password), CredentialStatus.Inactive), body.managerEmail)), errorMessage)
        case Some((_, _)) => Conflict("{ \"body\": { \"message\": \"User is already registered.\"}} ")
        case None => translateResultAndActivate(Person.create(Person(None, body.name, body.role, Credentials(body.email, Authentication.hash(body.password), CredentialStatus.Inactive), body.managerEmail)), errorMessage)
      }
    }
  }
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
    (JsPath \ "body" \ "email").format[String](Reads.email) and
    (JsPath \ "body" \ "password").format[String] and
    (JsPath \ "body" \ "managerEmail").format[String](Reads.email)
  )(RegistrationContent.apply, unlift(RegistrationContent.unapply))
}

class ActivationCtrl extends Controller {

  def activate = Action { request =>
    request.getQueryString("username").flatMap{username =>
      request.getQueryString("token").map{token => SessionToken(username, token.replaceAll(" ", "+"))}}
    match {
      case None => BadRequest
      case Some(st) => if (!Activation.validateToken(st)) Forbidden else if (Activation.activate(st)) st.signIn(Redirect("/#/list")) else BadRequest
    }
  }

  def sendActivationEmail = Action { request =>
    request.body.asJson.flatMap ( json => (json \ "body" \ "username").asOpt[String].flatMap(Person.findByEmail(_))) match {
      case None => BadRequest
      case Some(person) => ActivationCtrl.generateActivationEmail(person); Ok
    }
  }

}

object ActivationCtrl {
  def generateActivationEmail(person: Person): Unit = Activation.createActivationToken(person.credentials.email) match {
    case Some(st) => {
      val emailBody: String =
        s"""Hi ${person.name},

            Thanks for registering to use Feedbacker.

            To activate your account please navigate to following link

            api/activate?username=${st.username}&token=${st.token}

            Thanks
            The Feedback Team
            (Feedback is always welcome)
         """.stripMargin
      println(emailBody)
    } // send activation email
  }
}

class ResetPassword @Inject() (emailer: Emailer) extends Controller {

  def resetPassword = Action { request =>
    val newPasswordOpt = request.body.asJson.flatMap { json => (json \ "body" \ "password").asOpt[String]}
    val sessionTokenOpt = request.getQueryString("username").flatMap{username =>
    request.getQueryString("token").map{token => SessionToken(username, token.replaceAll(" ", "+"))}}

    (newPasswordOpt, sessionTokenOpt) match {
      case (Some(newPassword), Some(st)) =>
        if (!Activation.validateToken(st)) Forbidden
        else {
          Person.findByEmail(st.username) match {
            case None => BadRequest
            case Some(p) => Person.update(p.setNewHash(Authentication.hash(newPassword))) match {
                  case Left(e) => BadRequest(Json.obj("message" -> e.getMessage))
                  case Right(_) => Ok
                }
          }
        }
      case _ => Forbidden
    }
  }


  def sendPasswordResetEmail = Action { request =>
    request.body.asJson
      .flatMap { json => (json \ "body" \ "username").asOpt[String](Reads.email) } match {
      case Some(username) => (Person.findByEmail(username),Activation.createActivationToken(username)) match {
        case (Some(p),Some(st)) => emailer.sendPasswordResetEmail(p.name, st); Ok
        case _ => BadRequest
      }
    }
  }
}