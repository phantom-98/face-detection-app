package com.facecool.ui.students.add

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
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.facecool.R
import com.facecool.databinding.ActivityAddNewStudentBinding
import com.facecool.navigation.NavigatorContract
import com.facecool.ui.BaseActivity
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.common.FaceCoolPhotoDialog
import com.facecool.ui.common.ProgressStatus
import com.facecool.utils.gone
import com.facecool.utils.isEmail
import com.facecool.utils.isLetterAndDigitsOnly
import com.facecool.utils.textChanges
import com.facecool.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@AndroidEntryPoint
class AddNewStudentActivity : BaseActivity() {

    companion object {

        private const val DATA_KEY = "data"

        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, AddNewStudentActivity::class.java)
            context.startActivity(starter)
        }

        @JvmStatic
        fun start(context: Context, dataKey: String) {
            val starter = Intent(context, AddNewStudentActivity::class.java)
            starter.putExtra(DATA_KEY, dataKey)
            context.startActivity(starter)
        }
    }

    private val settingLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppSearchResult.RESULT_OK) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private val REQUEST_WRITE_EXTERNAL_STORAGE = 10

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickCamera()
        }
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
                        bmp.recycle()
                    }
                }
            }
        }

    private val getDBFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data
                val uri = data?.data
//                Log.d("TAG", "ru from one image: $uri")
                viewModel.loadImageFromUri(uri)
            }
        }

    @Inject
    lateinit var navigator: NavigatorContract

    private val viewModel: AddNewStudentViewModel by viewModels()

    private lateinit var binding: ActivityAddNewStudentBinding

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            binding.tvTakePicture.text = getString(R.string.capture)
            binding.btnTakePicture.setOnClickListener {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } else {
            binding.tvTakePicture.text = getString(R.string.grant_permission)
            binding.btnTakePicture.setOnClickListener {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                settingLauncher.launch(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarContainer.toolbar)
        supportActionBar?.title = getString(R.string.enroll_new_student)

        val dataKey: String? = intent.extras?.getString(DATA_KEY)
        val data = receiver.get<CameraDetectionModel>(dataKey)
        viewModel.setExistingData(data)

        data?.let {
            binding.firstName.requestFocus()
        }

        viewModel.imgQuality.observe(this) {
            it.getValueIfNotUsed()?.let {
                binding.tvImageQuality.setText(it.toString())
            }
        }

        viewModel.automaticId.observe(this){
            it.getValueIfNotUsed()?.let { data ->
                binding.id.setText(data)
            }
        }

        viewModel.automaticName.observe(this){
            it.getValueIfNotUsed()?.let { data ->
                binding.firstName.setText(data)
            }
        }

        viewModel.automaticLastName.observe(this){
            it.getValueIfNotUsed()?.let { data ->
                binding.lastName.setText(data)
            }
        }

        viewModel.nullUserData.observe(this) {
            it.getValueIfNotUsed()?.let {
                binding.profilePicture.setOnClickListener {}
                binding.profilePicture.setImageResource(R.drawable.unknown_user)
            }
        }

        viewModel.userData.observe(this) {
            it.getValueIfNotUsed()?.let { model ->
                val btm = model.rez?.getFaceImage()
                btm?.let {
                    binding.profilePicture.setImageBitmap(btm)
                    binding.profilePicture.setOnClickListener {
                        FaceCoolPhotoDialog(model.name, btm = btm).show(
                            supportFragmentManager,
                            FaceCoolPhotoDialog.TAG
                        )
                    }
                }
            }
        }

        viewModel.endEvent.observe(this) {
            it.getValueIfNotUsed()?.let {
                finish()
            }
        }

        viewModel.remainingUsers.observe(this) {
            it.getValueIfNotUsed().let { pairs ->
                pairs?.forEach {
                    navigator.openAddNewStudentActivity(this, it)
                }
            }
        }

        viewModel.profileImageLoading.observe(this) {
            it.getValueIfNotUsed()?.let {
                when (it) {
                    ProgressStatus.LOADING -> {
                        binding.profilePicture.gone()
                        binding.progressUserImage.visible()
                    }

                    ProgressStatus.DONE -> {
                        binding.profilePicture.visible()
                        binding.progressUserImage.gone()
                    }

                    ProgressStatus.ERROR -> {
                        binding.profilePicture.visible()
                        binding.progressUserImage.gone()
                        Toast.makeText(
                            this,
                            getString(R.string.face_not_detected_on_image),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        binding.checkboxTwin.setOnCheckedChangeListener { buttonView, isChecked -> viewModel.updateTwin(isChecked) }

        binding.firstName.textChanges()
            .debounce(300)
            .onEach { viewModel.updateName(it.toString()) }
            .launchIn(lifecycleScope)

        binding.lastName.textChanges()
            .debounce(300)
            .onEach { viewModel.updateLastName(it.toString()) }
            .launchIn(lifecycleScope)

        binding.id.textChanges()
            .debounce(300)
            .onEach { viewModel.updateId(it.toString()) }
            .launchIn(lifecycleScope)

        binding.email.textChanges()
            .debounce(300)
            .onEach { viewModel.updateEmail(it.toString()) }
            .launchIn(lifecycleScope)

        binding.adress.textChanges()
            .debounce(300)
            .onEach { viewModel.updateAddress(it.toString()) }
            .launchIn(lifecycleScope)

        binding.phoneNumber.textChanges()
            .onEach { viewModel.updatePhoneNumber(it.toString()) }
            .launchIn(lifecycleScope)

        binding.checkboxParent.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.updateEnableNotification(isChecked)
            binding.parentNames.isEnabled = isChecked
            binding.parentEmails.isEnabled = isChecked
            binding.parentSmsNumbers.isEnabled = isChecked
            binding.parentWhatsappNumbers.isEnabled = isChecked
            binding.checkboxEmail.isEnabled = isChecked
            binding.checkboxWhatsapp.isEnabled = isChecked
            binding.checkboxSms.isEnabled = isChecked
            binding.checkboxAll.isEnabled = isChecked
        }

        binding.parentNames.textChanges()
            .onEach { viewModel.updateParentNames(it.toString()) }
            .launchIn(lifecycleScope)

        binding.checkboxSms.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.updateEnableSMS(isChecked)
            binding.checkboxAll.isChecked = binding.checkboxSms.isChecked && binding.checkboxEmail.isChecked && binding.checkboxWhatsapp.isChecked
        }

        binding.checkboxEmail.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.updateEnableEmail(isChecked)
            binding.checkboxAll.isChecked = binding.checkboxSms.isChecked && binding.checkboxEmail.isChecked && binding.checkboxWhatsapp.isChecked
        }

        binding.checkboxWhatsapp.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.updateEnableWhatsapp(isChecked)
            binding.checkboxAll.isChecked = binding.checkboxSms.isChecked && binding.checkboxEmail.isChecked && binding.checkboxWhatsapp.isChecked
        }

        binding.checkboxAll.setOnClickListener {
            val isChecked = binding.checkboxAll.isChecked
            viewModel.updateEnableSMS(isChecked)
            viewModel.updateEnableEmail(isChecked)
            viewModel.updateEnableWhatsapp(isChecked)
            binding.checkboxSms.isChecked = isChecked
            binding.checkboxEmail.isChecked = isChecked
            binding.checkboxWhatsapp.isChecked = isChecked
        }

        binding.parentSmsNumbers.textChanges()
            .onEach { viewModel.updateSMSNumbers(it.toString()) }
            .launchIn(lifecycleScope)

        binding.parentEmails.textChanges()
            .onEach { viewModel.updateEmailAddress(it.toString()) }
            .launchIn(lifecycleScope)

        binding.parentWhatsappNumbers.textChanges()
            .onEach { viewModel.updateWhatsappNumbers(it.toString()) }
            .launchIn(lifecycleScope)

        binding.nextButton.setOnClickListener {
            if (binding.firstName.text.isNullOrBlank()){
                Toast.makeText(this, getString(R.string.validation_first_name_required), Toast.LENGTH_SHORT).show()
                binding.firstName.requestFocus()
                return@setOnClickListener
            }
            if (binding.lastName.text.isNullOrBlank()){
                Toast.makeText(this, getString(R.string.validation_last_name_required), Toast.LENGTH_SHORT).show()
                binding.lastName.requestFocus()
                return@setOnClickListener
            }
            if (binding.id.text.isNullOrBlank()){
                Toast.makeText(this, getString(R.string.validation_id_required), Toast.LENGTH_SHORT).show()
                binding.id.requestFocus()
                return@setOnClickListener
            }
            if (binding.id.text.isLetterAndDigitsOnly().not()){
                Toast.makeText(this, getString(R.string.validation_id_not_matched), Toast.LENGTH_SHORT).show()
                binding.id.requestFocus()
                return@setOnClickListener
            }
            if (binding.email.text.isNullOrBlank().not() && binding.email.text.isEmail().not()){
                Toast.makeText(this, getString(R.string.validation_email_not_matched), Toast.LENGTH_SHORT).show()
                binding.email.requestFocus()
                return@setOnClickListener
            }
            viewModel.saveItem()
        }

        binding.btnImageChoser.setOnClickListener {
            showImageChooser()
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }

    }

    private fun showImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
//        intent.putExtra("requestCode", "SettingsFragment.FOLDERPICKER_CODE")
        getDBFile.launch(intent)
    }

    private fun pickCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startCamera.launch(cameraIntent)
    }

}
