package com.facecool.navigation

import android.app.Activity
import android.graphics.Bitmap
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.classes.add.time.LessonModel

interface NavigatorContract {

    fun openCameraFragment(activity: Activity)
    fun openClassesFragment(activity: Activity)
    fun openStudentsFragment(activity: Activity)
    fun openReportsFragment(activity: Activity)
    fun openSettingsFragment(activity: Activity)
    fun openAdminsFragment(activity: Activity)
    fun openHelpFragment(activity: Activity)
    fun openProfileFragment(activity: Activity)

    fun startMainActivity(activity: Activity)
    fun startLogInActivity(activity: Activity)
    fun startSingUpActivity(activity: Activity)
    fun openClassDetailsActivity(activity: Activity, classId: Long)
    fun openClassOccurrenceDetailsActivity(activity: Activity, classOccurrenceModel: LessonModel)

    fun openStudentDetailsActivity(activity: Activity, studentId: Long)

    fun openAdminDetailsActivity(activity: Activity, adminId: Long)
    fun openAddNewClassActivity(activity: Activity)
    fun openAddNewClassActivity(activity: Activity, classId: Long)
    fun openAddNewStudentActivity(activity: Activity, data: CameraDetectionModel)
    fun openAddNewStudentActivity(activity: Activity)
    fun openAddNewAdminActivity(activity: Activity)
    fun openAddNewAdminActivity(activity: Activity, data: Bitmap)
    fun openStudentSelectionActivity(activity: Activity)

    fun openAddNewOccurrenceActivity(activity: Activity, classId: Long)
    fun openAddNewOccurrenceActivity(activity: Activity, classId: Long, lessonId: Long)

    fun openTermsAndConditionsActivity(activity: Activity)

    fun openOnBoardingActivity(activity: Activity)

    fun closeJourney(activity: Activity)

    fun openAttendanceActivity(activity: Activity, studentId: String)

}
