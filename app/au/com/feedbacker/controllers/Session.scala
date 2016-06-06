package au.com.feedbacker.controllers

import java.security.SecureRandom
import java.util.concurrent.{ConcurrentMap, ConcurrentHashMap}

import java.util.Base64
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.json.{Format, JsString, Json, JsValue}
import play.api.mvc._

import au.com.feedbacker.model._

import scala.concurrent.Future

/**
 * Created by lachlang on 09/05/2016.
 */

trait AuthenticatedController extends Controller {

  def AuthenticatedAction(body: Person => Result) = Action { request =>
    Authentication.getUser(request) match {
      case Some(person) => body(person)
      case _ => Forbidden
    }
  }

  def AuthenticatedRequestAction(body: (Person, JsValue) => Result) = Action { request =>
    (Authentication.getUser(request), request.body.asJson) match {
      case (Some(person), Some(requestBody)) => body(person, requestBody)
      case _ => Forbidden
    }
  }

//  def wrapEither[A]: Either[Throwable, A] => Result = either =>
//    either match {
//      case Left(e) => BadRequest(Json.obj("message" -> e.getMessage))
//      case Right(_) => Created
//    }
//  def wrapEither2[A](sideEffect: A => Unit): Either[Throwable, A] => Result = either =>
//    either match {
//      case Left(e) => BadRequest(Json.obj("message" -> e.getMessage))
//      case Right(r) => sideEffect(r); Created
//  }
  def wrapEither[A]: (Either[Throwable, A], A => Unit) => Result = (either, sideEffect) =>
    either match {
      case Left(e) => BadRequest(Json.obj("message" -> e.getMessage))
      case Right(r) => sideEffect(r); Created
    }
}

class Authentication extends Controller {

  def login = Action { request =>
    val jsonBody: Option[JsValue] = request.body.asJson

    jsonBody.map { json => ((json \ "body" \ "username").asOpt[String], (json \ "body" \ "password").asOpt[String]) } match {
      case Some((Some(userName), Some(password))) => SessionToken.initialiseToken(userName, password) match {
        case None => Forbidden
        case Some(st) => st.signIn(Ok)
      }
      case _ => BadRequest
    }
  }

  def logout = Action { request =>
    SessionToken.extractToken(request) match {
      case Some(st) => st.signOut(Ok)
      case None => BadRequest
    }
  }
}

object Authentication {

  def hash(planetext :String): String = BCrypt.hashpw(planetext, BCrypt.gensalt())

  def validatePassword(cleartext: String, hash: String): Boolean = BCrypt.checkpw(cleartext, hash)

  def getUser(request: RequestHeader): Option[Person] = SessionToken.extractToken(request).flatMap(st => Person.findByEmail(st.username))

}

case class SessionToken(username: String, token: String) {
  // TODO: pull this out into config

  def signIn: Result => Result = response => {
    SessionToken.initialiseSession(this)
    response.withCookies(
      Cookie(SessionToken.cookieName,
        token,
        SessionToken.cookieMaxAge,
        SessionToken.cookiePathOption,
        SessionToken.cookieDomainOption,
        SessionToken.secureOnly,
        SessionToken.httpOnly)
    )
  }

  def signOut: Result => Result = response => {
    SessionToken.destroySession(this)
    response.discardingCookies(DiscardingCookie(SessionToken.cookieName))
  }
}

object SessionToken {

  protected val cookieName: String = "FEEDBACKER_SESSION"
  protected val cookieMaxAge: Option[Int] = Some(60 * 60 * 10) // 60 minutes * 60 seconds
  protected val cookiePathOption: String = "/"
  protected val cookieDomainOption: Option[String] = None
  protected val secureOnly : Boolean = false
  protected val httpOnly: Boolean = false

  implicit val format: Format[SessionToken] = Json.format[SessionToken]

  private val tokenMap: ConcurrentMap[String, String] = new ConcurrentHashMap[String, String]

  private val tokenGenerator = SecureRandom.getInstanceStrong

  def extractToken(request: RequestHeader): Option[SessionToken] = request.cookies.get(cookieName).flatMap(c => validateToken(c.value))

  def validateToken(token: String): Option[SessionToken] = Some(SessionToken(tokenMap.get(token),token))

  def initialiseToken(username: String, password: String): Option[SessionToken] = {
    Person.findByEmail(username).filter{_.credentials.status == CredentialStatus.Active}.flatMap{p =>
      if (!Authentication.validatePassword(password, p.credentials.hash)) None else {
        val token = generateToken
        Some(SessionToken(username, token))
      }
    }
  }

  def generateToken: String = {
    val bytes = new Array[Byte](64)
    tokenGenerator.nextBytes(bytes)
    Base64.getEncoder.encodeToString(bytes)
  }

  private def initialiseSession(st: SessionToken): Unit = tokenMap.put(st.token, st.username)

  private def destroySession(st: SessionToken): Unit = tokenMap.remove(st.token)
}

object AuthenticatedAction extends ActionFilter[Request] {
  def filter[A](request: Request[A]): Future[Option[Result]] = Future.successful {
    SessionToken.extractToken(request) match {
      case Some(_) => None
      case None => Some(Results.Status(401))
    }
  }
}

//object SslRedirectFilter extends Filter {
//
//  def apply(nextFilter: (RequestHeader) => Future[SimpleResult])(requ)
//}