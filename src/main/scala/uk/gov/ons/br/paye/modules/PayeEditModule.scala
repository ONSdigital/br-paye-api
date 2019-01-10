package uk.gov.ons.br.paye.modules

import com.google.inject.{AbstractModule, Provides}
import javax.inject.{Inject, Singleton}
import play.api.mvc.{BodyParser, PlayBodyParsers}
import uk.gov.ons.br.http.{DefaultPatchResultHandler, PatchResultHandler}
import uk.gov.ons.br.models.Ubrn
import uk.gov.ons.br.models.patch.Patch
import uk.gov.ons.br.parsers.JsonPatchBodyParser
import uk.gov.ons.br.paye.models.PayeRef
import uk.gov.ons.br.repository.CommandRepository
import uk.gov.ons.br.services.UnitRegistryService.{UnitFound, UnitRegistryResult}
import uk.gov.ons.br.services.admindata.AdminDataPatchService
import uk.gov.ons.br.services.{PatchService, UnitRegistryService}

import scala.concurrent.{ExecutionContext, Future}

/*
 * This class must be listed in application.conf under 'play.modules.enabled' for this to be used.
 */
class PayeEditModule extends AbstractModule {

  override def configure(): Unit = ()

  /*
   * The expectation is that this functionality will eventually be provided by a client of the Legal Unit
   * microservice.  Currently there is no such service - so we simply accept any reference that satisfies
   * the format of a UBRN ...
   */
  @Provides
  def providesUnitRegistryService: UnitRegistryService[Ubrn] =
    new UnitRegistryService[Ubrn] {
      override def isRegistered(unitRef: Ubrn): Future[UnitRegistryResult] =
        Future.successful(UnitFound)
    }

  @Provides
  def providesPatchService(@Inject() commandRepository: CommandRepository[PayeRef, Ubrn],
                           unitRegistryService: UnitRegistryService[Ubrn])
                          (implicit ec: ExecutionContext): PatchService[PayeRef] =
    new AdminDataPatchService[PayeRef](commandRepository, unitRegistryService)

  @Provides
  @Singleton
  def providesJsonPatchBodyParser(@Inject() bodyParsers: PlayBodyParsers, ec: ExecutionContext): BodyParser[Patch] =
    new JsonPatchBodyParser(bodyParsers.tolerantJson)(ec)

  @Provides
  @Singleton
  def providesPatchResultHandler: PatchResultHandler =
    DefaultPatchResultHandler
}
