package au.com.feedbacker.model

import au.com.feedbacker.model.CredentialStatus.CredentialStatus
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.prop.PropertyChecks

/**
  * Created by lachlang on 27/02/2017.
  */
trait PersonFixtures extends PropertyChecks {

  implicit val arbCredentialGen = Arbitrary(Gen.resultOf({
    (email: String, hash: String, status: CredentialStatus) =>
      Credentials(email, hash, status)
  }))

  implicit val arbPersonGen: Arbitrary[Gen[Person]] = Arbitrary(Gen.resultOf({
    (id: Option[Long], name: String, role: String, credentials: Credentials, managerEmail: String, isLeader: Boolean) =>
      Person(id, name, role, credentials, managerEmail, isLeader)
  }))

  implicit val arbCredentialStatusGen = Arbitrary(Gen.oneOf(CredentialStatus.values.toSeq))
}
