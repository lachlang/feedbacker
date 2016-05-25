package au.com.feedbacker.controllers

import java.security.SecureRandom

import au.com.feedbacker.model
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.json.{JsString, Json, JsValue}
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import au.com.feedbacker.model._

import scala.concurrent.Future

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
          val sessionKey = java.util.UUID.randomUUID.toString
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

case class SessionToken(username: String, token: String)

object SessionToken {
  // TODO: pull this out into config
  protected val cookieName: String = "FEEDBACKER_SESSION"
  protected val cookieMaxAge: Option[Int] = Some(3600)
  protected val cookiePathOption: String = "/"
  protected val cookieDomainOption: Option[String] = None
  protected val secureOnly : Boolean = false
  protected val httpOnly: Boolean = true

  private val tokenMap: Map[String, String] = Map()

  private val tokenGenerator = SecureRandom.getInstanceStrong

  def extractToken(request: RequestHeader): Option[SessionToken] = request.cookies.get(cookieName).flatMap(c => validateToken(c.value))

  def validateToken(token: String): Option[SessionToken] = {
    tokenMap.get(token) match {
      case (Some(u)) => Some(SessionToken(u,token))
      case _ => None
    }
  }

  def setToken(token: SessionToken, result: Result)(implicit request: RequestHeader): Result = {
    tokenMap + (token.token -> token.username)
    val c: Cookie = Cookie(cookieName, token.token, cookieMaxAge, cookiePathOption, cookieDomainOption, secureOnly, httpOnly)
    result.withCookies(c)
  }

  def removeToken(token: SessionToken, result: Result)(implicit request: RequestHeader): Result = {
    tokenMap - token.token
    result.discardingCookies(DiscardingCookie(cookieName))
  }

  def generateToken = ???
//  {
//    var bytes = byte[]
//    tokenGenerator.nextBytes()
//  }
}
class UserIdRequest[A](val username: Option[String], request: Request[A]) extends WrappedRequest[A](request)

class UserRequest[A](val user: Option[Person], request: UserIdRequest[A]) extends WrappedRequest[A](request) {
  def username = request.username
}

object UserAction extends ActionBuilder[UserIdRequest] with ActionTransformer[Request, UserIdRequest] {
  def transform[A](request: Request[A]) = Future.successful {
    new UserIdRequest[A](SessionToken.extractToken(request).map(_.username), request)
  }
}

object PermissionCheckAction extends ActionFilter[UserIdRequest] {
  def filter[A](request: UserIdRequest[A]): Future[Option[Result]] = Future.successful {
    request.username match {
      case Some(_) => None
      case None => Some(Results.Status(401))
    }
  }
}

//def AuthenticatedUserAction(username: String) = new ActionRefiner[UserIdRequest, UserRequest] {
//  def refine[A](input: UserIdRequest[A]) = Future.successful {
//    Person.findByEmail(username).map(new ItemRequest(_, input)).toRight(NotFound)
//  }
//}

//trait AuthenticatedAction[T] extends Action[T] {
//
//}

