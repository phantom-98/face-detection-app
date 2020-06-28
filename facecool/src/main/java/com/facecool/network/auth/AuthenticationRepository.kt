package com.facecool.network.auth

import com.facecool.network.auth.mappers.ForgotPasswordAuthMapper
import com.facecool.network.auth.mappers.LoginAuthMapper
import com.facecool.network.auth.mappers.RegisterAuthMapper
import com.network.auth.AuthApiProvider
import com.network.auth.ForgoerPasswordResponse
import com.network.auth.LoginResponse
import com.network.auth.RegisterResponse
import com.network.common.NetworkResult

class AuthenticationRepository constructor(
    private val api: AuthApiProvider,
    private val loginAuthMapper: LoginAuthMapper,
    private val registerAuthMapper: RegisterAuthMapper,
    private val forgotPasswordAuthMapper: ForgotPasswordAuthMapper,
) : AuthRepository {

    override suspend fun register(request: RegisterModel): NetworkResult<RegisterResponse> {
        return api.register(registerAuthMapper.modelToRequest(request))
    }

    override suspend fun logIn(request: LoginModel): NetworkResult<LoginResponse> {
        return api.logIn(loginAuthMapper.modelToRequest(request))
    }

    override suspend fun forgotPassword(request: ForgoerPasswordModel): NetworkResult<ForgoerPasswordResponse> {
        return api.forgotPassword(forgotPasswordAuthMapper.modelToRequest(request))
    }
}
