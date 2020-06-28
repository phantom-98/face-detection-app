package com.facecool.network.auth.mappers

import com.facecool.network.auth.RegisterModel
import com.network.auth.RegisterRequest

class RegisterAuthentificationMapper: RegisterAuthMapper {

    override fun modelToRequest(model: RegisterModel): RegisterRequest {
      return  RegisterRequest(
          model.email,
          model.password,
          "1234567",//??
          android.os.Build.MANUFACTURER + android.os.Build.MODEL,
          model.name + "Name",
      )
    }
}
