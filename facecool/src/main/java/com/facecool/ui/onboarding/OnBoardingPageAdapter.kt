package com.facecool.ui.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnBoardingPageAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity){

    val fragments: List<Fragment> = listOf(
        OnBoardingFragmentOne(),
        OnBoardingFragmentTwo(),
        OnBoardingFragmentThree()
    )

    override fun getItemCount() = fragments.size


    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

}