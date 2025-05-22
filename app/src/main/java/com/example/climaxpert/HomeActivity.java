package com.example.climaxpert;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.climaxpert.AiApi.OpenAIApiClient;
import com.example.climaxpert.AiApi.OpenAIResponse;
import com.example.climaxpert.WeatherApi.WeatherApiClient;
import com.example.climaxpert.WeatherApi.WeatherResponse;

import org.w3c.dom.Text;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding_swipes();
        binding_clicks();
        fillWeatherInfo();
    }

    private void fillWeatherInfo() {
        SharedPreferences sh = getSharedPreferences(
                "Weather", Context.MODE_PRIVATE
        );
        String homeAssistant = sh.getString("HomeAssistant", "Не удалось получить данные");
        String temp = sh.getString("Temp", "Н.о.");
        String feelsLike = sh.getString("FeelsLike", "Н.о.");
        String humidity = sh.getString("Humidity", "Н.о.");
        String desc = sh.getString("Description", "Н.о.");

        ((TextView)findViewById(R.id.home_assistant_text)).setText(homeAssistant);
        ((TextView)findViewById(R.id.tempTextView)).setText(temp + "°C");
        ((TextView)findViewById(R.id.air_state)).setText(
                desc.substring(0, 1)
                        .toUpperCase()
                        .concat(desc.substring(1))
        );
        ((TextView)findViewById(R.id.humidityTextView)).setText(humidity + "%");
    }

    private void binding_clicks() {
        findViewById(R.id.constraintLayout).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                }
        );

        findViewById(R.id.update_btn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getWeather("Казань");
                    }
                }
        );
    }

    private void binding_swipes() {
        findViewById(R.id.main).setOnTouchListener(
            new OnSwipeTouchListener(this) {
                @Override
                public void onSwipeTop() {
                    Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
                }
            }
        );
    }
    public void getWeather(String city) {
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

                Log.e("[Home Log]", weatherInfo);

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
        OpenAIApiClient client = new OpenAIApiClient();
        client.getChatResponse(
                request,
                new OpenAIApiClient.Callback() {
                    @Override
                    public void onSuccess(OpenAIResponse response) {
                        String assistantReply = response.getAssistantReply();
                        if (assistantReply != null) {
                            Log.e("[Home Log]", "Ответ на запрос: " + assistantReply);

                            SharedPreferences sh = getApplicationContext().getSharedPreferences(
                                    "Weather", Context.MODE_PRIVATE
                            );
                            SharedPreferences.Editor editor = sh.edit();
                            editor.putString("HomeAssistant", assistantReply);
                            editor.apply();

                        } else {
                            Log.e("[Home Log]", "Нет ответа от сервера");
                            SharedPreferences sh = getApplicationContext().getSharedPreferences(
                                    "Weather", Context.MODE_PRIVATE
                            );
                            SharedPreferences.Editor editor = sh.edit();
                            editor.putString("HomeAssistant", "Не удалось получить данные от сервера!");
                            editor.apply();
                        }

                        fillWeatherInfo();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("[Home Log]", "Ошибка запроса", e);
                        SharedPreferences sh = getApplicationContext().getSharedPreferences(
                                "Weather", Context.MODE_PRIVATE
                        );
                        SharedPreferences.Editor editor = sh.edit();
                        editor.clear();
                        editor.putString("HomeAssistant", "Ошибка запроса!");
                        editor.apply();

                        fillWeatherInfo();
                    }
                }
        );
    }
}