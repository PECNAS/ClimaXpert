package com.example.climaxpert.WeatherApi;

import okhttp3.*;
import com.google.gson.Gson;
import java.io.IOException;

public class WeatherApiClient {
    private static final String API_KEY = "63085ab9b973a44fa9e3768349bcfb44";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public interface WeatherCallback {
        void onSuccess(WeatherResponse response);
        void onFailure(Exception e);
    }

    public void getWeatherByCity(String cityName, WeatherCallback callback) {
        HttpUrl url = HttpUrl.parse(BASE_URL).newBuilder()
                .addQueryParameter("q", cityName)
                .addQueryParameter("appid", API_KEY)
                .addQueryParameter("units", "metric") // для получения в °C
                .addQueryParameter("lang", "ru")     // русский язык ответа
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure(new IOException("Ошибка: " + response.code()));
                    return;
                }

                String responseData = response.body().string();
                WeatherResponse weatherResponse = gson.fromJson(responseData, WeatherResponse.class);
                callback.onSuccess(weatherResponse);
            }
        });
    }
}