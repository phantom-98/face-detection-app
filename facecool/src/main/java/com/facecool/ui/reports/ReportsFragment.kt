package com.facecool.ui.reports

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.facecool.R
import com.facecool.databinding.FragmentReportsBinding
import com.facecool.ui.BaseFragment
import com.facecool.ui.students.common.ViewWithTitle
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReportsFragment : BaseFragment<FragmentReportsBinding>(FragmentReportsBinding::inflate) {

    override fun getTitle(): String = getTitle(requireContext())

    override fun getTitle(context: Context) = context.getString(R.string.report_screen_title)

    private val viewModel: ReportsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ReportPageAdapter(requireActivity())
        binding.reportsViewPager.adapter = adapter
        TabLayoutMediator(binding.reportsTabLayout, binding.reportsViewPager) { tab, position ->
            tab.text = (adapter.fragments[position] as? ViewWithTitle?)?.getTitle(view.context)
        }.attach()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }
    override fun onDestroyView() {
        super.onDestroyView()
//        activity?.let {
//            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
//        }
    }
}
