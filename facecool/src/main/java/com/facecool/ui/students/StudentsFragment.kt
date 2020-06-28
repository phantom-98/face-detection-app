package com.facecool.ui.students

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facecool.R
import com.facecool.databinding.FragmentStudentsBinding
import com.facecool.navigation.NavigatorContract
import com.facecool.ui.BaseFragment
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.common.FaceCoolDialog
import com.facecool.ui.common.FaceSelectorCoolDialog
import com.facecool.ui.common.ProgressStatus
import com.facecool.ui.students.addfolder.AddFolderDialog
import com.facecool.ui.students.common.EnrollmentStatus
import com.facecool.utils.gone
import com.facecool.utils.textChanges
import com.facecool.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import lib.folderpicker.FolderPicker
import java.io.File
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
class StudentsFragment : BaseFragment<FragmentStudentsBinding>(FragmentStudentsBinding::inflate),
    StudentListAdapter.Listener, AddFolderDialog.Listener {

    @Inject
    lateinit var navigator: NavigatorContract

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(this.requireContext(), FolderPicker::class.java)
            getDBFile.launch(intent)
        } // TODO handle permission not granted
    }

    private val listUriImg = mutableListOf<Uri>()

    private fun getFileExtension(strFileName: String): String {
        return if (strFileName.contains(".")) {
            strFileName.substring(strFileName.lastIndexOf("."))
        } else {
            ""
        }
    }

    private fun doRegisterFolder(strPath: String) {
        val childFiles = File(strPath).listFiles()
        childFiles ?: return
        for (file in childFiles) {
            if (file.isDirectory) {
                doRegisterFolder(file.path)
            } else if (file.isFile) {
                val fileName = file.name
                val fileExtension: String = getFileExtension(fileName)
                if (fileExtension.equals(".jpeg", ignoreCase = true) ||
                    fileExtension.equals(".png", ignoreCase = true) ||
                    fileExtension.equals(".jpg", ignoreCase = true) ||
                    fileExtension.equals(".bmp", ignoreCase = true)
                ) {
                    val uriImg = Uri.fromFile(file)
                    listUriImg.add(uriImg)
                }
            }
        }
    }

    private val getDBFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                listUriImg.clear()
                val data = it.data
                val folderLocation = data?.extras?.getString("data")
                folderLocation?.let { it1 ->
                    doRegisterFolder(it1)
                    AddFolderDialog(listUriImg, this).show(
                        childFragmentManager,
                        AddFolderDialog.TAG
                    )
                }
            }
        }

    private val viewModel: StudentsViewModel by viewModels()
    private val enrolledAdapter = StudentListAdapter(this)
    private val rejectedAdapter = StudentListAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.entireScrollView.post {
//            val height = binding.entireScrollView.height
//            binding.rejectedList.layoutParams.height = height
//            binding.enrolledList.layoutParams.height = height
//        }
        viewModel.cleanRejectedStudents()

        binding.studentsList.layoutManager = LinearLayoutManager(requireContext())
        binding.studentsList.adapter = enrolledAdapter

        binding.rejectedStudentsList.layoutManager = LinearLayoutManager(requireContext())
        binding.rejectedStudentsList.adapter = rejectedAdapter

        binding.searchStudents.textChanges()
            .onEach { viewModel.searchForStudent(it.toString()) }
            .launchIn(lifecycleScope)

        viewModel.students.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()){
                binding.enrolledList.visible()
            } else binding.enrolledList.gone()
            enrolledAdapter.setStudents(it)
        }

        viewModel.rejectedStudents.observe(viewLifecycleOwner) {
            rejectedAdapter.setStudents(it)
        }

        viewModel.checkIfEmptyRejected()

        viewModel.isEmptyRejected.observe(viewLifecycleOwner) {
            if (it) binding.rejectedList.gone()
            else binding.rejectedList.visible()
        }

        binding.toggleEnrolled.setOnClickListener {
            if (binding.studentsList.isVisible){
                binding.studentsList.gone()
                binding.ivEnrolled.setImageResource(R.drawable.down_arrow)
            } else {
                binding.studentsList.visible()
                binding.ivEnrolled.setImageResource(R.drawable.up_arrow)
            }
        }
        viewModel.generalProgress.observe(viewLifecycleOwner){
            if (it == ProgressStatus.LOADING) {
                binding.progressUserLoading.visible()
            } else {
                binding.progressUserLoading.gone()
            }
        }
        binding.toggleRejected.setOnClickListener {
            if (binding.rejectedStudentsList.isVisible){
                binding.rejectedStudentsList.gone()
                binding.ivRejected.setImageResource(R.drawable.down_arrow)
                viewModel.clearRejectedStuents()
            } else {
                binding.rejectedStudentsList.visible()
                binding.ivRejected.setImageResource(R.drawable.up_arrow)
                viewModel.getRejectedStudents()
            }
        }

        binding.buttonAddNewStudent.setOnClickListener {


            FaceSelectorCoolDialog(
                getString(R.string.enrollment_method),
                getString(R.string.folder_enrollment),
                getString(R.string.single_student),
                getString(R.string.dialog_alert_cancel_button),
                object : FaceSelectorCoolDialog.Listener {
                    override fun onBtnOne() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        } else {
                            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    }

                    override fun onBtnTwo() {
                        navigator.openAddNewStudentActivity(requireActivity())
                    }

                    override fun onBtnThree() {}

                }
            ).show(childFragmentManager, FaceSelectorCoolDialog.TAG)
        }

//        binding.studentsList.addOnScrollListener(object : RecyclerView.OnScrollListener(){
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                Log.d("SCROLL", "canscrollDown ${recyclerView.canScrollVertically(1)} up ${recyclerView.canScrollVertically(-1)}")
//                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_DRAGGING){
//                    if (binding.searchStudents.text.isNullOrEmpty()) viewModel.getMoreStudents()
//                    else viewModel.searchMore(binding.searchStudents.text.toString())
//                }
//            }
//        })
        binding.entireScrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (!v.canScrollVertically(1)){
                if (binding.searchStudents.text.isNullOrEmpty()) viewModel.getMoreStudents()
                else viewModel.searchMore(binding.searchStudents.text.toString())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getStudents()
        viewModel.checkIfEmptyRejected()
    }

    override fun onStop() {
        super.onStop()

    }

    override fun onStudentClicked(student: CameraDetectionModel) {
        viewModel.onStudentClicked(student)
            student.id?.let {
//                if (student.enrolmentStatus == EnrollmentStatus.REJECTED)
//                    navigator.openAddNewStudentActivity(requireActivity(), student)
//                else
                    navigator.openStudentDetailsActivity(requireActivity(), it)
            }
    }

    override fun removeStudent(student: CameraDetectionModel) {
        FaceCoolDialog.newInstance(
            getString(R.string.delete_confirm_message, student.name),
            object : FaceCoolDialog.Listener {
                override fun onPositiveClicked() {
                    viewModel.removeStudent(student)
                }
            }).show(childFragmentManager, FaceCoolDialog.TAG)
    }

    override fun getTitle(): String = getTitle(requireActivity())

    override fun getTitle(context: Context) = context.getString(R.string.students_title)

    override fun onDone() {
        viewModel.getStudents()
        viewModel.checkIfEmptyRejected()
    }

}
