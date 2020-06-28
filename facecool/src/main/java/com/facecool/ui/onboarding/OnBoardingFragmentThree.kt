package com.facecool.ui.onboarding

import android.os.Bundle
import android.view.View
import com.facecool.R
import com.facecool.databinding.FragmentOnBoardingBinding
import com.facecool.ui.BaseFragment

class OnBoardingFragmentThree : BaseFragment <FragmentOnBoardingBinding>(FragmentOnBoardingBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.titleTextFragment.text = getString(R.string.onboarding_third_fragment_title)
        binding.textDescriptionFragment.text = getString(R.string.onboarding_third_fragment_description)
        binding.backgroundImage.setImageDrawable(resources.getDrawable(R.drawable.onboarding_page_3_backgrownd))
        binding.centerImage.setImageDrawable(resources.getDrawable(R.drawable.onboarding_page_3_image))
    }

    override fun getTitle(): String = ""
}