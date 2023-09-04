package com.example.hazedetect;

import java.time.LocalDateTime;

public class AqiInfo {
    /**
     * 当前数据的响应式页面，便于嵌入网站或应用
     */
    public String link;

    /**
     * 当前API的最近更新时间
     */
    public LocalDateTime updateTime;

    /**
     * 空气质量数据发布时间
     */
    public LocalDateTime pubTime;

    /**
     *  空气质量指数
     */
    public int aqi;

    /**
     * 空气质量指数等级 <br>
     *  <br>
     * 1    0-50	一级	优   	绿色 <br>
     * 2  51-100	二级	良	    黄色 <br>
     * 3 101-150	三级	轻度污染	橙色 <br>
     * 4 151-200	四级	中度污染	红色 <br>
     * 5 201-300	五级	重度污染	紫色 <br>
     * 6 >300	    六级	严重污染	褐红色
     */
    public int level;

    /**
     * 空气质量指数级别
     */
    public String category;

    /**
     * 空气质量的主要污染物，空气质量为优时，返回值为NA
     */
    public String primary;

    /**
     * PM10
     */
    public int pm10;

    /**
     * PM2.5
     */
    public int pm2p5;

    /**
     * 二氧化氮
     */
    public int no2;

    /**
     * 二氧化硫
     */
    public int so2;

    /**
     * 一氧化碳
     */
    public double co;

    /**
     * 臭氧
     */
    public int o3;

    @Override
    public String toString() {
        return "AqiInfo{" +
                "aqi=" + aqi +
                '}';
    }
}
