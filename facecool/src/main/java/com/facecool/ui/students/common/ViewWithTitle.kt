package com.facecool.ui.students.common

import android.content.Context

interface ViewWithTitle {

    fun getTitle(): String

    fun isCameraScreen(): Boolean

    fun getTitle(context: Context): String = ""
}