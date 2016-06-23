package au.com.feedbacker.controllers

import java.security.SecureRandom
import java.util.concurrent.{ConcurrentMap, ConcurrentHashMap}

import java.util.Base64
import javax.inject.Inject
import org.mindrot.jbcrypt.BCrypt
import play.Logger
import play.api.libs.json._
import play.api.mvc._

import au.com.feedbacker.model._

import scala.concurrent.Future

/**
 * Created by lachlang on 09/05/2016.
 */

class AuthenticatedController(person: PersonDao) extends Controller {

  def AuthenticatedAction(body: Person => Result) = LoggingAction { request =>
    getUser(request) match {
      case Some(person) => body(person)
      case _ => Forbidden
    }
  }

  def AuthenticatedRequestAction(body: (Person, JsValue) => Result) = LoggingAction { request =>
    (getUser(request), request.body.asJson) match {
      case (Some(person), Some(requestBody)) => body(person, requestBody)
      case _ => Forbidden
    }
  }

  private def getUser(request: RequestHeader): Option[Person] = SessionToken.extractToken(request).flatMap(st => person.findByEmail(st.username))

  def wrapEither[A]: (Either[Throwable, A], A => Unit) => Result = (either, sideEffect) =>
    either match {
      case Left(e) => BadRequest(Json.obj("message" -> e.getMessage))
      case Right(r) => sideEffect(r); Created
    }
}

class Authentication @Inject() (person: PersonDao) extends Controller {

  // NOTE: this is horrible but I'm tired
  def login = Action { request =>
    val jsonBody: Option[JsValue] = request.body.asJson

    jsonBody.map { json => ((json \ "body" \ "username").asOpt[String](Reads.email).map(_.toLowerCase), (json \ "body" \ "password").asOpt[String]) } match {
      case Some((Some(username), Some(password))) =>
        val personOpt = person.findByEmail(username)
        personOpt match {
          case Some(p) => if (p.credentials.status == CredentialStatus.Inactive) Unauthorized else
            SessionToken.initialiseToken(personOpt, password) match {
            case None => BadRequest
            case Some(st) => st.signIn(Ok)
          }
          case None => Forbidden
        }
      case _ => BadRequest
    }
  }

  def logout = LoggingAction { request =>
    SessionToken.extractToken(request) match {
      case Some(st) => st.signOut(Ok)
      case None => BadRequest
    }
  }
}

object Authentication {

  def hash(planetext :String): String = BCrypt.hashpw(planetext, BCrypt.gensalt())

  def validatePassword(cleartext: String, hash: String): Boolean = BCrypt.checkpw(cleartext, hash)

}

case class SessionToken(username: String, token: String) {

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

  // TODO: pull this out into config
  protected val cookieName: String = "FEEDBACKER_SESSION"
  protected val cookieMaxAge: Option[Int] = Some(60 * 60 * 2) // 60 minutes * 60 seconds * 2 hours
  protected val cookiePathOption: String = "/"
  protected val cookieDomainOption: Option[String] = None
  protected val secureOnly : Boolean = false
  protected val httpOnly: Boolean = false

  implicit val format: Format[SessionToken] = Json.format[SessionToken]

  private val tokenMap: ConcurrentMap[String, String] = new ConcurrentHashMap[String, String]

  private val tokenGenerator = SecureRandom.getInstanceStrong

  def extractToken(request: RequestHeader): Option[SessionToken] = request.cookies.get(cookieName).flatMap(c => validateToken(c.value))

  def validateToken(token: String): Option[SessionToken] = Some(SessionToken(tokenMap.get(token),token))

  def initialiseToken(person: Option[Person], password: String): Option[SessionToken] = {
    person.filter{_.credentials.status == CredentialStatus.Active}.flatMap{p =>
      if (!Authentication.validatePassword(password, p.credentials.hash)) None else {
        val token = generateToken
        Some(SessionToken(p.credentials.email, token))
      }
    }
  }

  def generateToken: String = {
    val bytes = new Array[Byte](64)
    tokenGenerator.nextBytes(bytes)
    Base64.getEncoder.encodeToString(bytes).dropRight(2)
  }

  private def initialiseSession(st: SessionToken): Unit = tokenMap.put(st.token, st.username)

  private def destroySession(st: SessionToken): Unit = tokenMap.remove(st.token)
}

object LoggingAction extends ActionBuilder[Request] {
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    Logger.debug(s"""request path:${request.path}, body:${request.body.toString}""")
    block(request)
  }
}

//object SslRedirectFilter extends Filter {
//
//  def apply(nextFilter: (RequestHeader) => Future[SimpleResult])(requ)
//}