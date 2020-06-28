package com.facecool.ui.students

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facecool.R
import com.facecool.attendance.facedetector.FaceDetectionEngine
import com.facecool.ui.camera.FaceDetectorCallback
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.camera.businesslogic.getNullCameraDetectionModel
import com.facecool.ui.camera.businesslogic.user.UserRepository
import com.facecool.ui.camera.camerax.BaseImageAnalyzer
import com.facecool.ui.camera.face_detection.FaceContourDetectionProcessor
import com.facecool.ui.common.ProgressStatus
import com.facecool.ui.students.common.EnrollmentStatus
import com.facecool.utils.toLiveData
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class StudentsViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private var itemCount: Long? = 25
    private var isVisibleRejected = false

    val isEmptyRejected = MutableLiveData<Boolean>()

    private val _students = MutableLiveData<List<CameraDetectionModel>>()
    val students = _students.toLiveData()

    private val _rejectedStudents = MutableLiveData<List<CameraDetectionModel>>()
    val rejectedStudents = _rejectedStudents.toLiveData()

    val generalProgress = MutableLiveData<ProgressStatus>()
//    val generalProgress = _generalProgress

//    private val cachedStudentList = mutableListOf<CameraDetectionModel>()

    fun onStudentClicked(student: CameraDetectionModel) {

    }

    fun clear(){
        itemCount = 25
        isVisibleRejected = false
        _students.postValue(listOf())
        _rejectedStudents.postValue(listOf())
    }

    fun getMoreStudents(){
        itemCount = itemCount!! + 25
        getStudents()
    }

    fun checkIfEmptyRejected(){
        viewModelScope.launch {
            isEmptyRejected.postValue( userRepository.getAllByEnrollment(EnrollmentStatus.REJECTED).isEmpty())
        }
    }

    fun getStudents() {
        generalProgress.postValue(ProgressStatus.LOADING)
        viewModelScope.launch(IO) {
//            val students = userRepository.getAll().filter { it.enrolmentStatus != EnrollmentStatus.UNKNOWN && it.enrolmentStatus == EnrollmentStatus.ENROLLED }
//            cachedStudentList.clear()
//            cachedStudentList.addAll(students)
            _students.postValue( userRepository.getAllByEnrollment(EnrollmentStatus.ENROLLED, itemCount) )
//            _rejectedStudents.postValue( cachedStudentList.filter { it.enrolmentStatus == EnrollmentStatus.REJECTED })
            generalProgress.postValue(ProgressStatus.DONE)
        }
    }

    fun getRejectedStudents(){
        isVisibleRejected = true
        generalProgress.postValue(ProgressStatus.LOADING)
        viewModelScope.launch(IO) {
            _rejectedStudents.postValue( userRepository.getAllByEnrollment(EnrollmentStatus.REJECTED))
            generalProgress.postValue(ProgressStatus.DONE)
        }
    }

    fun cleanRejectedStudents(){
        viewModelScope.launch {
            var yesterday = Calendar.getInstance().timeInMillis - 24*60*60*1000
            userRepository.cleanRejectedEnrollment(yesterday)
        }
    }
    fun clearRejectedStuents(){
        isVisibleRejected = false
        generalProgress.postValue(ProgressStatus.LOADING)
        _rejectedStudents.postValue( listOf() )
        generalProgress.postValue(ProgressStatus.DONE)
    }

    fun searchForStudent(searchTerm: String) {
        generalProgress.postValue(ProgressStatus.LOADING)
        viewModelScope.launch(IO) {
            if (searchTerm.isEmpty()) {
                _students.postValue( userRepository.getAllByEnrollment(EnrollmentStatus.ENROLLED, itemCount))
                if (isVisibleRejected) _rejectedStudents.postValue( userRepository.getAllByEnrollment(EnrollmentStatus.REJECTED))
            } else {
                _students.postValue( userRepository.getAllWhereNameAndEnrollment(searchTerm, EnrollmentStatus.ENROLLED, itemCount))
                if(isVisibleRejected) _rejectedStudents.postValue( userRepository.getAllWhereNameAndEnrollment(searchTerm, EnrollmentStatus.REJECTED))
            }
            generalProgress.postValue(ProgressStatus.DONE)
        }
    }
    fun searchMore(searchTerm: String) {
        itemCount = itemCount!! + 25
        searchForStudent(searchTerm)
    }
    fun removeStudent(student: CameraDetectionModel) {
        viewModelScope.launch(IO) {
            student.id?.let { userRepository.deleteUser(it) }
            if (student.enrolmentStatus == EnrollmentStatus.ENROLLED) getStudents()
            else getRejectedStudents()
        }
    }
}
