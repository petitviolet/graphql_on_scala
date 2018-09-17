package net.petitviolet.graphql.commons

import org.slf4j.{ Logger, LoggerFactory }

trait WithLogger {
  lazy val logger: Logger = LoggerFactory.getLogger(getClass)
}
