package com.facecool.ui.reports.byclass.report

import android.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import com.facecool.databinding.FragmentReportListBinding
import com.facecool.navigation.NavigatorContract
import com.facecool.ui.BaseActivity
import com.facecool.ui.common.ProgressStatus
import com.facecool.ui.reports.byclass.selection.ClassSelectionModel
import com.facecool.utils.getExtraClass
import com.facecool.utils.gone
import com.facecool.utils.shareReport
import com.facecool.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ClassReportActivity : BaseActivity(), ReportTableAdapter.Listener {

    companion object {
        private const val CLASS_SELECTION = "classSelectionModel"

        @JvmStatic
        fun start(context: Context, classSelectionModel: ClassSelectionModel) {
            val starter = Intent(context, ClassReportActivity::class.java)
                .putExtra(CLASS_SELECTION, classSelectionModel)
            context.startActivity(starter)
        }
    }

    @Inject
    lateinit var navigator: NavigatorContract

    private lateinit var binding: FragmentReportListBinding

    private val viewModel: ClassReportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentReportListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbarContainer.toolbar)

        val classSelectionModel: ClassSelectionModel = getExtraClass(CLASS_SELECTION) ?: return

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = String.format(
            getString(com.facecool.R.string.class_report_title),
            classSelectionModel.className
        )

        binding.ivShare.setOnClickListener {
            viewModel.prepareReportForSharing()
        }

        viewModel.getReportFor(classSelectionModel)

        viewModel.reportFileData.observe(this) {
            this.shareReport(it)
        }
        viewModel.reportList.observe(this) {
            try {
                val adapter = ReportTableAdapter(this, it, this)
                binding.ftReportTable.adapter = adapter
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        viewModel.generalLoading.observe(this) {
            when (it) {
                ProgressStatus.LOADING -> binding.progress.visible()
                else -> binding.progress.gone()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPhotoViewClicked(id: Long) {
        navigator.openStudentDetailsActivity(this, id)
    }
}
