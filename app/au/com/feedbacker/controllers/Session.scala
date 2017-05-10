package au.com.feedbacker.controllers

import java.security.SecureRandom
import java.util.concurrent.{ConcurrentMap, ConcurrentHashMap}

import java.util.Base64
import javax.inject.Inject
import org.mindrot.jbcrypt.BCrypt
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._

import au.com.feedbacker.model._

import scala.concurrent.Future

/**
 * Created by lachlang on 09/05/2016.
 */

class AuthenticatedController(person: PersonDao, sessionManager: SessionManager) extends Controller {

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

  private def getUser(request: RequestHeader): Option[Person] =
    sessionManager.extractToken(request).flatMap{st => person.findByEmail(st.username)}

}

class Authentication @Inject() (person: PersonDao, sessionManager: SessionManager) extends Controller {

  // NOTE: LOGGING action is NOT appropriate since we don't want to log credentials in clear text
  def login = Action { request =>
    val jsonBody: Option[JsValue] = request.body.asJson

    jsonBody.map { json => ((json \ "body" \ "username").asOpt[String](Reads.email).map(_.toLowerCase), (json \ "body" \ "password").asOpt[String]) } match {
      case Some((Some(username), Some(password))) =>
        val personOpt = person.findByEmail(username)
        personOpt match {
          case None => Forbidden
          case Some(p) =>
            if (p.credentials.status != CredentialStatus.Active) Unauthorized
            else {
              sessionManager.initialiseToken(p, password) match {
                case None => BadRequest
                case Some(st) => sessionManager.signIn(st, Ok)
            }
          }
        }
      case _ => BadRequest
    }
  }

  def logout = LoggingAction { request =>
    sessionManager.extractToken(request) match {
      case Some(st) => sessionManager.signOut(st, Ok)
      case None => BadRequest
    }
  }
}

case class SessionToken(username: String, token: String)

object SessionToken {
  implicit val format: Format[SessionToken] = Json.format[SessionToken]
}

class SessionManager {

  private val tokenGenerator = SecureRandom.getInstanceStrong

  def hash(planetext :String): String = BCrypt.hashpw(planetext, BCrypt.gensalt())

  def validatePassword(cleartext: String, hash: String): Boolean = BCrypt.checkpw(cleartext, hash)

  def extractToken(request: RequestHeader): Option[SessionToken] = {
    val cookie = request.cookies.get(SessionManager.cookieName)
      cookie.flatMap(c => validateToken(c.value))
  }

  private def validateToken(token: String): Option[SessionToken] = {
    if (SessionManager.tokenMap.containsKey(token)) {
      Some(SessionToken(SessionManager.tokenMap.get(token), token))
    } else {
      None
    }
  }

  def initialiseToken(person: Person, password: String): Option[SessionToken] = {
    if (person.credentials.status != CredentialStatus.Active ||
        !validatePassword(password, person.credentials.hash)) None
    else {
      val token = generateToken
      Some(SessionToken(person.credentials.email, token))
    }
  }

  def generateToken: String = {
    val bytes = new Array[Byte](64)
    tokenGenerator.nextBytes(bytes)
    Base64.getEncoder.encodeToString(bytes).dropRight(2)
  }

  private def initialiseSession(st: SessionToken): Unit = SessionManager.tokenMap.put(st.token, st.username)

  private def destroySession(st: SessionToken): Unit = SessionManager.tokenMap.remove(st.token)

  def signIn(st: SessionToken, response: Result): Result = {
    initialiseSession(st)
    response.withCookies(SessionManager.createSessionCookie(st.token))
  }

  def signOut(st: SessionToken, response: Result) = {
    destroySession(st)
    response.discardingCookies(DiscardingCookie(SessionManager.cookieName))
  }

}

object SessionManager {
  private val tokenMap: ConcurrentMap[String, String] = new ConcurrentHashMap[String, String]

  // TODO: pull this out into config
  val cookieName: String = "FEEDBACKER_SESSION"
  protected val cookieMaxAge: Option[Int] = Some(60 * 60 * 2) // 60 minutes * 60 seconds * 2 hours
  protected val cookiePathOption: String = "/"
  protected val cookieDomainOption: Option[String] = None
  protected val secureOnly : Boolean = false
  protected val httpOnly: Boolean = false
  def createSessionCookie(token: String): Cookie = Cookie(cookieName, token, cookieMaxAge, cookiePathOption, cookieDomainOption, secureOnly, httpOnly)
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