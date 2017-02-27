package au.com.feedbacker.controllers

import org.scalatestplus.play.PlaySpec

/**
  * Created by lachlang on 25/02/2017.
  */
class SessionManagerSpec extends PlaySpec {

  "SessionManager#hash" should {
    "never return the value it is given" in {
//      fail()
    }
    "never return two values from the same seed" in {
//      fail()
    }
  }
  "SessionManager#validateToken" should {
    "always validate the correct password" in {
//      fail()
    }
    "never validate the incorrect password" in {
//      fail()
    }
  }
  "SessionManager#initialiseToken" should {
    "" in {
//      fail()
    }
  }
  "SessionManagergenerateToken" should {
    "generate a string of length 30" in {
//      fail()
    }
  }

}
