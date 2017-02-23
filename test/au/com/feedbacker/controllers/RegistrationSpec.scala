package au.com.feedbacker.controllers

import au.com.feedbacker.model._
import au.com.feedbacker.util.Emailer
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
class RegistrationSpec extends PlaySpec with MockitoSugar with Results{

  val validEmail: String = "valid@test.com"
  val registrationContent = RegistrationContent("Test Guy", "El Guapo", validEmail, "passwordThisIsAHilariousPassword","boss@email.com")
//  val invalidEmail: String = "invalid@test.com"
//  val validToken: String = "valid_token"
//  val invalidToken: String = "invalid_token"
//  val testPerson = Person(Some(1),"Test Guy","User", Credentials(validEmail,"password",CredentialStatus.Active),"boss@test.com", false)
//  val validSessionToken = SessionToken(validEmail, validToken)

  "Registration#register" should {
    "should reject empty requests" in {
      // mock
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockCredentialsDao = mock[CredentialsDao]
      val mockActivationDao = mock[ActivationDao]

      // call
      val body: JsValue = JsNull
      val controller = new Registration(mockEmailer, mockPersonDao, mockCredentialsDao, mockActivationDao)
      val result: Future[Result] = controller.register().apply(FakeRequest().withBody(body))

      // verify
      status(result) mustBe 400
      contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> "Could not parse request."))
      verifyZeroInteractions(mockEmailer)
      verifyZeroInteractions(mockPersonDao)
      verifyZeroInteractions(mockCredentialsDao)
      verifyZeroInteractions(mockActivationDao)
    }
    "should reject invalid payloads" in {
      // mocks
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockCredentialsDao = mock[CredentialsDao]
      val mockActivationDao = mock[ActivationDao]

      // call
      val body: JsValue = Json.obj("some" -> "object")
      val controller = new Registration(mockEmailer, mockPersonDao, mockCredentialsDao, mockActivationDao)
      val result: Future[Result] = controller.register().apply(FakeRequest().withBody(body))

      // verify
      status(result) mustBe 400
      contentAsJson(result) mustEqual Json.obj("body" -> Json.obj("message" -> "Could not parse request."))

    }
    "should reject duplicate user names" in {
      // mock
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockCredentialsDao = mock[CredentialsDao]
      val mockActivationDao = mock[ActivationDao]
      when(mockCredentialsDao.findStatusByEmail(validEmail)).thenReturn(Some(1L, CredentialStatus.Active))

      // call
      val body: JsValue = Json.toJson(registrationContent)
      val controller = new Registration(mockEmailer, mockPersonDao, mockCredentialsDao, mockActivationDao)
      val result: Future[Result] = controller.register().apply(FakeRequest().withBody(body))

      // verify
      verify(mockCredentialsDao).findStatusByEmail(validEmail)
      status(result) mustBe 409
      contentAsJson(result) mustBe Json.obj("body" -> Json.obj("message" -> "User is already registered."))
    }
    "should return valid request for nominated emails not not active" in {

    }
    "should return bad request when failing to send email" in {
      // mocks
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockCredentialsDao = mock[CredentialsDao]
      val mockActivationDao = mock[ActivationDao]

    }
    "should create a new user" in {
      // mocks
      val mockEmailer = mock[Emailer]
      val mockPersonDao = mock[PersonDao]
      val mockCredentialsDao = mock[CredentialsDao]
      val mockActivationDao = mock[ActivationDao]

      // call
      val body: JsValue = JsNull
      val controller = new Registration(mockEmailer, mockPersonDao, mockCredentialsDao, mockActivationDao)
      val result: Future[Result] = controller.register().apply(FakeRequest().withBody(body))

      // verify
    }
  }
}
