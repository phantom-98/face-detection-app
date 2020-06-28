package com.facecool.ui.reports.bystudent.reports

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.facecool.R
import com.facecool.databinding.ActivityStudentReportBinding
import com.facecool.databinding.FragmentReportListStudentBinding
import com.facecool.ui.BaseActivity
import com.facecool.ui.common.ProgressStatus

import com.facecool.ui.reports.bystudent.selection.StudentSelectionModel
import com.facecool.utils.getExtraClass
import com.facecool.utils.gone
import com.facecool.utils.shareReport
import com.facecool.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StudentReportActivity : BaseActivity() ,StudentReportAdapter.Listener {

    companion object {
        private const val STUDENT_SELECTION = "studentSelectionModel"

        @JvmStatic
        fun start(context: Context, studentSelectionModel: StudentSelectionModel) {
            val starter = Intent(context, StudentReportActivity::class.java)
                .putExtra(STUDENT_SELECTION, studentSelectionModel)
            context.startActivity(starter)
        }
    }

    private lateinit var binding: FragmentReportListStudentBinding

    private val viewModel: StudentReportViewModel by viewModels()
    private val adapter = StudentReportAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentReportListStudentBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbarContainer.toolbar)

        val studentSelectionModel: StudentSelectionModel = getExtraClass(STUDENT_SELECTION) ?: return

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = String.format(
            getString(R.string.class_report_title),
            studentSelectionModel.studentName
        )

        binding.rvStudentEventList.layoutManager = LinearLayoutManager(this)
        binding.rvStudentEventList.adapter = adapter

        viewModel.getStudentReportData(studentSelectionModel.studentId!!)
        viewModel.studentReportList.observe(this) {
            adapter.updateData(it)
        }
        binding.ivShare.setOnClickListener {
            viewModel.prepareReportForSharing()
        }

        viewModel.reportFileData.observe(this) {
            this.shareReport(it)
        }

        viewModel.generalLoading.observe(this) {
            when (it) {
                ProgressStatus.LOADING -> binding.progress.visible()
                else -> binding.progress.gone()
            }
        }
    }

    override fun onItemClick(studentReportModel: StudentReportModel) {

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}