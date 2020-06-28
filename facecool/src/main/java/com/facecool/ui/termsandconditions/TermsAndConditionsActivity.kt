package com.facecool.ui.termsandconditions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.facecool.R
import com.facecool.databinding.ActivityTermsAndConditionsBinding
import com.facecool.navigation.NavigatorContract
import com.facecool.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TermsAndConditionsActivity : BaseActivity() {

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, TermsAndConditionsActivity::class.java)
            context.startActivity(starter)
        }
    }

    @Inject
    lateinit var navigator: NavigatorContract

    private lateinit var binding: ActivityTermsAndConditionsBinding

    private val viewModel: TermsAndConditionsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsAndConditionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.genericToolbar.toolbar)
        supportActionBar?.title = getString(R.string.terms_and_conditions)
        
        binding.txtTermsAndConditions.setText(getString(R.string.terms_and_conditions_body))

        binding.btnAccept.setText(getString(R.string.dialog_alert_accept_button))
        binding.btnAccept.setOnClickListener {
            viewModel.acceptTermsAndConditions()
        }

        viewModel.termsAndConditions.observe(this){
            navigator.openOnBoardingActivity(this)
        }

    }

}
