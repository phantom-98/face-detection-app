package com.facecool.ui.classes.add.students

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.facecool.R
import com.facecool.databinding.FragmentAddClassSelectStudentsBinding
import com.facecool.ui.BaseFragment
import com.facecool.ui.classes.add.AddClassNavigator
import com.facecool.ui.classes.common.SharedAddNewClassData
import com.facecool.ui.students.select.AddStudentsViewModel
import com.facecool.ui.students.select.StudentSelectionAdapter
import com.facecool.ui.students.select.StudentSelectionModel
import com.facecool.utils.textChanges
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class AddClassSelectStudentsFragment :
    BaseFragment<FragmentAddClassSelectStudentsBinding>(FragmentAddClassSelectStudentsBinding::inflate),
    StudentSelectionAdapter.Listener {

    companion object {
        fun newInstance() = AddClassSelectStudentsFragment()
    }

    private val adapter = StudentSelectionAdapter(this)


    private val viewModel: AddStudentsViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AddClassNavigator)?.updateTitle(getString(R.string.add_class_select_student))
        binding.nextButton.setOnClickListener {
            (activity as? AddClassNavigator)?.onStudentsAdded()
        }

        binding.cancelButton.setOnClickListener {
            activity?.finish()
        }

        viewModel.getStudentFilter()

        viewModel.studentFilter.observe(viewLifecycleOwner) {
            val spinnerAdapter =
                ArrayAdapter(requireContext(), R.layout.item_spinner_custom, it)
            binding.spinnerFilter.adapter = spinnerAdapter
        }

        binding.studentListSelection.layoutManager = LinearLayoutManager(requireContext())
        binding.studentListSelection.adapter = adapter

//        binding.searchStudents.setText(SharedAddNewClassData.className)

        binding.searchStudents
            .textChanges()
            .onEach {
//                viewModel.searchWord = it.toString()
                viewModel.searchForStudentData(it.toString())
            }
            .launchIn(lifecycleScope)

        binding.spinnerFilter.onItemSelectedListener = object : OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                binding.searchStudents.setText("")
                viewModel.onFilterSelected(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }


        binding.selectAllCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAllSelectedStateTo(isChecked)
        }

        viewModel.students.observe(viewLifecycleOwner) {
            adapter.setStudents(it)
        }

    }

    override fun getTitle(): String = ""

    override fun onResume() {
        super.onResume()
        viewModel.updateStudentList()
    }

    override fun onStudentSelected(student: StudentSelectionModel, selectionStatus: Boolean) {
        viewModel.onStudentSelected(student, selectionStatus)
    }

    override fun onStudentClick(student: StudentSelectionModel) {
//        viewModel.onStudentClick(student)
    }

}
