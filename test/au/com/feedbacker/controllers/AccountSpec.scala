package au.com.feedbacker.controllers

import au.com.feedbacker.model._
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
      when(mockSessionManager.extractToken(any())).thenReturn(None)

      // call
      val controller = new Account(mockPersonDao, mockNominationDao, mockSessionManager)
      val result: Future[Result] = controller.getUser().apply(FakeRequest())

      // verify
      status(result) mustBe 403
      contentAsString(result) mustBe ""
      verifyZeroInteractions(mockPersonDao)
    }
    "should be valid" in {
      // mocks
      val mockPersonDao = mock[PersonDao]
      val mockNominationDao = mock[NominationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockSessionManager.extractToken(any())).thenReturn(Some(validSessionToken))
      when(mockPersonDao.findByEmail(validEmail)).thenReturn(Some(testPerson))

      // call
      val controller = new Account(mockPersonDao, mockNominationDao, mockSessionManager)
      val result: Future[Result] = controller.getUser().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(validToken)))

      // verify
      status(result) mustBe 200
      contentAsJson(result) mustEqual Json.obj("body" -> Json.toJson(testPerson))
      verify(mockSessionManager).extractToken(any())
      verify(mockPersonDao).findByEmail(validEmail)
    }
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
    "should render a report" in {
      // mock
      val feedbackGroup: FeedbackGroup = FeedbackGroup(FeedbackCycle.orphan, Seq(Nomination(Some(1), Some(testPerson), Some(testPerson), FeedbackStatus.Pending, None, Seq(), false, 1)))
      val mockPersonDao = mock[PersonDao]
      val mockNominationDao = mock[NominationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockPersonDao.findByEmail(validEmail)).thenReturn(Some(testPerson))
      when(mockSessionManager.extractToken(any())).thenReturn(Some(validSessionToken))
      when(mockPersonDao.findDirectReports(validEmail)).thenReturn(Seq(testPerson))
      when(mockNominationDao.getHistoryReportForUser(validEmail)).thenReturn(Seq(feedbackGroup))

      // call
      val controller = new Account(mockPersonDao, mockNominationDao, mockSessionManager)
      val result: Future[Result] = controller.getReports().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(validToken)))

      // verify
      verify(mockSessionManager).extractToken(any())
      verify(mockPersonDao).findByEmail(validEmail)
      verify(mockPersonDao).findDirectReports(validEmail)
      verify(mockNominationDao).getHistoryReportForUser(validEmail)
      status(result) mustBe 200
      contentAsJson(result) mustEqual Json.obj("body" -> Json.arr(Json.toJson(Report(testPerson, Seq(feedbackGroup)))))
    }
    "should be forbidden for invalid user" in {
      // mock
      val mockPersonDao = mock[PersonDao]
      val mockNominationDao = mock[NominationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockSessionManager.extractToken(any())).thenReturn(None)

      // call
      val controller = new Account(mockPersonDao, mockNominationDao, mockSessionManager)
      val result: Future[Result] = controller.getReports().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(validToken)))

      // verify
      verify(mockSessionManager).extractToken(any())
      status(result) mustBe 403
      contentAsString(result) mustEqual ""
    }
  }

  "Account#updateUserDetails" should {
    "should be forbidden for an invalid user" in {
      // mock
      val mockPersonDao = mock[PersonDao]
      val mockNominationDao = mock[NominationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockSessionManager.extractToken(any())).thenReturn(None)

      // call
      val controller = new Account(mockPersonDao, mockNominationDao, mockSessionManager)
      val result: Future[Result] = controller.updateUserDetails().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(validToken)))

      // verify
      status(result) mustBe 403
      contentAsString(result) mustBe ""
      verifyZeroInteractions(mockPersonDao)
    }
    "should require a request body" in {
      // mock
      val mockPersonDao = mock[PersonDao]
      val mockNominationDao = mock[NominationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockSessionManager.extractToken(any())).thenReturn(Some(validSessionToken))
      when(mockPersonDao.findByEmail(validEmail)).thenReturn(Some(testPerson))

      // call
      val controller = new Account(mockPersonDao, mockNominationDao, mockSessionManager)
      val result: Future[Result] = controller.updateUserDetails().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(validToken)))

      // verify
      verify(mockSessionManager).extractToken(any())
      verify(mockPersonDao).findByEmail(validEmail)
      status(result) mustBe 403
    }
    "should require a json request body" in {
      // mock
      val mockPersonDao = mock[PersonDao]
      val mockNominationDao = mock[NominationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockSessionManager.extractToken(any())).thenReturn(Some(validSessionToken))
      when(mockPersonDao.findByEmail(validEmail)).thenReturn(Some(testPerson))

      // call
      val fakeRequest = FakeRequest(PUT, "api/user").withTextBody("This is some text").withCookies(SessionManager.createSessionCookie(validToken))
      val controller = new Account(mockPersonDao, mockNominationDao, mockSessionManager)
      val result: Future[Result] = controller.updateUserDetails().apply(fakeRequest)

      // verify
      verify(mockSessionManager).extractToken(any())
      verify(mockPersonDao).findByEmail(validEmail)
      status(result) mustBe 403
    }
    "should require a valid request body to update" in {
      // mock
      val mockPersonDao = mock[PersonDao]
      val mockNominationDao = mock[NominationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockSessionManager.extractToken(any())).thenReturn(Some(validSessionToken))
      when(mockPersonDao.findByEmail(validEmail)).thenReturn(Some(testPerson))

      // call
      val fakeRequest = FakeRequest().withJsonBody(Json.toJson("{'this':'thing'}")).withCookies(SessionManager.createSessionCookie(validToken))
      val controller = new Account(mockPersonDao, mockNominationDao, mockSessionManager)
      val result: Future[Result] = controller.updateUserDetails().apply(fakeRequest)

      // verify
      verify(mockSessionManager).extractToken(any())
      verify(mockPersonDao).findByEmail(validEmail)
      status(result) mustBe 400
      contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> "Could not update user details. "))

      // another call
      val fakeRequest2 = FakeRequest().withJsonBody(Json.toJson(testPerson)).withCookies(SessionManager.createSessionCookie(validToken))
      val result2 = controller.updateUserDetails().apply(fakeRequest2)

      // another verify
      status(result2) mustBe 400
      contentAsJson(result2) mustBe Json.obj("body" -> Json.obj("message" -> "Could not update user details. "))
    }
    "should return an error when the update fails" in {
      // mock
      val mockPersonDao = mock[PersonDao]
      val mockNominationDao = mock[NominationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockSessionManager.extractToken(any())).thenReturn(Some(validSessionToken))
      when(mockPersonDao.findByEmail(validEmail)).thenReturn(Some(testPerson))
      val errorMessage: String = "Could not update yo' stuff."
      when(mockPersonDao.update(testPerson)).thenReturn(Left(new Exception(errorMessage)))

      // call
      val fakeRequest = FakeRequest().withJsonBody(Json.toJson(UpdateContent(testPerson.name,testPerson.role,testPerson.managerEmail))).withCookies(SessionManager.createSessionCookie(validToken))
      val controller = new Account(mockPersonDao, mockNominationDao, mockSessionManager)
      val result: Future[Result] = controller.updateUserDetails().apply(fakeRequest)

      // verify
      verify(mockSessionManager).extractToken(any())
      verify(mockPersonDao).findByEmail(validEmail)
      status(result) mustBe 400
      contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> errorMessage))
    }
    "should return the updated user on success" in {
      // mock
      val mockPersonDao = mock[PersonDao]
      val mockNominationDao = mock[NominationDao]
      val mockSessionManager = mock[SessionManager]
      when(mockSessionManager.extractToken(any())).thenReturn(Some(validSessionToken))
      when(mockPersonDao.findByEmail(validEmail)).thenReturn(Some(testPerson))
      when(mockPersonDao.update(testPerson)).thenReturn(Right(testPerson))

      // call
      val fakeRequest = FakeRequest().withJsonBody(Json.toJson(UpdateContent(testPerson.name,testPerson.role,testPerson.managerEmail))).withCookies(SessionManager.createSessionCookie(validToken))
      val controller = new Account(mockPersonDao, mockNominationDao, mockSessionManager)
      val result: Future[Result] = controller.updateUserDetails().apply(fakeRequest)

      // verify
      verify(mockSessionManager).extractToken(any())
      verify(mockPersonDao).findByEmail(validEmail)
      status(result) mustBe 200
      contentAsJson(result) mustBe Json.obj("apiVersion" -> "1.0") ++ Json.obj("body" -> Json.toJson(testPerson))
    }
  }
}
