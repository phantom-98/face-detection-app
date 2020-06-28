package com.facecool.ui.students.details

data class StudentDetailItemModel(
    val icon: Int,
    val value: String,
    val action: ItemAction,
    val label: String
)

enum class ItemAction{
    STUDENT_NAME,
    STUDENT_LAST_NAME,
    STUDENT_EMAIL,
    STUDENT_ID,
    STUDENT_ADDRESS,
    STUDENT_PHONE_NUMBER,
    STUDENT_PARENT_NAMES,
    STUDENT_PARENT_SMS,
    STUDENT_PARENT_EMAIL,
    STUDENT_PARENT_WHATSAPP
}