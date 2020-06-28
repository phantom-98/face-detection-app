package com.face.cool.manualsync

import android.graphics.Bitmap
import androidx.annotation.Keep
import com.face.cool.databasa.classes.ClassEntity
import com.face.cool.databasa.detection_events.EventEntity
import com.face.cool.databasa.lessons.LessonEntity
import com.face.cool.databasa.users.UserEntity

@Keep
data class ExpertDataWrapper(
    val userList: List<UserDataSync?> = listOf(),
    val classList: List<ClassEntity?> = listOf(),
    val lessonList: List<LessonEntity?> = listOf(),
    val eventList: List<EventDataSync?> = listOf()
)

@Keep
data class UserDataSync(
    val user: UserEntity?,
    val image: String? = null,
    var bitmap: Bitmap? = null,
)

@Keep
data class EventDataSync(
    val event: EventEntity?,
    val image: String? = null,
    var bitmap: Bitmap? = null,
)
