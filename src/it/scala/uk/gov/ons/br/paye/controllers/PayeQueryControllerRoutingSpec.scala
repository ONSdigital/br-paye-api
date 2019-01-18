package uk.gov.ons.br.paye.controllers


import com.google.inject.{AbstractModule, TypeLiteral}
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestData
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, _}
import play.mvc.Http.HttpVerbs.GET
import uk.gov.ons.br.models.Address
import uk.gov.ons.br.paye.controllers.PayeQueryControllerRoutingSpec.SamplePayeUnit
import uk.gov.ons.br.paye.models.{Paye, PayeRef}
import uk.gov.ons.br.repository.QueryRepository
import uk.gov.ons.br.test.UnitSpec

import scala.concurrent.Future

/*
 * We are relying on the Play Router to perform request parameter validation for us (in accordance with regex
 * constraints specified in the routes file).
 * This spec tests that the Router is configured correctly for Paye queries.
 *
 * NOTE: MockFactory needs to be the right-most trait to avoid 'assertion failed: Null expectation context -
 * missing withExpectations?' error.
 */
class PayeQueryControllerRoutingSpec extends UnitSpec with GuiceOneAppPerTest with MockFactory {
  /*
   * Stub the repository layer so that valid requests find a unit.
   */
  override def newAppForTest(testData: TestData): Application = {
    val queryResult = Right(Some(SamplePayeUnit))
    val queryRepository = stub[QueryRepository[PayeRef, Paye]]
    (queryRepository.queryByUnitReference _).when(*).returns(Future.successful(queryResult))

    val fakeModule = new AbstractModule {
      override def configure(): Unit = {
        bind(new TypeLiteral[QueryRepository[PayeRef, Paye]]() {}).toInstance(queryRepository)
        ()
      }
    }

    new GuiceApplicationBuilder().overrides(fakeModule).build()
  }

  private trait Fixture {
    val ValidMinLengthPayeRef = "125H"
    val ValidMaxLengthPayeRef = "125H7A716207"

    def fakeRequestTo(uri: String) =
      FakeRequest(method = GET, path = uri)
  }

  "A request to query a PAYE admin unit by PAYE reference" - {
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

        status(result.value) shouldBe OK
      }

      "the target PAYE reference comprises 12 alphanumeric characters" in new Fixture {
        val result = route(app, fakeRequestTo(s"/v1/paye/$ValidMaxLengthPayeRef"))

        status(result.value) shouldBe OK
      }
    }
  }
}

private object PayeQueryControllerRoutingSpec {
  val SamplePayeUnit = Paye(
    payeref = PayeRef("065H7Z31732"),
    name = "some-name",
    tradingStyle = None,
    legalStatus = "some-legalStatus",
    stc = None,
    previousPayeref = None,
    employerCategory = None,
    actionDate = None,
    lifespan = None,
    jobs = None,
    employeeSplits = None,
    address = Address(
      line1 = "some-line1",
      line2 = None,
      line3 = None,
      line4 = None,
      line5 = None,
      postcode = "some-postcode"
    ),
    links = None)
}