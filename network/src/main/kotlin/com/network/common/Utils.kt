package com.network.common

import com.google.gson.Gson
import retrofit2.Response

suspend fun <T : Any> handleApi(
    gson: Gson,
    execute: suspend () -> Response<T>
): NetworkResult<T> {
    try {
        val response = execute()
        val body = response.body()
        if (body != null) {
            if (response.isSuccessful) {
                return NetworkResult.Success(body)
            } else {
                return NetworkResult.Error(
                    ErrorBodyResponse(
                        false,
                        ErrorResponse(
                            "900",
                            CustomData(""),
                            "error",
                            body.toString()
                        ),
                        900
                    )
                )
            }
        } else {
            response.errorBody()?.let {
                val errorJson = it.string()
                System.out.println(errorJson);
                val error = gson.fromJson(errorJson, ErrorBodyResponse::class.java)
                return NetworkResult.Error(error)
            }
            return NetworkResult.Exception(Exception())
        }
    } catch (e: Throwable) {
        return NetworkResult.Exception(e)
    }
}
