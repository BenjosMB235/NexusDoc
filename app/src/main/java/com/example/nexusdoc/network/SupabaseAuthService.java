package com.example.nexusdoc.network;

import com.example.nexusdoc.ui.authenfication.model.SupabaseAuthRequest;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface SupabaseAuthService {

    @Headers("Content-Type: application/json")
    @POST("signup")
    Call<JsonObject> signUp(@Body SupabaseAuthRequest request);

    @Headers("Content-Type: application/json")
    @POST("token?grant_type=password")
    Call<JsonObject> signIn(@Body SupabaseAuthRequest request);

    @Headers("Content-Type: application/json")
    @POST("logout")
    Call<JsonObject> signOut();

    @Headers("Content-Type: application/json")
    @POST("token?grant_type=refresh_token")
    Call<JsonObject> refreshToken(@Body JsonObject refreshTokenRequest);
}