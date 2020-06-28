package com.facecool.ui.reports.yxl

import com.facecool.ui.reports.yxl.selection.ClassSelectionModel

interface YXLReportNavigator {

    fun onClassSelected(classSelectionModel: ClassSelectionModel)

    fun openClassSelection()

}