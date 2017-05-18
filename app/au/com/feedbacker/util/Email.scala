package au.com.feedbacker.util

import au.com.feedbacker.controllers.SessionToken
import javax.inject.Inject

import au.com.feedbacker.model.{AdHocFeedback, Nomination, Person}
import play.api.libs.mailer.{Email, MailerClient}

/**
 * Created by lachlang on 6/06/2016.
 */
class Emailer @Inject() (mailerClient: MailerClient, configuration: play.api.Configuration) {

  private def to(name: String, email: String) = Seq(s"""$name <$email>""")
  private val from = "Feedbacker <no-reply@feedbacker.com.au>"
  private def getServerPath: String = {
    val server = configuration.getString("feedbacker.server.path").getOrElse("localhost")
    val port = configuration.getString("feedbacker.server.port").getOrElse("80")
    s"""$server:$port"""
  }

  def sendEmail(email: Email): Unit = {
    mailerClient.send(email)
  }

  def sendActivationEmail(name: String, st: SessionToken): Unit = {
    val email = Email(
      "Welcome to Feedbacker",
      from,
      to(name, st.username),
//      Some(Emailer.activationBody(name, st, getServerPath)),
//      None
      None,
      Some(Emailer.activationBodyHtml(name, st, getServerPath))

    )
    sendEmail(email)
  }

  def sendPasswordResetEmail(name: String, st: SessionToken): Unit = {
    val email = Email(
      "Feedbacker Password Reset",
      from,
      to(name, st.username),
      None,
      Some(Emailer.resetPasswordBodyHtml(name, st, getServerPath))
    )
    sendEmail(email)
  }

  def sendNominationNotificationEmail: Nomination => Unit = { nomination =>
    (nomination.to, nomination.from) match {
      case (Some(toUser), Some(fromUser)) => {
        val email = Email(
          "Feedbacker - You have been nominated to provide feedback",
          from,
          to(toUser.name, toUser.credentials.email),
//          Some(Emailer.nominationBody(toUser.name, fromUser.name, getServerPath)),
//          None
          None,
          Some(Emailer.nominationBodyHtml(toUser.name, fromUser.name, getServerPath, nomination.message))
        )
        sendEmail(email)
      }
      case _ => Unit
    }
  }

  def sendAdHocFeedbackEmail(feedback: AdHocFeedback, boss: Option[Person], bossEmail: String): Unit = {
    val toAddress: String = boss match {
      case None => bossEmail
      case Some(b) => s"${b.name} <$bossEmail>"
    }
    val ccAddress: Seq[String] = {
      if (feedback.publish) {
        Seq(s"${feedback.toName} <${feedback.toEmail}>")
      } else {
        Seq()
      }
    }
    val bossName: String = boss.map(m => s" ${m.name}").getOrElse("")
    val email = Email(
      "Feedbacker Password Reset",
      from,
      Seq(toAddress),
      None,
      Some(Emailer.adHocFeedbackBodyHtml(bossName, feedback, getServerPath)),
      None,
      ccAddress
    )
    sendEmail(email)
  }
}

object Emailer {

  private def activationBodyHtml(name: String, st: SessionToken, serverPath: String): String = s"""
            <p>Hi $name,</p>
            <p/>
            <p>Thanks for registering to use Feedbacker.</p>
            <p/>
            <p>To activate your account please navigate to following link:
            <a href="https://$serverPath/api/activate?username=${st.username}&token=${st.token}">Activate your account</a></p>
            <p/>
            <p>Thanks</p>
            <p>The Feedback Team</p>
            <p>(Feedback is always welcome)</p>
         """.stripMargin

  private def nominationBodyHtml(toName: String, fromName: String, serverPath: String, message: Option[String]): String =
    s"""
      <p>Hi $toName,</p>
      <p/>
      <p>$fromName has nominated you to provide feedback on their performance using Feedbacker.</p>
      <p/>
      ${message.map{m => s"<blockquote><p>${m}</p></blockquote><p/>"}.getOrElse("")}
      <p>Feedbacker is a simple web application which simplifies the submission and collations of feedback.</p>
      <p/>
      <p>To sign up to Feedbacker and submit your response, please navigate to <a href="https://$serverPath/#/landing">Feedbacker</a></p>
      <p/>
      <p>Thanks</p>
      <p>The Feedbacker Team</p>
      <p>(Feedback is always welcome)</p>
     """.stripMargin

  private def resetPasswordBodyHtml(name: String, st: SessionToken, serverPath: String): String = s"""
            <p>Hi $name,</p>
            <p/>
            <p>Thanks for registering to use Feedbacker.</p>
            <p/>
            <p>To reset your password please navigate to following link:
            <a href="https://$serverPath/#/resetPassword?username=${st.username}&token=${st.token}">Password reset</a></p>
            <p/>
            <p>Thanks</p>
            <p>The Feedback Team</p>
            <p>(Feedback is always welcome)</p>
         """.stripMargin

  private def adHocFeedbackBodyHtml(name: String, feedback: AdHocFeedback, serverPath: String): String =
    s"""
       |<p>Hi $name,</p>
       |<p/>
       |<p>${feedback.fromName} has just provided some feedback for ${feedback.toName} using <a href="https://$serverPath/#/landing">Feedbacker</a>.</p>
       |<p/>
       |<p>${feedback.fromName} said:</p>
       |<p/>
       |<blockquote><p><em>${feedback.message}</em></p></blockquote>
       |<p/>
       |<p>You can view the response using Feedbacker by navigating to: <a href="https://$serverPath/#/landing">Feedbacker address</a></p>
       |<p/>
       |<p>Thanks</p>
       |<p>The Feedbacker Team</p>
       |<p>(Feedback is always welcome)</p>
       |     """.stripMargin
}