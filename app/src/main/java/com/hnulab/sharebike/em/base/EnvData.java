package com.hnulab.sharebike.em.base;

/**
 * Description：
 * Auther：luojie
 * E-mail：luojie@hnu.edu.cn
 * Time：2017/9/12 19:30
 */
public class EnvData {
    //经度
    String e_longitude;
    //纬度
    String e_latitfude;
    //二氧化碳浓度
    int e_co2;
    //地址
    String e_address;
    //城市
    String e_city;
    //时间
    String e_time;

    public String getE_longitude() {
        return e_longitude;
    }

    public void setE_longitude(String e_longitude) {
        this.e_longitude = e_longitude;
    }

    public String getE_latitfude() {
        return e_latitfude;
    }

    public void setE_latitfude(String e_latitfude) {
        this.e_latitfude = e_latitfude;
    }

    public int getE_co2() {
        return e_co2;
    }

    public void setE_co2(int e_co2) {
        this.e_co2 = e_co2;
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
                "e_longitude='" + e_longitude + '\'' +
                ", e_latitfude='" + e_latitfude + '\'' +
                ", e_co2=" + e_co2 +
                ", e_address='" + e_address + '\'' +
                ", e_city='" + e_city + '\'' +
                ", e_time='" + e_time + '\'' +
                '}';
    }
}
