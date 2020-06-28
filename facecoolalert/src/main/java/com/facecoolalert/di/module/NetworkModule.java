package com.facecoolalert.di.module;

import android.app.Application;
import android.content.Context;


import com.facecoolalert.sesssions.SessionManager;
import com.google.gson.Gson;
import com.network.NetworkConfiguration;
import com.network.auth.AuthApiProvider;
import com.network.auth.AuthenticationApiProvider;
import com.network.common.APIClient;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Named;
import javax.inject.Singleton;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;



public final class NetworkModule {
    private static final String HEADERS = "HEADERS_NAME";
    @NotNull
    public static final NetworkModule INSTANCE;

    @Provides
    @Singleton
    @NotNull
    public final NetworkConfiguration provideNetworkConfiguration() {
        return new NetworkConfiguration("34.250.134.216", "https://34.250.134.216/api/v1/", "auth/", "signin/", "auth/", "signup", "", "");
    }

    @Provides
    @Singleton
    @Named("HEADERS_NAME")
    @NotNull
    public final Map provideHeaders(@NotNull Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        Map var2 = (Map)(new LinkedHashMap());
        int var4 = 0;
        try {
            var2.put("Authorization", "Bearer " + SessionManager.readToken(context));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return var2;
    }

    @Provides
    @Singleton
    @NotNull
    public final APIClient provideAPIClient(@NotNull Application app, @NotNull NetworkConfiguration config, @Named("HEADERS_NAME") @NotNull Map headers) {
        Intrinsics.checkNotNullParameter(app, "app");
        Intrinsics.checkNotNullParameter(config, "config");
        Intrinsics.checkNotNullParameter(headers, "headers");
        return new APIClient(config, headers, (Context)app);
    }



    private NetworkModule() {
    }

    static {
        NetworkModule var0 = new NetworkModule();
        INSTANCE = var0;
    }
}
