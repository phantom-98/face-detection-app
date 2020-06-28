package com.facecool.ui.login

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.design.buttons.PrimaryButton
import com.face.cool.cache.Cache
import com.facecool.R
import com.facecool.network.AUTH_TOO_MAN_REQUESTS
import com.facecool.network.AUTH_USER_NOT_FOUND
import com.facecool.network.auth.AuthRepository
import com.facecool.network.auth.LoginModel
import com.facecool.utils.toLiveData
import com.network.common.NetworkResult.Error
import com.network.common.NetworkResult.Exception
import com.network.common.NetworkResult.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogInViewModel @Inject constructor(
    private val cache: Cache,
    private val authRepository: AuthRepository,
    private val app: Application
) : ViewModel() {

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess = _loginSuccess

    private val _loginButtonStatus = MutableLiveData(PrimaryButton.State.DISABLED)
    val loginButtonStatus = _loginButtonStatus.toLiveData()

    private val _errorEvent = MutableLiveData<String>()
    val errorEvent = _errorEvent.toLiveData()

    private var loginData = LoginModel(
        "",
        ""
    )

    fun onEmailUpdate(email: String) {
        loginData.email = email
        setButtonStatus(loginData)
    }

    fun onPasswordUpdate(password: String) {
        loginData.password = password
        setButtonStatus(loginData)
    }

    fun onLogIn() {
        _loginButtonStatus.postValue(PrimaryButton.State.LOADING)
        viewModelScope.launch {
            when (val response = authRepository.logIn(loginData)) {
                is Error -> {
                    when (response.data.error?.code) {
                        AUTH_TOO_MAN_REQUESTS -> _errorEvent.postValue(app.getString(R.string.login_error_too_many_request))
                        AUTH_USER_NOT_FOUND -> _errorEvent.postValue(app.getString(R.string.login_error_user_not_found))
                        else -> _errorEvent.postValue(app.getString(R.string.login_error_unknown))
                    }
                    _loginButtonStatus.postValue(PrimaryButton.State.ENABLED)
                }
                is Exception -> {
                    _loginButtonStatus.postValue(PrimaryButton.State.ENABLED)
                }
                is Success -> {
                    _loginButtonStatus.postValue(PrimaryButton.State.ENABLED)
                    _loginSuccess.postValue(true)
                    val token = response.data.token ?: ""
                    cache.updateToken(token)
                }
            }
        }
    }

    private fun setButtonStatus(model: LoginModel) {
        _loginButtonStatus.postValue(PrimaryButton.State.DISABLED)
        if (model.email == "") return
        if (model.password == "") return
        _loginButtonStatus.postValue(PrimaryButton.State.ENABLED)
    }


}
