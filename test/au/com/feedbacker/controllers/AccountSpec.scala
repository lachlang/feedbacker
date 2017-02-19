package au.com.feedbacker.controllers

import au.com.feedbacker.model.{CredentialStatus, Credentials, Nomination, NominationDao, Person, PersonDao}

import scala.concurrent.Future
import org.scalatestplus.play._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._

/**
  * Created by lachlang on 16/02/2017.
  */
class AccountSpec extends PlaySpec with MockitoSugar with Results{

  val mockPersonDao = mock[PersonDao]
  val mockNominationDao = mock[NominationDao]
  val mockSessionManager = mock[SessionManager]
  val validEmail: String = "valid@test.com"
  val invalidEmail: String = "invalid@test.com"
  val validToken: String = "asdf1234"
  val invalidToken: String = "1234asdf"
  val testPerson = Person(Some(1),"Test Guy","User", Credentials("user@test.com","password",CredentialStatus.Active),"boss@test.com", false)
  when(mockPersonDao.findByEmail(validEmail)).thenReturn(Some(testPerson))
  when(mockPersonDao.findByEmail(invalidEmail)).thenReturn(None)
  doCallRealMethod().when(mockSessionManager).extractToken(any())
  when(mockSessionManager.validateToken(validToken)).thenReturn(Some(SessionToken(validEmail, validToken)))
  when(mockSessionManager.validateToken(invalidToken)).thenReturn(None)

  "Account#getUser" should {
    "should be forbidden" in {
      val controller = new Account(mockPersonDao, mockNominationDao, mockSessionManager)
      val result: Future[Result] = controller.getUser().apply(FakeRequest())
      status(result) mustBe 403
    }
  }

  "Account#getUser" should {
    "should be valid" in {
      val controller = new Account(mockPersonDao, mockNominationDao, mockSessionManager)
      val result: Future[Result] = controller.getUser().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(validToken)))
      val resultStatus: Int = status(result)
      resultStatus mustBe 200
    }
  }

//  "Account#getReports" should {
//    "should be valid" in {
//      //      when(mockPersonDao.findDirectReports).thenReturn(Person())
//      //      when(mockNominationDao.getHistoryReportForUser).thenReturn(Nomination())
//      val controller = new Account(mockPersonDao, mockNominationDao)
//      val result: Future[Result] = controller.getReports().apply(FakeRequest())
//      val bodyText: String = contentAsString(result)
//      bodyText mustBe "anything"
//    }
//  }
//
//  "Account#updateUserDetails" should {
//    "should be valid" in {
//      //      when(mockPersonDao.findDirectReports).thenReturn(Person())
//      //      when(mockNominationDao.getHistoryReportForUser).thenReturn(Nomination())
//      val controller = new Account(mockPersonDao, mockNominationDao)
//      val result: Future[Result] = controller.updateUserDetails().apply(FakeRequest())
//      val bodyText: String = contentAsString(result)
//      bodyText mustBe "not nothing"
//    }
//  }
}
