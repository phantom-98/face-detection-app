package com.facecool.ui.settings.administrators.add

import android.Manifest
import android.app.Activity
import android.app.appsearch.AppSearchResult
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.lifecycleScope
import com.face.cool.databasa.Database
import com.face.cool.databasa.administrators.AdministratorEntity
import com.face.cool.manualsync.stringToBitMap
import com.facecool.R
import com.facecool.databinding.ActivityAddNewAdministratorBinding
import com.facecool.databinding.ActivityAddNewStudentBinding
import com.facecool.navigation.NavigatorContract
import com.facecool.ui.BaseActivity
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.common.FaceCoolPhotoDialog
import com.facecool.ui.common.ProgressStatus
import com.facecool.ui.students.add.AddNewStudentViewModel
import com.facecool.utils.gone
import com.facecool.utils.isEmail
import com.facecool.utils.isLetterAndDigitsOnly
import com.facecool.utils.textChanges
import com.facecool.utils.visible
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class AddNewAdministratorActivity : BaseActivity() {

    companion object {

        private const val ADMIN_ID = "adminId"
        private const val ADMIN_KEY = "adminKey"

        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, AddNewAdministratorActivity::class.java)
            context.startActivity(starter)
        }

        @JvmStatic
        fun start(context: Context, adminId: Long) {
            val starter = Intent(context, AddNewAdministratorActivity::class.java)
            starter.putExtra(ADMIN_ID, adminId)
            context.startActivity(starter)
        }

        @JvmStatic
        fun start(context: Context, adminKey: String) {
            val starter = Intent(context, AddNewAdministratorActivity::class.java)
            starter.putExtra(ADMIN_KEY, adminKey)
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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickCamera()
        }
    }

    private val getImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data
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

    @Inject
    lateinit var navigator: NavigatorContract

    private val viewModel: AddNewAdministratorViewModel by viewModels()

    private lateinit var binding: ActivityAddNewAdministratorBinding

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
        binding = ActivityAddNewAdministratorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarContainer.toolbar)
        supportActionBar?.title = getString(R.string.add_new_admin)

        val adminId: Long? = intent.getLongExtra(ADMIN_ID, -1)
        val adminKey: String? = intent.getStringExtra(ADMIN_KEY)

        if (adminId!!>-1){
            viewModel.setExistingData(adminId!!)
            binding.nextButton.setText(getString(R.string.add_new_user_activity_update_button))
        } else if (adminKey != null){
            val data = receiver.get<Bitmap>(adminKey)
            viewModel.setExistingData(data!!)
        }

        viewModel.nullUserData.observe(this) {
            it.getValueIfNotUsed()?.let {
                binding.profilePicture.setOnClickListener {}
                binding.profilePicture.setImageResource(R.drawable.unknown_user)
            }
        }

        viewModel.userData.observe(this) {
            it.getValueIfNotUsed()?.let { model ->

                val btm = model.base64Image?.stringToBitMap()

                btm?.let {
                    binding.profilePicture.setImageBitmap(btm)
                    binding.profilePicture.setOnClickListener {
                        FaceCoolPhotoDialog(model.name!!, btm = btm).show(
                            supportFragmentManager,
                            FaceCoolPhotoDialog.TAG
                        )
                    }
                }
                binding.firstName.setText(model.name)
                binding.lastName.setText(model.lastName)
                binding.email.setText(model.email)
                binding.phoneNumber.setText(model.phoneNumber)
                binding.pin.setText(model.pin)
            }
        }

        viewModel.endEvent.observe(this) {
            it.getValueIfNotUsed().let {
                finish()
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

        binding.firstName.textChanges()
            .debounce(300)
            .onEach { viewModel.updateName(it.toString()) }
            .launchIn(lifecycleScope)

        binding.lastName.textChanges()
            .debounce(300)
            .onEach { viewModel.updateLastName(it.toString()) }
            .launchIn(lifecycleScope)

        binding.pin.textChanges()
            .debounce(300)
            .onEach { viewModel.updatePin(it.toString()) }
            .launchIn(lifecycleScope)

        binding.email.textChanges()
            .debounce(300)
            .onEach { viewModel.updateEmail(it.toString()) }
            .launchIn(lifecycleScope)

        binding.phoneNumber.textChanges()
            .debounce(300)
            .onEach { viewModel.updatePhone(it.toString()) }
            .launchIn(lifecycleScope)

        binding.nextButton.setOnClickListener {
            if (viewModel.currentFace == null) {
                Toast.makeText(this, getString(R.string.input_your_face), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
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
            if (binding.email.text.isNullOrBlank().not() && binding.email.text.isEmail().not()){
                Toast.makeText(this, getString(R.string.validation_email_not_matched), Toast.LENGTH_SHORT).show()
                binding.email.requestFocus()
                return@setOnClickListener
            }
            if (binding.phoneNumber.text.isNullOrBlank().not() && binding.phoneNumber.text.isDigitsOnly().not()){
                Toast.makeText(this, getString(R.string.validation_phone_number_not_matched), Toast.LENGTH_SHORT).show()
                binding.phoneNumber.requestFocus()
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
        getImage.launch(intent)
    }

    private fun pickCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        getImage.launch(cameraIntent)
    }

}
