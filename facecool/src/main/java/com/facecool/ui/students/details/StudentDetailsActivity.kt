package com.facecool.ui.students.details

import android.Manifest
import android.app.Activity
import android.app.appsearch.AppSearchResult
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.facecool.R
import com.facecool.databinding.ActivityStudentDetailsBinding
import com.facecool.navigation.NavigatorContract
import com.facecool.ui.BaseActivity
import com.facecool.ui.common.FaceCoolDialog
import com.facecool.ui.common.FaceCoolEditFieldDialog
import com.facecool.ui.common.FaceCoolPhotoChooserDialog
import com.facecool.ui.common.ProgressStatus
import com.facecool.ui.reports.ReportPageAdapter
import com.facecool.ui.reports.bystudent.reports.StudentReportFragment
import com.facecool.ui.students.common.ViewWithTitle
import com.facecool.utils.gone
import com.facecool.utils.isEmail
import com.facecool.utils.isLetterAndDigitsOnly
import com.facecool.utils.loadImageFromLocal
import com.facecool.utils.visible
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StudentDetailsActivity : BaseActivity(),
    StudentDetailsItemsAdapter.Listener {

    companion object {
        private const val STUDENT_ID = "STUDENT_ID"

        @JvmStatic
        fun start(context: Context, studentId: Long) {
            val starter = Intent(context, StudentDetailsActivity::class.java)
            starter.putExtra(STUDENT_ID, studentId)
            context.startActivity(starter)
        }
    }

    private val REQUEST_WRITE_EXTERNAL_STORAGE = 10

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickCamera()
        } // TODO handle permission not granted
    }

    private val startCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val uri = data?.data
                if (uri != null) {
                    viewModel.loadImageFromUri(uri)
                } else {
                    data?.extras?.let {b ->
                        val bmp = b.get("data") as Bitmap
                        viewModel.loadImageFromBitmap(bmp)
                    }
                }
            }
        }

    private val getDBFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data
                val uri = data?.data
                viewModel.loadImageFromUri(uri)
            }
        }

    @Inject
    lateinit var navigator: NavigatorContract

    private val viewModel: StudentDetailsViewModel by viewModels()

    private lateinit var binding: ActivityStudentDetailsBinding

    private val errorAdapter = ErrorAdapter()
    private val itemsAdapter = StudentDetailsItemsAdapter(this)
    private val parentAdapter = StudentDetailsItemsAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarContainer.toolbar)
        supportActionBar?.title = getString(R.string.student_details)

        val studentId = intent.getLongExtra(STUDENT_ID, -1)
        viewModel.checkStudentById(studentId)

        binding.studentDetails.layoutManager = LinearLayoutManager(this)
        binding.studentDetails.adapter = itemsAdapter

        binding.parentDetails.layoutManager = LinearLayoutManager(this)
        binding.parentDetails.adapter = parentAdapter

        binding.toolbarContainer.ivCloseJourney?.visibility = View.VISIBLE
        binding.toolbarContainer.ivCloseJourney?.setOnClickListener {
            navigator.closeJourney(this)
        }

        binding.userImage.setOnClickListener {
            FaceCoolPhotoChooserDialog(
                getString(R.string.from_where_to_take_photo),
                object : FaceCoolPhotoChooserDialog.Listener {
                    override fun onCamera() {

                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }

                    override fun onFile() {
                        showImageChooser()
                    }

                }).show(supportFragmentManager, FaceCoolPhotoChooserDialog.TAG)
        }

        binding.ckbxTwin.setOnCheckedChangeListener { buttonView, isChecked -> viewModel.updateTwin(isChecked) }

        viewModel.quality.observe(this) {
            if (it==0){
                Toast.makeText(this, getString(R.string.image_quality_low), Toast.LENGTH_SHORT).show()
            } else binding.tvImageQuality.setText(it.toString())
        }

        viewModel.errorList.observe(this){
            errorAdapter.updateData(it)
        }

        binding.rvErrorList.layoutManager = LinearLayoutManager(this)
        binding.rvErrorList.adapter = errorAdapter

        viewModel.pictureLoading.observe(this) {
            it.getValueIfNotUsed()?.let {
                when (it) {
                    ProgressStatus.LOADING -> {
                        binding.userImage.gone()
                        binding.imageProgress.visible()
                    }
                    ProgressStatus.DONE -> {
                        binding.userImage.visible()
                        binding.imageProgress.gone()
                    }
                    ProgressStatus.ERROR -> {
                        binding.userImage.visible()
                        binding.imageProgress.gone()
                        Toast.makeText(this, R.string.face_not_detected_on_image, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewModel.userImage.observe(this) {
            it.getValueIfNotUsed()?.let {
                val (path, name) = it
                binding.userImage.loadImageFromLocal(path)

            }
        }

        viewModel.userTwin.observe(this){
            it.getValueIfNotUsed()?.let {
                binding.ckbxTwin.isChecked = it
            }
        }
        viewModel.enableNotification.observe(this){
            it.getValueIfNotUsed()?.let {
                binding.checkboxParent.isChecked = it
            }
        }
        viewModel.enableSMS.observe(this){
            it.getValueIfNotUsed()?.let {
                binding.checkboxSms.isChecked = it
            }
        }
        viewModel.enableEmail.observe(this){
            it.getValueIfNotUsed()?.let {
                binding.checkboxEmail.isChecked = it
            }
        }
        viewModel.enableWhatsapp.observe(this){
            it.getValueIfNotUsed()?.let {
                binding.checkboxWhatsapp.isChecked = it
            }
        }
        binding.checkboxParent.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.updateNotification(isChecked)

        }
        binding.checkboxSms.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.updateSMS(isChecked)
            binding.checkboxAll.isChecked = binding.checkboxSms.isChecked && binding.checkboxEmail.isChecked && binding.checkboxWhatsapp.isChecked
        }
        binding.checkboxEmail.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.updateEmail(isChecked)
            binding.checkboxAll.isChecked = binding.checkboxSms.isChecked && binding.checkboxEmail.isChecked && binding.checkboxWhatsapp.isChecked
        }
        binding.checkboxWhatsapp.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.updateWhatsapp(isChecked)
            binding.checkboxAll.isChecked = binding.checkboxSms.isChecked && binding.checkboxEmail.isChecked && binding.checkboxWhatsapp.isChecked
        }
        binding.checkboxAll.setOnClickListener {
            val isChecked = binding.checkboxAll.isChecked
            viewModel.updateSMS(isChecked)
            viewModel.updateEmail(isChecked)
            viewModel.updateWhatsapp(isChecked)
            binding.checkboxSms.isChecked = isChecked
            binding.checkboxEmail.isChecked = isChecked
            binding.checkboxWhatsapp.isChecked = isChecked
        }

        viewModel.studentDetailsItems.observe(this) {
            itemsAdapter.setItems(it)
        }
        viewModel.parentDetailsItems.observe(this) {
            parentAdapter.setItems(it)
        }

        val adapter = StudentEnrollAndAttendanceAdapter(this)

        binding.studentViewPager.adapter = adapter

        TabLayoutMediator(binding.studentTabLayout, binding.studentViewPager) { tab, position ->
            tab.text = (adapter.fragments[position] as? ViewWithTitle?)?.getTitle(this)
            (adapter.fragments[0] as StudentEnrolledClassesFragment).studentId = studentId
            (adapter.fragments[1] as StudentReportFragment).studentId = studentId
        }.attach()
    }

    override fun onItemEditClicked(item: StudentDetailItemModel) {
        FaceCoolEditFieldDialog(
            item.label,
            item.value,
            item.action,
            object : FaceCoolEditFieldDialog.Listener<ItemAction> {
                override fun onPositiveClicked(newData: String?, code: ItemAction) {
                    newData?.let {
                        when (code) {
                            ItemAction.STUDENT_NAME -> {
                                if (newData.isNullOrBlank()){
                                    Toast.makeText(this@StudentDetailsActivity, getString(R.string.validation_first_name_required), Toast.LENGTH_SHORT).show()
                                    return
                                }
                            }
                            ItemAction.STUDENT_LAST_NAME -> {
                                if (newData.isNullOrBlank()){
                                    Toast.makeText(this@StudentDetailsActivity, getString(R.string.validation_last_name_required), Toast.LENGTH_SHORT).show()
                                    return
                                }
                            }
                            ItemAction.STUDENT_ID -> {
                                if (newData.isNullOrBlank()){
                                    Toast.makeText(this@StudentDetailsActivity, getString(R.string.validation_id_required), Toast.LENGTH_SHORT).show()
                                    return
                                }
                                if (newData.isLetterAndDigitsOnly().not()){
                                    Toast.makeText(this@StudentDetailsActivity, getString(R.string.validation_id_not_matched), Toast.LENGTH_SHORT).show()
                                    return
                                }
                            }
                            ItemAction.STUDENT_EMAIL -> {
                                if (newData.isNullOrBlank().not() && newData.isEmail().not()){
                                    Toast.makeText(this@StudentDetailsActivity, getString(R.string.validation_email_not_matched), Toast.LENGTH_SHORT).show()
                                    return
                                }
                            }
                            else -> {

                            }
                        }
                        viewModel.updateField(it, code)
                    }
                }
            },
        ).show(this.supportFragmentManager, FaceCoolDialog.TAG)
    }

    private fun showImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra("requestCode", "SettingsFragment.FOLDERPICKER_CODE")
        getDBFile.launch(intent)
    }

    private fun pickCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startCamera.launch(cameraIntent)
    }
}
