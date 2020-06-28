package com.facecool.ui.slpash

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.facecool.navigation.NavigatorContract
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InitialActivity : AppCompatActivity() {

    @Inject
    lateinit var navigator: NavigatorContract

    private val viewModel: InitialViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

//        viewModel.getInitialNavigation()

        navigator.startMainActivity(this)
        finish()

        viewModel.navigation.observe(this) {
            when (it) {
                InitialViewModel.MAIN -> {
                    navigator.startMainActivity(this)
                    finish()
                }
                InitialViewModel.LOG_IN -> {
                    navigator.startLogInActivity(this)
                    finish()
                }
            }
        }

    }

}
