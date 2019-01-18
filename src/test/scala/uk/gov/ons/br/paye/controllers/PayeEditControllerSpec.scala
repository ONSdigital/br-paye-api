package uk.gov.ons.br.paye.controllers

import akka.util.ByteString
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.JsString
import play.api.libs.streams.Accumulator
import play.api.mvc.Results.ImATeapot
import play.api.mvc.{BodyParser, Request, RequestHeader, Result}
import play.api.test.{FakeRequest, StubControllerComponentsFactory}
import uk.gov.ons.br.models.patch.{Patch, TestOperation}
import uk.gov.ons.br.paye.controllers.PayeEditControllerSpec.{SamplePatch, SomeHttpStatus, SomePatchStatus, fakePatchRequest}
import uk.gov.ons.br.paye.models.PayeRef
import uk.gov.ons.br.paye.test.SamplePaye.SamplePayeRef
import uk.gov.ons.br.services.PatchService
import uk.gov.ons.br.services.PatchService.{PatchApplied, PatchStatus}
import uk.gov.ons.br.test.UnitSpec

import scala.concurrent.Future

class PayeEditControllerSpec extends UnitSpec with MockFactory with ScalaFutures with GuiceOneAppPerTest {

  private trait Fixture extends StubControllerComponentsFactory {
    implicit val materializer = app.materializer
    val requestHeader = stub[RequestHeader]
    val bodyParser = mock[BodyParser[Patch]]
    val patchService = mock[PatchService[PayeRef]]
    val patchHandler = mockFunction[PatchStatus, Result]

    val underTest = new PayeEditController(stubControllerComponents(), bodyParser, patchService, patchHandler)
  }

  "An Edit Controller" - {
    "delegates to the patch bodyParser, service and handler to process a patch request" in new Fixture {
      (bodyParser.apply _).expects(requestHeader).returning(Accumulator.done(Right(SamplePatch)))
      (requestHeader.withBody[Patch] _).when(SamplePatch).returns(fakePatchRequest(SamplePatch))
      (patchService.applyPatchTo _).expects(SamplePayeRef, SamplePatch).returning(Future.successful(SomePatchStatus))
      patchHandler.expects(SomePatchStatus).returning(SomeHttpStatus)

      val patchAction = underTest.applyPatch(SamplePayeRef)

      whenReady(patchAction.apply(requestHeader).run(ByteString("some-request-body"))) { result =>
        result shouldBe SomeHttpStatus
      }
    }
  }
}

private object PayeEditControllerSpec {
  val SamplePatch = Seq(TestOperation(path = "/foo", value = JsString("bar")))
  val SomePatchStatus = PatchApplied
  val SomeHttpStatus = ImATeapot

  def fakePatchRequest(patch: Patch): Request[Patch] =
    FakeRequest().withBody(patch)
}