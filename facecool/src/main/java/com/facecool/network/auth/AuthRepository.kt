package com.facecool.network.auth

import com.network.auth.ForgoerPasswordResponse
import com.network.auth.LoginResponse
import com.network.auth.RegisterResponse
import com.network.common.NetworkResult

interface AuthRepository {
    suspend fun register(request: RegisterModel): NetworkResult<RegisterResponse>
    suspend fun logIn(request: LoginModel): NetworkResult<LoginResponse>
    suspend fun forgotPassword(request: ForgoerPasswordModel): NetworkResult<ForgoerPasswordResponse>
}
