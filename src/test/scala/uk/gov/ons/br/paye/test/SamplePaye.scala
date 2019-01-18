package uk.gov.ons.br.paye.test

import uk.gov.ons.br.models.{Address, Lifespan, LinkToLegalUnit}
import uk.gov.ons.br.paye.models.{EmployeeSplits, Jobs, Paye, PayeRef}
import uk.gov.ons.br.paye.test.SamplePaye.Values

trait SamplePaye {
  import Values._
  val SamplePayeRef = PayeRef(Payeref)
  val SamplePayeUnitWithAllFields = Paye(
    payeref = SamplePayeRef,
    name = Name,
    tradingStyle = Some(TradingStyle),
    legalStatus = LegalStatus,
    stc = Some(Stc),
    previousPayeref = Some(PreviousPayeref),
    employerCategory = Some(EmployerCategory),
    actionDate = Some(ActionDate),
    lifespan = Some(Lifespan(
      birthDate = BirthDate,
      deathDate = Some(DeathDate),
      deathCode = Some(DeathCode)
    )),
    jobs = Some(Jobs(
      mar = Some(MarJobs),
      jun = Some(JunJobs),
      sep = Some(SepJobs),
      dec = Some(DecJobs),
      lastUpdated = Some(LastUpdatedJobs)
    )),
    employeeSplits = Some(EmployeeSplits(
      mfullemp = Some(Mfullemp),
      msubemp = Some(Msubemp),
      ffullemp = Some(Ffullemp),
      fsubemp = Some(Fsubemp),
      unclemp = Some(Unclemp),
      unclsubemp = Some(Unclsubemp)
    )),
    address = Address(
      line1 = AddressLine1,
      line2 = Some(AddressLine2),
      line3 = Some(AddressLine3),
      line4 = Some(AddressLine4),
      line5 = Some(AddressLine5),
      postcode = Postcode
    ),
    links = Some(LinkToLegalUnit(ubrn = Ubrn))
  )

  val SamplePayeUnitWithOnlyMandatoryFields = Paye(
    payeref = SamplePayeRef,
    name = Name,
    tradingStyle = None,
    legalStatus = LegalStatus,
    stc = None,
    previousPayeref = None,
    employerCategory = None,
    actionDate = None,
    lifespan = None,
    jobs = None,
    employeeSplits = None,
    address = Address(
      line1 = AddressLine1,
      line2 = None,
      line3 = None,
      line4 = None,
      line5 = None,
      postcode = Postcode
    ),
    links = None
  )

  val SamplePayeUnit = SamplePayeUnitWithAllFields
}

object SamplePaye extends SamplePaye {
  object Values {
    val Payeref = "065H7Z31732"
    val Name= "Big Box Cereal Limited"
    val TradingStyle = "Big Box Cereal"
    val LegalStatus = "A"
    val Stc = "6616"
    val PreviousPayeref = "035H7A22"
    val EmployerCategory = "9"
    val ActionDate = "20/12/2018"
    val BirthDate = "11/12/2011"
    val DeathDate = "16/11/2018"
    val DeathCode = "590723"
    val MarJobs = 120
    val JunJobs = 160
    val SepJobs = 130
    val DecJobs = 100
    val LastUpdatedJobs = "01/10/2018"
    val Mfullemp = 23
    val Msubemp = 26
    val Ffullemp = 42
    val Fsubemp = 35
    val Unclemp = 6
    val Unclsubemp = 18
    val AddressLine1 = "Lane Top Farm"
    val AddressLine2 = "1 Bottom Lane"
    val AddressLine3 = "Blackshaw Head"
    val AddressLine4 = "Hebden Bridge"
    val AddressLine5 = "West Yorkshire"
    val Postcode = "SS5 4PR"
    val Ubrn = "1000012345000080"
  }
}