package com.facecool.ui.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor():ViewModel() {

    private val _profileInformation = MutableLiveData<List<ProfileItemModel>>()
    val profileInformation = _profileInformation

    fun getProfileInformation() {
        _profileInformation.postValue(
            listOf(
                ProfileItemModel("Name", "name"),
                ProfileItemModel("Surname", "surname"),
                ProfileItemModel("Email", "email"),
                ProfileItemModel("Phone", "phone"),
                ProfileItemModel("Address", "address"),
                ProfileItemModel("City", "city"),
                ProfileItemModel("Country", "country"),
                ProfileItemModel("Zip Code", "zipCode"),
            )
        )
    }

}
