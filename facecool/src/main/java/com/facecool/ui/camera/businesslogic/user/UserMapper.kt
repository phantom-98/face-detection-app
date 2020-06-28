package com.facecool.ui.camera.businesslogic.user

import com.face.cool.databasa.detection_events.EventEntity
import com.face.cool.databasa.users.UserEntity
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.camera.businesslogic.events.EventModel
import com.facecool.ui.students.select.StudentSelectionModel

interface UserMapper {

    fun entityToModel(entity: UserEntity): CameraDetectionModel

    fun entityToSelectionModel(entity: UserEntity): StudentSelectionModel

    fun modelToEntity(model: CameraDetectionModel): UserEntity

    fun eventListToUsers(events: List<EventEntity>, usersData: List<UserEntity>): List<Pair<CameraDetectionModel, EventModel?>>

}
