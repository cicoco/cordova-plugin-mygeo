package org.apache.cordova.geolocation;

import android.location.Location;

/**
 * Author：tafiagu on 2015/12/29 21:33
 */
public interface IGPSListener {
    /**
     * 地理位置获取成功
     *
     * @param location
     */
    void onGPSSuccess(Location location);

    /**
     * 地理位置获取失败
     */
    void onGPSFailed();
}
