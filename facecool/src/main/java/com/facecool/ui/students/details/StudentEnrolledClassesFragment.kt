package com.facecool.ui.students.details

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.facecool.R
import com.facecool.databinding.FragmentStudentEnrolledClassesBinding
import com.facecool.navigation.NavigatorContract
import com.facecool.ui.BaseFragment
import com.facecool.ui.common.FaceCoolDialog
import com.facecool.ui.common.ProgressStatus
import com.facecool.utils.gone
import com.facecool.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StudentEnrolledClassesFragment : BaseFragment<FragmentStudentEnrolledClassesBinding>(FragmentStudentEnrolledClassesBinding::inflate), StudentEnrolledClassesAdapter.Listener{

    @Inject
    lateinit var navigator: NavigatorContract
    private val viewModel: StudentEnrolledClassesViewModel by viewModels()

    private val adapter = StudentEnrolledClassesAdapter(this)

    var studentId: Long = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        binding.studentClasses.layoutManager = LinearLayoutManager(requireActivity())
        binding.studentClasses.adapter = adapter

        binding.btnAttendance.setOnClickListener {
            navigator.openAttendanceActivity(requireActivity(), "")
        }

        viewModel.studentEnrolledClasses.observe(viewLifecycleOwner) {
            adapter.setItems(it)
        }

        viewModel.enrolledClassesProgress.observe(viewLifecycleOwner) {
            when (it) {
                ProgressStatus.LOADING -> {
                    binding.progressEnrolledClasses.visible()
                    binding.studentClasses.gone()
                }

                else -> {
                    binding.studentClasses.visible()
                    binding.progressEnrolledClasses.gone()
                }
            }
        }

        viewModel.getStudentEnrolledClasses(studentId)

    }

    override fun getTitle(): String = getTitle(requireActivity())

    override fun getTitle(context: Context) = context.getString(R.string.enrolled_classes)

    override fun onClassClicked(studentEnrolledClassModel: StudentEnrolledClassModel) {
        navigator.openClassDetailsActivity(requireActivity(), studentEnrolledClassModel.id)
    }

    override fun onRemoveClass(studentEnrolledClassModel: StudentEnrolledClassModel) {
        FaceCoolDialog.newInstance(
            getString(R.string.alert_for_delete_student, studentEnrolledClassModel.name),
            object : FaceCoolDialog.Listener {
                override fun onPositiveClicked() {
                    viewModel.onRemoveClass(studentEnrolledClassModel, studentId)
                }
            }
        ).show(childFragmentManager, FaceCoolDialog.TAG)
    }

}