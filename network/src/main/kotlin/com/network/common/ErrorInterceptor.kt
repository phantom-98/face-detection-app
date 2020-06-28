package com.network.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection

class ErrorInterceptor(private val  context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {


        try {
            if (isInternetAvailable(context)){
                Log.d("TAG","STEP _1")
              return  handleResponse(chain.proceed(chain.request()))

            }else{
                Log.d("TAG","STEP _2")
                throw ConnectionError()
            }
        }catch (e: Exception){
            Log.d("TAG","STEP _3")
            throw e
        }


//
//        val request: Request = chain.request()
//        val newRequest = request.newBuilder()
//        return chain.proceed(newRequest.build())
    }

    private fun handleResponse(response: Response): Response {


//        val body = request.body?.string()

//        Log.d("ERRORHAND",""+ body)


//        return Response.Builder()
//            .request(request)
//            .protocol(Protocol.HTTP_1_1)
//            .code(999)
//            .message(msg)
//            .body(ResponseBody.create(null, "{${e}}")).build()








        when(response.code){
            in 200..305 -> return response
            HttpURLConnection.HTTP_UNAUTHORIZED,
            HttpURLConnection.HTTP_FORBIDDEN -> return response
            else -> return response
        }


    }


    private fun isInternetAvailable(context: Context): Boolean {
        var result = false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }

                }
            }
        }

        return result
    }
}

class ConnectionError: Exception() {
    override val message: String?
        get() = "no connection avalable"
}