package com.network.common

import android.content.Context
import com.network.NetworkConfiguration
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class APIClient constructor(
    configuration: NetworkConfiguration,
    headers: Map<String, String>,
    private val context: Context
) {

    private var retrofit: Retrofit

    init {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val certificatePinner = CertificatePinner.Builder()
            .add(configuration.hotName, "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
//            .add(configuration.hotName, "sha256/6YEUWytgCU9ek0FXAb1h3DL336joF1BlLDMCvGcEn6s=")
//            .add(configuration.hotName, "sha256/R3hcMOAGw0WFztuG2skTodoHp8IGid3Qg63Cn7YUYoM=")
            .build()


        val client: OkHttpClient = OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .addInterceptor(interceptor)
            .addInterceptor(HeaderInterceptor(headers))
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(configuration.baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
//            .client(client)
            .client(getUnsafeOkHttpClient(headers, context))
            .build()
    }

    fun getClient(): Retrofit {
        return retrofit
    }
}


private fun getUnsafeOkHttpClient(headers: Map<String, String>, context:Context): OkHttpClient? {

    val interceptor = HttpLoggingInterceptor()
    interceptor.level = HttpLoggingInterceptor.Level.BODY

    return try {
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) =
                    Unit

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) =
                    Unit

                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            }
        )
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
        return OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) = Unit

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) = Unit

                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            })
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(interceptor)
            .addInterceptor(HeaderInterceptor(headers))
//            .addInterceptor(ErrorInterceptor(context))
            .build()
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}