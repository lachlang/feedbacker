package au.com.feedbacker.controllers

import au.com.feedbacker.AllFixtures
import au.com.feedbacker.model.CredentialStatus.CredentialStatus
import au.com.feedbacker.model.{CredentialStatus, Person}
import org.scalatest.prop.PropertyChecks
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Result
import play.api.test.FakeRequest
import org.scalacheck.Arbitrary.arbitrary

/**
  * Created by lachlang on 25/02/2017.
  */
class SessionManagerSpec extends PlaySpec with AllFixtures with PropertyChecks {

  val arbInvalidStatus = arbitrary[CredentialStatus].suchThat(_ != CredentialStatus.Active)

  "SessionManager#hash" should {
    "never return the value it is given" in {
      forAll (minSuccessful(10)) { arbString:String =>
        whenever (arbString.length >= 1 && arbString.length <= 100) {
          val sessionManager = new SessionManager
          sessionManager.hash(arbString) must not equal arbString
        }
      }
    }
    "never return two values from the same seed" in {
      forAll (minSuccessful(10)) { arbString:String =>
        whenever (arbString.length >= 1 && arbString.length <= 100) {
          val sessionManager = new SessionManager
          val hash1 = sessionManager.hash(arbString)
          val hash2 = sessionManager.hash(arbString)
          hash1 must not equal hash2
        }
      }
    }
  }
  "SessionManager#validatePassword" should {
    "always validate the correct password" in {
      forAll (minSuccessful(3)) { (arbString:String) =>
        whenever (arbString.length >= 8) {
          val sessionManager = new SessionManager
          val hash = sessionManager.hash(arbString)
          sessionManager.validatePassword(arbString, hash) mustBe true
        }
      }
    }
    "never validate the incorrect password" in {
      forAll (minSuccessful(3)) { (arbString1:String, arbString2:String) =>
        whenever (arbString1 != arbString2) {
          val sessionManager = new SessionManager
          val hash = sessionManager.hash(arbString1)
          sessionManager.validatePassword(arbString2, hash) mustBe false
        }
      }
    }
  }
  "SessionManager#extractToken" should {
    "return a valid session token when it exists" in {
      forAll() { (sessionToken:SessionToken, result: Result) =>

        val sessionManager = new SessionManager
        sessionManager.signIn(sessionToken, result)
        val sessionTokenResponse = sessionManager.extractToken(FakeRequest().withCookies(SessionManager.createSessionCookie(sessionToken.token)))
        sessionTokenResponse mustEqual Some(sessionToken)
      }
    }
    "return None for invalid session" in {
      forAll() { (sessionToken: SessionToken) =>
        val sessionManager = new SessionManager
        val sessionTokenResponse = sessionManager.extractToken(FakeRequest().withCookies(SessionManager.createSessionCookie(sessionToken.token)))
        sessionTokenResponse mustBe None
      }
    }
  }
  "SessionManager#initialiseToken" should {
    "return None when the credentials are not active" in {
      forAll(arbInvalidStatus, arbitrary[Person], arbitrary[String]) { (invalidStatus, example, password) =>
        val person = example.copy(credentials = example.credentials.copy(status = invalidStatus))
        whenever(person.credentials.status != CredentialStatus.Active && password.length > 0) {
          val sessionManager = new SessionManager
          val result = sessionManager.initialiseToken(person.setNewHash(sessionManager.hash(password)), password)
          result mustBe None
        }
      }
    }
    "return None when the password is incorrect" in {
      forAll() { (example:Person, password:String) =>
        val person = example.copy(credentials = example.credentials.copy(status = CredentialStatus.Active))
        val sessionManager = new SessionManager
        val result = sessionManager.initialiseToken(person.setNewHash(sessionManager.hash(person.credentials.hash)),password)
        result mustBe None
      }
    }
    "create a session token" in {
      forAll() { example:Person =>
        val person = example.copy(credentials = example.credentials.copy(status = CredentialStatus.Active))
        val sessionManager = new SessionManager
        val result = sessionManager.initialiseToken(person.setNewHash(sessionManager.hash(person.credentials.hash)),person.credentials.hash)
        result must not be None
      }
    }
  }
  "SessionManager#generateToken" should {
    "generate a string of length 86" in {
      val sessionManager = new SessionManager
      val result = sessionManager.generateToken
      result.length mustBe 86
    }
    "generate different strings from consecutive calls" in {
      val sessionManager = new SessionManager
      val result1 = sessionManager.generateToken
      val result2 = sessionManager.generateToken
      val result3 = sessionManager.generateToken
      val result4 = sessionManager.generateToken
      val result5 = sessionManager.generateToken
      result1 must not equal result2
      result1 must not equal result3
      result1 must not equal result4
      result1 must not equal result5
      result2 must not equal result3
      result2 must not equal result4
      result2 must not equal result5
      result3 must not equal result4
      result3 must not equal result5
      result4 must not equal result5
    }
  }
  "SessionManager#signIn" should {
    // cache is already checked by signout and extract token
    "add the session cookie to the response" in {
      forAll { (sessionToken: SessionToken, arbResult: Result) =>
        val sessionManager = new SessionManager
        val result = sessionManager.signIn(sessionToken, arbResult)
        result.header.headers.getOrElse("Set-Cookie","") contains(sessionToken.token) mustBe true
      }
    }
  }
  "SessionManager#signOut" should {
    "remove the session from the cache" in {
      forAll { (sessionToken:SessionToken, arbResult:Result) =>
        val sessionManager = new SessionManager
        sessionManager.signIn(sessionToken, arbResult)
        val signInResponse = sessionManager.extractToken(FakeRequest().withCookies(SessionManager.createSessionCookie(sessionToken.token)))
        signInResponse mustEqual Some(sessionToken)
        sessionManager.signOut(sessionToken, arbResult)
        val signOutResponse = sessionManager.extractToken(FakeRequest().withCookies(SessionManager.createSessionCookie(sessionToken.token)))
        signOutResponse mustBe None
      }
    }
    "remove the session cookie from the response" in {
      forAll { (sessionToken: SessionToken, arbResult: Result) =>
        val sessionManager = new SessionManager
        val resultIn = sessionManager.signIn(sessionToken, arbResult)
        resultIn.header.headers.getOrElse("Set-Cookie","") contains(sessionToken.token) mustBe true
        val resultOut = sessionManager.signOut(sessionToken, arbResult)
        resultOut.header.headers.getOrElse("Set-Cookie","") contains(sessionToken.token) mustBe false
      }
    }
  }
}
