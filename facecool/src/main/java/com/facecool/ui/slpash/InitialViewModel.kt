package com.facecool.ui.slpash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.face.cool.cache.Cache
import com.facecool.utils.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InitialViewModel @Inject constructor(
    private val cache: Cache
) : ViewModel() {

    private val _navigation = MutableLiveData<Int>()
    val navigation = _navigation.toLiveData()

    fun getInitialNavigation() {
        val token = cache.getToken()
        if (token.isEmpty()){
            _navigation.postValue(LOG_IN)
        }else{
            _navigation.postValue(MAIN)
        }
    }

    companion object{
        const val MAIN = 1
        const val LOG_IN = 2
    }

}
