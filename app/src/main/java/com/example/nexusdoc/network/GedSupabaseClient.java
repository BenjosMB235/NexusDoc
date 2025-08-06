package com.example.nexusdoc.network;

import android.util.Log;

import com.example.nexusdoc.config.GedConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.annotations.Expose;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

import com.example.nexusdoc.ui.authenfication.repository.SupabaseAuthRepository;

public class GedSupabaseClient {
    private static volatile GedSupabaseClient instance;
    private final GedSupabaseService service;
    private final OkHttpClient httpClient;
    private SupabaseAuthRepository authRepository;

    private GedSupabaseClient() {
        this.httpClient = createHttpClient();
        this.service = createService();
    }

    public static GedSupabaseClient getInstance() {
        if (instance == null) {
            synchronized (GedSupabaseClient.class) {
                if (instance == null) {
                    instance = new GedSupabaseClient();
                }
            }
        }
        return instance;
    }

    public void setAuthRepository(SupabaseAuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public GedSupabaseService getService() {
        return service;
    }

    private OkHttpClient createHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(createAuthInterceptor())
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    private Interceptor createAuthInterceptor() {
        return chain -> {
            Request original = chain.request();

            Request.Builder requestBuilder = original.newBuilder()
                    .header("apikey", GedConfig.SUPABASE_ANON_KEY)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=representation");

            // Ajouter le token d'authentification si disponible
            String authToken = getAuthToken();
            if (authToken != null && !authToken.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + authToken);
            } else {
                Log.w("GedSupabaseClient", "Aucun token d'authentification disponible");
            }

            return chain.proceed(requestBuilder.build());
        };
    }

    private String getAuthToken() {
        if (authRepository != null) {
            return authRepository.getAccessToken();
        }
        return null;
    }

    private GedSupabaseService createService() {
        // CORRECTION: Configurer Gson pour respecter les annotations @Expose
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                        final Expose expose = fieldAttributes.getAnnotation(Expose.class);
                        if (expose != null) {
                            // Pour les requêtes POST/PUT, on vérifie serialize
                            // Pour les réponses GET, on vérifie deserialize
                            return !expose.serialize(); // On exclut si serialize = false
                        }
                        return false;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GedConfig.SUPABASE_REST_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient)
                .build();

        return retrofit.create(GedSupabaseService.class);
    }

    // Méthodes utilitaires pour les requêtes
    public static String buildSelectQuery(String... fields) {
        return String.join(",", fields);
    }

    public static String buildOrderQuery(String field, boolean ascending) {
        return field + (ascending ? ".asc" : ".desc");
    }

    public static String buildSearchFilter(String field, String query) {
        return field + ".ilike.*" + query + "*";
    }

    public static String buildOrFilter(String... filters) {
        return "(" + String.join(",", filters) + ")";
    }

    // Nettoyage des ressources
    public void cleanup() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }
    }
}