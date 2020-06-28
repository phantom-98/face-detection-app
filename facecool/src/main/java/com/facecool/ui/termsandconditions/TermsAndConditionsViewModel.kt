package com.facecool.ui.termsandconditions

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TermsAndConditionsViewModel @Inject constructor():ViewModel() {

    private val _termsAndConditions = MutableLiveData<Boolean>()
    val termsAndConditions = _termsAndConditions

    fun acceptTermsAndConditions() {
        _termsAndConditions.postValue(true)
    }

}
