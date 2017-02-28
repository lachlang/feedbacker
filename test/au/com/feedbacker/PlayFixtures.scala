package au.com.feedbacker

import org.scalacheck.{Arbitrary, Gen}
import play.api.mvc.{Result, Results}


trait PlayFixtures extends Results {

  implicit val arbResult: Arbitrary[Result] = Arbitrary(
    Gen.oneOf(
      Seq(
        Ok,
        Created,
        Accepted,
        NonAuthoritativeInformation,
        NoContent,
        ResetContent,
        PartialContent,
        MultiStatus,
        MovedPermanently(""),
        Found(""),
        SeeOther(""),
        NotModified,
        TemporaryRedirect(""),
        PermanentRedirect(""),
        BadRequest,
        Unauthorized,
        PaymentRequired,
        Forbidden,
        NotFound,
        MethodNotAllowed,
        NotAcceptable,
        RequestTimeout,
        Conflict,
        Gone,
        PreconditionFailed,
        EntityTooLarge,
        UriTooLong,
        UnsupportedMediaType,
        ExpectationFailed,
        UnprocessableEntity,
        Locked,
        FailedDependency,
        TooManyRequests,
        InternalServerError,
        NotImplemented,
        BadGateway,
        ServiceUnavailable,
        GatewayTimeout,
        HttpVersionNotSupported,
        InsufficientStorage,
        Redirect("", 200)
      )
    )
  )
}