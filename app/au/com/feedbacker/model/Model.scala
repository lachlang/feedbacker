package au.com.feedbacker.model

import org.joda.time.DateTime

// import javax.inject.Inject
import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.concurrent.{Future, Promise}
// import scala.language.postfixOps

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

  /**
   * Parse a Person from a ResultSet
   */
  val simple = {
    get[Option[Long]]("person.id") ~
    get[String]("person.name") ~
    get[String]("person.role") ~
    get[String]("person.email") ~
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
//  def findById(id: Long): Future[Option[Person]] = DB.withConnection { implicit connection =>
//    val p = Promise[Option[Person]]()
//    p success SQL("select * from person where id = {id}").on('id -> id).as(Person.simple.singleOpt)
//    p future
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
              insert into person (name, role, email, pass_hash, user_status, manager_email)values (
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

  def update(person: Person): Either[Throwable, Person] = {
    DB.withConnection { implicit connection =>
      try {
        SQL(
          """
              update into person (id, name, role, email, pass_hash, user_status, manager_email)values (
                {name},{role},{email},{pass_hash},{user_status},{manager_email}
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
          case 0 => Left(new Exception("Could not update."))
          case _ => Right(person)
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
      ).execute()
  }

  def logout: Boolean = {
    ???
  }

  def activate: Boolean = {
    ???
  }

  def resetPassword: Boolean = {
    ???
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
    SQL("select email, hash, user_status from person where email = {email}").on('email -> email).as(Credentials.status.singleOpt)
  }
}

case class Activation(token: String, email: String, created: DateTime, used: Boolean, expires: DateTime)

object Activation {

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
  def activate: Boolean = {
    ???
  }

}

case class QuestionResponse(id: Option[Long], text: String, responseOptions: String, response: Option[String], comments: Option[String])

object QuestionResponse {

  val simple = {
    get[Option[Long]]("question.id") ~
      get[String]("question.text") ~
      get[String]("question.responseOptions") ~
      get[Option[String]]("question.response") ~
      get[Option[String]]("question.comments") map {
      case id~text~responseOptions~response~comments => QuestionResponse(id, text, responseOptions, response, comments)
    }
  }

  implicit val format: Format[QuestionResponse] = (
      (JsPath \ "id").formatNullable[Long] and
      (JsPath \ "text").format[String] and
      (JsPath \ "responseOptions").format[String] and
      (JsPath \ "response").formatNullable[String] and
      (JsPath \ "comments").formatNullable[String]
    )(QuestionResponse.apply, unlift(QuestionResponse.unapply))

  def findForNomination(nominationId: Long): Seq[QuestionResponse] = DB.withConnection { implicit connection =>
    SQL("""select * from question_response where nomination_id = {id}""").on('id -> nominationId).as(QuestionResponse.simple *).toSeq
  }

  def updateResponses(responses :Seq[QuestionResponse]): Boolean = DB.withConnection { implicit connection =>
    responses.map ( response =>
    SQL("""update into question_response (response, comments) values ({response}, {comments})""")
      .on('response -> response.response, 'comments -> response.comments).executeUpdate() == 1).foldLeft(true)( (a, b) => a&b)
  }

  def initialiseResponses(nominationId :Long, questions: Seq[QuestionTemplate]): Boolean = DB.withConnection { implicit connection =>
    questions.map ( q =>
    SQL("""insert into question_response (nomination_id, text, response_options) values ({nominationId}, {text}, {responseOptions})""")
    .on('nominationId -> nominationId, 'text -> q.text, 'responseOptions -> q.responseOptions).execute()).foldLeft(true)((a,b) => a&b)
  }
}

case class QuestionTemplate(id: Option[Long], text: String, responseOptions: String)

object QuestionTemplate {

  val simple = {
    get[Option[Long]]("question.id") ~
      get[String]("question.text") ~
      get[String]("question.responseOptions") map {
      case id~text~responseOptions => QuestionTemplate(id, text, responseOptions)
    }
  }

  def findQuestionsForCycle(cycleId: Long): Seq[QuestionTemplate] = DB.withConnection { implicit connection =>
    SQL("""select * from question_templates where cycle_id = {id}""").on('id -> cycleId).as(QuestionTemplate.simple *)
  }
}

case class Nomination (id: Option[Long], from: Option[Person], to: Option[Person], status: FeedbackStatus, lastUpdated: DateTime, questions: Seq[QuestionResponse], shared: Boolean)

object Nomination {

  val simple = {
    get[Long]("nominations.id") ~
    get[Long]("nominations.from_id") ~
    get[String]("nominations.to_email") ~
    get[String]("nominations.status") ~
    get[DateTime]("nominations.last_updated") ~
    get[Boolean]("nominations.shared") map {
      case id~fromId~toEmail~status~lastUpdated~shared => (id, fromId, toEmail, FeedbackStatus.withName(status), lastUpdated, shared)
    }
  }

  implicit val nominationWrites: Writes[Nomination] = (
    (JsPath \ "id").writeNullable[Long] and
      (JsPath \ "from").writeNullable[Person] and
      (JsPath \ "to").writeNullable[Person] and
      (JsPath \ "status").write[FeedbackStatus] and
      (JsPath \ "lastUpdated").write[DateTime] and
      (JsPath \ "questions").write[Seq[QuestionResponse]] and
      (JsPath \ "shared").write[Boolean]
    )(unlift(Nomination.unapply))

  def enrich: (Long, Long, String, FeedbackStatus, DateTime, Boolean) => Nomination = {
    (id: Long, fromId: Long, toEmail: String, status: FeedbackStatus, lastUpdated: DateTime, shared: Boolean) =>
    Nomination(Some(id), Person.findById(fromId), Person.findByEmail(toEmail), status, lastUpdated, Seq(), shared)
  }

  def getSummary(id: Long): Option[Nomination] = DB.withConnection { implicit connection =>
    SQL("""select * from nominations left join person on nominations.from_id = person.id where nominations.id = {id} and status != {status}""")
      .on('id -> id, 'status -> FeedbackStatus.Cancelled.toString).as(Nomination.simple.singleOpt) match {
      case None => None
      case Some((a,b,c,d,e,f)) => Some(Nomination.enrich(a,b,c,d,e,f))
    }
  }

  def getDetail(id: Long): Option[Nomination] = DB.withConnection { implicit connection =>
    Nomination.getDetail(id).map{ case n => Nomination(n.id, n.from, n.to, n.status, n.lastUpdated, QuestionResponse.findForNomination(id), n.shared)}
  }

  def getPendingNominationsForUser(id: Long): Seq[Nomination] = DB.withConnection { implicit connection =>
     Person.findById(id) match {
       case Some(user) => SQL("""select * from nominations where to_email = {email} and status != {status} and cycle_id in (select id from cycle where active = TRUE)""")
         .on('email -> user.credentials.email, 'status -> FeedbackStatus.Cancelled.toString)
         .as(Nomination.simple *).map{case (a,b,c,d,e,f) => Nomination.enrich(a,b,c,d,e,f)}
       case None => Seq()
     }
  }

  def getCurrentFeedbackForUser(userId: Long): Seq[Nomination] = DB.withConnection { implicit connection =>
    SQL("""select * from nominations where cycle_id in (select id from cycle where active = TRUE)""").as(Nomination.simple *)
      .map { case (a,b,c,d,e,f) => enrich(a,b,c,d,e,f)}
  }

  def getHistoryFeedbackForUser(userId: Long): Seq[Nomination] = DB.withConnection { implicit connection =>
    SQL("""select * from nominations where cycle_id not in (select id from cycle where active = TRUE) ORDER BY last_updated DESC NULLS LAST""").as(Nomination.simple *)
      .map { case (a,b,c,d,e,f) => enrich(a,b,c,d,e,f)}
  }

  def createNomination(fromId: Long, toEmail: String, cycleId: Long): Either[Throwable, Long] = DB.withConnection { implicit connection =>

    // status is new until email notification sent
    SQL("""insert into nominations (from_id, to_email, status) values ({fromId},{toEmail},{status})""")
      .on('fromId -> fromId, 'toEmail -> toEmail, 'status -> FeedbackStatus.New.toString).executeInsert() match {
      case None => Left(new Exception("Could not create nomination."))
      case Some(nominationId) => QuestionResponse.initialiseResponses(nominationId, QuestionTemplate.findQuestionsForCycle(cycleId)) match {
        case false => Left(new Exception)
        case true => Right(nominationId)
      }
    }
  }

  def submitFeedback(feedback: Nomination) : Boolean = DB.withConnection { implicit connection =>

    QuestionResponse.updateResponses(feedback.questions) match {
      case true =>
        SQL("update into nominations (status, last_updated) values ({status}, {lastUpdated})")
        .on('status -> FeedbackStatus.Submitted.toString, 'lastUpdated -> DateTime.now().getMillis).execute() == 1
      case _ => false
    }
  }

  def cancelNomination(nominationId: Long): Boolean = DB.withConnection { implicit connection =>
    SQL("""update into nominations (status) values ({status}) id = {id}""").on('id -> nominationId, 'status -> FeedbackStatus.Cancelled.toString) == 1
  }
}

case class FeedbackCycle(id: Long, label: String, start_date: DateTime, end_date: DateTime, active: Boolean)

object FeedbackCycle {

  val simple = {
    get[Long]("cycle.id") ~
    get[String]("cycle.label") ~
    get[DateTime]("cycle.start_date") ~
    get[DateTime]("cycle.end_date") ~
    get[Boolean]("cycle.active") map {
      case id~label~start_date~end_date~active => FeedbackCycle(id, label, start_date, end_date, active)
    }
  }
}