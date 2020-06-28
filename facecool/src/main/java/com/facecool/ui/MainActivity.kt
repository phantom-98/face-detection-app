package com.facecool.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.facecool.R
import com.facecool.databinding.ActivityMainBinding
import com.facecool.navigation.NavigatorContract
import com.facecool.ui.camera.CameraFragment
import com.facecool.ui.camera.CameraModeListener
import com.facecool.ui.common.CameraMode
import com.facecool.ui.common.FaceCoolDialog
import com.facecool.ui.students.common.ToolbarListener
import com.facecool.ui.students.common.ViewWithTitle
import com.google.android.material.navigation.NavigationView
import com.google.firebase.appdistribution.InterruptionLevel
import com.google.firebase.appdistribution.ktx.appDistribution
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
    ToolbarListener {

    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityMainBinding

//    private var mode = CameraMode.MANUAL

//    private val viewModel: MainViewModel by viewModels()

//    var confirmDialog = false

//    fun getCameraMode() = mode

    @Inject
    lateinit var navigator: NavigatorContract

    private fun getLatestVisibleFragment(): Fragment? {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragments: List<Fragment> = fragmentManager.fragments
        for (i in fragments.indices.reversed()) {
            val fragment: Fragment = fragments[i]
            if (fragment.isVisible) {
                return fragment
            }
        }
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Firebase.appDistribution.showFeedbackNotification("Firebase Feedback", InterruptionLevel.HIGH)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbarContainer.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarContainer.cameraInfoContainer.visibility = View.VISIBLE
        actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            binding.myDrawerLayout,
            binding.toolbarContainer.toolbar,
            R.string.nav_open,
            R.string.nav_close
        )

        actionBarDrawerToggle.isDrawerIndicatorEnabled = true
        actionBarDrawerToggle.syncState()

        binding.myDrawerLayout.addDrawerListener(actionBarDrawerToggle)
        binding.navigationView.setNavigationItemSelectedListener(this)

        supportFragmentManager.addOnBackStackChangedListener {
            val title = (getLatestVisibleFragment() as? ViewWithTitle)?.getTitle(this) ?: ""
            val isCameraScreen =
                (getLatestVisibleFragment() as? ViewWithTitle)?.isCameraScreen() ?: false
            provideToolbarTitle(title)
            if (isCameraScreen) {
                isCameraScreenVisible()
            }
        }

        if (savedInstanceState==null) {
            navigator.openCameraFragment(this)
//            this.mode = CameraMode.MANUAL
            binding.toolbarContainer.cvCameraMode.setCardBackgroundColor(Color.WHITE)
            binding.toolbarContainer.tvKioskModeSwitcher.setTextColor(Color.GREEN)
            binding.toolbarContainer.tvKioskModeSwitcher.text = getString(R.string.camera_hand_mode_label)
            val fragment = getLatestVisibleFragment() as? CameraModeListener
            if((getLatestVisibleFragment() as? ViewWithTitle)?.isCameraScreen() ?: false)
                fragment?.changeModeTo()
        }


        binding.toolbarContainer.cvCameraMode.setOnClickListener {
            cameraFragment?.changeModeTo()
        }
        binding.toolbarContainer.cvKioskMode.setOnClickListener {
            cameraFragment?.requestUpdateKioskMode()
        }
    }

    var cameraFragment: CameraFragment? = null
    fun setKioskMode(){
        startLockTask()
        binding.myDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        setEnableViews(false)
        binding.toolbarContainer.cvKioskMode.setCardBackgroundColor(Color.GREEN)
        binding.toolbarContainer.tvKioskModeSwitcher.setTextColor(Color.WHITE)
        binding.toolbarContainer.tvKioskModeSwitcher.text = getString(R.string.camera_kiosk_mode_label)
    }

    fun setHandMode(){
        stopLockTask()
        binding.myDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        setEnableViews(true)
        binding.toolbarContainer.cvKioskMode.setCardBackgroundColor(Color.WHITE)
        binding.toolbarContainer.tvKioskModeSwitcher.setTextColor(Color.GREEN)
        binding.toolbarContainer.tvKioskModeSwitcher.text = getString(R.string.camera_hand_mode_label)
    }

    fun setEnableViews( enabled: Boolean) {
        binding.toolbarContainer.cvCameraMode.isEnabled = enabled
        binding.toolbarContainer.spinnerClass?.isEnabled = enabled
        binding.toolbarContainer.spinnerNoClass?.isEnabled = enabled
        binding.toolbarContainer.switchMode?.isEnabled = enabled
        binding.toolbarContainer.viewStartTimer?.isEnabled = enabled
        cameraFragment?.binding?.spinnerClass?.isEnabled = enabled
        cameraFragment?.binding?.spinnerNoClass?.isEnabled = enabled
        cameraFragment?.binding?.switchMode?.isEnabled = enabled
        cameraFragment?.binding?.viewStartTimer?.isEnabled = enabled
        cameraFragment?.setEnableListener(enabled)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraFragment?.forceSaveStates()
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        binding.myDrawerLayout.closeDrawer(GravityCompat.START)
        binding.toolbarContainer.cameraInfoContainer.visibility = View.GONE
        when (item.itemId) {
            R.id.live_camera -> {
                navigator.openCameraFragment(this)
                binding.toolbarContainer.cameraInfoContainer.visibility = View.VISIBLE
            }

            R.id.nav_classes -> navigator.openClassesFragment(this)
            R.id.nav_students -> navigator.openStudentsFragment(this)
            R.id.nav_reports -> navigator.openReportsFragment(this)
            R.id.nav_settings -> navigator.openSettingsFragment(this)
            R.id.nav_help -> navigator.openHelpFragment(this)
//            R.id.nav_profile -> navigator.openProfileFragment(this)
        }

        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun provideToolbarTitle(title: String) {
        supportActionBar?.title = title
        binding.toolbarContainer.cameraInfoContainer.visibility = View.GONE
    }

    override fun isCameraScreenVisible() {
        binding.toolbarContainer.cameraInfoContainer.visibility = View.VISIBLE
    }

}


