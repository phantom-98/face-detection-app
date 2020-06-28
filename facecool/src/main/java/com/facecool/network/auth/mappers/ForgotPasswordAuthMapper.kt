package com.facecool.network.auth.mappers

import com.facecool.network.auth.ForgoerPasswordModel
import com.network.auth.ForgoerPasswordRequest

interface ForgotPasswordAuthMapper {

    fun modelToRequest(model: ForgoerPasswordModel): ForgoerPasswordRequest

}