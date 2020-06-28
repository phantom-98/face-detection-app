package com.facecool.ui.camera

import android.Manifest
import android.animation.AnimatorSet
import android.app.appsearch.AppSearchResult.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.setPadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.facecool.R
import com.facecool.common.AppLogger
import com.facecool.databinding.FragmentCameraBinding
import com.facecool.navigation.NavigatorContract
import com.facecool.ui.BaseFragment
import com.facecool.ui.MainActivity
import com.facecool.ui.camera.CameraMode.IDLE
import com.facecool.ui.camera.CameraMode.RECORDING
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.camera.businesslogic.events.EventModeModel
import com.facecool.ui.camera.businesslogic.events.EventModel
import com.facecool.ui.camera.camerax.CameraManager
import com.facecool.ui.camera.face_detection.FaceContourGraphic
import com.facecool.ui.classes.add.time.LessonModel
import com.facecool.ui.common.CameraMode
import com.facecool.ui.common.FaceCoolDialog
import com.facecool.ui.common.FaceCoolInputDialog
import com.facecool.ui.common.FaceCoolLessonSelectAdapter
import com.facecool.ui.common.FaceCoolLessonSelectDialog
import com.facecool.ui.common.unknownUserImage
import com.facecool.ui.students.common.EnrollmentStatus
import com.facecool.utils.Notify
import com.facecool.utils.gone
import com.facecool.utils.startRecordAnimation
import com.facecool.utils.visible
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.timerTask


@AndroidEntryPoint
class CameraFragment : BaseFragment<FragmentCameraBinding>(FragmentCameraBinding::inflate),
    CameraDetectionAdapter.Listener, FaceDetectorCallback, CameraModeListener, FaceCoolLessonSelectAdapter.Listener {

    @Inject
    lateinit var navigator: NavigatorContract

    @Inject
    lateinit var logger: AppLogger

    private val viewModel: CameraViewModel by viewModels()

    private var latestDetectedModel: CameraDetectionModel? = null
    private var latestDetectedFace: Bitmap? = null

    private val adapter = CameraDetectionAdapter(this)

    private var cameraManager: CameraManager? = null

    private var overlayData = hashMapOf<Int, String>()

    private var anim: AnimatorSet? = null

    private var isUserAction: Boolean = false

    private var dialog: DialogFragment? = null

    private val settingLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            var rotation: Int?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                rotation = context?.display?.rotation
            }else {
                rotation = (context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager)?.defaultDisplay?.rotation
            }
            cameraManager?.changeCameraSelector(rotation?:0)
            binding.btnCameraPermissionReRequest.visibility = View.GONE
        } else {
            binding.btnCameraPermissionReRequest.visibility = View.VISIBLE
        }
    }

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        Log.d("SMS Test", "Permission Granted : ${it}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        viewModel.cleanCachedData()
        viewModel.cacheFaces()
        viewModel.loadLivenessVariables()

        Timer().schedule(timerTask{
            viewModel.onStartHandler()
        }, 300)
    }

    override fun onPause() {
        super.onPause()
        viewModel.clearCachedFace()
        latestDetectedModel = null
        latestDetectedFace = null
    }

    fun forceSaveStates(){
        viewModel.onCloseHandler()
    }

    override fun isCameraScreen() = true

    fun showSnackBar(msg: String, long: Boolean){
        var snackbar = Snackbar.make(binding.snackbarContainer, msg, if (long) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT)
        snackbar.setTextColor(Color.DKGRAY)
        snackbar.setBackgroundTint(Color.WHITE)
//        (snackbar.view as Snackbar.SnackbarLayout).background =
        snackbar.show()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
        (activity as MainActivity).cameraFragment = this

        binding.btnCameraPermissionReRequest.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireActivity().packageName, null)
            intent.data = uri
            settingLauncher.launch(intent)
        }

        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        createCameraManager()

        var viewStartTimer: View
        var ivRecordIndicator: ImageView
        var tvRecordInfo: TextView
        var spinnerNoClass: View
        var spinnerClass: Spinner
        var switchMode: SwitchCompat
        var inOutText: TextView

        if (resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
            viewStartTimer = binding.viewStartTimer!!
            ivRecordIndicator = binding.ivRecordIndicator!!
            tvRecordInfo = binding.tvRecordInfo!!
            spinnerNoClass = binding.spinnerNoClass!!
            spinnerClass = binding.spinnerClass!!
            switchMode = binding.switchMode!!
            inOutText = binding.inOutText!!
        }else{
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//            WindowCompat.getInsetsController(requireActivity().window, requireActivity().window.decorView).hide(WindowInsetsCompat.Type.systemBars())
            val app = (activity as MainActivity)
            viewStartTimer = app.findViewById(R.id.view_start_timer)
            ivRecordIndicator = app.findViewById(R.id.iv_record_indicator)
            tvRecordInfo = app.findViewById(R.id.tv_record_info)
            spinnerNoClass = app.findViewById(R.id.spinner_no_class)
            spinnerClass = app.findViewById(R.id.spinner_class)
            switchMode = app.findViewById(R.id.switch_mode)
            inOutText = app.findViewById(R.id.in_out_text)
        }

        viewStartTimer.setOnClickListener {
            viewModel.toggleCameraRecord()
            viewModel.saveCameraMode()
        }

        viewModel.classes.observe(viewLifecycleOwner){
            if (it?.size!!>0) {
                viewModel.getClasses(it)
            }
            viewModel.setupClassVisibility()
        }

        viewModel.kiosk.observe(viewLifecycleOwner){
            it.getValueIfNotUsed()?.let {
                if (it) {
                    kioskMode()
                } else {
                    dialog?.let {
                        if (it.isVisible) {
                            if (it is FaceCoolDialog) return@observe
                            else it.dismiss()
                        }
                    }
                    dialog = FaceCoolDialog(
                        getString(R.string.camera_screen_switch_to_hand_mode_question),
                        object : FaceCoolDialog.Listener {
                            override fun onPositiveClicked() {
                                handMode()
                            }

                            override fun onNegativeClicked() {
                                viewModel.requestChangeMode = false
                            }
                        })
                    dialog?.show(childFragmentManager, FaceCoolDialog.TAG)
                }
            }
        }
        viewModel.autoKiosk.observe(viewLifecycleOwner){
            it?.let {
                if (it) kioskMode()
                else handMode()
            }
        }
        viewModel.askToAdminDialog.observe(viewLifecycleOwner){
            it.getValue()?.let {
                Log.d("KioskModeTest", "Before dialog")
                dialog?.let { if (it.isVisible) return@observe }
                Log.d("KioskModeTest", "After dialog")
                dialog = FaceCoolDialog(
                        getString(R.string.camera_screen_enroll_yourself_as_administrator),
                        object : FaceCoolDialog.Listener{
                            override fun onPositiveClicked() {
                                latestDetectedFace?.let {
                                    navigator.openAddNewAdminActivity(this@CameraFragment.requireActivity(), it)
                                }
                            }

                            override fun onNegativeClicked() {
                                viewModel.requestChangeMode = false
                            }

                            override fun onDismissed() {
                                super.onDismissed()
                                Log.d("KioskModeTest", "Closed dialog")
                            }
                        }
                    )
                dialog?.show(childFragmentManager, FaceCoolDialog.TAG)
            }
        }
        viewModel.askPin.observe(viewLifecycleOwner){
            it.getValue()?.let{
                dialog?.let { if (it.isVisible) return@observe }
                dialog = FaceCoolInputDialog(
                    getString(R.string.camera_screen_ask_admin_pin),
                    "",
                    true,
                    object : FaceCoolInputDialog.Listener{
                        override fun onPositiveClicked(newData: String?) {
                            if (newData.equals(it)) {
                                handMode()
                            } else {
                                showSnackBar(getString(R.string.camera_screen_wrong_pin), false)
                            }
                        }
                    }
                )
                dialog?.show(childFragmentManager, FaceCoolInputDialog.TAG)
            }

        }

        viewModel.cameraMode.observe(viewLifecycleOwner) {
            it.getValue()?.let {
                cameraManager?.setMode(it)
                when (it) {
                    RECORDING -> {
                        anim?.cancel()
                        anim = ivRecordIndicator.startRecordAnimation()
                        tvRecordInfo.text = getString(R.string.stop_tracking)
                    }

                    else -> {
                        anim?.cancel()
                        tvRecordInfo.text = getString(R.string.start_tracking)
                    }
                }
            }
        }

        viewModel.generalProgress.observe(viewLifecycleOwner) {
            it.getValueIfNotUsed()?.let {
                if (it)
                    binding.generalProgress.visible()
                else
                    binding.generalProgress.gone()
            }
        }

        viewModel.latestDetectedData.observe(viewLifecycleOwner) { latestEvent ->
            val it = latestEvent.getValueIfNotUsed() ?: return@observe
            binding.ivLatestDetectedFace.root.visible()
            binding.ivLatestDetectedFaceDatabase.root.visible()
            binding.tvLatestIdentifiedInformation.visible()

            binding.ivLatestDetectedFace.imageCameraDetection.setImageBitmap(it.detectedFaceImage)
            binding.ivLatestDetectedFaceDatabase.imageCameraDetection.setImageBitmap(it.databaseMatchingImage)
            binding.tvLatestIdentifiedInformation.text = it.name
            latestDetectedModel = it.data

//            if (viewModel.KioskMode){
//                if ( timerEraseLatestFace != null ) {
//                    timerEraseLatestFace?.cancel()
//                    timerEraseLatestFace = null
//                }
//                timerEraseLatestFace = Timer("EraseLatestFace")
//                timerEraseLatestFace?.schedule(
//                    timerTask {
//                        Handler(Looper.getMainLooper()).post(Runnable {
//                            try {
////                                latestDetectedModel = null
//                                binding.ivLatestDetectedFace.root.setCardBackgroundColor(Color.WHITE)
//                                binding.ivLatestDetectedFaceDatabase.root.setCardBackgroundColor(Color.WHITE)
//                                binding.tvLatestIdentifiedInformation.setTextColor(Color.WHITE)
//                                binding.tvLatestIdentifiedInformation.setText("")
//                                binding.ivLatestDetectedFace.imageCameraDetection.setImageBitmap(
//                                    unknownUserImage(requireContext())
//                                )
//                                binding.ivLatestDetectedFaceDatabase.imageCameraDetection.setImageBitmap(
//                                    unknownUserImage(requireContext())
//                                )
//                            }catch (e: Exception){}
//                        })
//                    },
//                    5000
//                )
//            }

            when (it.enrollmentStatus) {
                EnrollmentStatus.ENROLLED -> {
                    binding.ivLatestDetectedFace.root.setCardBackgroundColor(Color.GREEN)
                    binding.ivLatestDetectedFaceDatabase.root.setCardBackgroundColor(Color.GREEN)
                    binding.tvLatestIdentifiedInformation.setTextColor(Color.GREEN)
                    binding.ivLatestDetectedFaceDatabase.root.setOnClickListener { v ->
                        if (!isEnable) return@setOnClickListener
                        it.data?.id?.let { it1 ->
                            viewModel.updateFlag()
                            if ( it.data?.creationTime == 0L){
                                navigator.openAdminDetailsActivity(this@CameraFragment.requireActivity(), it1)
                            } else {
                                navigator.openStudentDetailsActivity(
                                    this@CameraFragment.requireActivity(),
                                    it1
                                )
                            }
                        }

                    }
                }
                EnrollmentStatus.REJECTED,
                EnrollmentStatus.UNKNOWN -> {
                    binding.ivLatestDetectedFace.root.setCardBackgroundColor(Color.WHITE)
                    binding.ivLatestDetectedFaceDatabase.root.setCardBackgroundColor(Color.WHITE)
                    binding.tvLatestIdentifiedInformation.setTextColor(Color.WHITE)
                    binding.ivLatestDetectedFaceDatabase.root.setOnClickListener { v ->
                        if (!isEnable) return@setOnClickListener
                        it.data?.let { it1 ->
                            viewModel.updateFlag()
                            navigator.openAddNewStudentActivity(
                                this@CameraFragment.requireActivity(),
                                it1
                            )
                        }
                    }
                }
            }

        }

        viewModel.latestEvents.observe(viewLifecycleOwner) {
            viewModel.processEvents(it)
        }

        viewModel.overlayData.observe(viewLifecycleOwner) {
            it.getValueIfNotUsed()?.let {
                overlayData = it
            }
        }

        viewModel.onDetectedUser.observe(viewLifecycleOwner) {
            it.getValueIfNotUsed()?.let {
                viewModel.updateFlag()
                navigator.openStudentDetailsActivity(this@CameraFragment.requireActivity(), it)
            }
        }
        viewModel.onUnDetectedUser.observe(viewLifecycleOwner) {
            it.getValueIfNotUsed()?.let {
                viewModel.updateFlag()
                navigator.openAddNewStudentActivity(this@CameraFragment.requireActivity(), it)
            }
        }

        binding.rvDetectedFacesList.layoutManager = object: LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false){
            override fun canScrollHorizontally(): Boolean {
                return isEnable
            }
        }
        binding.rvDetectedFacesList.adapter = adapter

        spinnerNoClass.setOnClickListener {
            dialog?.let { if (it.isVisible) return@setOnClickListener }
            dialog = FaceCoolDialog(
                getString(R.string.add_new_class_title),
                object : FaceCoolDialog.Listener {
                    override fun onPositiveClicked() {
                        viewModel.updateFlag()
                        navigator.openAddNewClassActivity(this@CameraFragment.requireActivity())
                    }
                }
            )
            dialog?.show(parentFragmentManager, FaceCoolDialog.TAG)
        }

        viewModel.spinnerNoClassView.observe(viewLifecycleOwner) {
            it.getValueIfNotUsed()?.let {
                when (it) {
                    Visibility.VISIBLE -> spinnerNoClass.visibility = View.VISIBLE
                    Visibility.GONE -> spinnerNoClass.visibility = View.GONE
                }
            }
        }
        viewModel.spinnerClassView.observe(viewLifecycleOwner) {
            it.getValueIfNotUsed()?.let {
                when (it) {
                    Visibility.VISIBLE -> spinnerClass.visibility = View.VISIBLE
                    Visibility.GONE -> spinnerClass.visibility = View.GONE
                }
            }
        }

        viewModel.classList.observe(viewLifecycleOwner) {
            it.getValueIfNotUsed()?.let {
                var list = mutableListOf<String>()
                list.add("")
                list.addAll(it)
                val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_custom_class, list)
                spinnerClass.adapter = spinnerAdapter
            }
        }

        viewModel.spinnerPosition.observe(viewLifecycleOwner){
            it.getValue()?.let {
                isUserAction = false
                spinnerClass.setSelection(it)
            }
        }

        spinnerClass.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0 ) {
                    isUserAction = true
                    return
                }//auto select
                viewModel.onClassSelectedAtPosition(position-1, isUserAction)
//                if(isUserAction)
                    viewModel.saveSpinnerPosition(position)
                isUserAction = true
            }
        }

        switchMode.setOnClickListener {
            val isChecked = switchMode.isChecked
            switchMode.isChecked = !isChecked
            dialog?.let { if (it.isVisible) return@setOnClickListener }
            dialog = FaceCoolDialog(
                getString(R.string.camera_screen_dialog_switch_in_out_mode_body),
                object : FaceCoolDialog.Listener {
                    override fun onPositiveClicked() {
                        if (isChecked) {
                            viewModel.updateEventMode(EventModeModel.OUT)
                        } else {
                            viewModel.updateEventMode(EventModeModel.IN)
                        }
                        viewModel.saveInOutState()
                    }
                }
            )
            dialog?.show(parentFragmentManager, FaceCoolDialog.TAG)

        }
        viewModel.eventMode.observe(viewLifecycleOwner){
            it.getValue()?.let {
                switchMode.isChecked = it
                inOutText.text = if (it) getString(R.string.camera_screen_dialog_switch_out_mode_button) else getString(R.string.camera_screen_dialog_switch_in_mode_button)
            }
        }

        switchMode.setOnCheckedChangeListener { _, _ -> }

        viewModel.cameraDetectionList.observe(viewLifecycleOwner) {
            it.getValueIfNotUsed()?.let {
                adapter.setItems(it)
            }
        }
        viewModel.lessonSelect.observe(viewLifecycleOwner) {
            it.getValueIfNotUsed()?.let {
                dialog?.let { if (it.isVisible) return@observe }
                dialog = FaceCoolLessonSelectDialog(it, this)
                dialog?.show(childFragmentManager, FaceCoolLessonSelectDialog.TAG)
            }
        }

        viewModel.alert.observe(viewLifecycleOwner) {
            it.getValue()?.let {
                    when (it) {
                        0 -> {
                            showSnackBar(getString(R.string.camera_screen_lesson_started), true)
                        }
                        1 -> {
                            showSnackBar(getString(R.string.camera_screen_lesson_ended), true)
                        }
                        2 -> {
                            showSnackBar(getString(R.string.camera_screen_admin_detected), true)
                        }
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.cancelTimer()
//        activity?.let {
//            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
//        }
    }

    override fun onDestroy() {
        cameraManager?.let { it.stopCamera() }
//        viewModel.saveCameraMode(false)
        super.onDestroy()
    }

    override fun getTitle(): String {
        return getTitle(requireActivity())
    }

    override fun getTitle(context: Context): String {
        return context.getString(R.string.camera_screen_title)
    }

    private var isEnable = true

    fun setEnableListener(enable: Boolean){
        isEnable = enable
    }

    override fun onEventItemClicked(item: EventModel) {
        if (isEnable) viewModel.onEventItemClicked(item)
    }

    override fun onFaceDetected(results: List<Face>, toBitmap: Bitmap) {
        latestDetectedFace = null
        latestDetectedFace = toBitmap.copy(toBitmap.config, false)
        viewModel.onFaceDetected(results, toBitmap)
        if (results.isEmpty()) latestDetectedModel = null
    }

    override fun onFaceDetected(results: Face, toBitmap: Bitmap) {
//        viewModel.onFaceDetected(results, toBitmap)
    }

    override fun onOutlineDetected(results: List<Face>, rect: Rect) {
        try {
            nullableBinding?.graphicOverlayFinder?.clear()
            if (viewModel.getCameraMode() == IDLE || results.isEmpty()) return
            val localData = overlayData
            val overlay = results.mapNotNull {
                nullableBinding?.graphicOverlayFinder?.let { overlay ->
                    val text = localData[it.trackingId]
                    val isEnrr = text?.contains(",") ?: false// TODO, improve performance on this by providing the boolean directly with no check of contains
                    FaceContourGraphic(
                        overlay,
                        it,
                        rect,
                        text,
                        cameraManager?.getCameraFacing(),
                        isEnrr,
                        viewModel.getCache(),
                        this
                    )
                }
            }
            overlay.forEach { nullableBinding?.graphicOverlayFinder?.add(it) }
            nullableBinding?.graphicOverlayFinder?.postInvalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createCameraManager() {
        cameraManager = CameraManager(
            this.requireContext(),
            binding.previewViewFinder,
            this,
            this,
            binding.graphicOverlayFinder
        )
    }

    override fun changeModeTo(mode: CameraMode?) {
        viewModel.cleanCachedData()
        overlayData.clear()

        var rotation: Int?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            rotation = context?.display?.rotation
        }else {
            rotation = (context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager)?.defaultDisplay?.rotation
        }
        if (mode == null)
            cameraManager?.changeCameraSelector(rotation?:0)
        else cameraManager?.changeCameraSelector(mode, rotation?:0)
    }

    fun requestUpdateKioskMode(){
        if (viewModel.KioskMode){
            if (latestDetectedModel?.creationTime == 0L){
                dialog?.let { if (it.isVisible) return }

                dialog = FaceCoolDialog(
                    getString(R.string.camera_screen_switch_to_hand_mode_question),
                    object : FaceCoolDialog.Listener {
                        override fun onPositiveClicked() {
                            handMode()
                        }

                        override fun onNegativeClicked() {
                            viewModel.requestChangeMode = false
                        }
                    })
                dialog?.show(childFragmentManager, FaceCoolDialog.TAG)
                return
            }
            viewModel.requestChangeMode = true
            /*latestDetectedModel?.let {
                viewModel.handleFaceForKiosk(it)
            }*/
        } else {
            dialog?.let { if (it.isVisible) return }
            dialog = FaceCoolDialog(
                getString(R.string.camera_screen_switch_to_kiosk_mode_question),
                object : FaceCoolDialog.Listener {
                    override fun onPositiveClicked() {
                        changeModeTo(CameraMode.KIOSK)
                        viewModel.requestChangeMode = true
//                        latestDetectedModel?.let {
//                            viewModel.handleFaceForKiosk(it)
//                        }
                    }

                    override fun onNegativeClicked() {
                        viewModel.requestChangeMode = false
                    }
                })
            dialog?.show(childFragmentManager, FaceCoolDialog.TAG)
        }
    }

    fun kioskMode(){
        dialog?.let { if (it.isVisible) it.dismiss() }
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        viewModel.KioskMode = true
        viewModel.requestChangeMode = false
        (requireActivity() as MainActivity).setKioskMode()
        viewModel.saveKioskMode()
        changeModeTo(CameraMode.KIOSK)
        showSnackBar(getString(R.string.camera_screen_kiosk_mode_started), true)
    }

    fun handMode(){
        dialog?.let { if (it.isVisible) it.dismiss() }
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        viewModel.KioskMode = false
        viewModel.requestChangeMode = false
        (requireActivity() as MainActivity).setHandMode()
        viewModel.saveKioskMode()
        viewModel.countDown = 5
        showSnackBar(getString(R.string.camera_screen_hand_mode_started), true)
    }

    override fun onStartButtonClicked(item: LessonModel) {
        viewModel.adjustLesson(item)
    }
}

interface FaceDetectorCallback {
    fun onFaceDetected(results: List<Face>, toBitmap: Bitmap)
    fun onFaceDetected(results: Face, toBitmap: Bitmap)
    fun onFacesDetected(results: List<Face>, toBitmap: Bitmap, data: Map<Int, String>) {}
    fun onOutlineDetected(results: List<Face>, rect: Rect)
    fun onError(e: java.lang.Exception) {
        e.printStackTrace()
    }
}

interface CameraModeListener {
    fun changeModeTo(mode: CameraMode? = null)
}
