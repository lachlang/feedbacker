package au.com.feedbacker.util

import au.com.feedbacker.controllers.SessionToken
import au.com.feedbacker.model.Person

/**
 * Created by lachlang on 6/06/2016.
 */
class Email {

}

object Email {

  def sendActivationEmail(email: String): Unit = ???

  def sendPasswordResetEmail(email: String): Unit = ???

  def sendNominationNotificationEmail(email:String): Unit = ???


  private def activationBody(name: String, st: SessionToken, serverPath: String): String = s"""Hi $name,

            Thanks for registering to use Feedbacker.

            To activate your account please navigate to following link

            $serverPath/api/reset?username=${st.username}&token=${st.token}

            Thanks
            The Feedback Team
            (Feedback is always welcome)
         """.stripMargin

  private def nominationBody(toName: String, fromName: String, serverPath: String): String =
    s"""
       Hi $toName,

       $fromName has nominated you to provide feedback on their performance using Feedbacker.

       Feedbacker is a simple web application which simplifies the submission and collations of feedback.

       To sign up to Feedbacker and submit your response, please navigate to:

       $serverPath/#/landing

       Thanks
       The Feedbacker Team
       (Feedback is always welcome)
     """.stripMargin

  private def nominationBody2(toName: String, fromName: String, serverPath: String): String =
    s"""
      Hi $toName,

      $fromName has nominated you to provide feedback on their performance as part of their annual review using Feedbacker.

      Feedbacker is a simple web application which simplifies the submission and collations of feedback.

      Feedbacker is currently only being used as a limited trail and a voluntary opt-in basis.  If you do
      not wish to use Feedback or are uncomfortable in any way please use your standard HR process.

      To sign up to Feedbacker and submit your response, please navigate to:

      $serverPath/#/landing

      Thanks
      The Feedbacker Team
      (Feedback is always welcome)
      """.stripMargin

  private def resetPasswordBody(name: String, st: SessionToken): String = s"""Hi $name,

            Thanks for registering to use Feedbacker.

            To reset your password please navigate to following link:

            api/resetPassword?username=${st.username}&token=${st.token}

            Thanks
            The Feedback Team
            (Feedback is always welcome)
         """.stripMargin

}

case class AccountEmailRequest(sessionToken: SessionToken, person: Person)