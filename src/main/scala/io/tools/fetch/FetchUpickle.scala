package io.tools.fetch

import com.raquo.laminar.api.L.*
import io.laminext.fetch.*
import _root_.upickle.default.*
import org.scalajs.dom
import org.scalajs.dom.Response

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js.Thenable.Implicits.*
import scala.util.{Failure, Success, Try}

object upickle:

  class NonOkayResponse(val response: Response) extends Throwable

  extension (underlying: FetchEventStreamBuilder)
    def decodeResponse[A](response: Response)(using decoder: Reader[A], ec: ExecutionContext): Future[A] =
      response.text().flatMap { text =>
        Try(read[A](text)) match {
          case Success(a) => Future.successful(a)
          case Failure(error) => Future.failed(ResponseError(error, response))
        }
      }

    def acceptJson(b: FetchEventStreamBuilder): FetchEventStreamBuilder =
      b.updateHeaders(_.updated("accept", "application/json"))

    def decode[A](using decoder: Reader[A], ec: ExecutionContext): EventStream[FetchResponse[A]] =
      acceptJson(underlying).build(decodeResponse[A](_))

    def decodeEither[NonOkay, Okay](implicit
                                    decodeNonOkay: Reader[NonOkay],
                                    decodeOkay: Reader[Okay],
                                    ec: ExecutionContext
                                   ): EventStream[FetchResponse[Either[NonOkay, Okay]]] =
      acceptJson(underlying).build { response =>
        if (response.ok) {
          decodeResponse[Okay](response).map(Right(_))
        } else {
          decodeResponse[NonOkay](response).map(Left(_))
        }
      }

    def decodeOkay[Okay](implicit decodeOkay: Reader[Okay], ec: ExecutionContext): EventStream[FetchResponse[Okay]] =
      acceptJson(underlying).build { response =>
        if (response.ok) {
          decodeResponse[Okay](response)
        } else {
          Future.failed(new NonOkayResponse(response))
        }
      }