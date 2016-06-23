package au.com.feedbacker.util

import au.com.feedbacker.controllers.SessionToken
import javax.inject.Inject
import au.com.feedbacker.model.Nomination
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
          Some(Emailer.nominationBodyHtml(toUser.name, fromUser.name, getServerPath))
        )
        sendEmail(email)
      }
      case _ => Unit
    }
  }

}

object Emailer {

  private def activationBody(name: String, st: SessionToken, serverPath: String): String = s"""
            Hi $name,

            Thanks for registering to use Feedbacker.

            To activate your account please navigate to following link

            http://$serverPath/api/activate?username=${st.username}&token=${st.token}

            Thanks
            The Feedback Team
            (Feedback is always welcome)
         """.stripMargin

  private def activationBodyHtml(name: String, st: SessionToken, serverPath: String): String = s"""
            <p>Hi $name,</p>
            <p/>
            <p>Thanks for registering to use Feedbacker.</p>
            <p/>
            <p>To activate your account please navigate to following link:
            <a href="http://$serverPath/api/activate?username=${st.username}&token=${st.token}">Acitvate your account</a></p>
            <p/>
            <p>Thanks</p>
            <p>The Feedback Team</p>
            <p>(Feedback is always welcome)</p>
         """.stripMargin

  private def nominationBody(toName: String, fromName: String, serverPath: String): String =
    s"""
       Hi $toName,

       $fromName has nominated you to provide feedback on their performance using Feedbacker.

       Feedbacker is a simple web application which simplifies the submission and collations of feedback.

       To sign up to Feedbacker and submit your response, please navigate to:

       http://$serverPath/#/landing

       Thanks
       The Feedbacker Team
       (Feedback is always welcome)
     """.stripMargin

  private def nominationBodyHtml(toName: String, fromName: String, serverPath: String): String =
    s"""
      <p>Hi $toName,</p>
      <p/>
      <p>$fromName has nominated you to provide feedback on their performance using Feedbacker.</p>
      <p/>
      <p>Feedbacker is a simple web application which simplifies the submission and collations of feedback.</p>
      <p/>
      <p>To sign up to Feedbacker and submit your response, please navigate to: <a href="http://$serverPath/#/landing">Feebacker address</a></p>
      <p/>
      <p>Thanks</p>
      <p>The Feedbacker Team</p>
      <p>(Feedback is always welcome)</p>
     """.stripMargin

  private def nominationBody2(toName: String, fromName: String, serverPath: String): String =
    s"""
      Hi $toName,

      $fromName has nominated you to provide feedback on their performance as part of their annual review using Feedbacker.

      Feedbacker is a simple web application which simplifies the submission and collations of feedback.

      Feedbacker is currently only being used as a limited trail and a voluntary opt-in basis.  If you do
      not wish to use Feedback or are uncomfortable in any way please use your standard HR process.

      To sign up to Feedbacker and submit your response, please navigate to:

      http://$serverPath/#/landing

      Thanks
      The Feedbacker Team
      (Feedback is always welcome)
      """.stripMargin

  private def resetPasswordBody(name: String, st: SessionToken, serverPath: String): String = s"""Hi $name,

            Thanks for registering to use Feedbacker.

            To reset your password please navigate to following link:

            http://$serverPath/#/resetPassword?username=${st.username}&token=${st.token}

            Thanks
            The Feedback Team
            (Feedback is always welcome)
         """.stripMargin

  private def resetPasswordBodyHtml(name: String, st: SessionToken, serverPath: String): String = s"""
            <p>Hi $name,</p>
            <p/>
            <p>Thanks for registering to use Feedbacker.</p>
            <p/>
            <p>To reset your password please navigate to following link:
            <a href="http://$serverPath/#/resetPassword?username=${st.username}&token=${st.token}">Password reset</a></p>
            <p/>
            <p>Thanks</p>
            <p>The Feedback Team</p>
            <p>(Feedback is always welcome)</p>
         """.stripMargin

}