package au.com.feedbacker.util

import org.mindrot.jbcrypt.BCrypt
import play.api.mvc.{Request, Session}
import au.com.feedbacker.model.Person

/**
 * Created by lachlang on 23/05/2016.
 */
object AuthenticationUtil {

  private val sessionUserKey: String = "user"

  def hash(planetext :String): String = BCrypt.hashpw(planetext, BCrypt.gensalt())

  def validatePassword(cleartext: String, hash: String): Boolean = BCrypt.checkpw(cleartext, hash)

  def createSession(username: String, session: Session): Session = session + (sessionUserKey, username)

  def invalidateSession(session: Session) : Session = session - sessionUserKey

  def getUser(session: Session): Option[Person] = session.get(sessionUserKey).flatMap(Person.findByEmail(_))
}
