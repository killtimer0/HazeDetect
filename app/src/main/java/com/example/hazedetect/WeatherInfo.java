package com.example.hazedetect;

import java.time.LocalDateTime;

public class WeatherInfo {
    /**
     * 进行查询时的日期
     */
    public LocalDateTime currentDate;

    /**
     * 应该显示的背景图片id
     */
    public int background;

    /**
     * 当前天气信息
     */
    public CurrentWeatherInfo weatherInfo;

    /**
     * 空气质量信息
     */
    public AqiInfo aqiInfo;

    /**
     * 未来7天的天气变化信息
     */
    public DailyWeatherInfo dailyWeatherInfo;

    @Override
    public String toString() {
        return "WeatherInfo{" +
                "currentDate=" + currentDate +
                ", background=" + background +
                ", weatherInfo=" + weatherInfo +
                ", aqiInfo=" + aqiInfo +
                ", dailyWeatherInfo=" + dailyWeatherInfo +
                '}';
    }
}
