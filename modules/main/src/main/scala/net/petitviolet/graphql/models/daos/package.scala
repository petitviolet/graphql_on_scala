package net.petitviolet.graphql.models

package object daos {
  def init(): Unit = {
    UserDao.init()
    ProjectDao.init()
    TaskDao.init()
  }

}
