package au.com.feedbacker.model

import au.com.feedbacker.controllers.{RegistrationContent, ResetPasswordContent, SessionToken, UpdateContent}
import au.com.feedbacker.model.CredentialStatus.CredentialStatus
import au.com.feedbacker.model.FeedbackStatus.FeedbackStatus
import au.com.feedbacker.model.ResponseFormat.ResponseFormat
import org.joda.time.DateTime
import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalacheck.Gen.oneOf
import org.scalacheck.Arbitrary.arbitrary

/**
  * Created by lachlang on 27/02/2017.
  */
trait ModelFixtures {

  implicit val arbAdHocFeedback: Arbitrary[AdHocFeedback] = Arbitrary(
    for {
      id        <- arbitrary[Option[Long]]
      fromEmail <- validEmailAddresses()
      fromName  <- arbitrary[String]
      fromRole  <- arbitrary[String]
      toEmail   <- validEmailAddresses()
      toName    <- arbitrary[String]
      toRole    <- arbitrary[String]
//      created   <- arbitrary[Option[DateTime]]
      message   <- arbitrary[String]
      publish   <- arbitrary[Boolean]
    } yield AdHocFeedback(id = id, fromEmail = fromEmail, fromName = fromName, fromRole = fromRole, toEmail = toEmail,
      toName = toName, toRole = toRole, created = DateTime.now, message = message, publish = publish)
  )

  implicit val arbCredential: Arbitrary[Credentials] = Arbitrary(
    for {
      email  <- validEmailAddresses()
      hash   <- arbitrary[String].suchThat(_.length > 0)
      status <- arbitrary[CredentialStatus]
    } yield Credentials(email = email, hash, status)
  )

  implicit val arbCredentialStatus: Arbitrary[CredentialStatus] = Arbitrary(oneOf(CredentialStatus.values.toSeq))

  implicit val arbDateTime: Arbitrary[DateTime] = Arbitrary(
    for {
      millis <- arbitrary[Long].suchThat(_ > 0)
    } yield new DateTime(millis)
  )

  implicit val arbFeedbackCycle: Arbitrary[FeedbackCycle] = Arbitrary(
    for {
      id      <- arbitrary[Int]
      label   <- arbitrary[String]
      start   <- arbitrary[DateTime]
      end     <- arbitrary[DateTime]
      active  <- arbitrary[Boolean]
    } yield FeedbackCycle(id = id, label = label, start_date = start, end_date = end, active = active)
  )

  implicit val arbFeedbackGroup: Arbitrary[FeedbackGroup] = Arbitrary(
    for {
      cycle       <- arbitrary[FeedbackCycle]
      n           <- arbitrary[Int].suchThat(_ < 10)
      nominations <- Gen.listOfN(n, arbitrary[Nomination])
    } yield FeedbackGroup(cycle = cycle, feedback = nominations)
  )

  implicit val arbFeedbackStatus: Arbitrary[FeedbackStatus] = Arbitrary(oneOf(FeedbackStatus.values.toSeq))

  implicit val arbNomination: Arbitrary[Nomination] = Arbitrary(
    for {
      id          <- arbitrary[Option[Long]]
      fromPerson  <- arbitrary[Option[Person]]
      toPerson    <- arbitrary[Option[Person]]
      status      <- arbitrary[FeedbackStatus]
      updated     <- arbitrary[Option[DateTime]]
      n           <- arbitrary[Int].suchThat(_ < 10)
      questions   <- Gen.listOfN(n, arbitrary[QuestionResponse])
      shared      <- arbitrary[Boolean]
      cycleId     <- arbitrary[Long]
      message     <- arbitrary[Option[String]]
    } yield Nomination(id = id, from = fromPerson, to = toPerson, status = status, lastUpdated = updated, questions = questions, shared = shared, cycleId = cycleId, message = message)
  )

  implicit val arbPerson: Arbitrary[Person] = Arbitrary(
    for {
      id <- arbitrary[Option[Long]]
      name <- arbitrary[String].suchThat(_.length > 0)
      role <- arbitrary[String].suchThat(_.length > 0)
      credentials <- arbitrary[Credentials]
      managerEmail <- arbitrary[String].suchThat(_.length > 0)
      isLeader <- arbitrary[Boolean]
    } yield Person(id, name, role, credentials, managerEmail, isLeader)
  )

  implicit val arbQuestionResponse: Arbitrary[QuestionResponse] = Arbitrary(
    for {
      id              <- arbitrary[Option[Long]]
      text            <- arbitrary[String]
      format          <- arbitrary[ResponseFormat]
      n               <- arbitrary[Int].suchThat(_ < 10)
      responseOptions <- Gen.listOfN(n, arbitrary[String])
      response        <- Gen.option(Gen.oneOf(responseOptions))
      comments        <- arbitrary[Option[String]]
      helpText        <- arbitrary[Option[String]]
    } yield QuestionResponse(id = id, text = text, format = format, responseOptions = responseOptions, response = response, comments = comments, helpText = helpText)
  )

  implicit val arbRegistrationContent: Arbitrary[RegistrationContent] = Arbitrary(
    for {
      name          <- arbitrary[String].suchThat( _.length > 0)
      role          <- arbitrary[String].suchThat( _.length > 0)
      email         <- validEmailAddresses()
      password      <- arbitrary[String].suchThat( _.length > 0)
      managerEmail  <- validEmailAddresses()
    } yield RegistrationContent(name = name, role = role, email = email, password = password, managerEmail = managerEmail)
  )

  implicit val arbResetPasswordContent: Arbitrary[ResetPasswordContent] = Arbitrary(
    for {
      password  <- arbitrary[String].suchThat(_.length > 0)
      username  <- validEmailAddresses()
      token     <- arbitrary[String].suchThat(_.length > 0)
    } yield ResetPasswordContent(password.take(100), username, token.take(80))
  )

  implicit val arbResponseFormat: Arbitrary[ResponseFormat] = Arbitrary(oneOf(ResponseFormat.values.toSeq))

  implicit val arbSessionToken: Arbitrary[SessionToken] = Arbitrary(
    for {
      email   <- validEmailAddresses()
      token   <- Gen.listOfN(86, Gen.alphaNumChar).map(_.mkString)
    } yield SessionToken(email, token)
  )

  implicit val arbUpdateContent: Arbitrary[UpdateContent] = Arbitrary(
    for {
      name      <- arbitrary[String].suchThat(_.length > 0)
      role      <- arbitrary[String].suchThat(_.length > 0)
      manEmail  <- validEmailAddresses()
    } yield UpdateContent(name = name, role = role, managerEmail = manEmail)
  )


  /**
    * email generator
    * @tparam T
    * @return
    */
  def noShrink[T] = Shrink[T](_ => Stream.empty)
  implicit val dontShrinkStrings: Shrink[String] = noShrink[String]

  def nonEmptyString(char: Gen[Char]) =
    Gen.nonEmptyListOf(char)
      .map(_.mkString)
      .suchThat(!_.isEmpty)

  def chars(chars: String) = Gen.choose(0, chars.length - 1).map(chars.charAt)

  val validMailbox = nonEmptyString(Gen.alphaChar).label("mailbox")

  val validDomain = (for {
    topLevelDomain <- nonEmptyString(Gen.alphaChar)
    otherParts <- Gen.listOf(nonEmptyString(Gen.alphaChar))
  } yield (otherParts :+ topLevelDomain).mkString(".")).label("domain")

  def validEmailAddresses(mailbox: Gen[String] = validMailbox, domain: Gen[String] = validDomain) =
    for {
      mailbox <- mailbox
      domain <- domain
    } yield mailbox.take(70).stripSuffix(".") + "@" + domain.take(50).stripSuffix(".")

}
