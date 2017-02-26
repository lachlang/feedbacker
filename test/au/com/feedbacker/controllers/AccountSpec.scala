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

  val email: String = "valid@test.com"
  val token: String = "valid_token"
  val testPerson = Person(Some(1),"Test Guy","User", Credentials(email,"password",CredentialStatus.Active),"boss@test.com", false)
  val sessionToken = SessionToken(email, token)

  def fixture = {
    new {
      val mockPersonDao = mock[PersonDao]
      val mockNominationDao = mock[NominationDao]
      val mockSessionManager = mock[SessionManager]
      val controller = new Account(mockPersonDao, mockNominationDao, mockSessionManager)
    }
  }

  "Account#getUser" should {
    "should be forbidden when no session token is found" in {
      val f = fixture
      when(f.mockSessionManager.extractToken(any())).thenReturn(None)
      val result: Future[Result] = f.controller.getUser().apply(FakeRequest())

      // verify
      status(result) mustBe 403
      contentAsString(result) mustBe ""
      verifyZeroInteractions(f.mockPersonDao)
    }
    "should fail for invalid username" in {
      val f = fixture
      when(f.mockSessionManager.extractToken(any())).thenReturn(None)
      val result: Future[Result] = f.controller.getUser().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(token)))

      // verify
      verify(f.mockSessionManager).extractToken(any())
      status(result) mustBe 403
      contentAsString(result) mustBe ""
    }
    "should return a valid user" in {
      val f = fixture
      when(f.mockSessionManager.extractToken(any())).thenReturn(Some(sessionToken))
      when(f.mockPersonDao.findByEmail(email)).thenReturn(Some(testPerson))
      val result: Future[Result] = f.controller.getUser().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(token)))

      // verify
      status(result) mustBe 200
      contentAsJson(result) mustEqual Json.obj("body" -> Json.toJson(testPerson))
      verify(f.mockSessionManager).extractToken(any())
      verify(f.mockPersonDao).findByEmail(email)
    }
  }
  "Account#getReports" should {
    "should successfully return no reports when none are found" in {
      val f = fixture
      when(f.mockPersonDao.findByEmail(email)).thenReturn(Some(testPerson))
      when(f.mockSessionManager.extractToken(any())).thenReturn(Some(sessionToken))
      when(f.mockPersonDao.findDirectReports(email)).thenReturn(Seq())
      val result: Future[Result] = f.controller.getReports().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(token)))

      // verify
      verify(f.mockSessionManager).extractToken(any())
      verify(f.mockPersonDao).findByEmail(email)
      verify(f.mockPersonDao).findDirectReports(email)
      status(result) mustBe 200
      contentAsJson(result) mustEqual Json.obj("body" -> Json.arr())
    }
    "should render a report when found" in {
      // mock
      val feedbackGroup: FeedbackGroup = FeedbackGroup(FeedbackCycle.orphan, Seq(Nomination(Some(1), Some(testPerson), Some(testPerson), FeedbackStatus.Pending, None, Seq(), false, 1)))
      val f = fixture
      when(f.mockPersonDao.findByEmail(email)).thenReturn(Some(testPerson))
      when(f.mockSessionManager.extractToken(any())).thenReturn(Some(sessionToken))
      when(f.mockPersonDao.findDirectReports(email)).thenReturn(Seq(testPerson))
      when(f.mockNominationDao.getHistoryReportForUser(email)).thenReturn(Seq(feedbackGroup))

      // call
      val result: Future[Result] = f.controller.getReports().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(token)))

      // verify
      verify(f.mockSessionManager).extractToken(any())
      verify(f.mockPersonDao).findByEmail(email)
      verify(f.mockPersonDao).findDirectReports(email)
      verify(f.mockNominationDao).getHistoryReportForUser(email)
      status(result) mustBe 200
      contentAsJson(result) mustEqual Json.obj("body" -> Json.arr(Json.toJson(Report(testPerson, Seq(feedbackGroup)))))
    }
    "should be forbidden for invalid user" in {
      val f = fixture
      when(f.mockSessionManager.extractToken(any())).thenReturn(None)
      val result: Future[Result] = f.controller.getReports().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(token)))

      // verify
      verify(f.mockSessionManager).extractToken(any())
      status(result) mustBe 403
      contentAsString(result) mustEqual ""
    }
  }
  "Account#updateUserDetails" should {
    "should be forbidden for an invalid user" in {
      val f = fixture
      when(f.mockSessionManager.extractToken(any())).thenReturn(None)
      val result: Future[Result] = f.controller.updateUserDetails().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(token)))

      // verify
      status(result) mustBe 403
      contentAsString(result) mustBe ""
      verifyZeroInteractions(f.mockPersonDao)
    }
    "should require a request body" in {
      val f = fixture
      when(f.mockSessionManager.extractToken(any())).thenReturn(Some(sessionToken))
      when(f.mockPersonDao.findByEmail(email)).thenReturn(Some(testPerson))
      val result: Future[Result] = f.controller.updateUserDetails().apply(FakeRequest().withCookies(SessionManager.createSessionCookie(token)))

      // verify
      verify(f.mockSessionManager).extractToken(any())
      verify(f.mockPersonDao).findByEmail(email)
      status(result) mustBe 403
    }
    "should require a json request body" in {
      val f = fixture
      when(f.mockSessionManager.extractToken(any())).thenReturn(Some(sessionToken))
      when(f.mockPersonDao.findByEmail(email)).thenReturn(Some(testPerson))
      val fakeRequest = FakeRequest(PUT, "api/user").withTextBody("This is some text").withCookies(SessionManager.createSessionCookie(token))
      val result: Future[Result] = f.controller.updateUserDetails().apply(fakeRequest)

      // verify
      verify(f.mockSessionManager).extractToken(any())
      verify(f.mockPersonDao).findByEmail(email)
      status(result) mustBe 403
    }
    "should require a valid request body to update" in {
      val f = fixture
      when(f.mockSessionManager.extractToken(any())).thenReturn(Some(sessionToken))
      when(f.mockPersonDao.findByEmail(email)).thenReturn(Some(testPerson))
      val fakeRequest = FakeRequest().withJsonBody(Json.toJson("{'this':'thing'}")).withCookies(SessionManager.createSessionCookie(token))
      val result: Future[Result] = f.controller.updateUserDetails().apply(fakeRequest)

      // verify
      verify(f.mockSessionManager).extractToken(any())
      verify(f.mockPersonDao).findByEmail(email)
      status(result) mustBe 400
      contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> "Could not update user details. "))

      // another call
      val fakeRequest2 = FakeRequest().withJsonBody(Json.toJson(testPerson)).withCookies(SessionManager.createSessionCookie(token))
      val result2 = f.controller.updateUserDetails().apply(fakeRequest2)

      // another verify
      status(result2) mustBe 400
      contentAsJson(result2) mustBe Json.obj("body" -> Json.obj("message" -> "Could not update user details. "))
    }
    "should return an error when the update fails" in {
      val f = fixture
      when(f.mockSessionManager.extractToken(any())).thenReturn(Some(sessionToken))
      when(f.mockPersonDao.findByEmail(email)).thenReturn(Some(testPerson))
      val errorMessage: String = "Could not update yo' stuff."
      when(f.mockPersonDao.update(testPerson)).thenReturn(Left(new Exception(errorMessage)))

      // call
      val fakeRequest = FakeRequest().withJsonBody(Json.toJson(UpdateContent(testPerson.name,testPerson.role,testPerson.managerEmail))).withCookies(SessionManager.createSessionCookie(token))
      val result: Future[Result] = f.controller.updateUserDetails().apply(fakeRequest)

      // verify
      verify(f.mockSessionManager).extractToken(any())
      verify(f.mockPersonDao).findByEmail(email)
      status(result) mustBe 400
      contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> errorMessage))
    }
    "should return the updated user on success" in {
      val f = fixture
      when(f.mockSessionManager.extractToken(any())).thenReturn(Some(sessionToken))
      when(f.mockPersonDao.findByEmail(email)).thenReturn(Some(testPerson))
      when(f.mockPersonDao.update(testPerson)).thenReturn(Right(testPerson))
      val fakeRequest = FakeRequest().withJsonBody(Json.toJson(UpdateContent(testPerson.name,testPerson.role,testPerson.managerEmail))).withCookies(SessionManager.createSessionCookie(token))
      val result: Future[Result] = f.controller.updateUserDetails().apply(fakeRequest)

      // verify
      verify(f.mockSessionManager).extractToken(any())
      verify(f.mockPersonDao).findByEmail(email)
      status(result) mustBe 200
      contentAsJson(result) mustBe Json.obj("apiVersion" -> "1.0") ++ Json.obj("body" -> Json.toJson(testPerson))
    }
  }
}
