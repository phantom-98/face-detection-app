package com.facecool.ui.classes.details

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.facecool.ui.classes.details.ocurrences.ClassOccurrencesFragment
import com.facecool.ui.classes.details.students.StudentListFragment

class ClassDetailsPageAdapter(fragmentActivity: FragmentActivity, classId: Long) :
    FragmentStateAdapter(fragmentActivity) {

     val fragments:List<Fragment> = listOf(
        ClassOccurrencesFragment.newInstance(classId),
        StudentListFragment.newInstance(classId)
    )

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}
