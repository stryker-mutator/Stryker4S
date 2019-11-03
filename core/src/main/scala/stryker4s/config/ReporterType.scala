package stryker4s.config

sealed trait ReporterType

case object Console extends ReporterType
case object Html extends ReporterType
case object Json extends ReporterType
case object Dashboard extends ReporterType
