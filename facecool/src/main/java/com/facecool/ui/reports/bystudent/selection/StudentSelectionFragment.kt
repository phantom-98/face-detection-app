package com.facecool.ui.reports.bystudent.selection

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.facecool.databinding.FragmentStudentSelectionBinding
import com.facecool.ui.BaseFragment
import com.facecool.ui.reports.bystudent.StudentReportNavigator
import com.facecool.utils.textChanges
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class StudentSelectionFragment: BaseFragment<FragmentStudentSelectionBinding>(FragmentStudentSelectionBinding::inflate),
    StudentSelectionAdapter.Listener {

    companion object {
        fun newInstance() = StudentSelectionFragment()
    }

    override fun getTitle(): String = ""

    private val adapter = StudentSelectionAdapter(this)
    private val viewModel : StudentSelectionViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.studentSelectionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.studentSelectionRecyclerView.adapter = adapter

        viewModel.students.observe(viewLifecycleOwner) {
            adapter.setStudentSelectionModels(it)
        }

        binding.searchStudentEditText.textChanges().onEach {
            viewModel.searchForStudent(it.toString())
        }.launchIn(lifecycleScope)

        viewModel.selectedStudent.observe(viewLifecycleOwner) {
            (parentFragment as? StudentReportNavigator)?.onStudentSelected(it)
        }

        viewModel.getStudents()

    }

    override fun onStudentSelected(studentModel: StudentSelectionModel) {
        viewModel.onStudentSelected(studentModel)
    }

}