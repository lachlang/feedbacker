package au.com.feedbacker.model

import au.com.feedbacker.controllers.{ResetPasswordContent, SessionToken}
import au.com.feedbacker.model.CredentialStatus.CredentialStatus
import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalacheck.Gen.oneOf
import org.scalacheck.Arbitrary.arbitrary

/**
  * Created by lachlang on 27/02/2017.
  */
trait ModelFixtures {

  implicit val arbCredential: Arbitrary[Credentials] = Arbitrary(
    for {
      email  <- validEmailAddresses()
      hash   <- arbitrary[String].suchThat(_.length > 0)
      status <- arbitrary[CredentialStatus]
    } yield Credentials(email = email, hash, status)
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

  implicit val arbCredentialStatus: Arbitrary[CredentialStatus] = Arbitrary(oneOf(CredentialStatus.values.toSeq))

  implicit val arbSessionToken: Arbitrary[SessionToken] = Arbitrary(
    for {
      email   <- validEmailAddresses()
      token   <- Gen.listOfN(86, Gen.alphaNumChar).map(_.mkString)
    } yield SessionToken(email, token)
  )

  implicit val arbResetPasswordContent: Arbitrary[ResetPasswordContent] = Arbitrary(
    for {
      password  <- arbitrary[String].suchThat(_.length > 0)
      username  <- validEmailAddresses()
      token     <- arbitrary[String].suchThat(_.length > 0)
    } yield ResetPasswordContent(password.take(100), username, token.take(80))
  )

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
