package com.example.hazedetect;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.annotations.EverythingIsNonNull;

public class WeatherDataManager {
    private final SharedPreferences sharedPreferences;
    private String hostname = "localhost";
    private int port = 8193;

    private static WeatherDataManager g_weatherDataManager = null;
    public static WeatherDataManager getInstance(Context context) {
        if (null == g_weatherDataManager) {
            SharedPreferences sp = context.getSharedPreferences("weather_config", MODE_PRIVATE);
            g_weatherDataManager = new WeatherDataManager(sp);
        }
        return g_weatherDataManager;
    }

    private void invokeCallback(Runnable runnable) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(runnable);
    }

    public WeatherDataManager(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        hostname = sharedPreferences.getString("server_addr", hostname);
        port = sharedPreferences.getInt("server_port", port);
        Log.d("ServerConfig", hostname + ":" + port);
    }

    private void tryStopReceive() {
        if (null != mCall[0]) {
            mCall[0].cancel();
        }
    }

    public void setDataSource(String hostname, int port) {
        tryStopReceive();
        this.hostname = hostname;
        this.port = port;
        sharedPreferences.edit()
                .putString("server_addr", hostname)
                .putInt("server_port", port)
                .apply();
        Log.d("ServerConfig", hostname + ":" + port);
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public static class LocationResult {
        public final boolean ok;
        public final IOException error;
        public final LocationInfo data;

        private LocationResult(IOException error) {
            ok = false;
            this.error = error;
            data = null;
        }

        private LocationResult(LocationInfo data) {
            ok = true;
            error = null;
            this.data = data;
        }

        @Override
        @NonNull
        public String toString() {
            return (ok ? "Ok" : "Err") + ": " + (ok ? data : error);
        }
    }

    public interface LocationListener {
        void onAcquireLocation(LocationResult result);
    }

    public void queryLocation(double longitude, double latitude, LocationListener listener) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(hostname)
                .port(port)
                .addPathSegment("haze_detect/city_from")
                .addQueryParameter("longitude", String.valueOf(longitude))
                .addQueryParameter("latitude", String.valueOf(latitude))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            @EverythingIsNonNull
            public void onFailure(Call call, IOException e) {
                invokeCallback(() -> listener.onAcquireLocation(new LocationResult(e)));
            }

            @Override
            @EverythingIsNonNull
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    IOException e = new IOException(response.message());
                    invokeCallback(() -> listener.onAcquireLocation(new LocationResult(e)));
                    return;
                }
                String jsonData;
                try {
                    ResponseBody body = response.body();
                    assert body != null;
                    jsonData = body.string();
                } catch (IOException e) {
                    invokeCallback(() -> listener.onAcquireLocation(new LocationResult(e)));
                    return;
                }
                LocationResponse locationInfo = new GsonBuilder()
                        .registerTypeAdapter(TimeZone.class, new TimeZoneSerializer())
                        .create()
                        .fromJson(jsonData, LocationResponse.class);
                if (null != locationInfo.location) {
                    invokeCallback(() -> listener.onAcquireLocation(new LocationResult(locationInfo.location)));
                }
            }
        });
    }

    public static class LocationQueryResult {
        public final boolean ok;
        public final IOException error;
        public final LocationInfo[] data;

        private LocationQueryResult(IOException error) {
            ok = false;
            this.error = error;
            data = null;
        }

        private LocationQueryResult(LocationInfo[] data) {
            ok = true;
            error = null;
            this.data = data;
        }

        @Override
        @NonNull
        public String toString() {
            return (ok ? "Ok" : "Err") + ": " + (ok ? Arrays.toString(data) : error);
        }
    }

    public interface QueryListener {
        void onQueryResult(LocationQueryResult result);
    }

    public void queryCities(String query, QueryListener listener) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(hostname)
                .port(port)
                .addPathSegment("haze_detect/city_search")
                .addQueryParameter("q", query)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            @EverythingIsNonNull
            public void onFailure(Call call, IOException e) {
                invokeCallback(() -> listener.onQueryResult(new LocationQueryResult(e)));
            }

            @Override
            @EverythingIsNonNull
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    IOException e = new IOException(response.message());
                    invokeCallback(() -> listener.onQueryResult(new LocationQueryResult(e)));
                    return;
                }
                String jsonData;
                try {
                    ResponseBody body = response.body();
                    assert body != null;
                    jsonData = body.string();
                } catch (IOException e) {
                    invokeCallback(() -> listener.onQueryResult(new LocationQueryResult(e)));
                    return;
                }
                LocationInfo[] locationInfo = new GsonBuilder()
                        .registerTypeAdapter(TimeZone.class, new TimeZoneSerializer())
                        .create()
                        .fromJson(jsonData, LocationInfo[].class);
                invokeCallback(() -> listener.onQueryResult(new LocationQueryResult(locationInfo)));
            }
        });
    }

    public static class Data {
        public final boolean ok;
        public final IOException error;
        public final WeatherInfo data;

        private Data(IOException e) {
            ok = false;
            error = e;
            data = null;
        }

        private Data(WeatherInfo data) {
            ok = true;
            error = null;
            this.data = data;
        }

        @Override
        @NonNull
        public String toString() {
            return (ok ? "Ok" : "Err") + ": " + (ok ? data : error);
        }
    }

    public interface DataListener {
        void onReceiveData(Data data);
    }

    private final Call[] mCall = {null};

    public boolean isReceivingData() {
        return null != mCall[0];
    }


    public void requestData(String location, DataListener listener) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(hostname)
                .port(port)
                .addPathSegment("haze_detect/weather")
                .addQueryParameter("location", location)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        mCall[0] = client.newCall(request);
        mCall[0].enqueue(new Callback() {
            @Override
            @EverythingIsNonNull
            public void onFailure(Call call, IOException e) {
                mCall[0] = null;
                invokeCallback(() -> listener.onReceiveData(new Data(e)));
            }

            @Override
            @EverythingIsNonNull
            public void onResponse(Call call, Response response) {
                mCall[0] = null;
                if (!response.isSuccessful()) {
                    IOException e = new IOException(response.message());
                    invokeCallback(() -> listener.onReceiveData(new Data(e)));
                    return;
                }
                String jsonData;
                try {
                    ResponseBody body = response.body();
                    assert body != null;
                    jsonData = body.string();
                } catch (IOException e) {
                    invokeCallback(() -> listener.onReceiveData(new Data(e)));
                    return;
                }
                WeatherInfo weatherInfo = new GsonBuilder()
                        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                        .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
                        .registerTypeAdapter(LocalTime.class, new LocalTimeDeserializer())
                        .create()
                        .fromJson(jsonData, WeatherInfo.class);
                invokeCallback(() -> listener.onReceiveData(new Data(weatherInfo)));
            }
        });
    }
}
