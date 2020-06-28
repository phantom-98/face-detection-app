package com.facecool.ui.onboarding

import android.os.Bundle
import android.view.View
import com.facecool.R
import com.facecool.databinding.FragmentOnBoardingBinding
import com.facecool.ui.BaseFragment

class OnBoardingFragmentTwo : BaseFragment <FragmentOnBoardingBinding>(FragmentOnBoardingBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.titleTextFragment.text = getString(R.string.onboarding_second_fragment_title)
        binding.textDescriptionFragment.text = getString(R.string.onboarding_second_fragment_description)
        binding.backgroundImage.setImageDrawable(resources.getDrawable(R.drawable.onboarding_page_2_backgrownd))
        binding.centerImage.setImageDrawable(resources.getDrawable(R.drawable.onboarding_page_2_image))
    }

    override fun getTitle(): String = ""
}