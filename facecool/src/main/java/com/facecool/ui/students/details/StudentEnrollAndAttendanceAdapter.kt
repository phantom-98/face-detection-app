package com.facecool.ui.students.details

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.facecool.ui.reports.bystudent.reports.StudentReportFragment

class StudentEnrollAndAttendanceAdapter (fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    val fragments = listOf<Fragment>(
        StudentEnrolledClassesFragment(),
        StudentReportFragment()
    )

//    fun setFragments(f: MutableList<Fragment>){
//        fragments.clear()
//        fragments.addAll(f)
//    }
    override fun getItemCount() = fragments.size
    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}