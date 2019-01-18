package uk.gov.ons.br.paye.modules

import akka.stream.Materializer
import com.google.inject.{AbstractModule, Provides}
import kamon.Kamon
import org.slf4j.LoggerFactory
import uk.gov.ons.br.filters.AccessLoggingFilter

import scala.concurrent.ExecutionContext

/*
 * Configures monitoring / logging behaviour.
 * This class must be listed in application.conf under 'play.modules.enabled' for this to be used.
 */
class PayeMonitoringModule extends AbstractModule {

  override def configure(): Unit =
    Kamon.loadReportersFromConfig()

  /*
   * We use a provider here so that we can control the dedicated logger that will receive the access log.
   * This should be configured appropriately in logback.xml.
   */
  @Provides
  def providesAccessLoggingFilter(implicit mat: Materializer, ec: ExecutionContext): AccessLoggingFilter =
    new AccessLoggingFilter(LoggerFactory.getLogger("access"))
}
