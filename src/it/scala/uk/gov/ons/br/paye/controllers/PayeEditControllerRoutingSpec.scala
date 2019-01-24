package uk.gov.ons.br.paye.controllers

import com.google.inject.{AbstractModule, TypeLiteral}
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestData
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, _}
import uk.gov.ons.br.actions.EditAction.UserIdHeaderName
import uk.gov.ons.br.parsers.JsonPatchBodyParser.JsonPatchMediaType
import uk.gov.ons.br.paye.models.PayeRef
import uk.gov.ons.br.services.PatchService
import uk.gov.ons.br.services.PatchService.PatchApplied
import uk.gov.ons.br.test.UnitSpec

import scala.concurrent.Future

/*
 * We are relying on the Play Router to perform request parameter validation for us (in accordance with regex
 * constraints specified in the routes file).
 * This spec tests that the Router is configured correctly for Paye clerical edits.
 *
 * NOTE: MockFactory needs to be the right-most trait to avoid 'assertion failed: Null expectation context -
 * missing withExpectations?' error.
 */
class PayeEditControllerRoutingSpec extends UnitSpec with GuiceOneAppPerTest with MockFactory {
  /*
   * Stub the service layer so that any patch requests that make it through to the controller are successfully applied.
   */
  override def newAppForTest(testData: TestData): Application = {
    val patchService = stub[PatchService[PayeRef]]
    (patchService.applyPatchTo _).when(*, *).returns(Future.successful(PatchApplied))

    val fakeModule = new AbstractModule {
      override def configure(): Unit = {
        bind(new TypeLiteral[PatchService[PayeRef]]() {}).toInstance(patchService)
        ()
      }
    }

    new GuiceApplicationBuilder().overrides(fakeModule).build()
  }

  private trait Fixture {
    val ValidMinLengthPayeRef = "125H"
    val ValidMaxLengthPayeRef = "125H7A716207"

    def fakeRequestTo(uri: String): FakeRequest[String] =
      FakeRequest(method = PATCH, path = uri).
        withHeaders(CONTENT_TYPE -> JsonPatchMediaType, UserIdHeaderName -> "auser").
        withBody("""[{"op": "test", "path": "/links/ubrn", "value": "old-ubrn"}]""")
  }

  "A request to clerically edit a PAYE admin unit by PAYE reference" - {
    "is rejected when" - {
      "the target PAYE reference comprises fewer than 4 characters" in new Fixture {
        val result = route(app, fakeRequestTo(s"/v1/paye/${ValidMinLengthPayeRef.drop(1)}"))

        status(result.value) shouldBe BAD_REQUEST
      }

      "the target PAYE reference comprises more than 12 characters" in new Fixture {
        val result = route(app, fakeRequestTo(s"/v1/paye/$ValidMaxLengthPayeRef" + "X"))

        status(result.value) shouldBe BAD_REQUEST
      }

      "the target PAYE reference contains non alphanumeric characters" in new Fixture {
        val result = route(app, fakeRequestTo(s"/v1/paye/${ValidMaxLengthPayeRef.drop(1)}" + "~"))

        status(result.value) shouldBe BAD_REQUEST
      }
    }

    "is accepted when" - {
      "the target PAYE reference comprises 4 alphanumeric characters" in new Fixture {
        val result = route(app, fakeRequestTo(s"/v1/paye/$ValidMinLengthPayeRef"))

        status(result.value) shouldBe NO_CONTENT
      }

      "the target PAYE reference comprises 12 alphanumeric characters" in new Fixture {
        val result = route(app, fakeRequestTo(s"/v1/paye/$ValidMaxLengthPayeRef"))

        status(result.value) shouldBe NO_CONTENT
      }
    }
  }
}
