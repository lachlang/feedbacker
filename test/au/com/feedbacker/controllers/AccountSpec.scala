package au.com.feedbacker.controllers

import au.com.feedbacker.model.{CredentialStatus, Credentials, Nomination, NominationDao, Person, PersonDao}

import play.api.libs.json._
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

  val validEmail: String = "valid@test.com"
  val invalidEmail: String = "invalid@test.com"
  val validToken: String = "valid_token"
  val invalidToken: String = "invalid_token"
  val testPerson = Person(Some(1),"Test Guy","User", Credentials(validEmail,"password",CredentialStatus.Active),"boss@test.com", false)
  val validSessionToken = SessionToken(validEmail, validToken)

  "Account#getUser" should {
    "should be forbidden" in {
      // mock
      val mockPersonDao = mock[PersonDao]
      val mockNominationDao = mock[NominationDao]
      val mockSessionManager = mock[SessionManager]
      doCallRealMethod().when(mockSessionManager).extractToken(any())

      // call
      val controller = new Account(mockPersonDao, mockNominationDao, mockSessionManager)
      val result: Future[Result] = controller.getUser().apply(FakeRequest())

      // verify
      status(result) mustBe 403
      contentAsString(result) mustBe ""
      verifyZeroInteractions(mockPersonDao)
    }
  }

  "Account#getUser" should {
    "should be valid" in {
      // mocks
      val mockPersonDao = mock[PersonDao]
      val mockNominationDao = mock[NominationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockSessionManager.extractToken(any())).thenReturn(Some(validSessionToken))
      when(mockPersonDao.findByEmail(validEmail)).thenReturn(Some(testPerson))

      // call
      val controller = new Account(mockPersonDao, mockNominationDao, mockSessionManager)
      val fakeRequest = FakeRequest().withCookies(SessionManager.createSessionCookie(validToken))
      val result: Future[Result] = controller.getUser().apply(fakeRequest)

      // verify
      status(result) mustBe 200
      contentAsJson(result) mustEqual Json.obj("body" -> Json.toJson(testPerson))
      verify(mockSessionManager).extractToken(any())
      verify(mockPersonDao).findByEmail(validEmail)
    }
  }

  "Account#getUser" should {
    "should fail for invalid user" in {
      // mock
      val mockPersonDao = mock[PersonDao]
      val mockNominationDao = mock[NominationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockSessionManager.extractToken(any())).thenReturn(None)

      // call
      val controller = new Account(mockPersonDao, mockNominationDao, mockSessionManager)
      val result: Future[Result] = controller.getUser().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(invalidToken)))

      // verify
      verify(mockSessionManager).extractToken(any())
      status(result) mustBe 403
      contentAsString(result) mustBe ""
    }
  }

  "Account#getReports" should {
    "should return no reports" in {
      // mock
      val mockPersonDao = mock[PersonDao]
      val mockNominationDao = mock[NominationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockPersonDao.findByEmail(validEmail)).thenReturn(Some(testPerson))
      when(mockSessionManager.extractToken(any())).thenReturn(Some(validSessionToken))
      when(mockPersonDao.findDirectReports(validEmail)).thenReturn(Seq())

      // call
      val controller = new Account(mockPersonDao, mockNominationDao, mockSessionManager)
      val result: Future[Result] = controller.getReports().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(validToken)))

      // verify
      verify(mockSessionManager).extractToken(any())
      verify(mockPersonDao).findByEmail(validEmail)
      verify(mockPersonDao).findDirectReports(validEmail)
      status(result) mustBe 200
      contentAsJson(result) mustEqual Json.obj("body" -> Json.arr())
    }
  }

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
