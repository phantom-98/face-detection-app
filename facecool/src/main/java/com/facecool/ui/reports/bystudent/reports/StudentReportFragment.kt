package com.facecool.ui.reports.bystudent.reports

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.facecool.R
import com.facecool.databinding.FragmentReportListStudentBinding
import com.facecool.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StudentReportFragment :
    BaseFragment<FragmentReportListStudentBinding>(FragmentReportListStudentBinding::inflate),
    StudentReportAdapter.Listener {

    companion object {
        const val STUDENT = "student"
        fun newInstance(studentId: Long): StudentReportFragment {
            return StudentReportFragment().apply {
                arguments = Bundle().apply { putLong(STUDENT, studentId) }
            }
        }
    }

    override fun getTitle(): String = getTitle(requireActivity())

    override fun getTitle(context: Context) = context.getString(R.string.attendance)

    var studentId: Long = -1
    private val viewModel: StudentReportViewModel by viewModels()
    private val adapter = StudentReportAdapter(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarContainer.root.visibility = View.GONE
        binding.ivShare.visibility = View.INVISIBLE

        arguments?.let { studentId = it.getLong(STUDENT) }

//        Log.d("ReportFragment", "studentId=$studentId")

        binding.rvStudentEventList.layoutManager = LinearLayoutManager(context)
        binding.rvStudentEventList.adapter = adapter

        viewModel.getStudentReportData(studentId)
        viewModel.studentReportList.observe(this.viewLifecycleOwner) {
            adapter.updateData(it)
        }

    }

    override fun onItemClick(studentReportModel: StudentReportModel) {

    }

}
