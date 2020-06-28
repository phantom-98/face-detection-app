package com.facecool.network.auth.mappers

import com.facecool.network.auth.ForgoerPasswordModel
import com.network.auth.ForgoerPasswordRequest

class ForgotPasswordAuthenticationMapper : ForgotPasswordAuthMapper {

    override fun modelToRequest(model: ForgoerPasswordModel): ForgoerPasswordRequest {
        return ForgoerPasswordRequest(
            model.contactDetails
        )
    }

}