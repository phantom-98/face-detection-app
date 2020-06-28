package com.facecool.network.auth.mappers

import com.facecool.network.auth.LoginModel
import com.network.auth.LoginRequest

class LoginAuthenticationMapper : LoginAuthMapper {

    override fun modelToRequest(model: LoginModel): LoginRequest {
        return LoginRequest(
            model.email,
            model.password
        )
    }
}