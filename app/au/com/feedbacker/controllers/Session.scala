package au.com.feedbacker.controllers

import au.com.feedbacker.model
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.json.{JsString, JsObject, Json, JsValue}
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import au.com.feedbacker.model._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

/**
 * Created by lachlang on 09/05/2016.
 */

class Authentication extends Controller {

  def login = Action { request =>
    val jsonBody: Option[JsValue] = request.body.asJson

    jsonBody.map { json => ((json \ "body" \ "username").asOpt[String], (json \ "body" \ "password").asOpt[String]) } match {
      case Some((Some(userName), Some(password))) => Person.findByEmail(userName) match {
        case None => println("username not found");Forbidden
        case Some(p) => if (Authentication.validatePassword(password, p.credentials.hash)) {
          // case Some(p) => if (BCrypt.checkpw(password, p.credentials.hash) && p.credentials.status == CredentialStatus.Active) {
          println(request.session.toString)
          request.session + ("user", p.credentials.email)
//          AuthenticationUtil.createSession(p.credentials.email, request.session) // mutate all the things?
          println(request.session.toString)
          Ok(Json.obj("apiVersion" -> JsString("1.0"), "body" -> Json.toJson(p))).withSession(request.session)
        } else {
          // set session cookie
          Forbidden
        }
      }
      case _ => BadRequest
    }
  }

  def logout = Action { request =>
    Authentication.invalidateSession(request.session)
    Ok
  }
}

object Authentication {

  private val sessionUserKey: String = "user"

  def hash(planetext :String): String = BCrypt.hashpw(planetext, BCrypt.gensalt())

  def validatePassword(cleartext: String, hash: String): Boolean = BCrypt.checkpw(cleartext, hash)

  def createSession(username: String, session: Session): Session = session + (sessionUserKey, username)

  def invalidateSession(session: Session) : Session = session - sessionUserKey

  def getUser(session: Session): Option[Person] = session.get(sessionUserKey).flatMap(Person.findByEmail(_))

}

trait AuthenticatedController extends Controller {

}

trait AuthenticatedAction[T] extends Action[T] {

}

