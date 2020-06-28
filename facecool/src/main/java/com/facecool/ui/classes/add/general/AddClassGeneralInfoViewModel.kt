package com.facecool.ui.classes.add.general

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facecool.ui.classes.common.ClassRepository
import com.facecool.ui.classes.common.SharedAddNewClassData
import com.facecool.ui.classes.list.ClassModel
import com.facecool.utils.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddClassGeneralInfoViewModel @Inject constructor(
    private val classRepository: ClassRepository
): ViewModel() {

    private val _classDetails = MutableLiveData<ClassDetailsToUpdate>()
    val classDetails = _classDetails.toLiveData()

    private var classModel: ClassModel? = null


    fun updateClassName(className: String) {
        SharedAddNewClassData.className = className
        classModel?.let {
            it.name = className
        }
    }

    fun updateClassId(classID: String) {
        SharedAddNewClassData.classID = classID
        classModel?.let {
            it.id = classID
        }
    }

    fun updateClassDetails() {
        _classDetails.postValue(
            ClassDetailsToUpdate(
                name = SharedAddNewClassData.className,
                classId = SharedAddNewClassData.classID
            )
        )
    }

    fun updateClassDetails(uid: Long){
        viewModelScope.launch {
            classModel = classRepository.getClassById(uid)
            classModel?.let {
                _classDetails.postValue(
                    ClassDetailsToUpdate(
                        it?.name!!, it?.id!!
                    )
                )
            }
        }
    }

    fun saveClassModel(){
        viewModelScope.launch {
            classModel?.let {
                classRepository.updateClass(it)
            }
        }
    }

}

data class ClassDetailsToUpdate(
    val name: String,
    val classId: String
)