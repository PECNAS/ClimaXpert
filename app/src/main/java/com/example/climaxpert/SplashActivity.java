package com.example.climaxpert;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.climaxpert.AiApi.OpenAIApiClient;
import com.example.climaxpert.AiApi.OpenAIResponse;
import com.example.climaxpert.WeatherApi.WeatherApiClient;
import com.example.climaxpert.WeatherApi.WeatherResponse;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        ((TextView)findViewById(R.id.load_status)).setText("Загрузка приложения...");
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ((TextView)findViewById(R.id.load_status)).setText("Обработка...");
        binding_swipes();

        getWeather("Казань");
    }

    public void getWeather(String city) {
        ((TextView)findViewById(R.id.load_status)).setText("Получение погоды...");
        WeatherApiClient weatherClient = new WeatherApiClient();

        weatherClient.getWeatherByCity(city, new WeatherApiClient.WeatherCallback() {
            @Override
            public void onSuccess(WeatherResponse response) {
                String temp = String.valueOf(response.getMain().getTemp());
                String feelsLike = String.valueOf(response.getMain().getFeelsLike());
                String humidity = String.valueOf(response.getMain().getHumidity());
                String desc = response.getWeather()[0].getDescription();

                String weatherInfo = String.format(
                        "Город: %s\nТемпература: %s°C\nОщущается как: %s°C\nВлажность: %s%%\nПогода: %s",
                        city,
                        temp, feelsLike,
                        humidity,  desc
                );

                SharedPreferences sh = getApplicationContext().getSharedPreferences(
                        "Weather", Context.MODE_PRIVATE
                );
                SharedPreferences.Editor editor = sh.edit();

                editor.putString("Temp", temp);
                editor.putString("FeelsLike", feelsLike);
                editor.putString("Humidity", humidity);
                editor.putString("Description", desc);

                editor.commit();

                Log.e("[Splash Log]", weatherInfo);

                getAIAnswer(
                        "У меня есть данные о погоде в городе " + city + ". " +
                                "Используя эти данные, сформируй ответ, в следующем формате: \n" +
                                "Сегодня я рекомендую надеть: [" +
                                "нужно ли надевать головной убор и если да, то какой , " +
                                "что надеть в качестве верхней одежды, " +
                                "что надеть в качестве нижней одежды," +
                                "какую обувь стоит надеть]\n" +
                                "[Зонт пригодится/Зонт не нужен]\n" +
                                "Комфортное время нахождения на улице: [Сколько времени можно провести на улице," +
                                "в указанной тобой одежде, не замерзнув и не перегревшись]\n" +
                                "Не добавляй в ответ ничего лишнего. Пиши только ту информацию, которая" +
                                " указана мною. В ответе не должно быть не русских букв," +
                                "слов 'Понятно', 'Ясно', 'Ладно' и других междометий \n\n" +
                                "Данные о погоде: " + weatherInfo
                );
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("WEATHER_ERROR", "Ошибка запроса", e);
            }
        });
    }

    public void getAIAnswer(String request) {
        ((TextView)findViewById(R.id.load_status)).setText("Спрашиваем нейросеть...");
        OpenAIApiClient client = new OpenAIApiClient();
        client.getChatResponse(
            request,
            new OpenAIApiClient.Callback() {
                @Override
                public void onSuccess(OpenAIResponse response) {
                    String assistantReply = response.getAssistantReply();
                    if (assistantReply != null) {
                        Log.e("[Splash Log]", "Ответ на запрос: " + assistantReply);

                        SharedPreferences sh = getApplicationContext().getSharedPreferences(
                                "Weather", Context.MODE_PRIVATE
                        );
                        SharedPreferences.Editor editor = sh.edit();
                        editor.putString("HomeAssistant", assistantReply);
                        editor.apply();

                    } else {
                        Log.e("[Splash Log]", "Нет ответа от сервера");
                        SharedPreferences sh = getApplicationContext().getSharedPreferences(
                                "Weather", Context.MODE_PRIVATE
                        );
                        SharedPreferences.Editor editor = sh.edit();
                        editor.putString("HomeAssistant", "Не удалось получить данные от сервера!");
                        editor.apply();
                    }

                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("[Splash Log]", "Ошибка запроса", e);
                    SharedPreferences sh = getApplicationContext().getSharedPreferences(
                            "Weather", Context.MODE_PRIVATE
                    );
                    SharedPreferences.Editor editor = sh.edit();
                    editor.clear();
                    editor.putString("HomeAssistant", "Ошибка запроса!");
                    editor.apply();

                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
                }
            }
        );
    }

    private void binding_swipes() {
        findViewById(R.id.main).setOnTouchListener(
            new OnSwipeTouchListener(this) {
                @Override
                public void onSwipeTop() {
                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
                }
        });
    }
}