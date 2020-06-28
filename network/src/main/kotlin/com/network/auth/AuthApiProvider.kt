package com.network.auth

import com.network.common.NetworkResult

interface AuthApiProvider {

    suspend fun register( request: RegisterRequest) : NetworkResult<RegisterResponse>
    suspend fun logIn( request: LoginRequest): NetworkResult<LoginResponse>
    suspend fun forgotPassword( request: ForgoerPasswordRequest): NetworkResult<ForgoerPasswordResponse>

}
