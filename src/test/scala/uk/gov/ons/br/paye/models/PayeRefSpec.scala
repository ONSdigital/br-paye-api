package uk.gov.ons.br.paye.models

import play.api.libs.json.{JsString, Json}
import uk.gov.ons.br.paye.test.SamplePaye.SamplePayeRef
import uk.gov.ons.br.test.UnitSpec

class PayeRefSpec extends UnitSpec {

  private trait Fixture {
    val PayeRefModel = SamplePayeRef
    val PayeRefUnderlyingValue = PayeRefModel.value
  }

  "A PayeRef" - {
    "is written to Json as a simple string value" in new Fixture {
      Json.toJson(PayeRefModel) shouldBe JsString(PayeRefUnderlyingValue)
    }

    "can be bound from a URL path parameter" in new Fixture {
      PayeRef.pathBindable.bind("some-key", PayeRefUnderlyingValue).right.value shouldBe PayeRefModel
    }

    "can be unbound to a URL path parameter" in new Fixture {
      PayeRef.pathBindable.unbind("some-key", PayeRefModel) shouldBe PayeRefUnderlyingValue
    }
  }
}
