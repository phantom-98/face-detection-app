package com.facecool.ui.reports.bystudent

import com.facecool.ui.reports.bystudent.selection.StudentSelectionModel

interface StudentReportNavigator {

    fun onStudentSelected(studentModel: StudentSelectionModel)

    fun openStudentSelection()

}