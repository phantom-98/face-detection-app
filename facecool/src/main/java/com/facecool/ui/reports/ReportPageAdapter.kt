package com.facecool.ui.reports

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.facecool.ui.reports.byclass.ReportsByClassFragment
import com.facecool.ui.reports.bystudent.ReportsByStudentFragment
import com.facecool.ui.reports.history.HistoryLogFragment
import com.facecool.ui.reports.yxl.ReportsYXLFragment

class ReportPageAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    val fragments = listOf<Fragment>(
        HistoryLogFragment(),
        ReportsByClassFragment(),
        ReportsYXLFragment(),
        ReportsByStudentFragment(),
    )

    override fun getItemCount() = fragments.size
    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}