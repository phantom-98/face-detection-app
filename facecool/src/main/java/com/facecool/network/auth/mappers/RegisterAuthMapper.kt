package com.facecool.network.auth.mappers

import com.facecool.network.auth.RegisterModel
import com.network.auth.RegisterRequest

interface RegisterAuthMapper {

    fun modelToRequest(model: RegisterModel): RegisterRequest

}