package au.com.feedbacker.model

import au.com.feedbacker.controllers.SessionToken
import org.joda.time.DateTime
import anorm.JodaParameterMetaData._

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._
import play.api.libs.functional.syntax._
import play.api.libs.json._

object CredentialStatus extends Enumeration {
	type CredentialStatus = Value
  val Active = Value("Active")
  val Nominated = Value("Nominated")
	val Inactive = Value("Inactive")
	val Restricted = Value("Restricted")

  implicit val writes: Writes[CredentialStatus] = new Writes[CredentialStatus] {
    def writes(v: CredentialStatus): JsValue = JsString(v.toString)
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

//  implicit def enumWrites[E <: Enumeration]: Writes[E#Value] = new Writes[E#Value] {
//    def writes(v: E#Value): JsValue = JsString(v.toString)
//  }
//
//  implicit def enumReads[E <: Enumeration](enum: E) : Reads[E#Value] = new Reads[E#Value] {
//    def reads(json: JsValue): JsResult[E#Value] = json match {
//      case JsString(s) => {
//        try {
//          JsSuccess(enum.withName(s))
//        } catch {
//          case _: NoSuchElementException => JsError(s"Enumeration expected of type: '${enum.getClass}', but it does not appear to contain the value: '$s'")
//        }
//      }
//      case _ => JsError("String value expected")
//    }
//  }
//
//  implicit def enumFormat[E <: Enumeration](enum: E): Format[E#Value] = {
//    Format(enumReads(enum), enumWrites)
//  }

  implicit val writes: Writes[FeedbackStatus] = new Writes[FeedbackStatus] {
    def writes(v: FeedbackStatus): JsValue = JsString(v.toString)
  }
//
//  implicit val reads(statusRead: FeedbackStatus) : Reads[FeedbackStatus] = new Reads[FeedbackStatus] {
//    def reads(json: JsValue): JsResult[FeedbackStatus] = json match {
//      case JsString(s) => {
//        try {
//          JsSuccess(FeedbackStatus.withName(s))
//        } catch {
//          case _: NoSuchElementException => JsError(s"Enumeration expected of type: 'FeedbackStatus', but it does not appear to contain the value: '$s'")
//        }
//      }
//      case _ => JsError("String value expected")
//    }
//  }
//  implicit val format(statusFormat: FeedbackStatus): Format[FeedbackStatus] = {
//    Format(reads(statusFormat), writes)
//  }
}
import FeedbackStatus._

case class Person(id: Option[Long], name: String, role: String, credentials: Credentials, managerEmail: String)

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
    get[String]("person.manager_email") map {
      case id~name~role~email~pass_hash~user_status~manager_email => Person(id, name, role, Credentials(email, pass_hash, CredentialStatus.withName(user_status)), manager_email)
    }
  }

  implicit val writes: Writes[Person] = Json.writes[Person]

  // Queries
  /**
   * Retrieve a person by id address.
   */
  def findById(id: Long): Option[Person] = DB.withConnection { implicit connection =>
    SQL("select * from person where id = {id}").on('id -> id).as(Person.simple.singleOpt)
  }

  def findByEmail(email: String): Option[Person] = DB.withConnection { implicit connection =>
    SQL("select * from person where email = {email}").on('email -> email).as(Person.simple.singleOpt)
  }

  def create(person: Person): Either[Throwable, Person] = {
    DB.withConnection { implicit connection =>
      try {
        SQL(
          """
              insert into person (name, role, email, pass_hash, user_status, manager_email) values (
                {name},{role},{email},{pass_hash},{user_status},{manager_email}
              )
            """).on(
              'name -> person.name,
              'role -> person.role,
              'email -> person.credentials.email,
              'pass_hash -> person.credentials.hash,
              'user_status -> person.credentials.status.toString,
              'manager_email -> person.managerEmail
            ).executeInsert() match {
          case None => Left(new Exception("Could not insert."))
          case Some(id) => findById(id) match {
            case None => Left( new Exception("Could not insert."))
            case Some(person) => Right(person)
          }
        }
      } catch {
        case e:Exception => Left(e)
      }
    }
  }

  def createNominee(username: Username): Either[Throwable, Long] = {
    DB.withConnection { implicit connection =>
      try { println("creating shadow user");
        SQL(
          """
            insert into person (name, role, email, pass_hash, user_status, manager_email)values (
              {email},'Nominee',{email},{pass_hash},{user_status},'placeholder@test.com'
            )
          """).on(
            'email -> username,
            'pass_hash -> SessionToken.generateToken,
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
    DB.withConnection { implicit connection =>
      try {
        SQL(
          """
            update person SET id={id},
                              name={name},
                              role={role},
                              email={email},
                              pass_hash={pass_hash},
                              user_status={user_status},
                              manager_email={manager_email}
                              where id={id}
            )
          """).on(
            'id -> person.id,
            'name -> person.name,
            'role -> person.role,
            'email -> person.credentials.email,
            'pass_hash -> person.credentials.hash,
            'user_status -> person.credentials.status.toString,
            'manager_email -> person.managerEmail
          ).executeUpdate() match {
          case 1 => Right(person)
          case _ => Left(new Exception("Could not update."))
        }
      } catch {
        case e:Exception => Left(e)
      }
    }
  }

  def login(email: String, hash: String): Boolean = DB.withConnection{ implicit connection =>
    SQL (
      """
        select count(*) from person where email = {email} and pass_hash = {pass_hash}
      """).on(
        'email -> email,
        'pass_hash -> hash
      ).as(scalar[Long].single) == 1
  }

  /**
   * validates the token exists, belongs to the correct user, has not been used and has not expired,
   * then invalidates the token and updates the user status
   * @param st: SessionToken
   * @return
   */
  def resetPassword(st: SessionToken, passwordHash: String): Boolean = DB.withConnection { implicit connection =>
    if (!Activation.validateToken(st)) false else
      Activation.expireToken(st.token) &&
        SQL("update into person (user_status, pass_hash) values ({status}, {hash}) where email = {email}")
          .on('status -> CredentialStatus.Active.toString,
              'hash -> passwordHash,
              'email -> st.username).execute == 1
  }
}

case class Nominee(name: Person.Name, email: Person.Username, role: String)

object Nominee {

  implicit val writes: Writes[Nominee] = Json.writes[Nominee]

  val simple = {
    get[String]("name") ~
    get[String]("email") ~
    get[String]("role") map {
      case name~email~role => Nominee(name, email, role)
    }
  }
  def findNomineeCandidates: Seq[Nominee] = DB.withConnection { implicit connection =>
    SQL("select name, email, role from person where user_status in ({activeStatus}, {inactiveStatus})")
      .on('activeStatus -> CredentialStatus.Active.toString, 'inactiveStatus -> CredentialStatus.Inactive.toString).as(Nominee.simple *)
  }


}

case class Credentials(email: String, hash: String, status: CredentialStatus = CredentialStatus.Inactive)

object Credentials {

  val status = {
    get[Long]("id") ~
    get[String]("user_status") map {
      case id~status => (id, CredentialStatus.withName(status))
    }
  }

  implicit val writes: Writes[Credentials] = Json.writes[Credentials]

  def findStatusByEmail(email:String): Option[(Long, CredentialStatus)] = DB.withConnection { implicit connection =>
    SQL("select email, pass_hash, user_status from person where email = {email}").on('email -> email).as(Credentials.status.singleOpt)
  }
}

case class Activation(token: String, email: String, created: DateTime, used: Boolean, expires: DateTime)

object Activation {

  private val tokenExpiryInSeconds = 3600

  val simple = {
    get[String]("activation.token") ~
    get[String]("activation.email") ~
    get[DateTime]("activation.created") ~
    get[Boolean]("activation.used") ~
    get[DateTime]("activation.expires") map {
      case token~email~created~used~expires => Activation(token, email, created, used, expires)
    }
  }

  // Queries
  /**
   * validates the token exists, belongs to the correct user, has not been used and has not expired,
   * then invalidates the token and updates the user status
   * @param st: SessionToken
   * @return
   */

  def validateToken(st: SessionToken): Boolean = DB.withConnection { implicit connection =>
    println("validating token ...")
    SQL("select * from activations where token = {token} and email = {email} and used = false and expires < {time}")
      .on('token -> st.token, 'email -> st.username, 'time -> DateTime.now).as(Activation.simple.singleOpt)
    match {
      case Some(_) => println("valid");true
      case None => println("invalid");false
    }
  }

  def expireToken(token: String): Boolean = DB.withConnection { implicit connection =>
    SQL("update activations used = true where token = {token}")
      .on('token -> token).executeUpdate == 1
  }

  def activate(st: SessionToken): Boolean = DB.withConnection { implicit connection =>
    if (!validateToken(st)) false else
      expireToken(st.token) &&
        SQL("update person user_status = {status} where email = {email}")
          .on('status -> CredentialStatus.Active.toString, 'email -> st.username).executeUpdate == 1
  }

  /**
   * create a new activataion token for a user
   * @param username
   * @return
   */
  def createActivationToken(username: String): Option[SessionToken] = DB.withConnection { implicit connection =>
    val token: String = SessionToken.generateToken
    if (SQL(
      """insert into activations (token, email, created, expires, used) values
         ({token}, {email}, {created}, {expires}, false) """)
      .on('token -> token,
        'email -> username,
        'created -> DateTime.now,
        'expires -> (DateTime.now.plus(tokenExpiryInSeconds* 1000))).executeUpdate == 1)
      Some(SessionToken(username, token)) else None
  }
}

case class QuestionResponse(id: Option[Long], text: String, responseOptions: Seq[String], response: Option[String], comments: Option[String])

object QuestionResponse {

  val simple = {
    get[Option[Long]]("question_response.id") ~
      get[String]("question_response.text") ~
      get[String]("question_response.response_options") ~
      get[Option[String]]("question_response.response") ~
      get[Option[String]]("question_response.comments") map {
      case id~text~responseOptions~response~comments => QuestionResponse(id, text, responseOptions.split(","), response, comments)
    }
  }

  implicit val format: Format[QuestionResponse] = (
      (JsPath \ "id").formatNullable[Long] and
      (JsPath \ "text").format[String] and
      (JsPath \ "responseOptions").format[Seq[String]] and
      (JsPath \ "response").formatNullable[String] and
      (JsPath \ "comments").formatNullable[String]
    )(QuestionResponse.apply, unlift(QuestionResponse.unapply))

  def findForNomination(nominationId: Long): Seq[QuestionResponse] = DB.withConnection { implicit connection =>
    SQL("""select * from question_response where nomination_id = {id} order by id asc""").on('id -> nominationId).as(QuestionResponse.simple *).toSeq
  }

  def updateResponses(responses :Seq[QuestionResponse]): Boolean = DB.withConnection { implicit connection =>
    responses.map ( response =>
    SQL("""update question_response SET response={response}, comments={comments} where id={id}""")
      .on('response -> response.response, 'comments -> response.comments, 'id-> response.id).executeUpdate() == 1).foldLeft(true)( (a, b) => a&b)
  }

  def initialiseResponses(nominationId :Long, questions: Seq[QuestionTemplate]): Boolean = DB.withConnection { implicit connection =>
    questions.map { q =>
    SQL("""insert into question_response (nomination_id, text, response_options) values ({nominationId}, {text}, {responseOptions})""")
      .on('nominationId -> nominationId, 'text -> q.text, 'responseOptions -> q.responseOptions.mkString(",")).executeInsert() match {
        case Some(_) => true
        case None => false
      }
    }.foldLeft(true)( _ && _)
  }
}

case class QuestionTemplate(id: Option[Long], text: String, responseOptions: Seq[String])

object QuestionTemplate {

  val simple = {
    get[Option[Long]]("question_templates.id") ~
      get[String]("question_templates.text") ~
      get[String]("question_templates.response_options") map {
      case id~text~responseOptions => QuestionTemplate(id, text, responseOptions.split(","))
    }
  }

  def findQuestionsForCycle(cycleId: Long): Seq[QuestionTemplate] = DB.withConnection { implicit connection =>
    SQL("""select * from question_templates where cycle_id = {id} order by id asc""").on('id -> cycleId).as(QuestionTemplate.simple *)
  }
}

case class Nomination (id: Option[Long], from: Option[Person], to: Option[Person], status: FeedbackStatus, lastUpdated: Option[DateTime], questions: Seq[QuestionResponse], shared: Boolean)

object Nomination {

  val simple = {
    get[Long]("nominations.id") ~
    get[String]("nominations.from_email") ~
    get[String]("nominations.to_email") ~
    get[String]("nominations.status") ~
    get[Option[DateTime]]("nominations.last_updated") ~
    get[Boolean]("nominations.shared") map {
      case id~fromId~toEmail~status~lastUpdated~shared => (id, fromId, toEmail, FeedbackStatus.withName(status), lastUpdated, shared)
    }
  }

  implicit val nominationWrites: Writes[Nomination] = (
    (JsPath \ "id").writeNullable[Long] and
      (JsPath \ "from").writeNullable[Person] and
      (JsPath \ "to").writeNullable[Person] and
      (JsPath \ "status").write[FeedbackStatus] and
      (JsPath \ "lastUpdated").writeNullable[DateTime] and
      (JsPath \ "questions").write[Seq[QuestionResponse]] and
      (JsPath \ "shared").write[Boolean]
    )(unlift(Nomination.unapply))

  def enrich: (Long, String, String, FeedbackStatus, Option[DateTime], Boolean) => Nomination = {
    (id: Long, fromEmail: String, toEmail: String, status: FeedbackStatus, lastUpdated: Option[DateTime], shared: Boolean) =>
    Nomination(Some(id), Person.findByEmail(fromEmail), Person.findByEmail(toEmail), status, lastUpdated, Seq(), shared)
  }

  def getSummary(id: Long): Option[Nomination] = DB.withConnection { implicit connection =>
    SQL("""select * from nominations left join person on nominations.from_email = person.email where nominations.id = {id} and status != {status}""")
      .on('id -> id, 'status -> FeedbackStatus.Cancelled.toString).as(Nomination.simple.singleOpt) match {
      case Some((a,b,c,d,e,f)) => Some(Nomination.enrich(a,b,c,d,e,f))
      case _ => None
    }
  }

  def getDetail(id: Long): Option[Nomination] = DB.withConnection { implicit connection =>
    Nomination.getSummary(id).map{ case n => Nomination(n.id, n.from, n.to, n.status, n.lastUpdated, QuestionResponse.findForNomination(id), n.shared)}
  }

  def getPendingNominationsForUser(username: String): Seq[Nomination] = DB.withConnection { implicit connection =>
      SQL("""select * from nominations where to_email = {email} and initiated_by != {email} and status != {status} and cycle_id in (select id from cycle where active = TRUE)""")
        .on('email -> username, 'status -> FeedbackStatus.Cancelled.toString)
        .as(Nomination.simple *).map{case (a,b,c,d,e,f) => Nomination.enrich(a,b,c,d,e,f)}
  }

  def getCurrentNominationsFromUser(username: String): Seq[Nomination] = DB.withConnection { implicit connection =>
    Person.findByEmail(username) match {
      case Some(_) => SQL( """select * from nominations where from_email = {email} and initiated_by = {email} and status = {status} and cycle_id in (select id from cycle where active = TRUE)""")
        .on('email -> username, 'status -> FeedbackStatus.New.toString)
        .as(Nomination.simple *).map { case (a, b, c, d, e, f) => Nomination.enrich(a, b, c, d, e, f) }
      case _ => Seq()
    }
  }

  def getCurrentFeedbackForUser(username: String): Seq[Nomination] = DB.withConnection { implicit connection =>
    SQL("""select * from nominations where to_email = {email} and cycle_id in (select id from cycle where active = TRUE)""")
      .on('email -> username).as(Nomination.simple *)
      .map { case (a,b,c,d,e,f) => enrich(a,b,c,d,e,f)}
  }

  def getHistoryFeedbackForUser(username: String): Seq[Nomination] = DB.withConnection { implicit connection =>
    SQL("""select * from nominations where to_email = {email} and cycle_id not in (select id from cycle where active = TRUE) ORDER BY last_updated DESC NULLS LAST""")
      .on('email -> username).as(Nomination.simple *)
      .map { case (a,b,c,d,e,f) => enrich(a,b,c,d,e,f)}
  }

  def findNominationByToFromCycle(fromEmail: String, toEmail: String, initiatedEmail: String, cycleId: Long): Option[(Long, FeedbackStatus)] = DB.withConnection { implicit connection =>
    SQL("""select * from nominations where from_email = {fromEmail} and to_email = {toEmail} and initiated_by = {initiated} and cycle_id = {cycleId}""")
      .on('fromEmail -> fromEmail, 'toEmail -> toEmail, 'initiated -> initiatedEmail, 'cycleId -> cycleId)
      .as(Nomination.simple.singleOpt)
      .map{ case (a,b,c,d,e,f) => (a, d)}
  }

  def reenableNomination(nominationId: Long): Either[Throwable, Long] = DB.withConnection { implicit connection =>
    SQL("""update nominations SET status = {status} where id = {id}""")
      .on('status -> FeedbackStatus.New.toString, 'id -> nominationId).executeUpdate == 1 match {
      case true => Right(nominationId)
      case false => Left(new Exception("Could not re-enable existing nomination."))
    }
  }

  def createNomination(fromEmail: String, toEmail: String, cycleId: Long): Either[Throwable, Long] = DB.withConnection { implicit connection =>

    findNominationByToFromCycle(fromEmail, toEmail, fromEmail, cycleId) match {
      case Some((nominationId, FeedbackStatus.Cancelled)) => reenableNomination(nominationId)
      case Some(_) => Left(new Exception("Nomination already exists."))
      case None =>
        // status is new until email notification sent
        SQL(
          """insert into nominations
            (from_email, to_email, initiated_by, status, cycle_id)
            values ({fromEmail},{toEmail},{initiated},{status}, {cycle_id})""")
          .on('fromEmail -> fromEmail,
            'toEmail -> toEmail,
            'initiated -> fromEmail,
            'status -> FeedbackStatus.New.toString,
            'cycle_id -> cycleId).executeInsert() match {
          case None => Left(new Exception("Could not create nomination."))
          case Some(newNominationId) =>
            QuestionResponse.initialiseResponses(newNominationId, QuestionTemplate.findQuestionsForCycle(cycleId)) match {
              case false => Left(new Exception("Could not initialise questions."))
              case true => Right(newNominationId)
            }
        }
    }
  }

  def submitFeedback(nominationId: Long, questions: Seq[QuestionResponse], submitted: Boolean) : Boolean = DB.withConnection { implicit connection =>

    val status: FeedbackStatus = if (submitted) FeedbackStatus.Submitted else FeedbackStatus.Pending
    QuestionResponse.updateResponses(questions) match {
      case true =>
        SQL("update nominations SET status={status}, last_updated = {lastUpdated} where id={id}")
        .on('status -> FeedbackStatus.Pending.toString, 'lastUpdated -> DateTime.now, 'id->nominationId)
          .executeUpdate == 1
      case _ => false
    }
  }

  def cancelNomination(nominationId: Long): Boolean = DB.withConnection { implicit connection =>
    SQL("""update nominations SET status = {status} where id = {id}""")
      .on('id -> nominationId, 'status -> FeedbackStatus.Cancelled.toString).executeUpdate == 1
  }
}

case class FeedbackCycle(id: Long, label: String, start_date: DateTime, end_date: DateTime, active: Boolean)

object FeedbackCycle {

  implicit val writes: Writes[FeedbackCycle] = Json.writes[FeedbackCycle]

  val simple = {
    get[Long]("cycle.id") ~
    get[String]("cycle.label") ~
    get[DateTime]("cycle.start_date") ~
    get[DateTime]("cycle.end_date") ~
    get[Boolean]("cycle.active") map {
      case id~label~start_date~end_date~active => FeedbackCycle(id, label, start_date, end_date, active)
    }
  }

  def findById(id: Long): Option[FeedbackCycle] = DB.withConnection { implicit connection =>
    SQL("""select * from cycle where id = {id}""").on('id -> id).as(FeedbackCycle.simple.singleOpt)
  }

  def findActiveCycles: Seq[FeedbackCycle] = DB.withConnection { implicit connection =>
    SQL("""select * from cycle where active = true""").as(FeedbackCycle.simple *)
  }

  def validateCycle(cycleId: Long) : Boolean = DB.withConnection { implicit connection =>
    SQL("""select * from cycle where id = {id} and active = true""").on('id -> cycleId)
      .as(FeedbackCycle.simple.singleOpt) match {
      case Some(_) => true
      case None => false
    }
  }
}