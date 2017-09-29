/**
 * Project Name:Android_Car_Example
 * File Name:Utils.java
 * Package Name:com.amap.api.car.example
 * Date:2015年4月7日下午3:43:05
 */

package com.hnulab.sharebike.em.lib;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.hnulab.sharebike.em.R;
import com.hnulab.sharebike.em.entity.RedPackageLocation;

import java.util.ArrayList;
import java.util.List;


/**
 * description:添加红包和车
 * auther：xuewenliao
 * time：2017/9/25 10:20
 */
public class PutRedpackageUtils {

    public static ArrayList<Marker> markers = new ArrayList<Marker>();
    public static BitmapDescriptor bitmapDescriptor;
    public static boolean isMarkbike;
    //车的对象
    public static BitmapDescriptor bitmapBike = BitmapDescriptorFactory
            .fromResource(R.drawable.stable_cluster_marker_one_normal);
    //红包的对象
    public static BitmapDescriptor bitmapPackage = BitmapDescriptorFactory
            .fromResource(R.drawable.marker_red_package);


    /**
     * 添加红包和车
     * 81个网格红包
     * 车随机产生
     */
    public static void addEmulateData(AMap amap, LatLng center, List<RedPackageLocation> redPackageLocations) {
        if (markers.size() == 0) {

            double redLongitude;
            double redLatitude;
            for (int i = 0; i < redPackageLocations.size(); i++) {
                redLongitude = redPackageLocations.get(i).getE_longitude();
                redLatitude = redPackageLocations.get(i).getE_latitfude();

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.icon(bitmapPackage);

                markerOptions.position(new LatLng(redLatitude, redLongitude));
                Marker marker = amap.addMarker(markerOptions);
                markers.add(marker);

            }

//            for (int i = 0; i < 20; i++) {
//                if (i % 3 == 0) {
//                    bitmapDescriptor = bitmapBike;
//                } else {
//                    bitmapDescriptor = bitmapPackage;
//                }
//                double latitudeDelt;
//                double longtitudeDelt;
//                if (i % 2 == 0) {
//                    latitudeDelt = (Math.random() - 0.5) * 0.1;
//                    longtitudeDelt = (Math.random() - 0.5) * 0.1;
//                } else {
//                    latitudeDelt = (Math.random() - 0.5) * 0.01;
//                    longtitudeDelt = (Math.random() - 0.5) * 0.01;
//                }
//                MarkerOptions markerOptions = new MarkerOptions();
//                markerOptions.icon(bitmapDescriptor);
//
//                markerOptions.position(new LatLng(center.latitude + latitudeDelt, center.longitude + longtitudeDelt));
//                Marker marker = amap.addMarker(markerOptions);
//                markers.add(marker);
//            }
        } else {
            for (Marker marker : markers) {
                double redLongitude;
                double redLatitude;
                for (int i = 0; i < redPackageLocations.size(); i++) {
                    redLongitude = redPackageLocations.get(i).getE_longitude();
                    redLatitude = redPackageLocations.get(i).getE_latitfude();
                    marker.setPosition(new LatLng(redLatitude, redLongitude));

                }

//                double latitudeDelt = (Math.random() - 0.5) * 0.1;
//                double longtitudeDelt = (Math.random() - 0.5) * 0.1;
//                marker.setPosition(new LatLng(center.latitude + latitudeDelt, center.longitude + longtitudeDelt));

            }
        }
    }

    /**
     * 移除marker
     */
    public static void removeMarkers() {
        for (Marker marker : markers) {
            marker.remove();
            marker.destroy();
        }
        markers.clear();
    }


    public static void upData(AMap amap, LatLng center, List<RedPackageLocation> redPackageLocations) {
        double redLongitude;
        double redLatitude;
        for (int i = 0; i < redPackageLocations.size(); i++) {
            redLongitude = redPackageLocations.get(i).getE_longitude();
            redLatitude = redPackageLocations.get(i).getE_latitfude();

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(bitmapPackage);
            markerOptions.position(new LatLng(redLatitude, redLongitude));
            Marker marker = amap.addMarker(markerOptions);
            markers.add(marker);

        }
    }


}
  
