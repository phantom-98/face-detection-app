package com.network

data class NetworkConfiguration(
    val hotName: String,
    val baseUrl: String,
    val loginProxy: String,
    val loginPath: String,
    val registrationProxy: String,
    val registrationPath: String,
    val forgotPasswordProxy: String,
    val forgotPasswordPath: String,
)
