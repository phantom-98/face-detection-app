package com.facecool.navigation

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.face.cool.common.transfer.DataTransferSender
import com.facecool.R
import com.facecool.ui.MainActivity
import com.facecool.ui.camera.CameraFragment
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.classes.ClassesFragment
import com.facecool.ui.classes.add.AddNewClassActivity
import com.facecool.ui.classes.add.time.LessonModel
import com.facecool.ui.classes.details.ClassDetailsActivity
import com.facecool.ui.classes.details.ocurrences.add.ActivityAddNewOccurrence
import com.facecool.ui.classes.details.ocurrences.details.ClassOccurrenceDetailsActivity
import com.facecool.ui.help.HelpFragment
import com.facecool.ui.login.LogInActivity
import com.facecool.ui.login.SingUpActivity
import com.facecool.ui.onboarding.OnBoardingActivity
import com.facecool.ui.profile.ProfileFragment
import com.facecool.ui.reports.ReportsFragment
import com.facecool.ui.settings.SettingsFragment
import com.facecool.ui.settings.administrators.AdministratorsFragment
import com.facecool.ui.settings.administrators.add.AddNewAdministratorActivity
import com.facecool.ui.students.StudentsFragment
import com.facecool.ui.students.add.AddNewStudentActivity
import com.facecool.ui.students.attendance.StudentAttendanceActivity
import com.facecool.ui.students.common.ToolbarListener
import com.facecool.ui.students.common.ViewWithTitle
import com.facecool.ui.students.details.StudentDetailsActivity
import com.facecool.ui.students.select.ActivitySelectStudent
import com.facecool.ui.termsandconditions.TermsAndConditionsActivity
import java.lang.Math.random
import javax.inject.Inject

class Navigator @Inject constructor(private var sender: DataTransferSender) : NavigatorContract {

    private fun checkForToolbarTitle(
        activity: Activity,
        viewWithTitle: ViewWithTitle,
        isCamera: Boolean = false
    ) {
        if (activity is ToolbarListener) {
            activity.provideToolbarTitle(viewWithTitle.getTitle(activity))
            if (isCamera) {
                activity.isCameraScreenVisible()
            }
        }
    }

    override fun startMainActivity(activity: Activity) {
        val starter = Intent(activity, MainActivity::class.java)
        activity.startActivity(starter)
    }

    override fun openAddNewAdminActivity(activity: Activity) {
        AddNewAdministratorActivity.start(activity)
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun openAddNewAdminActivity(activity: Activity, data: Bitmap) {
        val timeOfCreation = "key_" + System.currentTimeMillis() + random().toInt()
        sender.put(timeOfCreation, data)
        AddNewAdministratorActivity.start(activity, timeOfCreation)
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    override fun openAdminDetailsActivity(activity: Activity, adminId: Long) {
        AddNewAdministratorActivity.start(activity, adminId)
    }

    override fun openAdminsFragment(activity: Activity) {
        (activity as? AppCompatActivity)?.replaceFragment(AdministratorsFragment().also {
            checkForToolbarTitle(activity, it)
        })
    }

    override fun openCameraFragment(activity: Activity) {
        (activity as? AppCompatActivity)?.replaceFragment(CameraFragment().also {
            checkForToolbarTitle(activity, it, true)
        })
    }

    override fun openClassesFragment(activity: Activity) {
        (activity as? AppCompatActivity)?.replaceFragment(ClassesFragment().also {
            checkForToolbarTitle(activity, it)
        }, true)
    }

    override fun openStudentsFragment(activity: Activity) {
        (activity as? AppCompatActivity)?.replaceFragment(StudentsFragment().also {
            checkForToolbarTitle(activity, it)
        }, true)
    }

    override fun openReportsFragment(activity: Activity) {
        (activity as? AppCompatActivity)?.replaceFragment(ReportsFragment().also {
            checkForToolbarTitle(activity, it)
        }, true)
    }

    override fun openSettingsFragment(activity: Activity) {
        (activity as? AppCompatActivity)?.replaceFragment(SettingsFragment().also {
            checkForToolbarTitle(activity, it)
        }, true)
    }

    override fun openHelpFragment(activity: Activity) {
        (activity as? AppCompatActivity)?.replaceFragment(HelpFragment().also {
            checkForToolbarTitle(activity, it)
        }, true)
    }

    override fun openProfileFragment(activity: Activity) {
        (activity as? AppCompatActivity)?.replaceFragment(ProfileFragment().also {
            checkForToolbarTitle(activity, it)
        }, true)
    }

    override fun startLogInActivity(activity: Activity) {
        LogInActivity.start(activity)
    }

    override fun startSingUpActivity(activity: Activity) {
        SingUpActivity.start(activity)
    }

    override fun openClassDetailsActivity(activity: Activity, classId: Long) {
        ClassDetailsActivity.start(activity, classId)
    }

    override fun openClassOccurrenceDetailsActivity(
        activity: Activity,
        classOccurrenceModel: LessonModel
    ) {
        ClassOccurrenceDetailsActivity.start(activity, classOccurrenceModel)
    }

    override fun openStudentDetailsActivity(activity: Activity, studentId: Long) {
        StudentDetailsActivity.start(activity, studentId)
    }

    override fun openAddNewClassActivity(activity: Activity) {
        AddNewClassActivity.start(activity)
    }

    override fun openAddNewClassActivity(activity: Activity, classId: Long) {
        AddNewClassActivity.start(activity, classId)
    }

    override fun openAddNewStudentActivity(activity: Activity, data: CameraDetectionModel) {
        val timeOfCreation = "key_" + System.currentTimeMillis() + random().toInt()
        sender.put(timeOfCreation, data)
        AddNewStudentActivity.start(activity, timeOfCreation)
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    override fun openAddNewStudentActivity(activity: Activity) {
        AddNewStudentActivity.start(activity)
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    override fun openStudentSelectionActivity(activity: Activity) {
        ActivitySelectStudent.start(activity)
    }

    override fun openAddNewOccurrenceActivity(activity: Activity, classId: Long) {
        ActivityAddNewOccurrence.start(activity, classId)
    }

    override fun openAddNewOccurrenceActivity(activity: Activity, classId: Long, lessonId: Long) {
        ActivityAddNewOccurrence.start(activity, classId, lessonId)
    }

    override fun openTermsAndConditionsActivity(activity: Activity) {
        TermsAndConditionsActivity.start(activity)
    }

    override fun openOnBoardingActivity(activity: Activity) {
        OnBoardingActivity.start(activity)
    }

    override fun closeJourney(activity: Activity) {
        val intent = Intent(activity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
        activity.finish()
    }

    override fun openAttendanceActivity(activity: Activity, studentId: String) {
        StudentAttendanceActivity.start(activity, studentId)
    }

}

fun AppCompatActivity.addFragment(fragment: Fragment, addToBackstack: Boolean = false) {
    val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
    ft.add(R.id.fragment_container, fragment)
    if (addToBackstack) ft.addToBackStack(fragment.javaClass.simpleName)
    ft.commit()
}

fun AppCompatActivity.replaceFragment(fragment: Fragment, addToBackstack: Boolean = false) {
    val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
    ft.replace(R.id.fragment_container, fragment)
    if (addToBackstack) ft.addToBackStack(fragment.javaClass.simpleName)
    ft.commit()
}
