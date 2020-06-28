package com.facecool.ui.students.addfolder

import android.net.Uri

sealed class EnrollmentModel {

    class Waiting(
        var imageUri: Uri,
        var detectedName: String = "",
        var isProgress: Boolean = false
    ) : EnrollmentModel()

    class Enrolled(
        var name: String,
        var imagePath: String = "",
        var imageQuality: Int = 0
    ) : EnrollmentModel()

    class Error(
        var reason: String,
        var imageUri: Uri?,
        var imageQuality: Int? = 0
    ) : EnrollmentModel()

}
