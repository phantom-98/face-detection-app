package com.facecool.ui.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.facecool.BuildConfig
import com.facecool.R
import com.facecool.common.AppLogger
import com.facecool.databinding.FragmentSettingsBinding
import com.facecool.navigation.NavigatorContract
import com.facecool.ui.BaseFragment
import com.facecool.ui.common.ProgressStatus
import com.facecool.utils.gone
import com.facecool.utils.shareData
import com.facecool.utils.textChanges
import com.facecool.utils.visible
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    @Inject
    lateinit var navigator: NavigatorContract

    companion object {
        private const val FOLDERPICKER_CODE = "FOLDERPICKER_CODE"
    }

    @Inject
    lateinit var logger: AppLogger

    private val viewModel: SettingsViewModel by viewModels()

    private val getDBFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data
                val uri = data?.data
                viewModel.importData(uri)
            }
        }

    override fun getTitle(): String = getTitle(requireActivity())

    override fun getTitle(context: Context) = context.getString(R.string.settings_title)

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

    @SuppressLint("StringFormatMatches")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getInitialFaceDetectionThreshold()

        viewModel.faceComparisonThreshold.observe(viewLifecycleOwner) {
            binding.sbDetectionTreashold.setProgress(it, true)
            binding.tvFaceThrsLable.text = getString(R.string.setting_matching_threshold, it)
        }
        binding.sbDetectionTreashold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.changeFaceDetectionThreshold(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        viewModel.livenessThreshold.observe(viewLifecycleOwner) {
            binding.sbLivenessThreshold.setProgress(it, true)
            binding.tvLivenessThreshold.text = getString(R.string.setting_liveness_threshold, it)
        }
        binding.sbLivenessThreshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.changeLivenessThreshold(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        viewModel.twinThreshold.observe(viewLifecycleOwner) {
            binding.sbTwinDetectionThreshold.setProgress(it, true)
            binding.tvTwinDetectionThreshold.text = getString(R.string.setting_twin_threshold, it)
        }
        binding.sbTwinDetectionThreshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser){
                    viewModel.changeTwinDetectionThreshold(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        viewModel.timeToSpear.observe(viewLifecycleOwner) {
            binding.etMinutesPreLesson.setText(it.toString())
        }

        binding.tvAppVersion.text = getString(R.string.setting_app_version, BuildConfig.VERSION_NAME, BuildConfig.BUILD_TIME)

        binding.etMinutesPreLesson.textChanges()
            .onEach { viewModel.updatePreLessonTime(it.toString()) }
            .launchIn(lifecycleScope)

        viewModel.debugMode.observe(viewLifecycleOwner) {
            binding.checkboxDebugMode.isChecked = it
        }
        binding.checkboxDebugMode.setOnCheckedChangeListener { buttonView, isChecked -> viewModel.updateDebugMode(isChecked) }

        viewModel.enableLiveness.observe(viewLifecycleOwner) {
            binding.checkboxEnableLiveness.isChecked = it
            if (it.not()) {
                binding.tvLivenessThreshold.isEnabled = it
                binding.sbLivenessThreshold.isEnabled = it
                binding.llLivenessTimeout.isEnabled = it
            }
        }
        binding.checkboxEnableLiveness.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.changeEnableLiveness(isChecked)
            binding.tvLivenessThreshold.isEnabled = isChecked
            binding.sbLivenessThreshold.isEnabled = isChecked
            binding.tvLivenessTimeout.isEnabled = isChecked
            binding.etLivenessTimeout.isEnabled = isChecked
        }
        binding.checkboxEnableLiveness.setOnClickListener {
            if (binding.checkboxEnableLiveness.isChecked){
                var snackbar = Snackbar.make(binding.llLivenessTimeout, getString(R.string.setting_liveness_alert), Snackbar.LENGTH_LONG)
                snackbar.setTextColor(Color.RED)
                snackbar.setBackgroundTint(Color.WHITE)
                snackbar.setTextMaxLines(3)
                snackbar.show()
            }
        }

        viewModel.livenessTimeout.observe(viewLifecycleOwner) {
            binding.etLivenessTimeout.setText(it.toString())
        }
        binding.etLivenessTimeout.textChanges()
            .onEach { viewModel.changeLivenessTimeout(it.toString()) }
            .launchIn(lifecycleScope)

        viewModel.enableNotification.observe(viewLifecycleOwner) {
            binding.checkboxEnableNotification.isChecked = it
        }
        binding.checkboxEnableNotification.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.changeEnableNotification(isChecked)
        }

        val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_custom_class, listOf(
            "Gmail", "Yahoo", "Outlook", "Apple"
        ))
        binding.spEmailService.adapter = spinnerAdapter

        viewModel.emailService.observe(viewLifecycleOwner) {
            binding.spEmailService.setSelection(it)
        }

        binding.spEmailService.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.changeEmailService(position)
            }
        }

        viewModel.emailAddress.observe(viewLifecycleOwner) {
            binding.etEmail.setText(it)
        }
        binding.etEmail.textChanges()
            .onEach { viewModel.changeEmail(it.toString()) }
            .launchIn(lifecycleScope)

        viewModel.emailPassword.observe(viewLifecycleOwner) {
            binding.etPassword.setText(it)
        }
        binding.etPassword.textChanges()
            .onEach { viewModel.changePassword(it.toString()) }
            .launchIn(lifecycleScope)

        viewModel.adminPin.observe(viewLifecycleOwner) {
            binding.etPin.setText(it)
        }
        binding.etPin.textChanges()
            .onEach { viewModel.updateAdminPin(it.toString()) }
            .launchIn(lifecycleScope)

        viewModel.imageQualityThreshold.observe(viewLifecycleOwner){
            binding.tvImageQualityThreshold.text = getString(R.string.setting_image_quality_threshold, it)
            binding.sbImageQualityThreshold.setProgress(it, true)
        }
        binding.sbImageQualityThreshold.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.updateImageQualityThreshold(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

        viewModel.generalProgress.observe(viewLifecycleOwner) {
            when (it) {
                ProgressStatus.LOADING -> {
                    binding.generalProgress.visible()
                    binding.btnExport.gone()
                    binding.btnImport.gone()
                }

                else -> {
                    binding.generalProgress.gone()
                    binding.btnExport.visible()
                    binding.btnImport.visible()
                }
            }
        }

        binding.llAdministrator.setOnClickListener {
            navigator.openAdminsFragment(requireActivity())
        }

        binding.btnExport.setOnClickListener {
            ExportDataSelectionDialog(getString(R.string.setting_export_message), object : ExportDataSelectionDialog.Listener {
                override fun onPositiveClicked(data: ExportDataSelectionDialog.Selection) {
                    viewModel.exportUserData(
                        saveUsers = data.saveUsers,
                        saveEvents = data.saveEvents,
                        saveClasses = data.saveClasses,
                        saveLessons = data.saveLessons,
                    )
                }
            }).show(parentFragmentManager, ExportDataSelectionDialog.TAG)
        }

        viewModel.exportData.observe(viewLifecycleOwner) {
            requireContext().shareData(it)
        }

        binding.btnImport.setOnClickListener {
            showFileChooser()
        }

    }

    private fun showFileChooser() {
        val intent = Intent()
        intent.type = "*/*"
        intent.action = Intent.ACTION_GET_CONTENT
//        intent.putExtra("requestCode", FOLDERPICKER_CODE)
        getDBFile.launch(intent)
    }

}
