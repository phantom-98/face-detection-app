package com.network.common

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class HeaderInterceptor(private val headers: Map<String, String>) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val newRequest = request.newBuilder()
        headers.forEach { (header, value) ->
            newRequest.addHeader(header, value)
        }
        return chain.proceed(newRequest.build())
    }

}