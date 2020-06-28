package com.facecool.ui.classes.add

interface AddClassNavigator {
    fun onGeneralInfoAdded()
    fun onStudentsAdded()
    fun onOccurrenceAdded(classId: Long)
    fun updateTitle(title: String)
}