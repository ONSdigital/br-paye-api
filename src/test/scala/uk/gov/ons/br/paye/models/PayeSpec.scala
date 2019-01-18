package uk.gov.ons.br.paye.models

import play.api.libs.json.Json
import uk.gov.ons.br.models.Lifespan
import uk.gov.ons.br.paye.test.SamplePaye.{SamplePayeUnitWithAllFields, SamplePayeUnitWithOnlyMandatoryFields}
import uk.gov.ons.br.test.UnitSpec
import uk.gov.ons.br.test.json.JsonString
import uk.gov.ons.br.test.json.JsonString._

class PayeSpec extends UnitSpec {

  private trait Fixture {
    def expectedJsonStrOf(paye: Paye): String = {
      JsonString.ofObject(
        withString(named = "payeref", withValue = paye.payeref.value),
        withString(named = "name", withValue = paye.name),
        withOptionalString(named = "tradingStyle", withValue = paye.tradingStyle),
        withString(named = "legalStatus", withValue = paye.legalStatus),
        withOptionalString(named = "stc", withValue = paye.stc),
        withOptionalString(named = "previousPayeref", withValue = paye.previousPayeref),
        withOptionalString(named = "employerCategory", withValue = paye.employerCategory),
        withOptionalString(named = "actionDate", withValue = paye.actionDate),
        withOptionalObject(named = "lifespan", paye.lifespan.fold(EmptyValues) { lifespan => Seq(
          withString(named = "birthDate", withValue = lifespan.birthDate),
          withOptionalString(named = "deathDate", withValue = lifespan.deathDate),
          withOptionalString(named = "deathCode", withValue = lifespan.deathCode)
        )}: _*),
        withOptionalObject(named = "jobs", paye.jobs.fold(EmptyValues) { jobs => Seq(
          withOptionalInt(named = "mar", withValue = jobs.mar),
          withOptionalInt(named = "jun", withValue = jobs.jun),
          withOptionalInt(named = "sep", withValue = jobs.sep),
          withOptionalInt(named = "dec", withValue = jobs.dec),
          withOptionalString(named = "lastUpdated", withValue = jobs.lastUpdated)
        )}: _*),
        withOptionalObject(named = "employeeSplits", paye.employeeSplits.fold(EmptyValues) { employeeSplits => Seq(
          withOptionalInt(named = "mfullemp", withValue = employeeSplits.mfullemp),
          withOptionalInt(named = "msubemp", withValue = employeeSplits.msubemp),
          withOptionalInt(named = "ffullemp", withValue = employeeSplits.ffullemp),
          withOptionalInt(named = "fsubemp", withValue = employeeSplits.fsubemp),
          withOptionalInt(named = "unclemp", withValue = employeeSplits.unclemp),
          withOptionalInt(named = "unclsubemp", withValue = employeeSplits.unclsubemp)
        )}: _*),
        withObject(named = "address",
          withString(named ="line1", withValue = paye.address.line1),
          withOptionalString(named = "line2", withValue = paye.address.line2),
          withOptionalString(named = "line3", withValue = paye.address.line3),
          withOptionalString(named = "line4", withValue = paye.address.line4),
          withOptionalString(named = "line5", withValue = paye.address.line5),
          withString(named = "postcode", withValue = paye.address.postcode)
        ),
        withOptionalObject(named = "links", paye.links.fold(EmptyValues) { links => Seq(
          withString(named = "ubrn", withValue = links.ubrn)
        )}: _*)
      )
    }
  }

  "A PAYE admin unit" - {
    "can be represented in Json" - {
      "when all fields are defined" in new Fixture {
        Json.toJson(SamplePayeUnitWithAllFields) shouldBe Json.parse(expectedJsonStrOf(SamplePayeUnitWithAllFields))
      }

      "when only mandatory fields are defined" in new Fixture {
        Json.toJson(SamplePayeUnitWithOnlyMandatoryFields) shouldBe Json.parse(expectedJsonStrOf(SamplePayeUnitWithOnlyMandatoryFields))
      }

      "when only some of the lifespan fields are defined" in new Fixture {
        val payeWithPartialLifespan = SamplePayeUnitWithAllFields.copy(lifespan = Some(Lifespan(
          birthDate = "06/07/2015", deathDate = Some("08/09/2018"), deathCode = None
        )))

        Json.toJson(payeWithPartialLifespan) shouldBe Json.parse(expectedJsonStrOf(payeWithPartialLifespan))
      }

      "when only some of the jobs fields are defined" in new Fixture {
        val payeWithPartialJobs = SamplePayeUnitWithAllFields.copy(jobs = Some(Jobs(
          mar = None, jun = Some(42), sep = None, dec = None, lastUpdated = Some("01/07/2018")
        )))

        Json.toJson(payeWithPartialJobs) shouldBe Json.parse(expectedJsonStrOf(payeWithPartialJobs))
      }

      "when only some of the employee splits fields are defined" in new Fixture {
        val payeWithPartialEmployeeSplits = SamplePayeUnitWithAllFields.copy(employeeSplits = Some(EmployeeSplits(
          mfullemp = None, msubemp = Some(3), ffullemp = None, fsubemp = Some(6), unclemp = None, unclsubemp = Some(9)
        )))

        Json.toJson(payeWithPartialEmployeeSplits) shouldBe Json.parse(expectedJsonStrOf(payeWithPartialEmployeeSplits))
      }
    }
  }
}
