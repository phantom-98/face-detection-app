package com.facecool.ui.login

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import com.facecool.R
import com.facecool.databinding.ActivitySingUpBinding
import com.facecool.navigation.NavigatorContract
import com.facecool.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SingUpActivity : BaseActivity() {

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, SingUpActivity::class.java)
            context.startActivity(starter)
        }
    }

    @Inject
    lateinit var navigator: NavigatorContract

    private lateinit var binding: ActivitySingUpBinding

    private val viewModel: SingUpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSingUp.setText(getString(R.string.login_signup_title))
        binding.btnSingUp.setOnClickListener {
            viewModel.singUp()
        }
        binding.tvSungUpSingUpPrompt.paintFlags = binding.tvSungUpSingUpPrompt.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        binding.tvSungUpSingUpPrompt.setOnClickListener {
            navigator.startLogInActivity(this@SingUpActivity)
            finish()
        }

        binding.etEmail.addTextChangedListener {
            viewModel.updateEmail(it.toString())
        }

        binding.etPassword.addTextChangedListener{
            viewModel.updatePassword(it.toString())
        }

        binding.etRepeatPassword.addTextChangedListener {
            viewModel.updateRepeatPassword(it.toString())
        }

        viewModel.registerButtonStatus.observe(this){
            binding.btnSingUp.setState(it)
        }

        viewModel.errorMessage.observe(this){
            binding.ivSungUpRepeatPassword.error = it
        }

        viewModel.onSuccess.observe(this) {
            navigator.openTermsAndConditionsActivity(this)
        }

    }

}
