package com.facecool.ui.reports.yxl

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.facecool.R
import com.facecool.databinding.FragmentReportByClassBinding
import com.facecool.ui.BaseFragment
import com.facecool.ui.reports.yxl.report.YXLReportActivity
import com.facecool.ui.reports.yxl.selection.ClassSelectionFragment
import com.facecool.ui.reports.yxl.selection.ClassSelectionModel

class ReportsYXLFragment :
    BaseFragment<FragmentReportByClassBinding>(FragmentReportByClassBinding::inflate),
    YXLReportNavigator {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        replaceFragment(ClassSelectionFragment.newInstance())
    }

    override fun getTitle(): String = getTitle(requireContext())

    override fun getTitle(context: Context) = context.getString(R.string.class_report_tab_title_yxl)

    override fun onClassSelected(classSelectionModel: ClassSelectionModel) {
        YXLReportActivity.start(requireActivity(), classSelectionModel)
    }

    override fun openClassSelection() {
        replaceFragment(ClassSelectionFragment.newInstance())
    }

    private fun replaceFragment(fragment: Fragment, containerId: Int = R.id.container) {
        childFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .commit()
    }
}