package com.network.auth

data class RegisterRequest(
    val email: String,
    val password: String,
    val phone: String,
    val device: String,
    val name: String,
)