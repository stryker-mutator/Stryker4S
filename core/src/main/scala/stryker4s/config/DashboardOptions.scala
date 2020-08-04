package stryker4s.config

import sttp.model.Uri
import sttp.client.UriContext

final case class DashboardOptions(
    baseUrl: Uri = uri"https://dashboard.stryker-mutator.io",
    reportType: DashboardReportType = Full,
    project: Option[String] = None,
    version: Option[String] = None,
    module: Option[String] = None
)

sealed trait DashboardReportType
case object Full extends DashboardReportType
case object MutationScoreOnly extends DashboardReportType
