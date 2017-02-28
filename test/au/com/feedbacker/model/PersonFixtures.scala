package au.com.feedbacker.model

import au.com.feedbacker.model.CredentialStatus.CredentialStatus
import org.scalacheck.Arbitrary
import org.scalacheck.Gen.oneOf
import org.scalacheck.Arbitrary.arbitrary

/**
  * Created by lachlang on 27/02/2017.
  */
trait PersonFixtures {

  implicit val arbCredential = Arbitrary(
    for {
      email  <- arbitrary[String]
      hash   <- arbitrary[String]
      status <- arbitrary[CredentialStatus]
    } yield Credentials(email = email, hash, status)
  )

  implicit val arbPerson = Arbitrary(
    for {
      id <- arbitrary[Option[Long]]
      name <- arbitrary[String]
      role <- arbitrary[String]
      credentials <- arbitrary[Credentials]
      managerEmail <- arbitrary[String]
      isLeader <- arbitrary[Boolean]
    } yield Person(id, name, role, credentials, managerEmail, isLeader)
  )

  implicit val arbCredentialStatusGen = Arbitrary(oneOf(CredentialStatus.values.toSeq))
}
