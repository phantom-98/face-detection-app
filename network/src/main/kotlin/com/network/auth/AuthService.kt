package com.network.auth

import com.network.PROXY
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthService {

    @POST("{$PROXY}")
    suspend fun register(
        @Path(PROXY, encoded = true) proxy: String,
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("{$PROXY}")
    suspend fun forgotPassword(
        @Path(PROXY, encoded = true) proxy: String,
        @Body request: ForgoerPasswordRequest
    ): Response<ForgoerPasswordResponse>

    @POST("{$PROXY}")
    suspend fun login(
        @Path(PROXY, encoded = true) proxy: String,
        @Body request: LoginRequest
    ): Response<LoginResponse>

}
