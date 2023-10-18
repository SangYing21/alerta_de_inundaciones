package com.example.alerta_de_inundaciones.clases;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONObject;

public class WeatherAPI {
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String TAG = "WeatherAPI";

    private final Context context;
    private final String apiKey;

    public WeatherAPI(Context context, String apiKey) {
        this.context = context;
        this.apiKey = apiKey;
    }

    public void getWeather(String city, final WeatherCallback callback) {
        // Crea una instancia de RequestQueue para enviar la solicitud a la API
        RequestQueue queue = Volley.newRequestQueue(context);

        // Construye la URL de la solicitud
        String url = BASE_URL + "?q=" + city + "&appid=" + apiKey + "&units=metric";

        // Crea una solicitud de tipo JsonObjectRequest para obtener la respuesta en formato JSON
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Procesa la respuesta JSON y muestra los resultados

                        Gson gson = new Gson();
                        WeatherResponse weatherResponse = gson.fromJson(response.toString(), WeatherResponse.class);

                        double temperature = weatherResponse.getMainData().getTemperature();
                        int humidity = weatherResponse.getMainData().getHumidity();
                        double windSpeed = weatherResponse.getWindData().getSpeed();
                        String description = weatherResponse.getWeatherDataList().get(0).getDescription();

                        Log.d(TAG, "Respose: " + response);
                        Log.d(TAG, "Temperature: " + temperature + "°C");
                        Log.d(TAG, "Humidity: " + humidity + "%");
                        Log.d(TAG, "Wind speed: " + windSpeed + " m/s");
                        Log.d(TAG, "Description: " + description);

                        // Invoca el método de la interfaz para retornar los resultados
                        callback.onSuccess(temperature, humidity, windSpeed, description);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error: " + error.getMessage());

                        // Invoca el método de la interfaz para manejar el error
                        callback.onError(error.getMessage());
                    }
                });

        // Agrega la solicitud a la cola de solicitudes
        queue.add(jsonObjectRequest);
    }

    public interface WeatherCallback {
        void onSuccess(double temperature, int humidity, double windSpeed, String descripcion);

        void onError(String message);
    }
}
