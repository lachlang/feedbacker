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
                              activation: ActivationDao,
                              sessionManager: SessionManager) extends Controller {

  private def translateResultAndActivate(result: Either[Throwable, Person]) : Result = result match {
    case Left(_) => BadRequest("{ \"body\": { \"message\": \"Could not create user.\"}} ")
    case Right(p) => activation.createActivationToken(p.credentials.email) match {
      case None => BadRequest("{ \"body\": { \"message\": \"Could not send activation email.\"}} ")
      case Some(st) => emailer.sendActivationEmail(p.name, st); Ok(Json.toJson(p))
    }
  }

  def register: Action[JsValue] = LoggingAction(parse.json(maxLength = 2000)) { request =>

    request.body.validate[RegistrationContent].asOpt
      .map(rc => RegistrationContent(rc.name,rc.role,rc.email.toLowerCase,rc.password,rc.managerEmail.toLowerCase)) match {
      case None => BadRequest("{ \"body\": { \"message\": \"Could not parse request.\"}} ")
      case Some(body) => credentials.findStatusByEmail(body.email.toLowerCase) match {
        case Some((id, CredentialStatus.Nominated)) => translateResultAndActivate(person.update(Person(Some(id), body.name, body.role, Credentials(body.email, sessionManager.hash(body.password), CredentialStatus.Inactive), body.managerEmail)))
        case Some((_, _)) => Conflict("{ \"body\": { \"message\": \"User is already registered.\"}} ")
        case None => translateResultAndActivate(person.create(Person(None, body.name, body.role, Credentials(body.email, sessionManager.hash(body.password), CredentialStatus.Inactive), body.managerEmail)))
      }
    }
  }
}

class Account @Inject() (person: PersonDao, nomination: NominationDao, sessionManager: SessionManager) extends AuthenticatedController(person, sessionManager) {

  def getUser = AuthenticatedAction { user =>
     Ok(Json.obj("body" -> Json.toJson(user)))
  }

  def getReports = AuthenticatedAction { user =>
    val reports = person.findDirectReports(user.credentials.email).map{ report =>
      Report(report,nomination.getHistoryReportForUser(report.credentials.email))
    }
    Ok(Json.obj("body" -> Json.toJson(reports)))
  }

  def updateUserDetails = AuthenticatedRequestAction { (user, json) =>
    val errorMessage = "Could not update user details."

    json.validate[UpdateContent].asOpt.map(uc =>
      Person(user.id, uc.name, uc.role, user.credentials, uc.managerEmail.toLowerCase, user.isLeader)) match {

      case None => BadRequest(s"""{ "body": { "message": "$errorMessage "}} """)
      case Some(personUpdates) => person.update(personUpdates) match {
       case Left(e) => BadRequest(Json.obj("body" -> Json.obj("message" -> e.getMessage)))
       case Right(updatedPerson) => Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(updatedPerson)))
      }
    }
  }
}

case class Report(person: Person, reviewCycle: Seq[FeedbackGroup])

object Report {
  implicit val writes: Writes[Report] = Json.writes[Report]
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


// LG: 2016-09-15 I suspect there is a better way to do this...
case class UpdateContent(name: String, role: String, managerEmail: String)

object UpdateContent {

  implicit val format: Format[UpdateContent] = (
    (JsPath \ "body" \ "name").format[String] and
      (JsPath \ "body" \ "role").format[String] and
      (JsPath \ "body" \ "managerEmail").format[String](Reads.email)
    )(UpdateContent.apply, unlift(UpdateContent.unapply))
}

class ActivationCtrl @Inject() (emailer: Emailer, person: PersonDao, activation: ActivationDao, sessionManager: SessionManager) extends Controller {

  def activate = LoggingAction { request =>
    request.getQueryString("username").map(_.toLowerCase).flatMap{username =>
      request.getQueryString("token").map{token => SessionToken(username, token.replaceAll(" ", "+"))}}
    match {
      case None => BadRequest
      case Some(st) => if (!activation.validateToken(st)) Forbidden else if (activation.activate(st)) sessionManager.signIn(st, Redirect("/#/list")) else BadRequest
    }
  }

  def sendActivationEmail = LoggingAction { request =>
    request.body.asJson.flatMap ( json => (json \ "body" \ "username").asOpt[String](Reads.email).flatMap(person.findByEmail(_))) match {
      case None => BadRequest
      case Some(user) => activation.createActivationToken(user.credentials.email) match {
        case None => BadRequest
        case Some(st) => emailer.sendActivationEmail(user.name, st); Ok
      }
    }
  }
}

class ResetPassword @Inject() (emailer: Emailer,
                               person: PersonDao,
                               activation: ActivationDao,
                               sessionManager: SessionManager) extends Controller {

  def resetPassword = LoggingAction(parse.json(maxLength = 300)) { request => {
    request.body.validate[ResetPasswordContent].asOpt match {
      case None => BadRequest
      case Some(content) =>
        val st = SessionToken(content.username.toLowerCase, content.token.replaceAll(" ", "+"))
        if (!activation.validateToken(st)) Forbidden
        else {
          person.findByEmail(st.username) match {
            case None => BadRequest
            case Some(p) => person.update(p.setNewHash(sessionManager.hash(content.password))) match {
                case Left(e) => BadRequest(Json.obj("body" -> Json.obj("message" -> e.getMessage)))
                case Right(_) => activation.expireToken(st.token); Ok
              }
            }
          }
        }
    }
  }


  def sendPasswordResetEmail = LoggingAction { request =>
    request.body.asJson.flatMap { json => (json \ "body" \ "email").asOpt[String](Reads.email) }.map(_.toLowerCase) match {
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

