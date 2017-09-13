package com.hnulab.sharebike.em.base;

/**
 * Description：
 * Auther：luojie
 * E-mail：luojie@hnu.edu.cn
 * Time：2017/9/12 19:30
 */
public class EnvData {
          //时间
          String time;

          public String getTime() {
                    return time;
          }

          public void setTime(String time) {
                    this.time = time;
          }

          public String getCo2() {
                    return Co2;
          }

          public void setCo2(String co2) {
                    Co2 = co2;
          }

          public double getLongitude() {
                    return longitude;
          }

          public void setLongitude(double longitude) {
                    this.longitude = longitude;
          }

          public double getLatitude() {
                    return latitude;
          }

          public void setLatitude(double latitude) {
                    this.latitude = latitude;
          }

          //二氧化碳浓度
          String Co2;
          //经度
          double longitude;
          //纬度
          double latitude;
}
