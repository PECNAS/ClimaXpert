package com.example.climaxpert;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/openai/v1/chat/completions")
    Call<String> getResult(@Body String question);
}