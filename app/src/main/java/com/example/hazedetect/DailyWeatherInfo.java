package com.example.hazedetect;

import java.util.Arrays;
import java.util.Date;

public class DailyWeatherInfo {
    /**
     * 当前数据的响应式页面，便于嵌入网站或应用
     */
    public String link;

    /**
     * 当前API的最近更新时间
     */
    public Date updateTime;

    /**
     * 未来24小时的天气变化情况
     */
    public DailyWeather[] data;

    @Override
    public String toString() {
        return "DailyWeatherInfo{" +
                "link='" + link + '\'' +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
