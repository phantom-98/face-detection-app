package com.facecool.ui.reports.byclass

import com.facecool.ui.reports.byclass.selection.ClassSelectionModel

interface ClassReportNavigator {

    fun onClassSelected(classSelectionModel: ClassSelectionModel)

    fun openClassSelection()

}