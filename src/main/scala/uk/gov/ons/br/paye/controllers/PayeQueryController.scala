package uk.gov.ons.br.paye.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import uk.gov.ons.br.actions.QueryActionMaker
import uk.gov.ons.br.http.QueryResultHandler
import uk.gov.ons.br.paye.models.{Paye, PayeRef}

/*
 * In order for the router to invoke methods with arguments that are instances of some model type T, a
 * suitable PathBindable[T] must be defined.
 * We leave validation of path parameters to the router (via regex constraints).
 */
@Singleton
class PayeQueryController @Inject()(protected val controllerComponents: ControllerComponents,
                                    queryAction: QueryActionMaker[PayeRef, Paye],
                                    responseFor: QueryResultHandler[Paye]) extends BaseController {
  def byReference(payeRef: PayeRef): Action[AnyContent] = queryAction.byUnitReference(payeRef) { request =>
    responseFor(request.queryResult)
  }
}
