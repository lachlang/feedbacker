package au.com.feedbacker.model

import au.com.feedbacker.controllers.{SessionManager, SessionToken}
import org.joda.time.DateTime
import anorm.JodaParameterMetaData._
import javax.inject.Inject

import anorm._
import anorm.SqlParser._
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.util.Base64
import java.nio.charset.StandardCharsets

object CredentialStatus extends Enumeration {
	type CredentialStatus = Value
  val Active = Value("Active")
  val Nominated = Value("Nominated")
	val Inactive = Value("Inactive")
	val Disabled = Value("Disabled")

  implicit val writes: Writes[CredentialStatus] = new Writes[CredentialStatus] {
    def writes(v: CredentialStatus): JsValue = JsString(v.toString)
  }
  implicit val reads: Reads[CredentialStatus] = new Reads[CredentialStatus] {
    def reads(js: JsValue) = JsSuccess(CredentialStatus.withName(js.as[String]))
  }
}

import CredentialStatus._

object FeedbackStatus extends Enumeration {
	type FeedbackStatus = Value
	val New = Value("New")
	val Pending = Value("Pending")
	val Submitted = Value("Submitted")
  val Cancelled = Value("Cancelled")
  val Closed = Value("Closed")

  implicit val writes: Writes[FeedbackStatus] = new Writes[FeedbackStatus] {
    def writes(v: FeedbackStatus): JsValue = JsString(v.toString)
  }
  implicit val reads: Reads[FeedbackStatus] = new Reads[FeedbackStatus] {
    def reads(js: JsValue) = JsSuccess(FeedbackStatus.withName(js.as[String]))
  }
}
import FeedbackStatus._

object ResponseFormat extends Enumeration {
  type ResponseFormat = Value
  val Radio = Value("RADIO")
  val Select = Value("SELECT")

  implicit val writes: Writes[ResponseFormat] = new Writes[ResponseFormat] {
    def writes(v: ResponseFormat): JsValue = JsString(v.toString)
  }
  implicit val reads: Reads[ResponseFormat] = new Reads[ResponseFormat] {
    def reads(js: JsValue) = JsSuccess(ResponseFormat.withName(js.as[String]))
  }
}
import ResponseFormat._

case class Person(id: Option[Long], name: String, role: String, credentials: Credentials, managerEmail: String, isLeader: Boolean = false, isAdmin: Boolean = false) {
  def setNewHash(hash: String)  = Person(id, name, role, Credentials(credentials.email, hash, credentials.status), managerEmail, isLeader)
}

object Person {

  type Id = Long
  type Name = String
  type Username = String

  /**
   * Parse a Person from a ResultSet
   */
  val simple = {
    get[Option[Id]]("person.id") ~
      get[Name]("person.name") ~
      get[String]("person.role") ~
      get[Username]("person.email") ~
      get[String]("person.pass_hash") ~
      get[String]("person.user_status") ~
      get[String]("person.manager_email") ~
      get[Boolean]("person.is_manager") ~
      get[Boolean]("person.is_admin") map {
      case id~name~role~email~pass_hash~user_status~manager_email~isLeader~isAdmin =>
        Person(id, name, role, Credentials(email, pass_hash, CredentialStatus.withName(user_status)), manager_email, isLeader, isAdmin)
    }
  }

  implicit val writes: Writes[Person] = Json.writes[Person]
}

class PersonDao @Inject() (db: play.api.db.Database, activation: ActivationDao, sessionManager: SessionManager) {

  // Queries
  /**
   * Retrieve a person by id address.
   */
  def findById(id: Long): Option[Person] = db.withConnection { implicit connection =>
    SQL("select * from person where id = {id}").on('id -> id).as(Person.simple.singleOpt)
  }

  def findByEmail(email: String): Option[Person] = db.withConnection { implicit connection =>
    SQL("select * from person where email = {email}").on('email -> email.toLowerCase).as(Person.simple.singleOpt)
  }

  def create(person: Person): Either[Throwable, Person] = db.withConnection { implicit connection =>
      try {
        SQL(
          """
              insert into person (name, role, email, pass_hash, user_status, manager_email) values (
                {name},{role},{email},{pass_hash},{user_status},{manager_email}
              )
          """).on(
            'name -> person.name,
            'role -> person.role,
            'email -> person.credentials.email.toLowerCase,
            'pass_hash -> person.credentials.hash,
            'user_status -> person.credentials.status.toString,
            'manager_email -> person.managerEmail.toLowerCase
          ).executeInsert() match {
          case None => Left(new Exception("Could not insert."))
          case Some(id) => findById(id) match {
            case None => Left( new Exception("Could not insert."))
            case Some(person) => {
              setAsLeader(person.managerEmail)
              Right(person)
            }
          }
        }
      } catch {
        case e:Exception => Left(e)
      }
    }

  private def setAsLeader(email: String): Boolean = db.withConnection { implicit connection =>
    SQL("""update person SET is_manager = true where email = {email}""").on('email -> email).executeUpdate == 1
  }

  def recalculateIsLeader(oldManagerEmail: String, newManagerEmail: String): Boolean = db.withConnection { implicit connection =>
    if (oldManagerEmail == newManagerEmail) true
    else {
      SQL("""update person SET is_manager = (select count(*) > 0 from person where manager_email = {email}) where email = {email}""").on('email -> oldManagerEmail).executeUpdate() == 1
      setAsLeader(newManagerEmail)
    }
  }

  def createNominee(username: String): Either[Throwable, Long] = {
    db.withConnection { implicit connection =>
      try {
        SQL(
          """
            insert into person (name, role, email, pass_hash, user_status, manager_email)values (
              {email},'Nominee',{email},{pass_hash},{user_status},'placeholder@test.com'
            )
          """).on(
            'email -> username.toLowerCase,
            'pass_hash -> sessionManager.generateToken,
            'user_status -> CredentialStatus.Nominated.toString
          ).executeInsert() match {
          case None => Left(new Exception("Could not insert."))
          case Some(id) => Right(id)
        }
      } catch {
        case e:Exception => Left(e)
      }
    }
  }

  def update(person: Person): Either[Throwable, Person] = {
    db.withConnection { implicit connection =>
      try {
        SQL(
          """
            update person SET name={name},
                              role={role},
                              email={email},
                              pass_hash={pass_hash},
                              user_status={user_status},
                              manager_email={manager_email}
                              where email={email}
          """).on(
            'name -> person.name,
            'role -> person.role,
            'email -> person.credentials.email.toLowerCase,
            'pass_hash -> person.credentials.hash,
            'user_status -> person.credentials.status.toString,
            'manager_email -> person.managerEmail.toLowerCase
          ).executeUpdate() match {
          case 1 => Right(person)
          case _ => Left(new Exception("Could not update."))
        }
      } catch {
        case e:Exception => Left(e)
      }
    }
  }

  def login(email: String, hash: String): Boolean = db.withConnection{ implicit connection =>
    SQL (
      """
        select count(*) from person where email = {email} and pass_hash = {pass_hash}
      """).on(
        'email -> email.toLowerCase,
        'pass_hash -> hash
      ).as(scalar[Long].single) == 1
  }

  def findDirectReports(username: String) : Seq[Person] = db.withConnection { implicit connection =>
    SQL("""select * from person where manager_email = {email} and user_status in ({activeStatus}, {inactiveStatus})""")
      .on('email -> username.toLowerCase,
        'activeStatus -> CredentialStatus.Active.toString,
        'inactiveStatus -> CredentialStatus.Inactive.toString)
      .as(Person.simple *)
  }
}

case class RegisteredUser(name: Person.Name, email: Person.Username, role: String, managerEmail: String,
                          isEnabled: Option[Boolean] = None, isAdmin: Option[Boolean] = None)

object RegisteredUser {

  implicit val writes: Writes[RegisteredUser] = Json.writes[RegisteredUser]

  val simple = {
    get[String]("name") ~
      get[String]("email") ~
      get[String]("role") ~
      get[String]("manager_email") map {
      case name ~ email ~ role ~ managerEmail => RegisteredUser(name, email, role, managerEmail)
    }
  }

  val admin = {
    get[String]("name") ~
      get[String]("email") ~
      get[String]("role") ~
      get[String]("manager_email") ~
      get[Boolean]("is_admin") ~
      get[String]("user_status") map {
      case name ~ email ~ role ~ managerEmail ~isAdmin ~ status =>
        RegisteredUser(name = name, email = email, role = role, managerEmail = managerEmail, isAdmin = Some(isAdmin),
          isEnabled = CredentialStatus.withName(status) match {
          case CredentialStatus.Disabled => Some(false)
          case CredentialStatus.Active => Some(true)
          case CredentialStatus.Inactive => Some(true)
          case CredentialStatus.Nominated => None
        })
    }
  }
}

class RegisteredUserDao @Inject()(db: play.api.db.Database) {
  def findActiveUsers: Seq[RegisteredUser] = db.withConnection { implicit connection =>
    SQL("select name, email, role, manager_email from person where user_status in ({activeStatus}, {inactiveStatus})")
      .on('activeStatus -> CredentialStatus.Active.toString, 'inactiveStatus -> CredentialStatus.Inactive.toString).as(RegisteredUser.simple *)
  }

  def findRegisteredUsers: Seq[RegisteredUser] = db.withConnection { implicit connection =>
    SQL("select name, email, role, manager_email, is_admin, user_status from person where user_status in ({activeStatus}, {inactiveStatus}, {disabledStatus})")
      .on('activeStatus -> CredentialStatus.Active.toString,
        'inactiveStatus -> CredentialStatus.Inactive.toString,
        'disabledStatus -> CredentialStatus.Disabled.toString).as(RegisteredUser.admin *)
  }
}

case class Credentials(email: String, hash: String, status: CredentialStatus = CredentialStatus.Inactive)

object Credentials {

  val status = {
    get[Long]("id") ~
      get[String]("user_status") map {
      case id ~ status => (id, CredentialStatus.withName(status))
    }
  }

  // make sure we don't ever write the hash to the client
  implicit val writes: Writes[Credentials] = new Writes[Credentials] {
    def writes(creds: Credentials) = Json.obj(
      "email" -> creds.email.toLowerCase,
      "status" -> creds.status
    )
  }
}

class CredentialsDao @Inject() (db: play.api.db.Database) {
  def findStatusByEmail(email:String): Option[(Long, CredentialStatus)] = db.withConnection { implicit connection =>
    SQL("select id, user_status from person where email = {email}").on('email -> email.toLowerCase).as(Credentials.status.singleOpt)
  }
}

case class Activation(token: String, email: String, created: DateTime, used: Boolean, expires: DateTime)

object Activation {

  val simple = {
    get[String]("activations.token") ~
      get[String]("activations.email") ~
      get[DateTime]("activations.created") ~
      get[Boolean]("activations.used") ~
      get[DateTime]("activations.expires") map {
      case token ~ email ~ created ~ used ~ expires => Activation(token, email, created, used, expires)
    }
  }
}

class ActivationDao @Inject() (db: play.api.db.Database, sessionManager: SessionManager) {
  private val tokenExpiryInSeconds = 3600

  // Queries
  /**
   * validates the token exists, belongs to the correct user, has not been used and has not expired,
   * then invalidates the token and updates the user status
   * @param st: SessionToken
   * @return
   */

  def validateToken(st: SessionToken): Boolean = db.withConnection { implicit connection =>
    val result: Option[Activation] = SQL("select * from activations where token = {token} and email = {email} and used = false and expires > {time}")
      .on('token -> st.token, 'email -> st.username.toLowerCase, 'time -> DateTime.now).as(Activation.simple.singleOpt)
    result match {
      case Some(_) => true
      case None => false
    }
  }

  def expireToken(token: String): Boolean = db.withConnection { implicit connection =>
    SQL("update activations SET used = true where token = {token}")
      .on('token -> token).executeUpdate == 1
  }

  def activate(st: SessionToken): Boolean = db.withConnection { implicit connection =>
    if (!validateToken(st)) false else
      expireToken(st.token) &&
        SQL("update person SET user_status = {status} where email = {email}")
          .on('status -> CredentialStatus.Active.toString, 'email -> st.username.toLowerCase).executeUpdate == 1
  }

  /**
   * create a new activataion token for a user
   * @param username
   * @return
   */
  def createActivationToken(username: String): Option[SessionToken] = db.withConnection { implicit connection =>
    val token: String = sessionManager.generateToken
    if (SQL(
      """insert into activations (token, email, created, expires, used) values
         ({token}, {email}, {created}, {expires}, false) """)
      .on('token -> token,
        'email -> username.toLowerCase,
        'created -> DateTime.now,
        'expires -> (DateTime.now.plus(tokenExpiryInSeconds* 1000))).executeUpdate == 1)
      Some(SessionToken(username, token)) else None
  }
}

case class QuestionResponse(id: Option[Long], text: String, format: ResponseFormat, responseOptions: Seq[String], response: Option[String], comments: Option[String], helpText: Option[String])

object QuestionResponse {

  val simple = {
    get[Option[Long]]("question_response.id") ~
      get[String]("question_response.text") ~
      get[String]("question_response.render_format") ~
      get[String]("question_response.response_options") ~
      get[Option[String]]("question_response.response") ~
      get[Option[String]]("question_response.comments") ~
      get[Option[String]]("question_response.help_text") map {
      case id ~ text ~ format ~ responseOptions ~ response ~ comments ~ helpText => QuestionResponse(id, text, ResponseFormat.withName(format), responseOptions.split(","), response, comments, helpText)
    }
  }

  implicit val format: Format[QuestionResponse] = (
    (JsPath \ "id").formatNullable[Long] and
      (JsPath \ "text").format[String] and
      (JsPath \ "format").format[ResponseFormat] and
      (JsPath \ "responseOptions").format[Seq[String]] and
      (JsPath \ "response").formatNullable[String] and
      (JsPath \ "comments").formatNullable[String] and
      (JsPath \ "helpText").formatNullable[String]
    )(QuestionResponse.apply, unlift(QuestionResponse.unapply))
}

class QuestionResponseDao @Inject() (db: play.api.db.Database) {

  def findForNomination(nominationId: Long): Seq[QuestionResponse] = db.withConnection { implicit connection =>
    SQL("""select * from question_response where nomination_id = {id} order by id asc""").on('id -> nominationId).as(QuestionResponse.simple *).toSeq
  }

  def updateResponses(responses :Seq[QuestionResponse]): Boolean = db.withConnection { implicit connection =>
    responses.map ( response =>
    SQL("""update question_response SET response={response}, comments={comments} where id={id}""")
      .on('response -> response.response, 'comments -> response.comments, 'id-> response.id).executeUpdate() == 1).foldLeft(true)( (a, b) => a&b)
  }

  def initialiseResponses(nominationId :Long, questions: Seq[QuestionTemplate]): Boolean = db.withConnection { implicit connection =>
    questions.map { q =>
    SQL("""insert into question_response (nomination_id, text, render_format, response_options, help_text) values ({nominationId}, {text}, {format}, {responseOptions}, {helpText})""")
      .on('nominationId -> nominationId, 'text -> q.text, 'format -> q.format.toString, 'responseOptions -> q.responseOptions.mkString(","), 'helpText -> q.helpText).executeInsert() match {
        case Some(_) => true
        case None => false
      }
    }.foldLeft(true)( _ && _)
  }
}

case class QuestionTemplate(id: Option[Long], text: String, format: ResponseFormat, responseOptions: Seq[String], helpText: Option[String])

object QuestionTemplate {

  val simple = {
    get[Option[Long]]("question_templates.id") ~
      get[String]("question_templates.text") ~
      get[String]("question_templates.render_format") ~
      get[String]("question_templates.response_options") ~
      get[Option[String]]("question_templates.help_text") map {
      case id ~ text ~ format ~ responseOptions ~ helpText => QuestionTemplate(id, text, ResponseFormat.withName(format), responseOptions.split(","), helpText)
    }
  }

  implicit val format: Format[QuestionTemplate] = Json.format[QuestionTemplate]
}

class QuestionTemplateDao @Inject() (db: play.api.db.Database) {

    def findQuestionsForCycle(cycleId: Long): Seq[QuestionTemplate] = db.withConnection { implicit connection =>
    SQL("""select * from question_templates where cycle_id = {id} order by id asc""").on('id -> cycleId).as(QuestionTemplate.simple *)
  }
}

case class Nomination (id: Option[Long], from: Option[Person], to: Option[Person], status: FeedbackStatus, lastUpdated: Option[DateTime], questions: Seq[QuestionResponse], shared: Boolean, cycleId: Long, message: Option[String])

object Nomination {

  val simple = {
    get[Long]("nominations.id") ~
    get[String]("nominations.from_email") ~
    get[String]("nominations.to_email") ~
    get[String]("nominations.status") ~
    get[Option[DateTime]]("nominations.last_updated") ~
    get[Boolean]("nominations.shared") ~
    get[Long]("nominations.cycle_id") ~
    get[Option[String]]("nominations.nomination_message") map {
      case id~fromId~toEmail~status~lastUpdated~shared~cycleId~message => (id, fromId, toEmail, FeedbackStatus.withName(status), lastUpdated, shared, cycleId,
        message.map{m => new String(Base64.getDecoder.decode(m))})
    }
  }

  implicit val nominationWrites: Writes[Nomination] = (
    (JsPath \ "id").writeNullable[Long] and
      (JsPath \ "from").writeNullable[Person] and
      (JsPath \ "to").writeNullable[Person] and
      (JsPath \ "status").write[FeedbackStatus] and
      (JsPath \ "lastUpdated").writeNullable[DateTime] and
      (JsPath \ "questions").write[Seq[QuestionResponse]] and
      (JsPath \ "shared").write[Boolean] and
      (JsPath \ "cycleId").write[Long] and
      (JsPath \ "nominationMessage").writeNullable[String]
    )(unlift(Nomination.unapply))
}

class NominationDao @Inject() (db: play.api.db.Database,
                               person: PersonDao,
                               questionTemplate: QuestionTemplateDao,
                               questionResponse: QuestionResponseDao,
                               feedbackCycle: FeedbackCycleDao) {

  def enrich: (Long, String, String, FeedbackStatus, Option[DateTime], Boolean, Long, Option[String]) => Nomination = {
    (id: Long, fromEmail: String, toEmail: String, status: FeedbackStatus, lastUpdated: Option[DateTime], shared: Boolean, cycleId: Long, message: Option[String]) =>
      Nomination(Some(id), person.findByEmail(fromEmail), person.findByEmail(toEmail), status, lastUpdated, Seq(), shared, cycleId, message)
  }

  def getSummary(id: Long): Option[Nomination] = db.withConnection { implicit connection =>
    SQL("""select * from nominations left join person on nominations.from_email = person.email where nominations.id = {id} and status != {status}""")
      .on('id -> id, 'status -> FeedbackStatus.Cancelled.toString).as(Nomination.simple.singleOpt) match {
      case Some((a,b,c,d,e,f,g,h)) => Some(enrich(a,b,c,d,e,f,g,h))
      case _ => None
    }
  }

  def getDetail(id: Long): Option[Nomination] = db.withConnection { implicit connection =>
    getSummary(id).map{ n => addDetail(n) }
  }

  private def addDetail(n: Nomination): Nomination = db.withConnection { implicit connection =>
    n.id match {
      case None     => n
      case Some(id) => Nomination(n.id, n.from, n.to, n.status, n.lastUpdated, questionResponse.findForNomination(id), n.shared, n.cycleId, n.message)}
  }

  def getPendingNominationsForUser(username: String): Seq[Nomination] = db.withConnection { implicit connection =>
    SQL("""select * from nominations where to_email = {email} and initiated_by != {email} and status NOT IN ({statusCancelled}, {statusCancelled}) and cycle_id in (select id from cycle where active = TRUE)""")
      .on('email -> username, 'statusCancelled -> FeedbackStatus.Cancelled.toString, 'statusClosed -> FeedbackStatus.Closed.toString)
      .as(Nomination.simple *).map{case (a,b,c,d,e,f,g,h) => enrich(a,b,c,d,e,f,g,h)}
  }

  def getCurrentNominationsFromUser(username: String): Seq[Nomination] = db.withConnection { implicit connection =>
    person.findByEmail(username) match {
      case Some(_) => SQL( """select * from nominations where from_email = {email} and initiated_by = {email} and status NOT IN ({statusCancelled}, {statusClosed}) and cycle_id in (select id from cycle where active = TRUE)""")
        .on('email -> username, 'statusCancelled -> FeedbackStatus.Cancelled.toString, 'statusClosed -> FeedbackStatus.Closed.toString)
        .as(Nomination.simple *).map { case (a, b, c, d, e, f, g, h) => enrich(a, b, c, d, e, f, g, h) }
      case _ => Seq()
    }
  }

  def getAllCurrentFeedbackForUserAsReport(username: String): Seq[FeedbackGroup] = db.withConnection { implicit connection =>
    getCurrentNominationsFromUser(username).groupBy(_.cycleId).toList
      .map{ case (id, xs) => FeedbackGroup(feedbackCycle.findById(id).getOrElse(FeedbackCycle.orphan), xs)}
  }

  def getAllFeedbackHistoryForUser(username: String): Seq[FeedbackGroup] = db.withConnection { implicit connection =>
    person.findByEmail(username) match {
      case Some(_) =>
        SQL( """select * from nominations where from_email = {email} and initiated_by = {email} and status != {statusCancelled} ORDER BY cycle_id DESC, last_updated DESC""")
          .on('email -> username, 'statusCancelled -> FeedbackStatus.Cancelled.toString, 'statusClosed -> FeedbackStatus.Closed.toString)
          .as(Nomination.simple *).map { case (a, b, c, d, e, f, g, h) => enrich(a, b, c, d, e, f, g, h) }.groupBy(_.cycleId).toList
          .map{ case (id, xs) => FeedbackGroup(feedbackCycle.findById(id).getOrElse(FeedbackCycle.orphan), xs)}
      case _ => Seq()
    }
  }

  def getAllFeedbackHistoryForUserWithDetail(username: String): Seq[FeedbackGroup] = db.withConnection { implicit connection =>
    person.findByEmail(username) match {
      case Some(_) =>
        SQL( """select * from nominations where from_email = {email} and initiated_by = {email} and status != {statusCancelled} ORDER BY cycle_id DESC, last_updated DESC""")
          .on('email -> username, 'statusCancelled -> FeedbackStatus.Cancelled.toString, 'statusClosed -> FeedbackStatus.Closed.toString)
          .as(Nomination.simple *).map { case (a, b, c, d, e, f, g, h) => enrich(a, b, c, d, e, f, g, h) }
          .map{ n => if (n.status == FeedbackStatus.Submitted || n.status == FeedbackStatus.Closed) addDetail(n) else n }
          .groupBy(_.cycleId).toList
          .map{ case (id, xs) => FeedbackGroup(feedbackCycle.findById(id).getOrElse(FeedbackCycle.orphan), xs)}
      case _ => Seq()
    }
  }

  def getPendingFeedbackItemsForUser(username: String): Seq[Nomination] = db.withConnection { implicit connection =>
    SQL("""select * from nominations where to_email = {email} and cycle_id in (select id from cycle where active = TRUE)""")
      .on('email -> username).as(Nomination.simple *)
      .map { case (a,b,c,d,e,f,g,h) => enrich(a,b,c,d,e,f,g,h)}
  }

  def getHistoryFeedbackForUser(username: String): Seq[Nomination] = db.withConnection { implicit connection =>
    SQL("""select * from nominations where to_email = {email} and cycle_id not in (select id from cycle where active = TRUE) ORDER BY last_updated DESC NULLS LAST""")
      .on('email -> username).as(Nomination.simple *)
      .map { case (a,b,c,d,e,f,g,h) => enrich(a,b,c,d,e,f,g,h)}
  }

  private def findNominationByToFromCycle(fromEmail: String, toEmail: String, initiatedEmail: String, cycleId: Long): Option[(Long, FeedbackStatus)] = db.withConnection { implicit connection =>
    SQL("""select * from nominations where from_email = {fromEmail} and to_email = {toEmail} and initiated_by = {initiated} and cycle_id = {cycleId}""")
      .on('fromEmail -> fromEmail, 'toEmail -> toEmail, 'initiated -> initiatedEmail, 'cycleId -> cycleId)
      .as(Nomination.simple.singleOpt)
      .map{ case (a,b,c,d,e,f,g,h) => (a, d)}
  }

  def findNominationForPeopleInCycleWithDetail(manager: String, cycleId: Long): Seq[Nomination] = db.withConnection { implicit connection =>
    SQL("""select * from nominations where from_email in (select email from person where manager_email = {manager}) and cycle_id = {cycleId}""")
      .on('manager -> manager, 'cycleId -> cycleId)
      .as(Nomination.simple *)
      .map { case (a,b,c,d,e,f,g,h) => enrich(a,b,c,d,e,f,g,h)}
      .map{ n => if (n.status == FeedbackStatus.Submitted || n.status == FeedbackStatus.Closed) addDetail(n) else n }
  }

  private def mapUpdateStatusToNomination(initialisedQuestions: Boolean, nominationId: Long, errorMsg: String): Either[Throwable, Nomination] =
    if (!initialisedQuestions) Left(new Exception(errorMsg))
    else getSummary(nominationId) match {
      case None => Left(new Exception(errorMsg))
      case Some(n) => Right(n)
    }

  def reenableNomination(nominationId: Long, message: Option[String]): Either[Throwable, Nomination] = db.withConnection { implicit connection =>
    mapUpdateStatusToNomination(SQL("""update nominations SET status = {status}, nomination_message = {message} where id = {id}""")
      .on(
        'status -> FeedbackStatus.New.toString,
        'id -> nominationId,
        'message -> message.map{m => Base64.getEncoder.encodeToString(m.getBytes(StandardCharsets.UTF_8))}
      ).executeUpdate == 1,
      nominationId, "Could not re-enable existing nomination.")
  }

  def createNomination(fromEmail: String, toEmail: String, cycleId: Long, message: Option[String]): Either[Throwable, Nomination] = db.withConnection { implicit connection =>

    findNominationByToFromCycle(fromEmail, toEmail, fromEmail, cycleId) match {
      case Some((nominationId, FeedbackStatus.Cancelled)) => reenableNomination(nominationId, message)
      case Some(_) => Left(new Exception("Nomination already exists."))
      case None =>
        // status is new until email notification sent
        SQL(
          """insert into nominations
            (from_email, to_email, initiated_by, status, cycle_id, nomination_message)
            values ({fromEmail},{toEmail},{initiated},{status}, {cycle_id}, {message})""")
          .on('fromEmail -> fromEmail,
            'toEmail -> toEmail,
            'initiated -> fromEmail,
            'status -> FeedbackStatus.New.toString,
            'cycle_id -> cycleId,
            'message ->
              message.map{m => Base64.getEncoder.encodeToString(m.getBytes)}
          ).executeInsert() match {
          case None => Left(new Exception("Could not create nomination."))
          case Some(newNominationId) =>
            mapUpdateStatusToNomination(questionResponse.initialiseResponses(newNominationId, questionTemplate.findQuestionsForCycle(cycleId)),
              newNominationId, "Could not initialise question")
        }
    }
  }

  def submitFeedback(nominationId: Long, questions: Seq[QuestionResponse], submitted: Boolean) : Boolean = db.withConnection { implicit connection =>

    val status: FeedbackStatus = if (submitted) FeedbackStatus.Submitted else FeedbackStatus.Pending
    questionResponse.updateResponses(questions) match {
      case true =>
        SQL("update nominations SET status={status}, last_updated = {lastUpdated} where id={id}")
          .on('status -> status.toString, 'lastUpdated -> DateTime.now, 'id->nominationId)
          .executeUpdate == 1
      case _ => false
    }
  }

  def cancelNomination(nominationId: Long): Boolean = db.withConnection { implicit connection =>
    SQL("""update nominations SET status = {status} where id = {id}""")
      .on('id -> nominationId, 'status -> FeedbackStatus.Cancelled.toString).executeUpdate == 1
  }
}

case class FeedbackGroup(cycle: FeedbackCycle, feedback: Seq[Nomination])
object FeedbackGroup {
  implicit val writes: Writes[FeedbackGroup] = Json.writes[FeedbackGroup]
}

case class FeedbackCycle(id: Option[Long], label: String, startDate: DateTime, endDate: DateTime, active: Boolean,
                         questions: Seq[QuestionTemplate], isThreeSixtyReview: Boolean = false,
                         hasOptionalSharing: Boolean = false, hasForcedSharing: Boolean = false)

object FeedbackCycle {

  implicit val writes: Writes[FeedbackCycle] = Json.writes[FeedbackCycle]

  val simple = {
    get[Long]("cycle.id") ~
      get[String]("cycle.label") ~
      get[DateTime]("cycle.start_date") ~
      get[DateTime]("cycle.end_date") ~
      get[Boolean]("cycle.active") ~
      get[Boolean]("cycle.three_sixty_review") ~
      get[Boolean]("cycle.optional_sharing") ~
      get[Boolean]("cycle.forced_sharing") map {
      case id ~ label ~ start_date ~ end_date ~ active ~ isThreeSixty ~ forcedSharing ~ optionalSharing =>
        FeedbackCycle(Some(id), label, start_date, end_date, active, Seq(), isThreeSixtyReview = isThreeSixty,
          hasForcedSharing = forcedSharing, hasOptionalSharing = optionalSharing)
    }
  }

  val rich = {
    get[Long]("cycle.id") ~
      get[String]("cycle.label") ~
      get[DateTime]("cycle.start_date") ~
      get[DateTime]("cycle.end_date") ~
      get[Boolean]("cycle.active") ~
      get[Boolean]("cycle.three_sixty_review") ~
      get[Boolean]("cycle.optional_sharing") ~
      get[Boolean]("cycle.forced_sharing") map {
      case id ~ label ~ start_date ~ end_date ~ active ~ isThreeSixty ~ forcedSharing ~ optionalSharing =>
        FeedbackCycle(Some(id), label, start_date, end_date, active, Seq(),//questionTemplate.findQuestionsForCycle(id),
          isThreeSixtyReview = isThreeSixty, hasForcedSharing = forcedSharing, hasOptionalSharing = optionalSharing)
    }
  }

  val orphan = FeedbackCycle(None, "Review Cycle no longer maintained", new DateTime(0), new DateTime(0), false, Seq())
}

class FeedbackCycleDao @Inject() (db: play.api.db.Database, questionTemplate: QuestionTemplateDao) {
  def findById(id: Long): Option[FeedbackCycle] = db.withConnection { implicit connection =>
    SQL("""select * from cycle where id = {id}""").on('id -> id).as(FeedbackCycle.simple.singleOpt)
  }

  def findDetailsById(id: Long): Option[FeedbackCycle] = findById(id).map{
    cycle => cycle.copy(questions = questionTemplate.findQuestionsForCycle(id))
  }

  def findActiveCycles: Seq[FeedbackCycle] = db.withConnection { implicit connection =>
    SQL("""select * from cycle where active = true order by start_date desc""").as(FeedbackCycle.simple *)
  }

  def findAllCycles: Seq[FeedbackCycle] = db.withConnection { implicit connection =>
    SQL("""select * from cycle order by start_date desc""").as(FeedbackCycle.simple *)
  }

  def validateCycle(cycleId: Long) : Boolean = db.withConnection { implicit connection =>
    SQL("""select * from cycle where id = {id} and active = true""").on('id -> cycleId)
      .as(FeedbackCycle.simple.singleOpt) match {
      case Some(_) => true
      case None => false
    }
  }
}

case class AdHocFeedback(id: Option[Long], fromEmail: String, fromName: String, fromRole: String, toEmail: String, toName: String, toRole: String, created: DateTime, message: String, publish: Boolean)

object AdHocFeedback{

  implicit val format: Format[AdHocFeedback] = Json.format[AdHocFeedback]

  val simple = {
    get[Long]("ad_hoc_feedback.id") ~
      get[String]("ad_hoc_feedback.from_email") ~
      get[String]("ad_hoc_feedback.from_name") ~
      get[String]("ad_hoc_feedback.from_role") ~
      get[String]("ad_hoc_feedback.to_email") ~
      get[String]("ad_hoc_feedback.to_name") ~
      get[String]("ad_hoc_feedback.to_role") ~
      get[String]("ad_hoc_feedback.message") ~
      get[DateTime]("ad_hoc_feedback.created") ~
      get[Boolean]("ad_hoc_feedback.recipient_visible") map {
      case id~fromEmail~fromName~fromRole~toEmail~toName~toRole~message~created~candidateVisible =>
        AdHocFeedback(id = Some(id), fromEmail = fromEmail, fromName = fromName, fromRole = fromRole, toEmail = toEmail, toName = toName, toRole = toRole, created = created, message = new String(Base64.getDecoder.decode(message)), publish = candidateVisible)
    }
  }
}

class AdHocFeedbackDao @Inject() (db: play.api.db.Database) {

  def createAdHocFeedback(f: AdHocFeedback): Option[AdHocFeedback] = db.withConnection { implicit connection =>
    SQL(
      """insert into ad_hoc_feedback
            (from_email, from_name, from_role, to_email, to_name, to_role, message, created, recipient_visible)
            values ({fromEmail},{fromName},{fromRole},{toEmail},{toName},{toRole},{message}, {created}, {visible})""")
      .on('fromEmail -> f.fromEmail,
        'fromName -> f.fromName,
        'fromRole -> f.fromRole,
        'toEmail -> f.toEmail,
        'toName -> f.toName,
        'toRole -> f.toRole,
        'message -> Base64.getEncoder.encodeToString(f.message.getBytes),
        'created -> f.created,
        'visible -> f.publish
      ).executeInsert().map{ newId => f.copy(id = Some(newId)) }
  }

  def getAdHocFeedbackForReport(email: String): Seq[AdHocFeedback] = db.withConnection { implicit connection =>
    SQL("""select * from ad_hoc_feedback where to_email = {email}""").on('email -> email).as(AdHocFeedback.simple *)
  }

  def getAdHocFeedbackForSelf(email: String): Seq[AdHocFeedback] = db.withConnection { implicit connection =>
    SQL("""select * from ad_hoc_feedback where to_email = {email} and recipient_visible = true""").on('email -> email).as(AdHocFeedback.simple *)
  }

  def getAdHocFeedbackFromSelf(email: String): Seq[AdHocFeedback] = db.withConnection { implicit connection =>
    SQL("""select * from ad_hoc_feedback where from_email = {email}""").on('email -> email).as(AdHocFeedback.simple *)
  }
}
