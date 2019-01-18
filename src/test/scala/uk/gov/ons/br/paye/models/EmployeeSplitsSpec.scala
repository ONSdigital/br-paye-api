package uk.gov.ons.br.paye.models

import play.api.libs.json.{JsObject, Json}
import uk.gov.ons.br.test.UnitSpec
import uk.gov.ons.br.test.json.JsonString
import uk.gov.ons.br.test.json.JsonString.withOptionalInt

class EmployeeSplitsSpec extends UnitSpec {

  private trait Fixture {
    def expectedJsonStrOf(employeeSplits: EmployeeSplits): String =
      JsonString.ofObject(
        withOptionalInt(named = "mfullemp", withValue = employeeSplits.mfullemp),
        withOptionalInt(named = "msubemp", withValue = employeeSplits.msubemp),
        withOptionalInt(named = "ffullemp", withValue = employeeSplits.ffullemp),
        withOptionalInt(named = "fsubemp", withValue = employeeSplits.fsubemp),
        withOptionalInt(named = "unclemp", withValue = employeeSplits.unclemp),
        withOptionalInt(named = "unclsubemp", withValue = employeeSplits.unclsubemp)
      )
  }

  "Employee Splits" - {
    "can be represented in Json" - {
      "when all fields are defined" in new Fixture {
        val employeeSplits = EmployeeSplits(mfullemp = Some(9), msubemp = Some(5), ffullemp = Some(8),
          fsubemp = Some(2), unclemp = Some(3), unclsubemp = Some(5))

        Json.toJson(employeeSplits) shouldBe Json.parse(expectedJsonStrOf(employeeSplits))
      }

      "when only some fields are defined" in new Fixture {
        val employeeSplits = EmployeeSplits(mfullemp = Some(9), msubemp = None, ffullemp = Some(8),
          fsubemp = None, unclemp = Some(3), unclsubemp = None)

        Json.toJson(employeeSplits) shouldBe Json.parse(expectedJsonStrOf(employeeSplits))
      }

      /*
       * In this scenario, it is the responsibility of any parent object to drop this sub-object completely.
       */
      "when no fields are defined" in new Fixture {
        val employeeSplits = EmployeeSplits(mfullemp = None, msubemp = None, ffullemp = None, fsubemp = None,
          unclemp = None, unclsubemp = None)

        Json.toJson(employeeSplits) shouldBe JsObject.empty
      }
    }
  }
}
