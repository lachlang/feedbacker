package au.com.feedbacker.controllers

import java.security.SecureRandom
import java.util.concurrent.{ConcurrentMap, ConcurrentHashMap}

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
        case None => Forbidden
        case Some(p) => if (Authentication.validatePassword(password, p.credentials.hash)) {
          // case Some(p) => if (BCrypt.checkpw(password, p.credentials.hash) && p.credentials.status == CredentialStatus.Active) {
          Ok(Json.obj("apiVersion" -> JsString("1.0"), "body" -> Json.toJson(p))).withCookies(SessionToken.initialiseToken(p.credentials.email))
        } else {
          Forbidden
        }
      }
      case _ => BadRequest
    }
  }

  def logout = Action { request =>
    SessionToken.extractToken(request) match {
      case Some(st) => SessionToken.removeToken(st, Ok)
      case None => Ok
    }
  }
}

object Authentication {

  def hash(planetext :String): String = BCrypt.hashpw(planetext, BCrypt.gensalt())

  def validatePassword(cleartext: String, hash: String): Boolean = BCrypt.checkpw(cleartext, hash)

  def getUser(request: RequestHeader): Option[Person] = SessionToken.extractToken(request).flatMap(st => Person.findByEmail(st.username))

}

case class SessionToken(username: String, token: String)

object SessionToken {
  // TODO: pull this out into config
  protected val cookieName: String = "FEEDBACKER_SESSION"
  protected val cookieMaxAge: Option[Int] = Some(3600)
  protected val cookiePathOption: String = "/"
  protected val cookieDomainOption: Option[String] = None
  protected val secureOnly : Boolean = false
  protected val httpOnly: Boolean = false

  private val tokenMap: ConcurrentMap[String, String] = new ConcurrentHashMap[String, String]

  private val tokenGenerator = SecureRandom.getInstanceStrong

  def extractToken(request: RequestHeader): Option[SessionToken] = request.cookies.get(cookieName).flatMap(c => validateToken(c.value))

  def validateToken(token: String): Option[SessionToken] = Some(SessionToken(tokenMap.get(token),token))

  def initialiseToken(username: String): Cookie = {
    val token = generateToken
    tokenMap.put(token, username)
    Cookie(cookieName, token, cookieMaxAge, cookiePathOption, cookieDomainOption, secureOnly, httpOnly)
  }

  def removeToken(token: SessionToken, result: Result): Result = {
    tokenMap.remove(token.token)
    result.discardingCookies(DiscardingCookie(cookieName))
  }

  def generateToken = BigInt(300, tokenGenerator).toString(32)
}

//class UserIdRequest[A](val username: Option[String], request: Request[A]) extends WrappedRequest[A](request)

//class UserRequest[A](val user: Option[Person], request: UserIdRequest[A]) extends WrappedRequest[A](request) {
//  def username = request.username
//}
//
//object UserAction extends ActionBuilder[UserIdRequest] with ActionTransformer[Request, UserIdRequest] {
//  def transform[A](request: Request[A]) = Future.successful {
//    new UserIdRequest[A](SessionToken.extractToken(request).map(_.username), request)
//  }
//}
//
//object PermissionCheckAction extends ActionFilter[UserIdRequest] {
//  def filter[A](request: UserIdRequest[A]): Future[Option[Result]] = Future.successful {
//    request.username match {
//      case Some(_) => None
//      case None => Some(Results.Status(401))
//    }
//  }
//}

object AuthenticatedAction extends ActionFilter[Request] {
  def filter[A](request: Request[A]): Future[Option[Result]] = Future.successful {
    SessionToken.extractToken(request) match {
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

