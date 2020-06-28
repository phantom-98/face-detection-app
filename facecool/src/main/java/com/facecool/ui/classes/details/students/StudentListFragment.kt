package com.facecool.ui.classes.details.students

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.facecool.R
import com.facecool.databinding.FragmentClassesStudentListBinding
import com.facecool.navigation.NavigatorContract
import com.facecool.ui.BaseFragment
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.common.FaceCoolDialog
import com.facecool.ui.students.select.ActivitySelectStudent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StudentListFragment :
    BaseFragment<FragmentClassesStudentListBinding>(FragmentClassesStudentListBinding::inflate),
    ClassStudentAdapter.Listener {

    companion object {
        private const val CLASS_ID = "CLASS_ID"
        fun newInstance(classId: Long): StudentListFragment {
            val args = Bundle()
            args.putLong(CLASS_ID, classId)
            val fragment = StudentListFragment()
            fragment.arguments = args
            return fragment
        }
    }

    @Inject
    lateinit var navigator: NavigatorContract

    override fun getTitle(): String = getTitle(requireActivity())

    override fun getTitle(context: Context) = context.getString(R.string.students_title)

    private val addStudentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == Activity.RESULT_OK){
            val studentList = it.data?.getLongArrayExtra("student_id_list")
            viewModel.addNewStudentsById(studentList)
        }
    }

    private val viewModel: StudentListViewModel by viewModels()
    private val adapter = ClassStudentAdapter(this)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val classID = arguments?.getLong(CLASS_ID, -1) ?: -1

        viewModel.getStudents(classID)

        binding.classesListStudentsDetails.layoutManager = LinearLayoutManager(requireContext())
        binding.classesListStudentsDetails.adapter = adapter

        binding.buttonAddNewStudent.setOnClickListener {
            addStudentLauncher.launch(context?.let { it1 ->
                ActivitySelectStudent.intentForAddStudents(
                    it1, classID)
            })
//            addStudentLauncher.launch(Intent(context, ActivitySelectStudent::class.java))
        }

        viewModel.students.observe(viewLifecycleOwner) {
            adapter.setStudentList(it)
        }

    }

    override fun onStudentClicked(student: CameraDetectionModel) {
        student.id?.let { navigator.openStudentDetailsActivity(requireActivity(), it) }
    }

    override fun onDeleteStudentClicked(student: CameraDetectionModel) {

        FaceCoolDialog.newInstance(
            getString(R.string.unenroll_student_from_class),
            object : FaceCoolDialog.Listener {
                override fun onPositiveClicked() {
                    viewModel.onDeleteStudentClicked(student)
                }
            }
        ).show(parentFragmentManager, FaceCoolDialog.TAG)
    }

}
