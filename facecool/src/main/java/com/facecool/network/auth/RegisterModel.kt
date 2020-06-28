package com.facecool.network.auth

data class RegisterModel(
    var name: String,
    var email: String,
    var password: String,
    var repeatPassword: String,
)
