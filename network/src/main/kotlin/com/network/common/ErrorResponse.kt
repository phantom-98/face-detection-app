package com.network.common

data class ErrorBodyResponse (
    val success: Boolean?,
    val error: ErrorResponse?,
    val status: Long?
) : Throwable()

data class ErrorResponse (
    val code: String?,
    val customData: CustomData?,
    val name: String?,
    val body: String?
)

data class CustomData (
    val appName: String?
)