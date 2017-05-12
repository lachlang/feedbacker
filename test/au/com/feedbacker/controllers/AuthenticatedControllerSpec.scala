package au.com.feedbacker.controllers

import au.com.feedbacker.model.{CredentialStatus, Credentials, Person, PersonDao}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import play.api.test.Helpers._
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import au.com.feedbacker.AllFixtures
import org.scalatest.prop.PropertyChecks

import scala.concurrent.Future


/**
  * Created by lachlang on 25/02/2017.
  */
class AuthenticatedControllerSpec extends PlaySpec with MockitoSugar with AllFixtures with PropertyChecks {

  val email: String = "test@email.com"
  val token: String = "token"

  def fixture = {
    new {
      val mockPersonDao = mock[PersonDao]
      val mockSessionManager = mock[SessionManager]
      val controller = new AuthenticatedController(mockPersonDao, mockSessionManager)
    }
  }
  "AuthenticatedController#AuthenticatedAction" should {

    "return forbidden when no session token is provided" in {
      forAll{ (arbResult: Result) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(None)
        val testBody: Person => Result = { _ => arbResult }
        val result: Future[Result] = f.controller.AuthenticatedAction(testBody).apply(FakeRequest())

        // verify
        status(result) mustBe 403
        contentAsString(result) mustBe ""
        verify(f.mockSessionManager).extractToken(any())
        verifyZeroInteractions(f.mockPersonDao)
      }
    }
    "return forbidden for an invalid user" in {
      forAll() { (arbResult: Result) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(SessionToken(email, token)))
        when(f.mockPersonDao.findByEmail(email)).thenReturn(None)
        val testBody: Person => Result = { _ => arbResult }
        val result: Future[Result] = f.controller.AuthenticatedAction(testBody).apply(FakeRequest())

        // verify
        status(result) mustBe 403
        contentAsString(result) mustBe ""
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(email)
      }
    }
    "return the body function for a valid user and session" in {
      forAll() { (arbResult: Result, person: Person) =>
        val f = fixture
        val testBody: Person => Result = { _ => arbResult }
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(SessionToken(email, token)))
        when(f.mockPersonDao.findByEmail(email)).thenReturn(Some(person))
        val result: Future[Result] = f.controller.AuthenticatedAction(testBody).apply(FakeRequest())

        // verify
        status(result) mustBe arbResult.header.status
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(email)
      }
    }
  }
  "AuthenticationController#AuthenticatedRequestAction" should {
    "return forbidden when no session token is provided" in {
      forAll { (arbResult: Result) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(None)
        val testRequestBody: (Person, JsValue) => Result = { (_, _) => arbResult }
        val result: Future[Result] = f.controller.AuthenticatedRequestAction(testRequestBody).apply(FakeRequest())

        // verify
        status(result) mustBe 403
        contentAsString(result) mustBe ""
        verify(f.mockSessionManager).extractToken(any())
        verifyZeroInteractions(f.mockPersonDao)
      }
    }
    "return forbidden for an invalid user" in {
      forAll() { (arbResult: Result) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(SessionToken(email, token)))
        when(f.mockPersonDao.findByEmail(email)).thenReturn(None)
        val testRequestBody: (Person, JsValue) => Result = { (_, _) => arbResult }
        val result: Future[Result] = f.controller.AuthenticatedRequestAction(testRequestBody).apply(FakeRequest())

        // verify
        status(result) mustBe 403
        contentAsString(result) mustBe ""
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(email)
      }
    }
    "return forbidden without a Json request body" in {
      forAll() { (arbResult: Result) =>
        val f = fixture
        val testPerson: Person = Person(Some(1), "name", "role", Credentials(email, "password", CredentialStatus.Active), "boss@test.com")
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(SessionToken(email, token)))
        when(f.mockPersonDao.findByEmail(email)).thenReturn(Some(testPerson))
        val testRequestBody: (Person, JsValue) => Result = { (_, _) => arbResult }
        val result: Future[Result] = f.controller.AuthenticatedRequestAction(testRequestBody).apply(FakeRequest().withTextBody("this is a text body"))

        // verify
        status(result) mustBe 403
        contentAsString(result) mustBe ""
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(email)
      }
    }
    "return the body function for a valid user and session" in {
      forAll() { (person: Person, arbResult: Result) =>
        val f = fixture
        when(f.mockSessionManager.extractToken(any())).thenReturn(Some(SessionToken(email, token)))
        when(f.mockPersonDao.findByEmail(email)).thenReturn(Some(person))
        val testRequestBody: (Person, JsValue) => Result = { (_, _) => arbResult }
        val result: Future[Result] = f.controller.AuthenticatedRequestAction(testRequestBody).apply(FakeRequest().withJsonBody(Json.obj("some" -> "thing")))

        // verify
        status(result) mustBe arbResult.header.status
        verify(f.mockSessionManager).extractToken(any())
        verify(f.mockPersonDao).findByEmail(email)
      }
    }
  }
  "SessionManager#isInReportingLine" should {
    "match first level reports" in {
      forAll { person: Person =>
        val f = fixture
        f.controller.isInReportingLine(person.managerEmail, Some(person)) mustBe true
      }
    }
    "match to at least fifth level reports" in {
      forAll { (person: Person, m1: Person, m2: Person, m3: Person, m4: Person, user: Person) =>
        val f = fixture
        when(f.mockPersonDao.findByEmail(person.managerEmail)).thenReturn(Some(m1))
        when(f.mockPersonDao.findByEmail(m1.managerEmail)).thenReturn(Some(m2))
        when(f.mockPersonDao.findByEmail(m2.managerEmail)).thenReturn(Some(m3))
        when(f.mockPersonDao.findByEmail(m3.managerEmail)).thenReturn(Some(m4.copy(managerEmail = user.credentials.email)))
        f.controller.isInReportingLine(user.credentials.email, Some(person)) mustBe true
      }
    }
    "return false for registered manager" in {
      forAll { (person: Person) =>
        val f = fixture
        f.controller.isInReportingLine(person.credentials.email, None) mustBe false
      }
    }
    "return false when the reporting line ends" in {
      forAll { (person: Person, m1: Person, m2: Person, m3: Person, m4: Person, user: Person) =>
        whenever(
          person.managerEmail != user.credentials.email &&
            m1.managerEmail != user.credentials.email &&
            m2.managerEmail != user.credentials.email &&
            m3.managerEmail != user.credentials.email &&
            m4.managerEmail != user.credentials.email
        ) {
          val f = fixture
          when(f.mockPersonDao.findByEmail(person.managerEmail)).thenReturn(Some(m1))
          when(f.mockPersonDao.findByEmail(m1.managerEmail)).thenReturn(Some(m2))
          when(f.mockPersonDao.findByEmail(m2.managerEmail)).thenReturn(Some(m3))
          when(f.mockPersonDao.findByEmail(m3.managerEmail)).thenReturn(Some(m4))
          when(f.mockPersonDao.findByEmail(m4.managerEmail)).thenReturn(None)
          f.controller.isInReportingLine(user.managerEmail, Some(person)) mustBe false
        }
      }
    }
  }

}
