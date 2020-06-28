package com.facecool.ui.reports.byclass

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.facecool.R
import com.facecool.databinding.FragmentReportByClassBinding
import com.facecool.ui.BaseFragment
import com.facecool.ui.reports.byclass.report.ClassReportActivity
import com.facecool.ui.reports.byclass.selection.ClassSelectionFragment
import com.facecool.ui.reports.byclass.selection.ClassSelectionModel

class ReportsByClassFragment :
    BaseFragment<FragmentReportByClassBinding>(FragmentReportByClassBinding::inflate),
    ClassReportNavigator {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        replaceFragment(ClassSelectionFragment.newInstance())
    }

    override fun getTitle(): String = getTitle(requireContext())

    override fun getTitle(context: Context) = context.getString(R.string.class_report_tab_title)

    override fun onClassSelected(classSelectionModel: ClassSelectionModel) {
        ClassReportActivity.start(requireActivity(), classSelectionModel)
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