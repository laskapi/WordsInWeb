package com.gmail.in2horizon.wordsinweb;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface TranslationApi {

    @Headers( "Content-Type: application/json")
    @POST("translate")
    Call<Translation> getTranslation(@Body RequestBody body);
}
