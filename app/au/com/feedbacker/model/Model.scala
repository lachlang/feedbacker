package au.com.feedbacker.model

import org.joda.time.DateTime

// import javax.inject.Inject
import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format}

// import scala.concurrent.Future
// import scala.language.postfixOps

object CredentialStatus extends Enumeration {
	type CredentialStatus = Value
  val Active = Value("Active")
  val Nominated = Value("Nominated")
	val Inactive = Value("Inactive")
	val Restricted = Value("Restricted")
}
import CredentialStatus._

object FeedbackStatus extends Enumeration {
	type FeedbackStatus = Value
	val New = Value("New")
	val Pending = Value("Pending")
	val Submitted = Value("Submitted")
  val Cancelled = Value("Cancelled")
  val Closed = Value("Closed")
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
      case id~name~role~email~pass_hash~user_status~manager_email => Person(id, name, role, Credentials(email, pass_hash, user_status), manager_email)
    }
  }

  implicit val format: Format[Person] = (
      (JsPath \ "body" \ "id").formatNullable[Long] and
      (JsPath \ "body" \ "name").format[String] and
      (JsPath \ "body" \ "role").format[String] and
      (JsPath \ "body" \ "creds").format[Credentials] and
      (JsPath \ "body" \ "managerEmail").format[String]
    )(Person.apply, unlift(Person.unapply))

  // Queries
  /**
   * Retrieve a person by email address.
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
              insert into person (name, role, email, pass_hash, user_status, manager_email)values (
                {name},{role},{email},{pass_hash},{user_status},{manager_email}
              )
            """).on(
              'name -> person.name,
              'role -> person.role,
              'email -> person.credentials.email,
              'pass_hash -> person.credentials.token,
              'user_status -> person.credentials.status,
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
            'pass_hash -> person.credentials.token,
            'user_status -> person.credentials.status,
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

case class Credentials(email: String, token: String, status: String = CredentialStatus.Inactive.toString)

object Credentials {

  val status = {
    get[Long]("id") ~
    get[String]("user_status") map {
      case id~status => (id, CredentialStatus.withName(status))
    }
  }

  implicit val format: Format[Credentials] = (
      (JsPath \ "email").format[String] and
      (JsPath \ "pass_hash").format[String] and
      (JsPath \ "user_status").format[String]
    )(Credentials.apply, unlift(Credentials.unapply))

  def findStatusByEmail(email:String): Option[(Long, CredentialStatus)] = DB.withConnection { implicit connection =>
    SQL("select id, user_status from person where email = {email}").on('email -> email).as(Credentials.status.singleOpt)
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

  def findForNomination(nominationId: Long): Seq[QuestionResponse] = DB.withConnection { implicit connection =>
    SQL("""select * from question_response where nomination_id = {id}""").on('id -> nominationId).as(QuestionResponse.simple *).toSeq
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

//  def enrich: Option[(Long, Long, String, FeedbackStatus, DateTime, Boolean)] => Option[Nomination] = _ match {
//    case None => None
//    case Some((id: Long, fromId: Long, toEmail: String, status: FeedbackStatus, lastUpdated: DateTime, shared: Boolean)) =>
//      Some(Nomination(Some(id), Person.findById(fromId), Person.findByEmail(toEmail), status, lastUpdated, Seq(), shared))
//  }
//
//  def getSummary(id: Long): Option[Nomination] = DB.withConnection { implicit connection =>
//    Nomination.enrich(SQL("""select * from nominations left join person on nominations.from_id = person.id where nominations.id = {id} and status != {status}""")
//    .on('id -> id, 'status -> FeedbackStatus.Cancelled.toString).as(Nomination.simple.singleOpt))
//  }

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
    val user: Person = Person.findById(id).get
    SQL("""select * from nominations where to_email = {email}""").on('email -> user.credentials.email).as(Nomination.simple *).map{case (a,b,c,d,e,f) => Nomination.enrich(a,b,c,d,e,f)}
  }

  def createNomination(id: Long, email: String): Boolean = DB.withConnection { implicit connection =>
    ???
  }

  def cancelNomination(id: Long): Boolean = DB.withConnection { implicit connection =>
    SQL("""update into nominations (status) values ({status}) id = {id}""").on('id -> id, 'status -> FeedbackStatus.Cancelled.toString) == 1
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

case class QuestionTemplate(id: Option[Long], text: String, responseOptions: String)

object QuestionTemplate {

  val simple = {
    get[Option[Long]]("question.id") ~
    get[String]("question.text") ~
    get[String]("question.responseOptions") map {
      case id~text~responseOptions => QuestionTemplate(id, text, responseOptions)
    }
  }
}