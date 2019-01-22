package uk.gov.ons.br.paye.test.matchers

import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.matching.{MatchResult, RequestMatcherExtension}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json._
import play.mvc.Http.HeaderNames.CONTENT_TYPE
import uk.gov.ons.br.paye.test.matchers.HBaseRequestWithFuzzyEditHistoryBodyMatcher.{FuzzyValue, ItemDelimiter}
import uk.gov.ons.br.repository.hbase.rest.HBaseRestData
import uk.gov.ons.br.repository.hbase.{HBaseCell, HBaseRow}

import scala.util.Try

/*
 * This belongs in a common library so that it can be shared across microservices.
 * However it cannot go in br-api-test-common as it relies on HBaseRestData from br-api-common (and our
 * dependency chain currently runs in the opposite direction).  Exposing it from br-api-common would
 * however require making WireMock a main dependency rather than just a test dependency, which would
 * transitively affect all clients.
 * For now, we'll have to rely on copy & paste reuse ...
 */
class HBaseRequestWithFuzzyEditHistoryBodyMatcher(expectedJson: String) extends RequestMatcherExtension {
  private lazy val logger: Logger = LoggerFactory.getLogger(this.getClass)
  private implicit val readsBody: Reads[Seq[HBaseRow]] = HBaseRestData.format
  private val expectedBody = Json.parse(expectedJson).as[Seq[HBaseRow]]

  override def `match`(request: Request, parameters: Parameters): MatchResult =
    getContentType(request).fold(ifEmpty = MatchResult.of(false)) { _ =>
      getBody(request).fold(
        _ => MatchResult.of(false),
        rows => MatchResult.of(isFuzzyBodyMatch(expectedBody.toList, rows.toList))
      )
    }

  private def isFuzzyBodyMatch(expectedBody: List[HBaseRow], actualBody: List[HBaseRow]): Boolean = {
    logger.debug("isFuzzyBodyMatch expected=[{}] actual=[{}]", expectedBody: Any, actualBody: Any)
    (expectedBody, actualBody) match {
      case (Nil, Nil) => true
      case (eh :: et, ah :: at) => isFuzzyRowMatch(eh, ah) && isFuzzyBodyMatch(et, at)
      case _ => false
    }
  }

  private def isFuzzyRowMatch(expectedRow: HBaseRow, actualRow: HBaseRow): Boolean =
    expectedRow.key == actualRow.key && isFuzzyCellsMatch(expectedRow.cells.toList, actualRow.cells.toList)

  /*
   * Order is important for request bodies - as this dictates which is the 'check cell' for concurrency control.
   * If the head cells have the edit history column family apply a fuzzy match, because the column qualifier is
   * a dynamically generated UUID and the cell value contains a dynamically allocated timestamp.  Otherwise, do
   * an equality comparison.
   */
  private def isFuzzyCellsMatch(expectedCells: List[HBaseCell], actualCells: List[HBaseCell]): Boolean =
    (expectedCells, actualCells) match {
      case (Nil, Nil) => true
      case (HBaseCell(ec, ev) :: et, HBaseCell(ac, av) :: at) if ec.startsWith("h:") && ac.startsWith("h:") =>
        isFuzzyValueMatch(ev, av) && isFuzzyCellsMatch(et, at)
      case (eCell :: et, aCell :: at) =>
        eCell == aCell && isFuzzyCellsMatch(et, at)
      case _ => false
    }

  private def isFuzzyValueMatch(expectedValue: String, actualValue: String): Boolean = {
    val expectedItems = valueToItems(expectedValue)
    val actualItems = valueToItems(actualValue)
    isFuzzyItemsMatch(expectedItems, actualItems)
  }

  private def valueToItems(value: String): List[String] =
    value.split(ItemDelimiter, -1).toList

  private def isFuzzyItemsMatch(expectedItems: List[String], actualItems: List[String]): Boolean =
    (expectedItems, actualItems) match {
      case (Nil, Nil) => true
      case (eh :: et, _ :: at) if eh == FuzzyValue => isFuzzyItemsMatch(et, at)
      case (eh :: et, ah :: at) => eh == ah && isFuzzyItemsMatch(et, at)
      case _ => false
    }

  private def getContentType(request: Request): Option[String] =
    if (request.containsHeader(CONTENT_TYPE)) Some(request.getHeader(CONTENT_TYPE)) else None

  private def getBody(request: Request): Either[String, Seq[HBaseRow]] =
    Try(Json.parse(request.getBody)).toEither.left.map { cause =>
      s"Failed during Json parsing [${cause.getClass.getSimpleName}] - [${cause.getMessage}]"
    }.flatMap { json =>
      readsBody.reads(json).asEither.left.map { errors =>
        s"Failed during conversion to HBaseRestData [$errors]"
      }
    }
}

object HBaseRequestWithFuzzyEditHistoryBodyMatcher {
  val FuzzyValue = "?FUZZY-VALUE?"
  private val ItemDelimiter = "~"

  def aRequestBodyWithFuzzyEditHistoryLike(expectedJson: String): RequestMatcherExtension =
    new HBaseRequestWithFuzzyEditHistoryBodyMatcher(expectedJson)
}
