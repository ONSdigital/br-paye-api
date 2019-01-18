package uk.gov.ons.br.paye.models

import play.api.libs.json.{JsObject, Json}
import uk.gov.ons.br.test.UnitSpec
import uk.gov.ons.br.test.json.JsonString
import uk.gov.ons.br.test.json.JsonString.{withOptionalInt, withOptionalString}

class JobsSpec extends UnitSpec {

  private trait Fixture {
    def expectedJsonStrOf(jobs: Jobs): String =
      JsonString.ofObject(
        withOptionalInt(named = "mar", withValue = jobs.mar),
        withOptionalInt(named = "jun", withValue = jobs.jun),
        withOptionalInt(named = "sep", withValue = jobs.sep),
        withOptionalInt(named = "dec", withValue = jobs.dec),
        withOptionalString(named = "lastUpdated", withValue = jobs.lastUpdated)
      )
  }

  "Jobs" - {
    "can be represented in Json" - {
      "when all fields are defined" in new Fixture {
        val jobs = Jobs(mar = Some(1), jun = Some(2), sep = Some(3), dec = Some(4), lastUpdated = Some("01/02/2018"))

        Json.toJson(jobs) shouldBe Json.parse(expectedJsonStrOf(jobs))
      }

      "when only some fields are defined" in new Fixture {
        val jobs = Jobs(mar = None, jun = None, sep = None, dec = Some(4), lastUpdated = Some("01/02/2018"))

        Json.toJson(jobs) shouldBe Json.parse(expectedJsonStrOf(jobs))
      }

      /*
       * In this scenario, it is the responsibility of any parent object to drop this sub-object completely.
       */
      "when no fields are defined" in new Fixture {
        val jobs = Jobs(mar = None, jun = None, sep = None, dec = None, lastUpdated = None)

        Json.toJson(jobs) shouldBe JsObject.empty
      }
    }
  }
}
