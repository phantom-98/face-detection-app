package com.facecool.ui.reports.byclass.selection

import java.io.Serializable

data class ClassSelectionModel(
    val className: String,
    val classID: Long?
) : Serializable
