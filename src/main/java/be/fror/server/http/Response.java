/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.fror.server.http;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.net.MediaType;

import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.TreeMap;

/**
 *
 * @author Olivier Grégoire <fror@users.noreply.github.com>
 */
public final class Response {

  private final int status;
  private final ImmutableMultimap<String, Object> metadata;
  private final Object entity;

  Response(int status, Multimap<String, Object> metadata, Object entity) {
    this.status = status;
    this.entity = entity;
    this.metadata = ImmutableMultimap.copyOf(metadata);
  }

  public static Builder copyOf(Response response) {
    return new Builder(response);
  }

  public static Builder status(Status status) {
    return status(status.getStatusCode());
  }

  public static Builder status(int status) {
    return new Builder().status(status);
  }

  public static Builder ok() {
    return status(Status.OK);
  }

  public static Builder ok(Object entity) {
    return ok().entity(entity);
  }

  public static Builder ok(Object entity, MediaType type) {
    return ok().entity(entity).type(type);
  }

  public static Builder serverError() {
    return status(Status.INTERNAL_SERVER_ERROR);
  }

  public static Builder created(URI location) {
    return status(Status.CREATED).location(location);
  }

  public static Builder accepted() {
    return status(Status.ACCEPTED);
  }

  public static Builder accepted(Object entity) {
    return accepted().entity(entity);
  }

  public static Builder noContent() {
    return status(Status.NO_CONTENT);
  }

  public static Builder temporaryRedirect(URI location) {
    return status(Status.TEMPORARY_REDIRECT).location(location);
  }

  public static final class Builder {

    private static final int NO_STATUS = -1;
    private int status = NO_STATUS;
    private final Multimap<String, Object> metadata = Multimaps.newMultimap(new TreeMap<>(String.CASE_INSENSITIVE_ORDER), () -> new ArrayList<>());
    private Object entity;

    Builder() {
    }

    Builder(Response template) {
      this.status = template.status;
      this.metadata.putAll(template.metadata);
      this.entity = template.entity;
    }

    public Response build() {
      if (this.status == NO_STATUS) {
        if (this.entity == null) {
          this.status = 204;
        } else {
          this.status = 200;
        }
      }
      return new Response(status, metadata, entity);
    }

    public Builder status(int status) {
      checkArgument(100 <= status && status <= 599, "Invalid status, found %s", status);
      this.status = status;
      return this;
    }

    public Builder status(Status status) {
      return status(status.getStatusCode());
    }

    public Builder entity(Object entity) {
      this.entity = entity;
      return this;
    }

    public Builder type(MediaType type) {
      return singleHeader(HttpHeaderNames.CONTENT_TYPE, Optional.ofNullable(type));
    }

    public Builder type(String type) {
      return singleHeader(HttpHeaderNames.CONTENT_TYPE, Optional.ofNullable(type));
    }

    public Builder language(String language) {
      return singleHeader(HttpHeaderNames.CONTENT_LANGUAGE, Optional.ofNullable(language));
    }

    public Builder language(Locale language) {
      return singleHeader(HttpHeaderNames.CONTENT_LANGUAGE, Optional.ofNullable(language));
    }

    public Builder location(URI location) {
      return singleHeader(HttpHeaderNames.LOCATION, Optional.ofNullable(location));
    }

    public Builder contentLocation(URI location) {
      return singleHeader(HttpHeaderNames.CONTENT_LOCATION, Optional.ofNullable(location));
    }

    public Builder lastModified(ZonedDateTime lastModified) {
      return this.singleHeader(HttpHeaderNames.LAST_MODIFIED, Optional.ofNullable(lastModified));
    }

//    public Builder cacheControl(CacheControl cacheControl) {
//      return this.singleHeader(HttpHeaderNames.CACHE_CONTROL, Optional.ofNullable(cacheControl));
//    }
    private static final DateTimeFormatter expiresFormatter = DateTimeFormatter
        .ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
        .withZone(ZoneId.of("GMT"));

    public Builder expires(ZonedDateTime expires) {
      return singleHeader(
          HttpHeaderNames.EXPIRES,
          Optional.ofNullable(expires).map((dateTime) -> expiresFormatter.format(dateTime))
      );
    }

    public Builder encoding(String encoding) {
      return this.singleHeader(HttpHeaderNames.CONTENT_ENCODING, Optional.ofNullable(encoding));
    }

    public Builder header(String name, Optional<?> value) {
      checkNotNull(name, "name must not be null");
      checkNotNull(value, "value must not be null");
      if (value.isPresent()) {
        this.metadata.put(name, value.get());
      } else {
        this.metadata.removeAll(name);
      }
      return this;
    }

//    public Builder cookie(NewCookie... cookies) {
//      if (cookies == null || cookies.length == 0) {
//        this.metadata.removeAll(HttpHeaderNames.SET_COOKIE);
//      } else {
//        this.metadata.putAll(HttpHeaderNames.SET_COOKIE, ImmutableList.copyOf(cookies));
//      }
//      return this;
//    }
    private Builder singleHeader(String headerName, Optional<? extends Object> value) {
      this.metadata.removeAll(headerName);
      if (value.isPresent()) {
        this.metadata.put(headerName, value.get());
      }
      return this;
    }
  }

  public enum Status {

    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NO_CONTENT(204, "No Content"),
    RESET_CONTENT(205, "Reset Content"),
    PARTIAL_CONTENT(206, "Partial Content"),
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),
    SEE_OTHER(303, "See Other"),
    NOT_MODIFIED(304, "Not Modified"),
    USE_PROXY(305, "Use Proxy"),
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    PAYMENT_REQUIRED(402, "Payment Required"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    REQUEST_TIMEOUT(408, "Request Timeout"),
    CONFLICT(409, "Conflict"),
    GONE(410, "Gone"),
    LENGTH_REQUIRED(411, "Length Required"),
    PRECONDITION_FAILED(412, "Precondition Failed"),
    REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
    REQUEST_URI_TOO_LONG(414, "Request-URI Too Long"),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
    EXPECTATION_FAILED(417, "Expectation Failed"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported");

    private final int code;
    private final String reason;
    Status(final int statusCode, final String reasonPhrase) {
      this.code = statusCode;
      this.reason = reasonPhrase;
    }

    public int getStatusCode() {
      return code;
    }

    public String getReasonPhrase() {
      return toString();
    }

    @Override
    public String toString() {
      return reason;
    }

    public static Status fromStatusCode(final int statusCode) {
      for (Status s : Status.values()) {
        if (s.code == statusCode) {
          return s;
        }
      }
      throw new IllegalArgumentException("Unknown statusCode");
    }
  }
}
