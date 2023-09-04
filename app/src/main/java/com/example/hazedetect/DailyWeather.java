package com.example.hazedetect;

import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.time.LocalTime;

public class DailyWeather {
    /**
     * 预报日期
     */
    public LocalDate fxDate;

    /**
     * 日出时间，在高纬度地区可能为空
     */
    @Nullable
    public LocalTime sunrise;

    /**
     * 日落时间，在高纬度地区可能为空
     */
    @Nullable
    public LocalTime sunset;

    /**
     * 当天月升时间，可能为空
     */
    @Nullable
    public LocalTime moonrise;

    /**
     * 当天月落时间，可能为空
     */
    @Nullable
    public LocalTime moonset;

    /**
     * 月相名称
     */
    public String moonPhase;

    /**
     * 月相图标代码，图标可通过<a href="https://dev.qweather.com/docs/resource/icons/">天气状况和图标</a>下载
     */
    public int moonPhaseIcon;

    /**
     * 预报当天最高温度
     */
    public int tempMax;

    /**
     * 预报当天最低温度
     */
    public int tempMin;

    /**
     * 预报白天天气状况的图标代码，图标可通过<a href="https://dev.qweather.com/docs/resource/icons/">天气状况和图标</a>下载
     */
    public int iconDay;

    /**
     * 预报白天天气状况文字描述，包括阴晴雨雪等天气状态的描述
     */
    public String textDay;

    /**
     * 预报夜间天气状况的图标代码，图标可通过<a href="https://dev.qweather.com/docs/resource/icons/">天气状况和图标</a>下载
     */
    public int iconNight;

    /**
     * 预报晚间天气状况文字描述，包括阴晴雨雪等天气状态的描述
     */
    public String textNight;

    /**
     * 预报白天风向360角度
     */
    public int wind360Day;

    /**
     * 预报白天风向
     */
    public String windDirDay;

    /**
     * 预报白天风力等级
     */
    public String windScaleDay;

    /**
     * 预报白天风速，公里/小时
     */
    public int windSpeedDay;

    /**
     * 预报夜间风向360角度
     */
    public int wind360Night;

    /**
     * 预报夜间风向
     */
    public String windDirNight;

    /**
     * 预报夜间风力等级
     */
    public String windScaleNight;

    /**
     * 预报夜间风速，公里/小时
     */
    public int windSpeedNight;

    /**
     * 预报当天总降水量，默认单位：毫米
     */
    public double precip;

    /**
     * 紫外线强度指数
     */
    public int uvIndex;

    /**
     * 相对湿度，百分比数值
     */
    public int humidity;

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

    @Override
    public String toString() {
        return "DailyWeather{" +
                "fxDate=" + fxDate +
                '}';
    }
}
