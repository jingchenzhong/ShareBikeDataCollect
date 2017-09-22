package com.hnulab.sharebike.em.base;

/**
 * Description：
 * Auther：luojie
 * E-mail：luojie@hnu.edu.cn
 * Time：2017/9/12 19:30
 */
public class EnvData {
    //经度
    double e_longitude;
    //纬度
    double e_latitfude;
    //二氧化碳浓度 单位%
    double e_co2;

    //PM2.5
    double e_pm2_5;

    //PM10
    double e_pm10;
    //湿度 单位PPM
    double e_humidity;
    //温度
    double e_temperature;
    //地址
    String e_address;
    //城市
    String e_city;
    //时间
    String e_time;
    //PM5
    double e_pm5;

    public double getE_longitude() {
        return e_longitude;
    }

    public void setE_longitude(double e_longitude) {
        this.e_longitude = e_longitude;
    }

    public double getE_latitfude() {
        return e_latitfude;
    }

    public void setE_latitfude(double e_latitfude) {
        this.e_latitfude = e_latitfude;
    }

    public double getE_co2() {
        return e_co2;
    }

    public void setE_co2(double e_co2) {
        this.e_co2 = e_co2;
    }

    public double getE_pm2_5() {
        return e_pm2_5;
    }

    public void setE_pm2_5(double e_pm2_5) {
        this.e_pm2_5 = e_pm2_5;
    }

    public double getE_pm5() {
        return e_pm5;
    }

    public void setE_pm5(double e_pm5) {
        this.e_pm5 = e_pm5;
    }

    public double getE_pm10() {
        return e_pm10;
    }

    public void setE_pm10(double e_pm10) {
        this.e_pm10 = e_pm10;
    }

    public double getE_humidity() {
        return e_humidity;
    }

    public void setE_humidity(double e_humidity) {
        this.e_humidity = e_humidity;
    }

    public double getE_temperature() {
        return e_temperature;
    }

    public void setE_temperature(double e_temperature) {
        this.e_temperature = e_temperature;
    }

    public String getE_address() {
        return e_address;
    }

    public void setE_address(String e_address) {
        this.e_address = e_address;
    }

    public String getE_city() {
        return e_city;
    }

    public void setE_city(String e_city) {
        this.e_city = e_city;
    }

    public String getE_time() {
        return e_time;
    }

    public void setE_time(String e_time) {
        this.e_time = e_time;
    }
    @Override
    public String toString() {
        return "EnvData{" +
             "e_longitude=" + e_longitude +
             ", e_latitfude=" + e_latitfude +
             ", e_co2=" + e_co2 +
             ", e_pm2_5=" + e_pm2_5 +
             ", e_pm5=" + e_pm5 +
             ", e_pm10=" + e_pm10 +
             ", e_humidity=" + e_humidity +
             ", e_temperature=" + e_temperature +
             ", e_address='" + e_address + '\'' +
             ", e_city='" + e_city + '\'' +
             ", e_time='" + e_time + '\'' +
             '}';
    }



}
