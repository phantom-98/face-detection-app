package com.facecool.ui.reports.bystudent

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.facecool.R
import com.facecool.databinding.FragmentReportByStudentBinding
import com.facecool.ui.BaseFragment
import com.facecool.ui.reports.bystudent.reports.StudentReportActivity
import com.facecool.ui.reports.bystudent.reports.StudentReportFragment
import com.facecool.ui.reports.bystudent.selection.StudentSelectionFragment
import com.facecool.ui.reports.bystudent.selection.StudentSelectionModel

class ReportsByStudentFragment: BaseFragment<FragmentReportByStudentBinding>(FragmentReportByStudentBinding::inflate),
    StudentReportNavigator{

    override fun getTitle(): String = getTitle(requireActivity())

    override fun getTitle(context: Context) = context.getString(R.string.class_report_tab_title_student)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        replaceFragment(StudentSelectionFragment.newInstance())
    }

    override fun onStudentSelected(studentModel: StudentSelectionModel) {
        StudentReportActivity.start(requireActivity(), studentModel)
//        replaceFragment(StudentReportFragment.newInstance(studentModel))
    }

    override fun openStudentSelection() {
        replaceFragment(StudentSelectionFragment.newInstance())
    }

    private fun replaceFragment(fragment: Fragment, containerId: Int = R.id.container) {
        childFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .commit()
    }
}