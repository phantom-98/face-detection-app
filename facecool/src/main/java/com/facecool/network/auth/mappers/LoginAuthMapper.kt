package com.facecool.network.auth.mappers

import com.facecool.network.auth.LoginModel
import com.network.auth.LoginRequest

interface LoginAuthMapper {

    fun modelToRequest(model: LoginModel): LoginRequest

}