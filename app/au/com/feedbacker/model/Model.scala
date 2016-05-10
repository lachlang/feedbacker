package au.com.feedbacker.models

import org.joda.time.DateTime

case class Person(id: Option[Long], credentials: Credentials, managerEmail: String, role: String)

case class Credentials(email: String, token: String) 

// case class Pending

object FeedbackStatus extends Enumeration {
	type FeedbackStatus = Value
	val New = Value("New")
	val Pending = Value("Pending")
	val Submitted = Value("Submitted")
	val Closed = Value("Closed")
}
import FeedbackStatus._

case class Nomination (id: Option[Long], from: Person, to: Person, toManager: Person, status: FeedbackStatus, lastUpdated: DateTime, feedbackId: Option[Long])

case class FeedbackItem(id: Long, feedbackForName: String, feedbackFromName: String, managerName: String, status: FeedbackStatus, shareFeedback: Boolean, questions: List[Question])

case class Question(text: String, responseOptions: List[String], response: Option[String], comments: Option[String])

case class SummaryItem(id: Option[Long], status: FeedbackStatus, name: String, role: String, managerName: String, lastUpdated: DateTime, shared: Option[Boolean])
