package com.facecool.di.module

import android.app.Application
import com.face.cool.cache.Cache
import com.facecool.network.auth.AuthRepository
import com.facecool.network.auth.AuthenticationRepository
import com.facecool.network.auth.mappers.ForgotPasswordAuthMapper
import com.facecool.network.auth.mappers.ForgotPasswordAuthenticationMapper
import com.facecool.network.auth.mappers.LoginAuthMapper
import com.facecool.network.auth.mappers.LoginAuthenticationMapper
import com.facecool.network.auth.mappers.RegisterAuthMapper
import com.facecool.network.auth.mappers.RegisterAuthentificationMapper
import com.google.gson.Gson
import com.network.NetworkConfiguration
import com.network.auth.AuthApiProvider
import com.network.auth.AuthenticationApiProvider
import com.network.common.APIClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module()
object NetworkModule {

    private const val HEADERS = "HEADERS_NAME"

    @Provides
    @Singleton
    fun provideNetworkConfiguration(): NetworkConfiguration = NetworkConfiguration(
        hotName = "34.250.134.216",
        baseUrl = "https://34.250.134.216/api/v1/",
        loginProxy = "auth/",
        loginPath = "signin/",
        registrationProxy = "auth/",
        registrationPath = "signup",
        forgotPasswordProxy = "",
        forgotPasswordPath = "",
    )

    @Provides
    @Singleton
    @Named(HEADERS)
    fun provideHeaders(cache: Cache): Map<String, String> {
        return mutableMapOf<String, String>().apply {
            put("Authorization", "Bearer " + cache.getToken())
        }
    }

    @Provides
    @Singleton
    fun provideAPIClient(
        app: Application,
        config: NetworkConfiguration,
        @Named(HEADERS) headers: Map<String, String>
    ): APIClient {
        return APIClient(config, headers, app)
    }

    @Provides
    @Singleton
    fun provideAuthApiProvider(
        config: NetworkConfiguration,
        client: APIClient,
        gson: Gson
    ): AuthApiProvider {
        return AuthenticationApiProvider(config, client, gson)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        api: AuthApiProvider,
        forgotPasswordAuthMapper: ForgotPasswordAuthMapper,
        loginAuthMapper: LoginAuthMapper,
        registerAuthMapper: RegisterAuthMapper,
    ): AuthRepository {
        return AuthenticationRepository(
            api,
            loginAuthMapper,
            registerAuthMapper,
            forgotPasswordAuthMapper
        )
    }

    @Provides
    @Singleton
    fun provideForgotPasswordAuthMapper(): ForgotPasswordAuthMapper =
        ForgotPasswordAuthenticationMapper()

    @Provides
    @Singleton
    fun provideLoginAuthMapper(): LoginAuthMapper = LoginAuthenticationMapper()

    @Provides
    @Singleton
    fun provideRegisterAuthMapper(): RegisterAuthMapper = RegisterAuthentificationMapper()

}
