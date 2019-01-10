package uk.gov.ons.br.paye.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import uk.gov.ons.br.http.PatchResultHandler
import uk.gov.ons.br.models.patch.Patch
import uk.gov.ons.br.paye.models.PayeRef
import uk.gov.ons.br.services.PatchService

import scala.concurrent.ExecutionContext

@Singleton
class PayeEditController @Inject() (protected val controllerComponents: ControllerComponents,
                                    jsonPatchBodyParser: BodyParser[Patch],
                                    patchService: PatchService[PayeRef],
                                    patchHandler: PatchResultHandler) extends BaseController {
  private implicit val executionContext: ExecutionContext = this.defaultExecutionContext

  def applyPatch(payeRef: PayeRef): Action[Patch] = Action.async(jsonPatchBodyParser) { request =>
    patchService.applyPatchTo(payeRef, request.body).map(patchHandler)
  }
}
