package uk.gov.ons.br.paye.modules

import com.github.ghik.silencer.silent
import com.google.inject.{AbstractModule, Provides, TypeLiteral}
import javax.inject.Inject
import org.slf4j.LoggerFactory
import play.api.libs.json.Reads
import play.api.{Configuration, Environment}
import uk.gov.ons.br.config.HBaseRestRepositoryConfigLoader
import uk.gov.ons.br.models.Ubrn
import uk.gov.ons.br.paye.models.{Paye, PayeRef}
import uk.gov.ons.br.paye.repository.hbase.{ParentLinkColumn, PayeHBaseRowKey, PayeHBaseRowMapper}
import uk.gov.ons.br.repository.{CommandRepository, QueryRepository}
import uk.gov.ons.br.repository.hbase.rest.{HBaseRestData, HBaseRestRepository, HBaseRestRepositoryConfig}
import uk.gov.ons.br.repository.hbase._

import scala.concurrent.ExecutionContext

/*
 * Configure HBase.
 * HBase is currently the repository used by both our read & edit functionality.  In the future reads will be
 * switched to target Solr.
 *
 * This class must be listed in application.conf under 'play.modules.enabled' for this to be used.
 *
 * @silent unused - in order to have Configuration injected into this constructor, we must also accept
 *                  Environment.  Attempts to inject Configuration alone are met with: play.api.PlayException: No
 *                  valid constructors[Module [uk.gov.ons.br.paye.modules.PayeHBaseModule] cannot be instantiated.]
 */
@SuppressWarnings(Array("UnusedMethodParameter"))
class PayeHBaseModule(@silent environment: Environment, configuration: Configuration) extends AbstractModule {

  // We use a dedicated logger for HBase tracing (which should be configured in logback.xml)
  private lazy val hBaseLogger = LoggerFactory.getLogger("hbase")

  override def configure(): Unit = {
    // eagerly evaluate config so that we fail fast if misconfigured
    val underlyingConfig = configuration.underlying
    val hBaseRestRepositoryConfig = HBaseRestRepositoryConfigLoader.load(rootConfig = underlyingConfig, path = "query.db.hbase")
    bind(classOf[HBaseRestRepositoryConfig]).toInstance(hBaseRestRepositoryConfig)

    bind(new TypeLiteral[Reads[Seq[HBaseRow]]]() {}).toInstance(HBaseRestData.format)
    bind(classOf[HBaseRepository]).to(classOf[HBaseRestRepository])
    ()  // explicitly return unit to avoid warning about disregarded return value
  }

  @Provides
  def providesPayeQueryRepository(@Inject() hBase: HBaseRepository)
                                 (implicit ec: ExecutionContext): QueryRepository[PayeRef, Paye] =
    // Note static binding to PAYE specific RowMapper & RowKey here
    new HBaseQueryRepository[PayeRef, Paye](hBase, PayeHBaseRowMapper, PayeHBaseRowKey)(ec, hBaseLogger)

  @Provides
  def providesCommandRepository(@Inject() hBase: HBaseRepository): CommandRepository[PayeRef, Ubrn] =
    // Note static binding to ubrn.toString & ubrn column name here
    new HBaseCommandRepository[PayeRef, Ubrn](hBase, PayeHBaseRowKey, ubrn => ubrn.value, ParentLinkColumn)(hBaseLogger)
}
