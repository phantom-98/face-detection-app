package com.facecool.ui.reports.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.face.cool.databasa.detection_events.EventEntity
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.camera.businesslogic.events.EventRepository
import com.facecool.ui.camera.businesslogic.user.UserRepository
import com.facecool.ui.common.ProgressStatus
import com.facecool.ui.students.common.EnrollmentStatus
import com.facecool.utils.getReadableDate
import com.facecool.utils.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryLogViewModel @Inject constructor(
    private val app: Application,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository
) : AndroidViewModel(app) {

    private val _historyLogItems = MutableLiveData<List<HistoryItemModel>>()
    val historyLogItems = _historyLogItems.toLiveData()

    private val _loadingStatus = MutableLiveData<ProgressStatus>()
    val loadingStatus = _loadingStatus.toLiveData()

    private val _onUnEnrolledUserSelected = MutableLiveData<CameraDetectionModel>()
    val onUnEnrolledUserSelectedLiveData = _onUnEnrolledUserSelected.toLiveData()

    private val _onUserSelected = MutableLiveData<Long>()
    val onUserSelectedLiveData = _onUserSelected.toLiveData()

    val latestEvents = eventRepository.getLatestEvents(99)

    fun onItemClick(item: HistoryItemModel) {
        viewModelScope.launch {
            if (item.enrollmentStatus == EnrollmentStatus.UNKNOWN) {
                val user = userRepository.getUser(item.studentDatabaseId)
//                val user = userRepository.getUser(item.eventDatabaseId)
                user?.let {
                    _onUnEnrolledUserSelected.postValue(it)
                    return@launch
                }
            } else {
                _onUserSelected.postValue(item.studentDatabaseId)
            }
        }
    }

    fun delete(item: HistoryItemModel) {
        viewModelScope.launch(Dispatchers.IO) {
            _loadingStatus.postValue(ProgressStatus.LOADING)
            userRepository.deleteUser(item.eventDatabaseId)
            eventRepository.deleteEventById(item.eventDatabaseId)
            _loadingStatus.postValue(ProgressStatus.DONE)
        }
    }

    fun processEvents(eventEntityList: List<EventEntity>?) {
        eventEntityList ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _loadingStatus.postValue(ProgressStatus.LOADING)
            val latestDetectedFaces = userRepository.getByEventsListAll(eventEntityList)
            val historyItemModels = latestDetectedFaces.map {
                HistoryItemModel(
                    if (it.second?.userName=="spoof") "Spoof Suspicious" else ((it.first.studentId
                        ?: "").toString() + "\n" + it.first.name + " " + it.first.lastName),
                    it.first.id ?: -1,
                    (it.second?.uid ?: -1).toLong(),
                    it.second?.timeInMilesOfCreation?.getReadableDate() ?: "",
                    it.second?.imagePathName ?: UNKNOWN_ENROLLMENT,
                    it.first.imageName,
                    it.first.enrolmentStatus,
                )
            }
            _historyLogItems.postValue(historyItemModels)
            _loadingStatus.postValue(ProgressStatus.DONE)
        }
    }

}
