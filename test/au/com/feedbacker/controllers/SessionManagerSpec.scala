package au.com.feedbacker.controllers

import au.com.feedbacker.AllFixtures
import org.scalatest.prop.PropertyChecks
import org.scalatestplus.play.PlaySpec

/**
  * Created by lachlang on 25/02/2017.
  */
class SessionManagerSpec extends PlaySpec with AllFixtures with PropertyChecks {

  "SessionManager#hash" should {
    "never return the value it is given" in {
      forAll (minSuccessful(3)) { arbString:String =>
        whenever (arbString.length >= 8) {
          val sessionManager = new SessionManager
          sessionManager.hash(arbString) must not equal arbString
        }
      }
    }
    "never return two values from the same seed" in {
      forAll (minSuccessful(3)) { arbString:String =>
        whenever (arbString.length >= 8) {
          val sessionManager = new SessionManager
          val hash1 = sessionManager.hash(arbString)
          val hash2 = sessionManager.hash(arbString)
          hash1 must not equal hash2
        }
      }
    }
  }
  "SessionManager#validateToken" should {
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
        whenever (arbString1.length >= 8 && arbString2.length >= 8 && arbString1 != arbString2) {
          val sessionManager = new SessionManager
          val hash = sessionManager.hash(arbString1)
          sessionManager.validatePassword(arbString2, hash) mustBe false
        }
      }
    }
  }
  "SessionManager#initialiseToken" should {
    "return None when the credendials are not active" in {
//      fail()
    }
    "return None when the password is incorrect" in {

    }
    "create a session token" in {

    }
  }
  "SessionManager#generateToken" should {
    "generate a string of length 30" in {
//      fail()
    }
  }
  "SessionManager#signIn" should {
    "add the session to the cache" in {

    }
    "add the session cookie to the response" in {

    }
  }
  "SessionManager#signOut" should {
    "remove the session from the cache" in {

    }
    "remove the session cookie from the response" in {

    }
  }
}
