package com.facecool.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.facecool.R
import com.facecool.databinding.ActivityOnBoardingBinding
import com.facecool.navigation.NavigatorContract
import com.facecool.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnBoardingActivity : BaseActivity() {

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, OnBoardingActivity::class.java)
            context.startActivity(starter)
        }
    }

    @Inject
    lateinit var navigator: NavigatorContract

    private lateinit var binding: ActivityOnBoardingBinding
    private val onBoardingPageAdapter = OnBoardingPageAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.onBoardingViewPager.adapter = onBoardingPageAdapter
        binding.dotsIndicator.attachTo(binding.onBoardingViewPager)
        binding.onBoardingViewPager.registerOnPageChangeCallback(object :
            androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 2) {
                    binding.nextButton.text = getString(R.string.onboarding_next_button_go)
                } else {
                    binding.nextButton.text = getString(R.string.onboarding_next_button_next)
                }
            }
        })
        binding.nextButton.setOnClickListener {
            if (binding.onBoardingViewPager.currentItem == 2) {
                navigator.startMainActivity(this)
                finish()
            } else {
                binding.onBoardingViewPager.currentItem =
                    binding.onBoardingViewPager.currentItem + 1
            }
        }
    }

}