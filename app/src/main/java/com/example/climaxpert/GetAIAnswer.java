package com.example.climaxpert;

import android.content.Intent;
import android.widget.TextView;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GetAIAnswer {
    @SerializedName("message")
    public String message;

    public String answer;
    public void setAnswer(String value) { this.answer = value; }
    public String getAnswer() { return answer; }

    public TextView texter;
    public void setTexter(TextView texter) { this.texter = texter; }
    public void setTexterText(String value) { this.texter.setText(value); }

    public void getResult() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://free.easychat.work/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<String> call = service.getResult("Погода в Казани сейчас?");
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                setTexterText(Integer.toString(response.code()));
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                setTexterText("Pizda");
            }
        });

    }
}