package com.network.auth

data class LoginResponse(
    val success: Boolean?,
    val token: String?,
    val status: Int?,
    val error: LoginErrorDetails?
)

data class LoginErrorDetails(
    val code: String?,
    val customData: String?,
    val name: String?
)
