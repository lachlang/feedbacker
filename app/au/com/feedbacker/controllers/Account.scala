package au.com.feedbacker.controllers

import javax.inject.Inject

import au.com.feedbacker.util.{Emailer, CsvReport}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._
import au.com.feedbacker.model._
import org.joda.time.DateTime

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
      .map(rc => RegistrationContent(rc.name,rc.role,rc.email.toLowerCase.toLowerCase,rc.password,rc.managerEmail.toLowerCase)) match {
      case None => BadRequest(Json.obj("message" -> "Your request was invalid.  Please submit all the required fields."))
      case Some(body) => if (body.managerEmail == body.email) {
        BadRequest(Json.obj("message" -> "Your email and manager's email must be different."))
      } else {
        credentials.findStatusByEmail(body.email.toLowerCase) match {
          case Some((id, CredentialStatus.Nominated)) => translateResultAndActivate(person.update(Person(Some(id), body.name, body.role, Credentials(body.email, sessionManager.hash(body.password), CredentialStatus.Inactive), body.managerEmail)))
          case Some((_, CredentialStatus.Inactive)) => Conflict(Json.obj("message" -> "This account is already registered but awaiting activation.  Please click the link in your activation email or click the link below to request a new activation email."))
          case Some((_, _)) => Conflict(Json.obj("message" -> "This email address is already registered.  Please try using the password reset link to update your password."))
          case None => translateResultAndActivate(person.create(Person(None, body.name, body.role, Credentials(body.email, sessionManager.hash(body.password), CredentialStatus.Inactive), body.managerEmail)))
        }
      }
    }
  }
}

class Account @Inject() (person: PersonDao,
                         nomination: NominationDao,
                         users: RegisteredUserDao,
                         sessionManager: SessionManager) extends AuthenticatedController(person, sessionManager) {

  def getUser = AuthenticatedAction { user =>
    Ok(Json.obj("body" -> Json.toJson(user)))
  }

  def getUserReports = AuthenticatedAction { user =>
    val reports = person.findDirectReports(user.credentials.email).map { report =>
      Report(report, nomination.getAllFeedbackHistoryForUser(report.credentials.email))
    }
    Ok(Json.obj("body" -> Json.toJson(reports)))
  }

  def updateUserDetails = AuthenticatedRequestAction { (user, json) =>
    json.validate[UpdateContent].asOpt.map(uc =>
      Person(user.id, uc.name, uc.role, user.credentials, uc.managerEmail.toLowerCase, user.isLeader)) match {

      case None => BadRequest(s"""{ "body": { "message": "Could not update user details."}} """)
      case Some(personUpdates) => person.update(personUpdates) match {
        case Left(e) => BadRequest(Json.obj("body" -> Json.obj("message" -> e.getMessage)))
        case Right(updatedPerson) => person.recalculateIsLeader(user.managerEmail, updatedPerson.managerEmail); Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(updatedPerson)))
      }
    }
  }

  def updateUserDetailsForAdmin(username: String) = AuthenticatedAdminRequestAction { json =>
    (json.validate[UpdateContentForAdmin].asOpt, person.findByEmail(username)) match {
      case (None, None) => BadRequest(Json.obj("message" -> "Invalid request."))
      case (None, _) => BadRequest(Json.obj("message" -> "Invalid Request."))
      case (_, None) => BadRequest(Json.obj("message" -> "Cannot update invalid user."))
      case (Some(uc), Some(targetPerson)) => {
        val status =
          if ((targetPerson.credentials.status == CredentialStatus.Active || targetPerson.credentials.status == CredentialStatus.Inactive) && uc.isEnabled == false) CredentialStatus.Disabled
          else if (targetPerson.credentials.status == CredentialStatus.Disabled && uc.isEnabled == true) CredentialStatus.Inactive
          else targetPerson.credentials.status
        person.updateWithAdmin(Person(id = targetPerson.id,
                  name = uc.name,
                  role = uc.role,
                  credentials = targetPerson.credentials.copy(status = status),
                  managerEmail = uc.managerEmail.toLowerCase,
                  isLeader = targetPerson.isLeader,
                  isAdmin = uc.isAdmin)) match {
            case Left(e) => BadRequest(Json.obj("body" -> Json.obj("message" -> e.getMessage)))
            case Right(updatedPerson) => person.recalculateIsLeader(targetPerson.managerEmail, updatedPerson.managerEmail); Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(updatedPerson)))

        }
      }
    }
  }

  def getActiveUsers = AuthenticatedAction { person =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(users.findActiveUsers)))
  }

  def getRegisteredUsers = AuthenticatedAdminAction { person =>
    Ok(Json.obj("apiVersion" -> "1.0", "body" -> Json.toJson(users.findRegisteredUsers)))
  }

}

class ReportFile @Inject() (person: PersonDao, nomination: NominationDao, cycle: FeedbackCycleDao, sessionManager: SessionManager, csvReport: CsvReport) extends AuthenticatedController(person, sessionManager) {

  private val mime: String = "text/csv"
  private def attachmentHeader(filename: String): (String, String) = ("Content-Disposition", s"""attachment; filename="$filename"""")

  def generateReportForCycle(cycleId: Long) = AuthenticatedAction { user =>
    cycle.findById(cycleId) match {
      case None => BadRequest
      case Some(c) => {
        val nominations = nomination.findNominationForPeopleInCycleWithDetail(user.credentials.email, cycleId)
        Ok(csvReport.createReportForCycle(c, nominations)).as(mime).withHeaders(attachmentHeader(s"Review_Summary_for_${c.label}_at_${DateTime.now()}.csv"))
      }
    }
  }

  def generateHistoryReportForUser(personId: Long) = AuthenticatedAction { user =>
    val pOpt = person.findById(personId)
    pOpt match {
      case None => BadRequest
      case Some(p) => if (!isInReportingLine(user.credentials.email, Some(p))) {
        Forbidden
      } else {
        val report: Report = Report(p, nomination.getAllFeedbackHistoryForUserWithDetail(p.credentials.email))
        Ok(csvReport.createReportForPerson(report)).as(mime).withHeaders(attachmentHeader(s"Review_Summary_for_${user.name}_at_${DateTime.now()}.csv"))
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

case class UpdateContentForAdmin(name: String, role: String, managerEmail: String, isAdmin: Boolean = false, isEnabled: Boolean = false)

object UpdateContentForAdmin {

  implicit val format: Format[UpdateContentForAdmin] = (
    (JsPath \ "body" \ "name").format[String] and
      (JsPath \ "body" \ "role").format[String] and
      (JsPath \ "body" \ "managerEmail").format[String](Reads.email) and
      (JsPath \ "body" \ "isAdmin").format[Boolean] and
      (JsPath \ "body" \ "isEnabled").format[Boolean]
    )(UpdateContentForAdmin.apply, unlift(UpdateContentForAdmin.unapply))
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
      case Some(content) => {
        val st = SessionToken(content.username.toLowerCase, content.token.replaceAll(" ", "+"))
        if (!activation.validateToken(st)) {
          Forbidden
        } else {
          person.findByEmail(st.username) match {
            case None => BadRequest
            case Some(p) =>
              if (p.credentials.status == CredentialStatus.Active) {
                person.update(p.setNewHash(sessionManager.hash(content.password))) match {
                  case Left(e) => BadRequest(Json.obj("body" -> Json.obj("message" -> e.getMessage)))
                  case Right(_) => activation.expireToken(st.token); Ok
                }
              } else {
                Forbidden
              }
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
        case (Some(p),Some(st)) => {
          if (p.credentials.status == CredentialStatus.Active) {
            emailer.sendPasswordResetEmail(p.name, st)
            Ok
          } else {
            Forbidden
          }
        }
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

