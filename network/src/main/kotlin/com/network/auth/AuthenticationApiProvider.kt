package com.network.auth

import com.google.gson.Gson
import com.network.NetworkConfiguration
import com.network.common.APIClient
import com.network.common.NetworkResult
import com.network.common.handleApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthenticationApiProvider constructor(
    private val configuration: NetworkConfiguration,
    private val client: APIClient,
    private val gson: Gson
) : AuthApiProvider {

    private var authService: AuthService = client.getClient().create(AuthService::class.java)

    override suspend fun register(
        request: RegisterRequest
    ): NetworkResult<RegisterResponse> = withContext(Dispatchers.IO) {
        return@withContext handleApi(gson) {
            authService.register(
                configuration.registrationProxy + configuration.registrationPath,
                request
            )
        }
    }

    override suspend fun logIn(request: LoginRequest): NetworkResult<LoginResponse> =
        withContext(Dispatchers.IO) {
            return@withContext handleApi(gson) {
                authService.login(
                    configuration.loginProxy + configuration.loginPath,
                    request
                )
            }
        }

    override suspend fun forgotPassword(request: ForgoerPasswordRequest): NetworkResult<ForgoerPasswordResponse> =
        withContext(Dispatchers.IO) {
            return@withContext handleApi(gson) {
                authService.forgotPassword(
                    configuration.forgotPasswordProxy + configuration.forgotPasswordPath,
                    request
                )
            }
        }
}
