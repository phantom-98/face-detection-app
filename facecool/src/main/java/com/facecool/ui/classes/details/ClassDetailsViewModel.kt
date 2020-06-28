package com.facecool.ui.classes.details

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.face.cool.databasa.Database
import com.facecool.R
import com.facecool.utils.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassDetailsViewModel @Inject constructor(
    private val  db: Database,
    private val app: Application
): ViewModel() {

    private val _title = MutableLiveData<String>()
    val title = _title.toLiveData()

    fun getTitle(classId: Long) {
        viewModelScope.launch(IO) {
            val classDetails = db.classDao().getClassById(classId)
            val title = String.format(app.getString(R.string.class_details_title), classDetails?.className ?: "")
            _title.postValue(title)
        }
    }

}
