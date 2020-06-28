package com.facecool.ui.login

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import com.facecool.R
import com.facecool.databinding.ActivityLoginBinding
import com.facecool.navigation.NavigatorContract
import com.facecool.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class LogInActivity : BaseActivity() {

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, LogInActivity::class.java)
            context.startActivity(starter)
        }
    }

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LogInViewModel by viewModels()

    @Inject
    lateinit var navigator: NavigatorContract

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.errorEvent.observe(this){
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        viewModel.loginSuccess.observe(this) {
            navigator.startMainActivity(this)
            finish()
        }

        viewModel.loginButtonStatus.observe(this) {
            binding.btnLogInLogin.setState(it)
        }

        binding.tvLogInAnonymousSingUpPrompt.setOnClickListener {
            navigator.startMainActivity(this)
            finish()
        }

        binding.tvLogInSingUpPrompt.setOnClickListener {
            navigator.startSingUpActivity(this)
            finish()
        }

        binding.btnLogInLogin.setText(getString(R.string.login_login_title))
        binding.btnLogInLogin.setOnClickListener {
            viewModel.onLogIn()
        }

        binding.ivLogInEmailEditText.addTextChangedListener {
            it?.let { editable ->
                viewModel.onEmailUpdate(editable.toString())
            }
        }

        binding.ivLogInPasswordEditText.addTextChangedListener {
            it?.let { editable ->
                viewModel.onPasswordUpdate(editable.toString())
            }
        }

        binding.tvLogInAnonymousSingUpPrompt.paintFlags =
            binding.tvLogInAnonymousSingUpPrompt.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.tvLogInSingUpPrompt.paintFlags =
            binding.tvLogInSingUpPrompt.paintFlags or Paint.UNDERLINE_TEXT_FLAG

    }

}
