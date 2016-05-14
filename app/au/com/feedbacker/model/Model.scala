package au.com.feedbacker.models

import org.joda.time.DateTime
// import java.util.{Date}

import play.api.db._
// import play.api.Play.current

import anorm._
import anorm.SqlParser._

// import scala.language.postfixOps


object CredentialStatus extends Enumeration {
	type CredentialStatus = Value
	val Active = Value("Active")
	val Inactive = Value("Inactive")
	val Restricted = Value("Restricted")
}
import CredentialStatus._

object FeedbackStatus extends Enumeration {
	type FeedbackStatus = Value
	val New = Value("New")
	val Pending = Value("Pending")
	val Submitted = Value("Submitted")
	val Closed = Value("Closed")
}
import FeedbackStatus._

case class Person(id: Option[Long], name: String, role: String, credentials: Credentials, managerEmail: String)
//case class Person(id: Option[Long], credentials: Credentials, managerEmail: String, role: String)

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


  // Queries
  /**
   * Retrieve a person by email address.
   */
//   def findByEmail(email: String): Option[Person] = {
//     DB.withConnection { implicit connection =>
//       SQL("select * from computer where email = {email}").on('email -> email).as(Person.simple.singleOpt)
//     }
//   }

}

case class Credentials(email: String, token: String, status: String)

case class Activations(token: String, email: String, created: DateTime, used: Boolean, expires: DateTime)

case class Nomination (id: Option[Long], from: Person, to: Person, toManager: Person, status: FeedbackStatus, lastUpdated: DateTime, questions: Option[List[QuestionResponse]], shared: Option[Boolean])

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

case class SummaryItem(id: Option[Long], status: FeedbackStatus, name: String, role: String, managerName: String, lastUpdated: DateTime, shared: Option[Boolean])
