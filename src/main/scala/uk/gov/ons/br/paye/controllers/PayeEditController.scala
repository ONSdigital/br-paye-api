package uk.gov.ons.br.paye.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import uk.gov.ons.br.actions.EditAction
import uk.gov.ons.br.http.PatchResultHandler
import uk.gov.ons.br.models.patch.Patch
import uk.gov.ons.br.paye.models.PayeRef
import uk.gov.ons.br.services.PatchService
import uk.gov.ons.br.services.PatchService.PatchDescriptor

import scala.concurrent.ExecutionContext

@Singleton
class PayeEditController @Inject() (protected val controllerComponents: ControllerComponents,
                                    editAction: EditAction,
                                    patchService: PatchService[PayeRef],
                                    toPatchResult: PatchResultHandler) extends BaseController {
  private implicit val executionContext: ExecutionContext = this.defaultExecutionContext

  def applyPatch(payeRef: PayeRef): Action[Patch] = editAction.async { request =>
    val patch = PatchDescriptor(editedBy = request.userId, operations = request.body)
    patchService.applyPatchTo(payeRef, patch).map(toPatchResult)
  }
}
