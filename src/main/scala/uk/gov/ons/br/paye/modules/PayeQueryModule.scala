package uk.gov.ons.br.paye.modules

import akka.stream.Materializer
import com.github.ghik.silencer.silent
import com.google.inject.{AbstractModule, Provides, TypeLiteral}
import javax.inject.Inject
import kamon.Kamon
import org.slf4j.LoggerFactory
import play.api.libs.json.Reads
import play.api.mvc.PlayBodyParsers
import play.api.{Configuration, Environment}
import uk.gov.ons.br.actions.{DefaultQueryActionMaker, QueryActionMaker}
import uk.gov.ons.br.config.HBaseRestRepositoryConfigLoader
import uk.gov.ons.br.filters.AccessLoggingFilter
import uk.gov.ons.br.http.{JsonQueryResultHandler, QueryResultHandler}
import uk.gov.ons.br.paye.models.{Paye, PayeRef}
import uk.gov.ons.br.paye.repository.hbase.{PayeHBaseRowKey, PayeHBaseRowMapper}
import uk.gov.ons.br.repository.QueryRepository
import uk.gov.ons.br.repository.hbase.rest.{HBaseRestData, HBaseRestRepository, HBaseRestRepositoryConfig}
import uk.gov.ons.br.repository.hbase.{HBaseQueryRepository, HBaseRepository, HBaseRow}

import scala.concurrent.ExecutionContext

/*
 * DI configuration.
 * This class must be listed in application.conf under 'play.modules.enabled' for this to be used.
 *
 * @silent unused - in order to have Configuration injected into this constructor, we must also accept
 *                  Environment.  Attempts to inject Configuration alone are met with: play.api.PlayException: No
 *                  valid constructors[Module [uk.gov.ons.br.paye.modules.QueryModule] cannot be instantiated.]
 */
@SuppressWarnings(Array("UnusedMethodParameter"))
class PayeQueryModule(@silent environment: Environment, configuration: Configuration) extends AbstractModule {

  override def configure(): Unit = {
    // eagerly evaluate config so that we fail fast if misconfigured
    val underlyingConfig = configuration.underlying
    val hBaseRestRepositoryConfig = HBaseRestRepositoryConfigLoader.load(rootConfig = underlyingConfig, path = "query.db.hbase")
    bind(classOf[HBaseRestRepositoryConfig]).toInstance(hBaseRestRepositoryConfig)

    bind(new TypeLiteral[Reads[Seq[HBaseRow]]]() {}).toInstance(HBaseRestData.format)
    bind(classOf[HBaseRepository]).to(classOf[HBaseRestRepository])

    // configure monitoring
    Kamon.loadReportersFromConfig()
  }

  @Provides
  def providesPayeQueryRepository(@Inject() hBase: HBaseRepository)
                                 (implicit ec: ExecutionContext): QueryRepository[PayeRef, Paye] = {
    // We use a dedicated logger for HBase query tracing (which should be configured in logback.xml)
    implicit val logger = LoggerFactory.getLogger("hbase")
    // Note static binding to PAYE specific RowMapper & RowKey here
    new HBaseQueryRepository[PayeRef, Paye](hBase, PayeHBaseRowMapper, PayeHBaseRowKey)
  }

  @Provides
  def providesPayeQueryActionMaker(@Inject() bodyParsers: PlayBodyParsers, queryRepository: QueryRepository[PayeRef, Paye])
                                  (implicit ec: ExecutionContext): QueryActionMaker[PayeRef, Paye] =
    new DefaultQueryActionMaker[PayeRef, Paye](bodyParsers.default, queryRepository)

  @Provides
  def providesPayeQueryResultHandler: QueryResultHandler[Paye] =
    new JsonQueryResultHandler[Paye]()

  /*
   * We use a provider here so that we can control the dedicated logger that will receive the access log.
   * This should be configured appropriately in logback.xml.
   */
  @Provides
  def providesAccessLoggingFilter(implicit mat: Materializer, ec: ExecutionContext): AccessLoggingFilter =
    new AccessLoggingFilter(LoggerFactory.getLogger("access"))
}
