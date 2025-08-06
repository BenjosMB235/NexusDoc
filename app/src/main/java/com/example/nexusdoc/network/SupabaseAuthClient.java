package com.example.nexusdoc.network;

import com.example.nexusdoc.config.AuthConfig;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseAuthClient {
    private static volatile SupabaseAuthClient instance;
    private final SupabaseAuthService authService;

    private SupabaseAuthClient() {
        this.authService = createAuthService();
    }

    public static SupabaseAuthClient getInstance() {
        if (instance == null) {
            synchronized (SupabaseAuthClient.class) {
                if (instance == null) {
                    instance = new SupabaseAuthClient();
                }
            }
        }
        return instance;
    }

    public SupabaseAuthService getAuthService() {
        return authService;
    }

    private SupabaseAuthService createAuthService() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("apikey", AuthConfig.SUPABASE_ANON_KEY)
                            .header("Content-Type", "application/json")
                            .build();
                    return chain.proceed(request);
                })
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AuthConfig.SUPABASE_AUTH_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit.create(SupabaseAuthService.class);
    }
}