package net.petitviolet.graphql.schemas.types

import net.petitviolet.graphql.models._
import net.petitviolet.graphql.schemas.Ctx
import sangria.macros.derive
import sangria.schema.{ EnumType, ObjectType }

object ObjectTypes {
  implicit lazy val userType: ObjectType[Ctx, User] = derive.deriveObjectType[Ctx, User]()
  implicit lazy val userIdType: ObjectType[Ctx, UserId] = derive.deriveObjectType[Ctx, UserId]()
  implicit lazy val userNameType: ObjectType[Ctx, UserName] = derive.deriveObjectType[Ctx, UserName]()
  implicit lazy val projectType: ObjectType[Ctx, Project] = derive.deriveObjectType[Ctx, Project]()
  implicit lazy val projectIdType: ObjectType[Ctx, ProjectId] = derive.deriveObjectType[Ctx, ProjectId]()
  implicit lazy val projectNameType: ObjectType[Ctx, ProjectName] = derive.deriveObjectType[Ctx, ProjectName]()
  implicit lazy val taskType: ObjectType[Ctx, Task] = derive.deriveObjectType[Ctx, Task]()
  implicit lazy val taskIdType: ObjectType[Ctx, TaskId] = derive.deriveObjectType[Ctx, TaskId]()
  implicit lazy val taskNameType: ObjectType[Ctx, TaskName] = derive.deriveObjectType[Ctx, TaskName]()
  implicit lazy val taskDescriptionType: ObjectType[Ctx, TaskDescription] = derive.deriveObjectType[Ctx, TaskDescription]()
  implicit lazy val taskStatusType: EnumType[TaskStatus] = derive.deriveEnumType[TaskStatus]()
}
