package au.com.feedbacker

import org.scalatest.prop.PropertyChecks
import play.api.mvc.Results


trait PlayFixtures extends PropertyChecks with Results {

  val responseStatuses = Table (
    "result",
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
}