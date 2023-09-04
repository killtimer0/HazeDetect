package com.example.hazedetect;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;

import com.example.hazedetect.databinding.ActivityMainBinding;
import com.example.hazedetect.databinding.DailyWeatherBinding;
import com.example.hazedetect.databinding.DialogSelectServerBinding;
import com.github.matteobattilana.weather.PrecipType;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Base64;
import android.util.Log;
import android.util.Range;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> mActivityLauncher;
    private ActivityMainBinding binding;
    private final DailyWeatherBinding[] dailyWeatherBinding = new DailyWeatherBinding[7];
    private SharedPreferences mSharedPreference;

    private void setWindowImmersive() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    private void requestWeatherData(LocationInfo locationInfo) {
        binding.content.cityLabel.setText(locationInfo.name);
        WeatherDataManager.getInstance(this).requestData(locationInfo.id, data -> {
            if (data.ok) {
                updateMainPage(data.data);
            } else {
                Toast.makeText(this, data.error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
            Log.d("OkHttp", data.toString());
            binding.content.swipeRefresh.setRefreshing(false);
        });
    }

    private @Nullable LocationInfo locationInfo = null;

    private static String serializableToString(Serializable object) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = null;
        try {
            objectStream = new ObjectOutputStream(stream);
            objectStream.writeObject(object);
            return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (null != objectStream) {
                    objectStream.close();
                }
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static <T extends Serializable> T stringToSerializable(String string) {
        byte[] bytes = Base64.decode(string, Base64.DEFAULT);
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectStream = null;
        try {
            objectStream = new ObjectInputStream(stream);
            try {
                Object object = objectStream.readObject();
                try {
                    return (T) object;
                } catch (ClassCastException e) {
                    return null;
                }
            } catch (ClassNotFoundException e) {
                return null;
            }
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if (null != objectStream) {
                    objectStream.close();
                }
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private LineData lineDataTemp, lineDataHumidity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreference = getSharedPreferences("main_config", MODE_PRIVATE);

        ActivityResultContract<Intent, ActivityResult> contract = new ActivityResultContracts.StartActivityForResult();
        mActivityLauncher = registerForActivityResult(contract, result -> {
            Log.d("Result", result.toString());
            Intent intent = result.getData();
            if (null != intent) {
                Log.d("Result", intent.toString());
                LocationInfo locationInfo = (LocationInfo) intent.getSerializableExtra("location");
                if (null != locationInfo) {
                    Log.d("Result", locationInfo.toString());
                    mSharedPreference.edit()
                            .putString("location", serializableToString(locationInfo))
                            .apply();
                    this.locationInfo = locationInfo;
                    if (!isFinishing()) {
                        onRefreshWeatherData(false);
                    }
                }
            }
        });

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.content.indexCard.setOnClickListener(v -> {
            if (null != weatherInfoCardLink) {
                openLink(weatherInfoCardLink);
            }
        });
        binding.content.aqiCard.setOnClickListener(v -> {
            if (null != airInfoCardLink) {
                openLink(airInfoCardLink);
            }
        });
        binding.content.dailyCard.setOnClickListener(v -> {
            if (null != dailyWeatherInfoCardLink) {
                openLink(dailyWeatherInfoCardLink);
            }
        });
        binding.content.cityLabel.setOnClickListener(v -> {
            Intent intent = new Intent(this, PositioningActivity.class);
            mActivityLauncher.launch(intent);
        });

        for (int i = 0; i < dailyWeatherBinding.length; ++i) {
            View view = getLayoutInflater().inflate(R.layout.daily_weather, null);
            dailyWeatherBinding[i] = DailyWeatherBinding.bind(view);
            binding.content.dailyWeather.addView(view);
        }

        setSupportActionBar(binding.toolbar);

        binding.content.aqiView.setColors(new int[]{
                getResources().getColor(R.color.aqi_level_1),
                getResources().getColor(R.color.aqi_level_2),
                getResources().getColor(R.color.aqi_level_3),
                getResources().getColor(R.color.aqi_level_4),
        });
        binding.content.aqiView.setDataRange(new Range<>(0f, 300f));

        Entry placeHolder = new Entry(0, 0);
        ValueFormatter dateFormatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return LocalDate.ofEpochDay((long)value).format(DateTimeFormatter.ofPattern("MM/dd"));
            }
        };

        List<Entry> entriesTempMax = new ArrayList<>(1);
        List<Entry> entriesTempMin = new ArrayList<>(1);
        entriesTempMax.add(placeHolder); entriesTempMin.add(placeHolder);
        ValueFormatter tempFormatter = new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                return ((int) entry.getY()) + "℃";
            }
        };
        LineDataSet datasetTempMax = new LineDataSet(entriesTempMax, "");
        datasetTempMax.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        datasetTempMax.setValueFormatter(tempFormatter);
        datasetTempMax.setValueTextColor(getResources().getColor(R.color.main_text_color));
        datasetTempMax.setColor(getResources().getColor(R.color.chart_temp_max_color));
        datasetTempMax.setCircleColor(getResources().getColor(R.color.chart_temp_max_color));
        LineDataSet datasetTempMin = new LineDataSet(entriesTempMin, "");
        datasetTempMin.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        datasetTempMin.setValueFormatter(tempFormatter);
        datasetTempMin.setValueTextColor(getResources().getColor(R.color.main_text_color));
        datasetTempMin.setColor(getResources().getColor(R.color.chart_temp_min_color));
        datasetTempMin.setCircleColor(getResources().getColor(R.color.chart_temp_min_color));
        lineDataTemp = new LineData(datasetTempMax, datasetTempMin);
        binding.content.tempChart.getXAxis().setTextColor(getResources().getColor(R.color.main_text_color));
        binding.content.tempChart.setData(lineDataTemp);
        binding.content.tempChart.setTouchEnabled(false);
        binding.content.tempChart.getLegend().setEnabled(false);
        binding.content.tempChart.getDescription().setEnabled(false);
        hideAllGridLines(binding.content.tempChart.getXAxis());
        hideAllGridLines(binding.content.tempChart.getAxisLeft()).setDrawLabels(false);
        hideAllGridLines(binding.content.tempChart.getAxisRight()).setDrawLabels(false);
        binding.content.tempChart.getXAxis().setValueFormatter(dateFormatter);

        List<Entry> entriesHumidity = new ArrayList<>(1);
        entriesHumidity.add(placeHolder);
        ValueFormatter humidityFormatter = new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                return ((int) entry.getY()) + "%";
            }
        };
        LineDataSet datasetHumidity = new LineDataSet(entriesHumidity, "");
        datasetHumidity.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        datasetHumidity.setDrawFilled(true);
        datasetHumidity.setValueFormatter(humidityFormatter);
        datasetHumidity.setValueTextColor(getResources().getColor(R.color.main_text_color));
        datasetHumidity.setColor(getResources().getColor(R.color.chart_humidity_color));
        datasetHumidity.setCircleColor(getResources().getColor(R.color.chart_humidity_color));
        datasetHumidity.setFillColor(getResources().getColor(R.color.chart_humidity_color));
        datasetHumidity.setFillAlpha(60);
        lineDataHumidity = new LineData(datasetHumidity);
        binding.content.humidityChart.setData(lineDataHumidity);
        binding.content.humidityChart.setTouchEnabled(false);
        binding.content.humidityChart.getLegend().setEnabled(false);
        binding.content.humidityChart.getDescription().setEnabled(false);
        hideAllGridLines(binding.content.humidityChart.getXAxis()).setDrawLabels(false);
        hideAllGridLines(binding.content.humidityChart.getAxisLeft()).setDrawLabels(false);
        hideAllGridLines(binding.content.humidityChart.getAxisRight()).setDrawLabels(false);

        setWindowImmersive();
        updateBackground(-1);

        binding.content.swipeRefresh.setOnRefreshListener(() -> onRefreshWeatherData(true));

        // 可能返回`null`
        locationInfo = stringToSerializable(mSharedPreference.getString("location", ""));

        onRefreshWeatherData(false);
    }

    private AxisBase hideAllGridLines(AxisBase axis) {
        axis.setDrawAxisLine(false);
        axis.setDrawGridLines(false);
        return axis;
    }

    private void onRefreshWeatherData(boolean bRefresh) {
        WeatherDataManager weatherDataManager = WeatherDataManager.getInstance(this);
        if (weatherDataManager.isReceivingData()) {
            Toast.makeText(this, "正在请求数据…", Toast.LENGTH_LONG).show();
        } else {
            if (null != locationInfo) {
                if (!bRefresh) {
                    binding.content.swipeRefresh.setRefreshing(true);
                }
                requestWeatherData(locationInfo);
            } else if (bRefresh) {
                Toast.makeText(this, "请先选择城市", Toast.LENGTH_LONG).show();
                binding.content.swipeRefresh.setRefreshing(false);
            }
        }
    }

    private static final String[] windScaleText = {"无风", "软风", "轻风", "微风", "和风", "清风", "强风", "疾风", "大风", "烈风", "狂风", "暴风", "飓风"};
    private static final Range<Integer> windScaleRange = new Range<>(0, 13);
    private void updateMainPage(WeatherInfo data) {
        updateBackground(data.background);

        DailyWeather dailyWeather = data.dailyWeatherInfo.data[0];

        // 更新温度等信息
        binding.content.tempLabel.setText(String.valueOf(data.weatherInfo.temp));
        binding.content.weather.setText(data.weatherInfo.text);
        binding.content.tempMax.setText(String.valueOf(dailyWeather.tempMax));
        binding.content.tempMin.setText(String.valueOf(dailyWeather.tempMin));

        // 天气指数
        weatherInfoCardLink = data.weatherInfo.link;
        binding.content.tempFeel.setText(String.valueOf(data.weatherInfo.feelsLike));
        binding.content.humidity.setText(String.valueOf(data.weatherInfo.humidity));
        binding.content.precipitation.setText(String.valueOf(dailyWeather.precip));
        String uvLevel;
        if (dailyWeather.uvIndex <= 2) { uvLevel = "低"; }
        else if (dailyWeather.uvIndex <= 5) { uvLevel = "中等"; }
        else if (dailyWeather.uvIndex <= 7) { uvLevel = "高"; }
        else if (dailyWeather.uvIndex <= 10) { uvLevel = "非常高"; }
        else { uvLevel = "极高"; }
        binding.content.uv.setText(uvLevel);
        binding.content.visibility.setText(String.valueOf(data.weatherInfo.vis));
        binding.content.windForce.setText(windScaleText[windScaleRange.clamp(Integer.valueOf(data.weatherInfo.windScale))]);
        binding.content.windDir.setText(data.weatherInfo.windDir);

        // 更新AQI测量表
        airInfoCardLink = data.aqiInfo.link;
        binding.content.aqiView.setGaugeValue(data.aqiInfo.aqi);
        StringBuilder sb = new StringBuilder();
        int pos = sb.append(data.aqiInfo.category).length();
        sb.append('\n').append(String.valueOf(data.aqiInfo.aqi)).length();
        SpannableString spanString = new SpannableString(sb.toString());
        Object colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.main_text_color));
        spanString.setSpan(colorSpan, 0, pos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        RelativeSizeSpan sizeSpan = new RelativeSizeSpan(1.5f);
        spanString.setSpan(sizeSpan, 0, pos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.content.aqiView.setText(spanString);
        // 更新空气状况数据
        if ("NA".equals(data.aqiInfo.primary)) {
            binding.content.airPrimary.setText("无主要污染物");
        } else {
            binding.content.airPrimary.setText("主要污染物：" + data.aqiInfo.primary);
        }
        binding.content.airPm10.setText(String.valueOf(data.aqiInfo.pm10));
        binding.content.airPm25.setText(String.valueOf(data.aqiInfo.pm2p5));
        binding.content.airNO2.setText(String.valueOf(data.aqiInfo.no2));
        binding.content.airSO2.setText(String.valueOf(data.aqiInfo.so2));
        binding.content.airCO.setText(String.valueOf(data.aqiInfo.co));
        binding.content.airO3.setText(String.valueOf(data.aqiInfo.o3));

        // 更新多日预报
        dailyWeatherInfoCardLink = data.dailyWeatherInfo.link;
        for (int i = 0; i < dailyWeatherBinding.length; ++i) {
            String dateText;
            if (i == 0) { dateText = "今天"; }
            else if (i == 1) { dateText = "明天"; }
            else { dateText = data.dailyWeatherInfo.data[i].fxDate.format(DateTimeFormatter.ofPattern("EEEE", Locale.CHINA)); }
            dailyWeatherBinding[i].dateText.setText(dateText);
            dailyWeatherBinding[i].dateTimeText.setText(
                data.dailyWeatherInfo.data[i].fxDate.format(DateTimeFormatter.ofPattern("MM/dd"))
            );
            Drawable icon = ResourcesCompat.getDrawable(getResources(), getIconResource(data.dailyWeatherInfo.data[i].iconDay), null);
            dailyWeatherBinding[i].dateWeatherIcon.setImageDrawable(icon);
            dailyWeatherBinding[i].dateWeatherText.setText(data.dailyWeatherInfo.data[i].textDay);
            dailyWeatherBinding[i].datetempMax.setText(String.valueOf(data.dailyWeatherInfo.data[i].tempMax));
            dailyWeatherBinding[i].datetempMin.setText(String.valueOf(data.dailyWeatherInfo.data[i].tempMin));
        }

        // 更新温度湿度曲线图
        ILineDataSet datasetTempMax = lineDataTemp.getDataSets().get(0);
        ILineDataSet datasetTempMin = lineDataTemp.getDataSets().get(1);
        ILineDataSet datasetHumidity = lineDataHumidity.getDataSets().get(0);
        datasetTempMax.clear(); datasetTempMin.clear(); datasetHumidity.clear();
        for (DailyWeather weather: data.dailyWeatherInfo.data) {
            datasetTempMax.addEntry(new Entry(weather.fxDate.toEpochDay(), weather.tempMax));
            datasetTempMin.addEntry(new Entry(weather.fxDate.toEpochDay(), weather.tempMin));
            datasetHumidity.addEntry(new Entry(weather.fxDate.toEpochDay(), weather.humidity));
        }
        datasetTempMax.calcMinMax();
        datasetTempMin.calcMinMax();
        datasetHumidity.calcMinMax();
        lineDataTemp.notifyDataChanged();
        lineDataHumidity.notifyDataChanged();
        binding.content.tempChart.notifyDataSetChanged();
        binding.content.humidityChart.notifyDataSetChanged();
        binding.content.tempChart.invalidate();
        binding.content.humidityChart.invalidate();

        binding.content.mainInfoView.setVisibility(View.VISIBLE);
    }

    private void onSelectServer() {
        Log.d("MainActivity", "onSelectServer");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_select_server, null);
        DialogSelectServerBinding binding = DialogSelectServerBinding.bind(view);

        WeatherDataManager weatherDataManager = WeatherDataManager.getInstance(this);
        binding.layoutServerName.setPlaceholderText(weatherDataManager.getHostname());
        binding.editServerName.setText(weatherDataManager.getHostname());
        binding.layoutServerPort.setPlaceholderText(String.valueOf(weatherDataManager.getPort()));
        binding.editServerPort.setText(String.valueOf(weatherDataManager.getPort()));

        // 设置默认焦点
        binding.editServerName.setOnFocusChangeListener((v, f) -> {
            v.post(() -> {
                InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            });
        });
        binding.editServerName.requestFocus();

        // 设置标题和按钮
        builder.setView(view)
                .setTitle(R.string.action_select_server)
                .setPositiveButton(R.string.action_ok, (dialog, id) -> {
                    Editable content; String value;
                    String serverName = weatherDataManager.getHostname();
                    int serverPort = weatherDataManager.getPort();

                    content = binding.editServerName.getText();
                    if (null != content) {
                        value = content.toString().strip();
                        if (!value.isEmpty())
                            serverName = value;
                    }

                    content = binding.editServerPort.getText();
                    if (null != content) {
                        value = content.toString().strip();
                        if (!value.isEmpty()) {
                            try {
                                serverPort = Integer.parseInt(value);
                            } catch (NumberFormatException e) {
                                // Ignore
                            }
                        }
                    }

                    Log.d("SetDataSource", serverName + ":" + serverPort);
                    weatherDataManager.setDataSource(serverName, serverPort);
                })
                .setNegativeButton(R.string.action_cancel, (dialog, id) -> {})
                .create()
                .show();
    }

    private void openLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private @Nullable String airInfoCardLink = null;
    private @Nullable String weatherInfoCardLink = null;
    private @Nullable String dailyWeatherInfoCardLink = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // 对于自定义图标，只能手动设置颜色
        for (int i = 0; i < menu.size(); ++i) {
            Drawable drawable = menu.getItem(i).getIcon();
            if(null != drawable) {
                drawable.mutate()
                        .setColorFilter(
                                getResources().getColor(R.color.white),
                                PorterDuff.Mode.SRC_ATOP
                        );
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_select_server) {
            onSelectServer();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateBackground(int index) {
        switch (index) {
            default:
            case 0: // sunny
            {
                binding.background.setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_sunny));
                binding.weatherView.setWeatherData(PrecipType.CLEAR);
            }
            break;

            case 1: // sunny_night
            {
                binding.background.setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_sunny_night));
                binding.weatherView.setWeatherData(PrecipType.CLEAR);
            }
            break;

            case 2: // cloudy
            {
                binding.background.setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_cloudy));
                binding.weatherView.setWeatherData(PrecipType.CLEAR);
            }
            break;

            case 3: // cloudy_night
            {
                binding.background.setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_cloudy_night));
                binding.weatherView.setWeatherData(PrecipType.CLEAR);
            }
            break;

            case 4: // overcast
            {
                binding.background.setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_overcast));
                binding.weatherView.setWeatherData(PrecipType.CLEAR);
            }
            break;

            case 5: // overcast_night
            {
                binding.background.setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_overcast_night));
                binding.weatherView.setWeatherData(PrecipType.CLEAR);
            }
            break;

            case 6: // rain
            {
                binding.background.setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_rain));
                binding.weatherView.setWeatherData(PrecipType.RAIN);
            }
            break;

            case 7: // rain_night
            {
                binding.background.setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_rain_night));
                binding.weatherView.setWeatherData(PrecipType.RAIN);
            }
            break;

            case 8: // snow
            {
                binding.background.setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_snow));
                binding.weatherView.setWeatherData(PrecipType.SNOW);
            }
            break;

            case 9: // snow_night
            {
                binding.background.setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_snow_night));
                binding.weatherView.setWeatherData(PrecipType.SNOW);
            }
            break;

            case 10: // fog
            {
                binding.background.setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_fog));
                binding.weatherView.setWeatherData(PrecipType.CLEAR);
            }
            break;

            case 11: // fog_night
            {
                binding.background.setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_fog_night));
                binding.weatherView.setWeatherData(PrecipType.CLEAR);
            }
            break;

            case 12: // haze
            {
                binding.background.setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_haze));
                binding.weatherView.setWeatherData(PrecipType.CLEAR);
            }
            break;

            case 13: // haze_night
            {
                binding.background.setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_haze_night));
                binding.weatherView.setWeatherData(PrecipType.CLEAR);
            }
            break;
        }
    }

    public static int getIconResource(int iconId) {
        switch (iconId) {
            default:
            case 100: return R.drawable.icon_100;
            case 101: return R.drawable.icon_101;
            case 102: return R.drawable.icon_102;
            case 103: return R.drawable.icon_103;
            case 104: return R.drawable.icon_104;
            case 150: return R.drawable.icon_150;
            case 151: return R.drawable.icon_151;
            case 152: return R.drawable.icon_152;
            case 153: return R.drawable.icon_153;
            case 300: return R.drawable.icon_300;
            case 301: return R.drawable.icon_301;
            case 302: return R.drawable.icon_302;
            case 303: return R.drawable.icon_303;
            case 304: return R.drawable.icon_304;
            case 305: return R.drawable.icon_305;
            case 306: return R.drawable.icon_306;
            case 307: return R.drawable.icon_307;
            case 308: return R.drawable.icon_308;
            case 309: return R.drawable.icon_309;
            case 310: return R.drawable.icon_310;
            case 311: return R.drawable.icon_311;
            case 312: return R.drawable.icon_312;
            case 313: return R.drawable.icon_313;
            case 314: return R.drawable.icon_314;
            case 315: return R.drawable.icon_315;
            case 316: return R.drawable.icon_316;
            case 317: return R.drawable.icon_317;
            case 318: return R.drawable.icon_318;
            case 350: return R.drawable.icon_350;
            case 351: return R.drawable.icon_351;
            case 399: return R.drawable.icon_399;
            case 400: return R.drawable.icon_400;
            case 401: return R.drawable.icon_401;
            case 402: return R.drawable.icon_402;
            case 403: return R.drawable.icon_403;
            case 404: return R.drawable.icon_404;
            case 405: return R.drawable.icon_405;
            case 406: return R.drawable.icon_406;
            case 407: return R.drawable.icon_407;
            case 408: return R.drawable.icon_408;
            case 409: return R.drawable.icon_409;
            case 410: return R.drawable.icon_410;
            case 456: return R.drawable.icon_456;
            case 457: return R.drawable.icon_457;
            case 499: return R.drawable.icon_499;
            case 500: return R.drawable.icon_500;
            case 501: return R.drawable.icon_501;
            case 502: return R.drawable.icon_502;
            case 503: return R.drawable.icon_503;
            case 504: return R.drawable.icon_504;
            case 507: return R.drawable.icon_507;
            case 508: return R.drawable.icon_508;
            case 509: return R.drawable.icon_509;
            case 510: return R.drawable.icon_510;
            case 511: return R.drawable.icon_511;
            case 512: return R.drawable.icon_512;
            case 513: return R.drawable.icon_513;
            case 514: return R.drawable.icon_514;
            case 515: return R.drawable.icon_515;
            case 900: return R.drawable.icon_900;
            case 901: return R.drawable.icon_901;
            case 999: return R.drawable.icon_999;
        }
    }
}