package com.facecool.ui.classes.add.time

import java.io.Serializable

data class LessonModel(
    var lessonName: String,
    var lessonClassId: Long = -1,
    var startLesson: Long? = null,
    var endLesson: Long? = null,
    var status: String = "",
    var lessonDdId: Long? = null,
    var autoKiosk: Boolean = false,
    var liveness: Boolean = false,
    var date: Long? = null
) : Serializable
