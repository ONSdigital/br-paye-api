package uk.gov.ons.br.paye

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.Status.{BAD_REQUEST, CONFLICT, INTERNAL_SERVER_ERROR, NO_CONTENT, NOT_FOUND, UNPROCESSABLE_ENTITY, UNSUPPORTED_MEDIA_TYPE}
import play.mvc.Http.MimeTypes.JSON
import uk.gov.ons.br.paye.EditPayeAcceptanceSpec.{aPayeQuery, anUpdatePayeRequest, EditPayeRef, HBaseCheckAndUpdateUbrnRequestBody, HBasePayeQueryResponseBody, IncorrectUBRN, InvalidJson, InvalidPatch, JsonPatchContentType, NoMatchFoundResponse, TargetUBRN}
import uk.gov.ons.br.paye.models.PayeRef
import uk.gov.ons.br.test.hbase.{AbstractServerAcceptanceSpec, HBaseJsonBodyBuilder, HBaseJsonRequestBuilder}
import uk.gov.ons.br.test.matchers.HttpServerErrorStatusCodeMatcher.aServerError

class EditPayeAcceptanceSpec extends AbstractServerAcceptanceSpec {

  // must match that configured in src/it/resources/it_application.conf
  override val HBasePort: Int = 8075

  info("As a data editor")
  info("I want to clerically update the linkage between PAYE and Legal Units")
  info("So that I can maintain the quality of the register")

  feature("maintain the Unique Business Reference Number (UBRN) of a PAYE admin unit") {
    scenario("when a successful update") { wsClient =>
      Given(s"PAYE admin data exists with PAYE reference ${EditPayeRef.value}")
      stubHBaseFor(aPayeQuery(withPayeRef = EditPayeRef).willReturn(
        anOkResponse().withBody(HBasePayeQueryResponseBody)))
      And(s"a Legal Unit exists that is identified by UBRN $TargetUBRN")
      And(s"a database request to update the UBRN from $IncorrectUBRN to $TargetUBRN will succeed")
      stubHBaseFor(anUpdatePayeRequest(withPayeRef = EditPayeRef).
        withRequestBody(equalToJson(HBaseCheckAndUpdateUbrnRequestBody)).
        willReturn(anOkResponse()))

      When(s"a clerical edit of the PAYE unit with reference ${EditPayeRef.value} requests the UBRN is updated from $IncorrectUBRN to $TargetUBRN")
      val response = await(wsClient.url(s"/v1/paye/${EditPayeRef.value}").
        withHttpHeaders(CONTENT_TYPE -> JsonPatchContentType).
        patch(s"""|[{"op": "test", "path": "/links/ubrn", "value": "$IncorrectUBRN"},
                  | {"op": "replace", "path": "/links/ubrn", "value": "$TargetUBRN"}]""".stripMargin))

      Then("a Success response is returned")
      response.status shouldBe NO_CONTENT
    }

    scenario("when another user has concurrently modified the PAYE admin unit") { wsClient =>
      Given(s"PAYE admin data exists with PAYE reference ${EditPayeRef.value}")
      stubHBaseFor(aPayeQuery(withPayeRef = EditPayeRef).willReturn(
        anOkResponse().withBody(HBasePayeQueryResponseBody)))
      And(s"a Legal Unit exists that is identified by UBRN $TargetUBRN")
      And(s"a database request to update the UBRN from $IncorrectUBRN to $TargetUBRN will not succeed because of another user's change")
      stubHBaseFor(anUpdatePayeRequest(withPayeRef = EditPayeRef).
        withRequestBody(equalToJson(HBaseCheckAndUpdateUbrnRequestBody)).
        willReturn(aNotModifiedResponse()))

      When(s"a clerical edit of the PAYE unit with reference ${EditPayeRef.value} requests the UBRN is updated from $IncorrectUBRN to $TargetUBRN")
      val response = await(wsClient.url(s"/v1/paye/${EditPayeRef.value}").
        withHttpHeaders(CONTENT_TYPE -> JsonPatchContentType).
        patch(s"""|[{"op": "test", "path": "/links/ubrn", "value": "$IncorrectUBRN"},
                  | {"op": "replace", "path": "/links/ubrn", "value": "$TargetUBRN"}]""".stripMargin))

      Then("a Conflict response is returned")
      response.status shouldBe CONFLICT
    }
  }

  feature("validate the target PAYE reference") {
    scenario("when the target PAYE reference does not adhere to the expected format") { wsClient =>
      Given("that a valid PAYE reference comprises between 4 and 12 (inclusive) alphanumeric characters")

      When("a clerical edit of a PAYE unit with a PAYE reference having 13 alphanumeric characters is requested")
      val response = await(wsClient.url(s"/v1/paye/1234567890ABC").
        withHttpHeaders(CONTENT_TYPE -> JsonPatchContentType).
        patch(s"""|[{"op": "test", "path": "/links/ubrn", "value": "$IncorrectUBRN"},
                  | {"op": "replace", "path": "/links/ubrn", "value": "$TargetUBRN"}]""".stripMargin))

      Then("a Bad Request response is returned")
      response.status shouldBe BAD_REQUEST
    }

    scenario("when the target PAYE admin unit does not exist in the register") { wsClient =>
      Given(s"PAYE admin data does not exist with PAYE reference ${EditPayeRef.value}")
      stubHBaseFor(aPayeQuery(withPayeRef = EditPayeRef).willReturn(
        anOkResponse().withBody(NoMatchFoundResponse)))

      When(s"a clerical edit of the PAYE unit with reference ${EditPayeRef.value} requests the UBRN is updated from $IncorrectUBRN to $TargetUBRN")
      val response = await(wsClient.url(s"/v1/paye/${EditPayeRef.value}").
        withHttpHeaders(CONTENT_TYPE -> JsonPatchContentType).
        patch(s"""|[{"op": "test", "path": "/links/ubrn", "value": "$IncorrectUBRN"},
                  | {"op": "replace", "path": "/links/ubrn", "value": "$TargetUBRN"}]""".stripMargin))

      Then("a Not Found response is returned")
      response.status shouldBe NOT_FOUND
    }
  }

  feature("validate the clerical edit specification (the request body)") {
    scenario("when the clerical edit specification does not have the Json Patch media type") { wsClient =>
      Given(s"that the media type of a Json Patch is $JsonPatchContentType")

      When(s"a clerical edit of a PAYE admin unit is requested with a content type of $JSON")
      val response = await(wsClient.url(s"/v1/paye/${EditPayeRef.value}").
        withHttpHeaders(CONTENT_TYPE -> JSON).
        patch(s"""|[{"op": "test", "path": "/links/ubrn", "value": "$IncorrectUBRN"},
                  | {"op": "replace", "path": "/links/ubrn", "value": "$TargetUBRN"}]""".stripMargin))

      Then("an Unsupported Media Type response is returned")
      response.status shouldBe UNSUPPORTED_MEDIA_TYPE
    }

    scenario("when the clerical edit specification is not a valid Json document") { wsClient =>
      When("a clerical edit of a PAYE admin unit is requested with a body that is not valid Json ")
      val response = await(wsClient.url(s"/v1/paye/${EditPayeRef.value}").
        withHttpHeaders(CONTENT_TYPE -> JsonPatchContentType).
        patch(InvalidJson))

      Then("a Bad Request response is returned")
      response.status shouldBe BAD_REQUEST
    }

    scenario("when the clerical edit specification does not comply with the Json Patch Specification") { wsClient =>
      Given("that the Json Patch Specification does not define an operation named 'update'")

      When("a clerical edit of a PAYE admin unit is requested with a Json body that requests an 'update' operation")
      val response = await(wsClient.url(s"/v1/paye/${EditPayeRef.value}").
        withHttpHeaders(CONTENT_TYPE -> JsonPatchContentType).
        patch(InvalidPatch))

      Then("a Bad Request response is returned")
      response.status shouldBe BAD_REQUEST
    }

    scenario("when the clerical edit specification complies with the Json Patch Specification but requests an unsupported edit") { wsClient =>
      When(s"a clerical edit of the PAYE unit with reference ${EditPayeRef.value} requests that the name is updated")
      val response = await(wsClient.url(s"/v1/paye/${EditPayeRef.value}").
        withHttpHeaders(CONTENT_TYPE -> JsonPatchContentType).
        patch(s"""|[{"op": "test", "path": "/name", "value": "Big Box Co"},
                  | {"op": "replace", "path": "/name", "value": "Big Box Company"}]""".stripMargin))

      Then("a Unprocessable Entity response is returned")
      response.status shouldBe UNPROCESSABLE_ENTITY
    }

    scenario("when the clerical edit specification requests the parent UBRN is changed to a value that does not comply with the UBRN format") { wsClient =>
      Given("that a valid UBRN consists of 16 digits")

      When(s"a clerical edit of the PAYE unit with reference ${EditPayeRef.value} requests the UBRN is updated to a value that is not 16 digits")
      val response = await(wsClient.url(s"/v1/paye/${EditPayeRef.value}").
        withHttpHeaders(CONTENT_TYPE -> JsonPatchContentType).
        patch(s"""|[{"op": "test", "path": "/links/ubrn", "value": "$IncorrectUBRN"},
                  | {"op": "replace", "path": "/links/ubrn", "value": "ABC123"}]""".stripMargin))

      Then("a Unprocessable Entity response is returned")
      response.status shouldBe UNPROCESSABLE_ENTITY
    }
  }

  feature("handle failure gracefully") {
    scenario("when the database server is unavailable") { wsClient =>
      Given("the database server is unavailable")
      stopMockHBase()

      When(s"a clerical edit of the PAYE unit with reference ${EditPayeRef.value} requests the UBRN is updated from $IncorrectUBRN to $TargetUBRN")
      val response = await(wsClient.url(s"/v1/paye/${EditPayeRef.value}").
        withHttpHeaders(CONTENT_TYPE -> JsonPatchContentType).
        patch(s"""|[{"op": "test", "path": "/links/ubrn", "value": "$IncorrectUBRN"},
                  | {"op": "replace", "path": "/links/ubrn", "value": "$TargetUBRN"}]""".stripMargin))

      Then("a server error is returned")
      response.status shouldBe aServerError
    }

    scenario("when the database server will return an error response to an edit request") { wsClient =>
      Given(s"PAYE admin data exists with PAYE reference ${EditPayeRef.value}")
      stubHBaseFor(aPayeQuery(withPayeRef = EditPayeRef).willReturn(
        anOkResponse().withBody(HBasePayeQueryResponseBody)))
      And(s"a Legal Unit exists that is identified by UBRN $TargetUBRN")
      And("the database server will return an error response when a PAYE edit is requested")
      stubHBaseFor(anUpdatePayeRequest(withPayeRef = EditPayeRef).
        withRequestBody(equalToJson(HBaseCheckAndUpdateUbrnRequestBody)).
        willReturn(aServiceUnavailableResponse()))

      When(s"a clerical edit of the PAYE unit with reference ${EditPayeRef.value} requests the UBRN is updated from $IncorrectUBRN to $TargetUBRN")
      val response = await(wsClient.url(s"/v1/paye/${EditPayeRef.value}").
        withHttpHeaders(CONTENT_TYPE -> JsonPatchContentType).
        patch(s"""|[{"op": "test", "path": "/links/ubrn", "value": "$IncorrectUBRN"},
                  | {"op": "replace", "path": "/links/ubrn", "value": "$TargetUBRN"}]""".stripMargin))

      Then("a an Internal Server Error is returned")
      response.status shouldBe INTERNAL_SERVER_ERROR
    }

    scenario("when the database server will return an error response to a read request") { wsClient =>
      Given("the database server will return an error response when PAYE data is requested")
      stubHBaseFor(aPayeQuery(withPayeRef = EditPayeRef).willReturn(aServiceUnavailableResponse()))
      And(s"a Legal Unit exists that is identified by UBRN $TargetUBRN")
      And(s"a database request to update the UBRN from $IncorrectUBRN to $TargetUBRN will succeed")
      stubHBaseFor(anUpdatePayeRequest(withPayeRef = EditPayeRef).
        withRequestBody(equalToJson(HBaseCheckAndUpdateUbrnRequestBody)).
        willReturn(anOkResponse()))

      When(s"a clerical edit of the PAYE unit with reference ${EditPayeRef.value} requests the UBRN is updated from $IncorrectUBRN to $TargetUBRN")
      val response = await(wsClient.url(s"/v1/paye/${EditPayeRef.value}").
        withHttpHeaders(CONTENT_TYPE -> JsonPatchContentType).
        patch(s"""|[{"op": "test", "path": "/links/ubrn", "value": "$IncorrectUBRN"},
                  | {"op": "replace", "path": "/links/ubrn", "value": "$TargetUBRN"}]""".stripMargin))

      Then("a an Internal Server Error is returned")
      response.status shouldBe INTERNAL_SERVER_ERROR
    }
  }
}

private object EditPayeAcceptanceSpec extends HBaseJsonRequestBuilder with HBaseJsonBodyBuilder {
  private val EditPayeRef = PayeRef("125H7A71620")
  private val IncorrectUBRN = "1000012345000000"
  private val TargetUBRN = "1000012345000999"
  private val JsonPatchContentType = "application/json-patch+json"
  private val InvalidJson = "[}"
  private val InvalidPatch =
    s"""|[{"op": "test", "path": "/links/ubrn", "value": "$IncorrectUBRN"}
        | {"op": "update", "path": "/links/ubrn", "value": "$TargetUBRN"}]""".stripMargin  // no such 'op'
  private val ColumnFamily = "d"
  private val ParentLinkQualifier = "ubrn"

  private val HBaseCheckAndUpdateUbrnRequestBody =
    aBodyWith(
      aRowWith(key = EditPayeRef.value,
        aColumnWith(ColumnFamily, qualifier = ParentLinkQualifier, value = TargetUBRN, timestamp = None),
        aColumnWith(ColumnFamily, qualifier = ParentLinkQualifier, value = IncorrectUBRN, timestamp = None)
      )
    )

  private val HBasePayeQueryResponseBody =
    aBodyWith(
      aRowWith(key = EditPayeRef.value,
        // admin data
        aColumnWith(ColumnFamily, qualifier = "payeref", value = EditPayeRef.value),
        aColumnWith(ColumnFamily, qualifier = "legalstatus", value = "A"),
        aColumnWith(ColumnFamily, qualifier = "nameline1", value = "VDEPJ0IVE5"),
        aColumnWith(ColumnFamily, qualifier = "address1", value = "VFHLNA0MSJ"),
        aColumnWith(ColumnFamily, qualifier = "postcode", value = "K6ZL 4GL"),
        // link data
        aColumnWith(ColumnFamily, qualifier = "ubrn", value = IncorrectUBRN)
      )
    )

  // must match the configuration at src/it/resources/it_application.conf
  private val Namespace = "br_paye_db"
  private val TableName = "paye"
  private val auth = Authorization(username = "br_paye_usr", password = "br_paye_pwd")

  private def anUpdatePayeRequest(withPayeRef: PayeRef): MappingBuilder =
    checkedPutHBaseJson(Namespace, TableName, rowKey = withPayeRef.value, auth)

  private def aPayeQuery(withPayeRef: PayeRef): MappingBuilder =
    getHBaseJson(Namespace, TableName, rowKey = withPayeRef.value, auth)
}