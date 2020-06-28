package com.facecool.ui.students.select

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.facecool.R
import com.facecool.databinding.ActivityStudentSelectionBinding
import com.facecool.ui.BaseActivity
import com.facecool.utils.textChanges
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class ActivitySelectStudent : BaseActivity(), StudentSelectionAdapter.Listener {

    companion object {
        private const val CLASS_ID = "CLASS_ID"

        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, ActivitySelectStudent::class.java)
            context.startActivity(starter)
        }

        fun intentForAddStudents(context: Context, classID: Long): Intent {
            val intent = Intent(context, ActivitySelectStudent::class.java)
            intent.putExtra(CLASS_ID,classID)
            return intent
        }

    }

    private lateinit var binding: ActivityStudentSelectionBinding

    private val viewModel by viewModels<AddStudentsViewModel>()

    private val adapter = StudentSelectionAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val classID = intent.getLongExtra(CLASS_ID, -1)
        if (classID != -1L){
            viewModel.updateScreenByClassData(classID)
        }

        setSupportActionBar(binding.toolbarContainer.toolbar)
        supportActionBar?.title = getString(R.string.select_students)


        viewModel.className.observe(this){
            supportActionBar?.title = getString(R.string.add_students_for, it)
        }

        binding.studentList.layoutManager = LinearLayoutManager(this)
        binding.studentList.adapter = adapter

        binding.btnAccept.setOnClickListener {
            viewModel.onFinishSelection()
        }

        binding.searchStudents
            .textChanges()
            .onEach { viewModel.searchForStudentData(it.toString()) }
            .launchIn(lifecycleScope)

        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.onFilterSelected(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }


        binding.selectAllCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAllSelectedStateTo(isChecked)
        }

        viewModel.selectedStudentsId.observe(this) {
            val intent = Intent()
            intent.putExtra("student_id_list", it.toLongArray())
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        viewModel.getStudentFilter()
        viewModel.studentFilter.observe(this) {
            val spinnerAdapter =
                ArrayAdapter(this, R.layout.item_spinner_custom, it)
            binding.spinnerFilter.adapter = spinnerAdapter
        }

        if (classID == -1L){
            viewModel.updateStudentList()
        } else {
            viewModel.updateStudentListExcludingClass(classID)
        }

        viewModel.students.observe(this) {
            adapter.setStudents(it)
        }

    }

    override fun onStudentSelected(student: StudentSelectionModel, selectionStatus: Boolean) {
        viewModel.onStudentSelected(student, selectionStatus)
    }

    override fun onStudentClick(student: StudentSelectionModel) {
        viewModel.onStudentClick(student)
    }

}
