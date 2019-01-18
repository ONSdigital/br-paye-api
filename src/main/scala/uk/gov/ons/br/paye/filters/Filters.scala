package uk.gov.ons.br.paye.filters

import javax.inject.Inject
import play.api.http.{DefaultHttpFilters, EnabledFilters}
import play.filters.gzip.GzipFilter
import uk.gov.ons.br.filters.AccessLoggingFilter

/*
 * See https://www.playframework.com/documentation/2.6.x/ScalaHttpFilters#using-filters
 *
 * This class must be listed in application.conf under 'play.http.filters' for this to take effect.
 */
class Filters @Inject() (playFilters: EnabledFilters,
                         gzipFilter: GzipFilter,
                         accessLoggingFilter: AccessLoggingFilter) extends
  DefaultHttpFilters(playFilters.filters :+ gzipFilter :+ accessLoggingFilter: _*)
