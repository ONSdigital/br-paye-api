package uk.gov.ons.br.paye.modules

import com.google.inject.{AbstractModule, Provides}
import javax.inject.Inject
import play.api.mvc.PlayBodyParsers
import uk.gov.ons.br.actions.{DefaultQueryActionMaker, QueryActionMaker}
import uk.gov.ons.br.http.{JsonQueryResultHandler, QueryResultHandler}
import uk.gov.ons.br.paye.models.{Paye, PayeRef}
import uk.gov.ons.br.repository.QueryRepository

import scala.concurrent.ExecutionContext

/*
 * This class must be listed in application.conf under 'play.modules.enabled' for this to be used.
 */
class PayeQueryModule extends AbstractModule {

  override def configure(): Unit = ()

  @Provides
  def providesPayeQueryActionMaker(@Inject() bodyParsers: PlayBodyParsers,
                                   queryRepository: QueryRepository[PayeRef, Paye])
                                  (implicit ec: ExecutionContext): QueryActionMaker[PayeRef, Paye] =
    new DefaultQueryActionMaker[PayeRef, Paye](bodyParsers.default, queryRepository)

  @Provides
  def providesPayeQueryResultHandler: QueryResultHandler[Paye] =
    new JsonQueryResultHandler[Paye]()
}
