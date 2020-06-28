package com.facecool.ui.login

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.design.buttons.PrimaryButton
import com.face.cool.cache.Cache
import com.facecool.R
import com.facecool.network.auth.AuthRepository
import com.facecool.network.auth.RegisterModel
import com.facecool.utils.debounce
import com.facecool.utils.toLiveData
import com.network.common.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SingUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cache: Cache,
    private val app: Application
) : ViewModel() {

    private val _onSuccess = MutableLiveData<Boolean>()
    val onSuccess = _onSuccess

    private val _registerButtonStatus = MutableLiveData(PrimaryButton.State.DISABLED)
    val registerButtonStatus = _registerButtonStatus.toLiveData()

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage = _errorMessage.toLiveData()

    private val registerModel = RegisterModel(
        "",
        "",
        "",
        "",
    )

    fun updateEmail(email: String) {
        registerModel.email = email
        checkRegistrationModel(registerModel)
    }

    fun updatePassword(password: String) {
        registerModel.password = password
        checkRegistrationModel(registerModel)
    }

    fun updateRepeatPassword(password: String) {
        registerModel.repeatPassword = password
        checkRegistrationModel(registerModel)
    }

    fun singUp() {
        viewModelScope.launch {
            _registerButtonStatus.postValue(PrimaryButton.State.LOADING)
            when (val response = authRepository.register(registerModel)) {
                is NetworkResult.Error -> {
                    _registerButtonStatus.postValue(PrimaryButton.State.ENABLED)
                }
                is NetworkResult.Exception -> {
                    _registerButtonStatus.postValue(PrimaryButton.State.ENABLED)
                }
                is NetworkResult.Success -> {
                    _registerButtonStatus.postValue(PrimaryButton.State.ENABLED)
                    _onSuccess.postValue(true)
                    val token = response.data.token ?: ""
                    cache.updateToken(token)
                }
            }
        }
    }

    private fun checkRegistrationModel(model: RegisterModel) {
        onModelChange(model)
    }

    private fun checkModel(model: RegisterModel) {
        _registerButtonStatus.postValue(PrimaryButton.State.DISABLED)
        _errorMessage.postValue(null)
        if (model.email == "") return
        if (model.password == "") return
        if (model.password.length < 6) {
            _errorMessage.postValue(app.getString(R.string.login_error_password_length))
            return
        }
        if (model.repeatPassword == "") return
        if (model.password != model.repeatPassword) {
            _errorMessage.postValue(app.getString(R.string.login_error_password_confirm))
            return
        }
        _registerButtonStatus.postValue(PrimaryButton.State.ENABLED)
        return
    }

    private val onModelChange: (RegisterModel) -> Unit = debounce(
        waitMs = 600L,
        coroutineScope = viewModelScope
    ) { checkModel(it) }


}