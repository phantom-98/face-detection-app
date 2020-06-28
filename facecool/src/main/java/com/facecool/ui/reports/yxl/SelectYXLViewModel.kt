package com.facecool.ui.reports.yxl

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facecool.ui.classes.common.ClassRepository
import com.facecool.ui.reports.yxl.selection.ClassSelectionModel
import com.facecool.utils.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectYXLViewModel @Inject constructor(
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _classes = MutableLiveData<List<ClassSelectionModel>>()
    val classes = _classes.toLiveData()

    private val _selectedClass = MutableLiveData<ClassSelectionModel>()
    val selectedClass = _selectedClass.toLiveData()

    fun getClasses() {
        viewModelScope.launch(IO) {
            val classes = classRepository.getAllCLasses().map {
                ClassSelectionModel(it.name, it.uuid)
            }
            _classes.postValue(classes)
        }
    }

    fun onClassSelected(classSelectionModel: ClassSelectionModel) {
        _selectedClass.postValue(classSelectionModel)
    }

}
