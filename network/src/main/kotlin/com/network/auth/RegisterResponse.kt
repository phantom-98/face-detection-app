package com.network.auth

data class RegisterResponse(
    val status: String?,
    val success: String?,
    val token: String?,
    val error: String?,
)
