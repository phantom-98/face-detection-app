package com.facecool.ui.classes

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facecool.ui.classes.common.ClassRepository
import com.facecool.ui.classes.list.ClassModel
import com.facecool.ui.common.ProgressStatus
import com.facecool.utils.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassesViewModel @Inject constructor(
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _generalProgress = MutableLiveData<ProgressStatus>()
    val generalProgress = _generalProgress.toLiveData()

    private val _classList = MutableLiveData<List<ClassModel>>()
    val classList = _classList.toLiveData()

    private var cachedClassList = mutableListOf<ClassModel>()

    fun getClassData() {
        _generalProgress.postValue(ProgressStatus.LOADING)
        viewModelScope.launch(IO) {
            val classes = classRepository.getAllCLasses()
            cachedClassList.clear()
            cachedClassList.addAll(classes)
            _classList.postValue(cachedClassList)
            _generalProgress.postValue(ProgressStatus.DONE)
        }
    }

    fun deleteClass(classModel: ClassModel) {
        viewModelScope.launch(IO) {
            classRepository.delete(classModel)
            getClassData()
        }
    }

    fun searchForClass(searchTerm: String) {
        if (searchTerm.isEmpty()) {
            _classList.postValue(cachedClassList)
        }else{
            val searchedRez = cachedClassList.filter { it.name.lowercase().contains(searchTerm.lowercase()) }
            _classList.postValue(searchedRez)
        }
    }

}
