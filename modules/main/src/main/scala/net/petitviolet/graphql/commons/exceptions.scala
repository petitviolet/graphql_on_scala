package net.petitviolet.graphql.commons

object exceptions {
  case class NotFoundException(msg: String) extends RuntimeException(msg)
  case class AuthenticationError(msg: String) extends RuntimeException(msg)
  case class ConditionException(msg: String) extends RuntimeException(msg)
}
