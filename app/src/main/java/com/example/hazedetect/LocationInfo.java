package com.example.hazedetect;

import java.io.Serializable;
import java.util.TimeZone;

public class LocationInfo implements Serializable {
    /**
     * 地区/城市名称
     */
    public String name;

    /**
     * 地区/城市ID
     */
    public String id;

    /**
     * 地区/城市纬度
     */
    public float lat;

    /**
     * 地区/城市经度
     */
    public float lon;

    /**
     * 地区/城市的上级行政区划名称
     */
    public String adm2;

    /**
     * 地区/城市所属一级行政区域
     */
    public String adm1;

    /**
     * 地区/城市所属国家名称
     */
    public String country;

    /**
     * 地区/城市所在时区
     */
    public TimeZone tz;

    /**
     * 地区/城市是否当前处于夏令时。1 表示当前处于夏令时，0 表示当前不是夏令时。
     */
    public boolean isDst;

    /**
     * 地区/城市的属性
     */
    public String type;

    /**
     * 地区评分
     */
    public int rank;

    /**
     * 该地区的天气预报网页链接，便于嵌入你的网站或应用
     */
    public String fxLink;

    @Override
    public String toString() {
        return "LocationInfo{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
