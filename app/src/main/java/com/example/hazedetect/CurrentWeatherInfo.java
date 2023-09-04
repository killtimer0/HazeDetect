package com.example.hazedetect;

import androidx.annotation.Nullable;

import com.google.gson.annotations.JsonAdapter;

import java.time.LocalDateTime;

public class CurrentWeatherInfo {
    /**
     * 当前数据的响应式页面，便于嵌入网站或应用
     */
    public String link;

    /**
     * 当前API的最近更新时间
     */
    public LocalDateTime updateTime;

    /**
     * 数据观测时间
     */
    public LocalDateTime obsTime;

    /**
     * 温度，默认单位：摄氏度
     */
    public int temp;

    /**
     * 体感温度，默认单位：摄氏度
     */
    public int feelsLike;

    /**
     * 天气状况和图标的代码，图标可通过<a href="https://dev.qweather.com/docs/resource/icons/">天气状况和图标</a>下载
     */
    public int icon;

    /**
     * 天气状况的文字描述，包括阴晴雨雪等天气状态的描述
     */
    public String text;

    /**
     * 风向360角度
     */
    public int wind360;

    /**
     * 风向
     */
    public String windDir;

    /**
     * 风力等级
     */
    public String windScale;

    /**
     * 风速，公里/小时
     */
    public int windSpeed;

    /**
     * 相对湿度，百分比数值
     */
    public int humidity;

    /**
     * 当前小时累计降水量，默认单位：毫米
     */
    public double precip;

    /**
     * 大气压强，默认单位：百帕
     */
    public int pressure;

    /**
     * 能见度，默认单位：公里
     */
    public int vis;

    /**
     * 云量，百分比数值。可能为空
     */
    public String cloud;

    /**
     * 露点温度。可能为空
     */
    public String dew;

    @Override
    public String toString() {
        return "CurrentWeatherInfo{" +
                "updateTime=" + updateTime +
                ", temp=" + temp +
                '}';
    }
}
