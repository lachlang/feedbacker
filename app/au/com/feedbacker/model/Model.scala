package au.com.feedbacker.model

import org.joda.time.DateTime

import javax.inject.Inject
import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Format}

import scala.concurrent.Future

// import scala.language.postfixOps

//
object CredentialStatus extends Enumeration {
	type CredentialStatus = Value
	val Active = Value("Active")
	val Inactive = Value("Inactive")
	val Restricted = Value("Restricted")
}
//import CredentialStatus._

object FeedbackStatus extends Enumeration {
	type FeedbackStatus = Value
	val New = Value("New")
	val Pending = Value("Pending")
	val Submitted = Value("Submitted")
	val Closed = Value("Closed")
}
import FeedbackStatus._

case class Person(id: Option[Long], name: String, role: String, credentials: Credentials, managerEmail: String)

object Person {

  // implicit val format: Format[Person] = (
  //   (JsPath \ "data" \ "id").formatNullable[Long] and
  //   (JsPath \ "data" \ "name").format[String] and
  //   (JsPath \ "data" \ "role").format[String] and
  //   (JsPath \ "data" \ "email").format[HashedCredentials]
  // )(Person.apply, unlift(Person.unapply))
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

//  def create(person: Person): Future[Either[Throwable, Person]] = {
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
  implicit val format: Format[Credentials] = (
      (JsPath \ "email").format[String] and
      (JsPath \ "pass_hash").format[String] and
      (JsPath \ "status").format[String]
    )(Credentials.apply, unlift(Credentials.unapply))

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

case class Nomination (id: Option[Long], from: Person, to: Person, toManager: Person, status: FeedbackStatus, lastUpdated: DateTime, questions: Option[List[QuestionResponse]], shared: Option[Boolean])

object Nomination {


  def viewDetail: Nomination = {
    ???
  }

  def cancelNomination: Boolean = {
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
}


case class FeedbackCycle(id: Long, label: String, start_date: DateTime, end_date: DateTime, active: Boolean)//, questions: List[QuestionTemplate])

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