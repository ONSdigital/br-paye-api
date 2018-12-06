package uk.gov.ons.br.paye

import com.github.tomakehurst.wiremock.client.MappingBuilder
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.libs.json.Reads.StringReads
import play.api.libs.json.{Json, Reads}
import play.mvc.Http.MimeTypes.JSON
import uk.gov.ons.br.models.{Address, Lifespan, LinkToLegalUnit}
import uk.gov.ons.br.paye.QueryPayeAcceptanceSpec.{TargetPaye, TargetPayeHBaseResponseBody, TargetPayeRef, aPayeRequest, readsPaye}
import uk.gov.ons.br.paye.models.{EmployeeSplits, Jobs, Paye, PayeRef}
import uk.gov.ons.br.test.hbase.HBaseJsonBodyBuilder.NoMatchFoundResponse
import uk.gov.ons.br.test.hbase.{AbstractServerAcceptanceSpec, HBaseJsonBodyBuilder, HBaseJsonRequestBuilder}
import uk.gov.ons.br.test.matchers.HttpServerErrorStatusCodeMatcher.aServerError

class QueryPayeAcceptanceSpec extends AbstractServerAcceptanceSpec {

  // must match that configured in src/it/resources/it_application.conf
  override val HBasePort: Int = 8075

  info("As a data explorer")
  info("I want to query PAYE admin data")
  info("So that I can build a picture of a business")

  feature("query PAYE admin data by PAYE reference") {
    scenario("when the target PAYE reference matches that of a known unit") { wsClient =>
      Given(s"PAYE admin data exists with PAYE reference $TargetPayeRef")
      stubHBaseFor(aPayeRequest(withPayeRef = TargetPayeRef).willReturn(
        anOkResponse().withBody(TargetPayeHBaseResponseBody)
      ))

      When(s"the PAYE unit with PAYE reference $TargetPayeRef is requested")
      val response = await(wsClient.url(s"/v1/paye/${TargetPayeRef.value}").get())

      Then(s"the details of the PAYE admin data with PAYE reference $TargetPayeRef are returned")
      response.status shouldBe OK
      response.header(CONTENT_TYPE).value shouldBe JSON
      response.json.as[Paye](readsPaye) shouldBe TargetPaye
    }

    scenario("when the target PAYE reference does not match that of a known unit") { wsClient =>
      Given(s"PAYE admin data does not exist with PAYE reference $TargetPayeRef")
      stubHBaseFor(aPayeRequest(withPayeRef = TargetPayeRef).willReturn(
        anOkResponse().withBody(NoMatchFoundResponse)
      ))

      When(s"the PAYE unit with PAYE reference $TargetPayeRef is requested")
      val response = await(wsClient.url(s"/v1/paye/${TargetPayeRef.value}").get())

      Then(s"a Not Found response is returned")
      response.status shouldBe NOT_FOUND
    }
  }

  feature("validate the requested PAYE reference") {
    scenario("when the target PAYE reference is invalid") { wsClient =>
      Given("that a valid PAYE reference comprises between 4 and 12 (inclusive) alphanumeric characters")

      When("the PAYE unit with a PAYE reference having 13 alphanumeric characters is requested")
      val response = await(wsClient.url(s"/v1/paye/1234567890ABC").get())

      Then("a Bad Request response is returned")
      response.status shouldBe BAD_REQUEST
    }
  }

  feature("handle failure gracefully") {
    scenario("when the database server is unavailable") { wsClient =>
      Given("the database server is unavailable")
      stopMockHBase()

      When(s"the PAYE unit with PAYE reference $TargetPayeRef is requested")
      val response = await(wsClient.url(s"/v1/paye/${TargetPayeRef.value}").get())

      Then(s"a server error is returned")
      response.status shouldBe aServerError
    }

    scenario("when the database server returns an error response") { wsClient =>
      Given("the database server will return an error response")
      stubHBaseFor(aPayeRequest(withPayeRef = TargetPayeRef).willReturn(
        aServiceUnavailableResponse()
      ))

      When(s"the PAYE unit with PAYE reference $TargetPayeRef is requested")
      val response = await(wsClient.url(s"/v1/paye/${TargetPayeRef.value}").get())

      Then(s"an Internal Server Error is returned")
      response.status shouldBe INTERNAL_SERVER_ERROR
    }
  }
}

object QueryPayeAcceptanceSpec extends HBaseJsonRequestBuilder with HBaseJsonBodyBuilder {
  private val ColumnFamily = "d"
  private val TargetPayeRef = PayeRef("065H7Z31732")
  private val TargetPayeHBaseResponseBody =
    aBodyWith(
      aRowWith(key = s"${TargetPayeRef.value}",
        // admin data
        aColumnWith(ColumnFamily, qualifier = "entref", value = "5235981614"),             // ignored field
        aColumnWith(ColumnFamily, qualifier = "payeref", value = TargetPayeRef.value),
        aColumnWith(ColumnFamily, qualifier = "deathcode", value = "658664"),
        aColumnWith(ColumnFamily, qualifier = "birthdate", value = "01/01/2016"),
        aColumnWith(ColumnFamily, qualifier = "deathdate", value = "05/05/2015"),
        aColumnWith(ColumnFamily, qualifier = "mfullemp", value = "9"),
        aColumnWith(ColumnFamily, qualifier = "msubemp", value = "5"),
        aColumnWith(ColumnFamily, qualifier = "ffullemp", value = "8"),
        aColumnWith(ColumnFamily, qualifier = "fsubemp", value = "2"),
        aColumnWith(ColumnFamily, qualifier = "unclemp", value = "3"),
        aColumnWith(ColumnFamily, qualifier = "unclsubemp", value = "5"),
        aColumnWith(ColumnFamily, qualifier = "dec_jobs", "6"),
        aColumnWith(ColumnFamily, qualifier = "mar_jobs", value = "1"),
        aColumnWith(ColumnFamily, qualifier = "june_jobs", value = "8"),
        aColumnWith(ColumnFamily, qualifier = "sept_jobs", value = "9"),
        aColumnWith(ColumnFamily, qualifier = "jobs_lastupd", value = "01/01/2018"),
        aColumnWith(ColumnFamily, qualifier = "legalstatus", value = "A"),
        aColumnWith(ColumnFamily, qualifier = "prevpaye", value = "2"),
        aColumnWith(ColumnFamily, qualifier = "employer_cat", value = "9"),
        aColumnWith(ColumnFamily, qualifier = "stc", value = "6616"),
        aColumnWith(ColumnFamily, qualifier = "crn", value = "1"),                         // ignored field
        aColumnWith(ColumnFamily, qualifier = "actiondate", value = "01/02/2018"),
        aColumnWith(ColumnFamily, qualifier = "addressref", value = "9607"),               // ignored field
        aColumnWith(ColumnFamily, qualifier = "marker", value = "1"),                      // ignored field
        aColumnWith(ColumnFamily, qualifier = "inqcode", value = "OR6PHFQ78Q"),            // ignored field
        aColumnWith(ColumnFamily, qualifier = "nameline1", value = "VDEPJ0IVE5"),
        aColumnWith(ColumnFamily, qualifier = "nameline2", value = "8JOS45YC8U"),
        aColumnWith(ColumnFamily, qualifier = "nameline3", value = "IEENIUFNHI"),
        aColumnWith(ColumnFamily, qualifier = "tradstyle1", value = "WD45"),
        aColumnWith(ColumnFamily, qualifier = "tradstyle2", value = "L3CS"),
        aColumnWith(ColumnFamily, qualifier = "tradstyle3", value = "U54L"),
        aColumnWith(ColumnFamily, qualifier = "address1", value = "VFHLNA0MSJ"),
        aColumnWith(ColumnFamily, qualifier = "address2", value = "P4FUV3QM7D"),
        aColumnWith(ColumnFamily, qualifier = "address3", value = "5TM1RA3CFR"),
        aColumnWith(ColumnFamily, qualifier = "address4", value = "00N7E1PVVM"),
        aColumnWith(ColumnFamily, qualifier = "address5", value = "HKJY8TOMJ8"),
        aColumnWith(ColumnFamily, qualifier = "postcode", value = "K6ZL 4GL"),
        // link data
        aColumnWith(ColumnFamily, qualifier = "ubrn", value = "1000012345000999")
      )
    )

  // the expected model representation of the above data cells
  private val TargetPaye = Paye(
    payeref = TargetPayeRef,
    name = "VDEPJ0IVE5" + "8JOS45YC8U" + "IEENIUFNHI",
    tradingStyle = Some("WD45" + "L3CS" + "U54L"),
    legalStatus = "A",
    stc = Some("6616"),
    previousPayeref = Some("2"),
    employerCategory = Some("9"),
    actionDate = Some("01/02/2018"),
    lifespan = Some(Lifespan(
      birthDate = "01/01/2016",
      deathDate = Some("05/05/2015"),
      deathCode = Some("658664")
    )),
    jobs = Some(Jobs(
      mar = Some(1),
      jun = Some(8),
      sep = Some(9),
      dec = Some(6),
      lastUpdated = Some("01/01/2018")
    )),
    employeeSplits = Some(EmployeeSplits(
      mfullemp = Some(9),
      msubemp = Some(5),
      ffullemp = Some(8),
      fsubemp = Some(2),
      unclemp = Some(3),
      unclsubemp = Some(5)
    )),
    address = Address(
      line1 = "VFHLNA0MSJ",
      line2 = Some("P4FUV3QM7D"),
      line3 = Some("5TM1RA3CFR"),
      line4 = Some("00N7E1PVVM"),
      line5 = Some("HKJY8TOMJ8"),
      postcode = "K6ZL 4GL"
    ),
    links = Some(LinkToLegalUnit("1000012345000999"))
  )

  // must match the configuration at src/it/resources/it_application.conf
  private def aPayeRequest(withPayeRef: PayeRef): MappingBuilder =
    getHBaseJson(namespace = "br_paye_db", tableName = "paye", rowKey = withPayeRef.value,
      auth = Authorization(username = "br_paye_usr", password = "br_paye_pwd"))

  private implicit val readsAddress: Reads[Address] = Json.reads[Address]
  private implicit val readsLifespan: Reads[Lifespan] = Json.reads[Lifespan]
  private implicit val readsLinkToLegalUnit: Reads[LinkToLegalUnit] = Json.reads[LinkToLegalUnit]
  private implicit val readsJobs: Reads[Jobs] = Json.reads[Jobs]
  private implicit val readsEmployeeSplits: Reads[EmployeeSplits] = Json.reads[EmployeeSplits]
  private implicit val readsPayeRef: Reads[PayeRef] = StringReads.map(PayeRef(_))
  private val readsPaye: Reads[Paye] = Json.reads[Paye]
}