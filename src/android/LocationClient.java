package org.apache.cordova.geolocation;

import android.content.Context;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;


/**
 * Author：tafiagu on 2015/12/29 21:22
 */
public class LocationClient implements LocationListener {
    private static final String TAG = LocationClient.class.getSimpleName();

    private LocationManager mLocationMgr;

    /**
     * 临时的location
     */
    private Location mLocation = null;

    /**
     * 最小间隔刷新时间
     */
    private long mInterval = 1000;

    /**
     * 定位监听
     */
    private IGPSListener mGPSCallback;

    /**
     * 最小位置变化距离
     */
    private float mMinDistance = 0;


    /**
     * 两个地理位置的获取时间校准
     */
    private static final int DELTA_TIME = 1000 * 60 * 2;


    /**
     * 精准模式（GPS）
     */
    public static final int FINE = 1;
    /**
     * 粗略模式（Network）
     */
    public static final int COARSE = 2;
    /**
     * 混合模式
     */
    public static final int BOTH = 3;

    /**
     * 定位Provider模式
     */
    private int mLocateMode = COARSE;

    private Context mContext;

    /**
     * 逆地理编码器
     */
    private Geocoder mGeoCoder;

    /**
     * GPS provider是否可用
     */
    private boolean mGPSEnabled = true;

    /**
     * network provider是否可用
     */
    private boolean mNetWorkEnabled = true;

    public LocationClient(Context context) {
        mContext = context.getApplicationContext();
        mLocationMgr = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mGeoCoder = new Geocoder(mContext);
    }


    public void setGPSCallback(IGPSListener callback) {
        if (null == mGPSCallback) {
            mGPSCallback = callback;
        }
    }


    public void removeGPSCallback() {
        if (null != mGPSCallback) {
            mGPSCallback = null;
        }
    }

    public Geocoder getGeoCoder() {
        if (null == mGeoCoder) {
            mGeoCoder = new Geocoder(mContext);
        }
        return mGeoCoder;
    }


    /**
     * 设置GPS Provider的获取模式
     *
     * @param mode
     * @see #FINE
     * @see #COARSE
     * @see #BOTH
     */
    public void setLocateMode(int mode) {
        mLocateMode = mode;
    }

    public int getLocateMode() {
        return mLocateMode;
    }

    /**
     * 设置多长时间进行位置获取
     *
     * @param interval
     */
    public void setTimeInterval(long interval) {
        if (interval < 0) {
            mInterval = 0;
        } else {
            mInterval = interval;
        }
    }

    /**
     * 设置多远距离进行位置获取
     *
     * @param minDistance
     */
    public void setMinDistance(float minDistance) {
        if (minDistance < 0) {
            mMinDistance = 0;
        } else {
            mMinDistance = minDistance;
        }

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "The provider " + provider + " is disabled");
        switch (provider) {
            case LocationManager.GPS_PROVIDER: {
                mGPSEnabled = false;
                break;
            }
            case LocationManager.NETWORK_PROVIDER: {
                mNetWorkEnabled = false;
                break;
            }
        }
        if (!mGPSEnabled && !mNetWorkEnabled) {
            if (null != mGPSCallback) {
                mGPSCallback.onGPSFailed();
            }
        }

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "The provider " + provider + " is enabled");
        switch (provider) {
            case LocationManager.GPS_PROVIDER: {
                mGPSEnabled = true;
                break;
            }
            case LocationManager.NETWORK_PROVIDER: {
                mNetWorkEnabled = true;
                break;
            }
        }
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "The status of the provider " + provider + " has changed");
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE: {
                Log.d(TAG, provider + " is OUT OF SERVICE");
                if (null != mGPSCallback) {
                    mGPSCallback.onGPSFailed();
                }
                break;
            }
            case LocationProvider.TEMPORARILY_UNAVAILABLE: {
                Log.d(TAG, provider + " is TEMPORARILY_UNAVAILABLE");
                if (null != mGPSCallback) {
                    mGPSCallback.onGPSFailed();
                }
                break;

            }
            case LocationProvider.AVAILABLE: {
                Log.d(TAG, provider + " is Available");
                break;

            }
            default: {
                Log.d(TAG, provider + " is Unknown");
                if (null != mGPSCallback) {
                    mGPSCallback.onGPSFailed();
                }
                break;
            }
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "The location has been updated!");
        if (null != mGPSCallback) {
            mGPSCallback.onGPSSuccess(location);
        }

    }

    public void release() {
        stop();
        mLocationMgr = null;
        mGPSCallback = null;
        mGeoCoder = null;
        mLocation = null;
    }

    private boolean requestFineLocation() {
        return requestFineLocation(true);
    }

    /**
     * 获取精确地理位置
     *
     * @param getLastLoc 是否要得到临时的上次地理位置
     * @return request是否成功
     */
    private boolean requestFineLocation(boolean getLastLoc) {
        if (mLocationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLocationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, mInterval, mMinDistance, this);

            if (getLastLoc) {
                mLocation = mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            return true;
        }
        Log.w(TAG, "Fine GPS Provider disabled...");
        return false;
    }

    private boolean requestCoarseLocation() {
        return requestCoarseLocation(false);
    }

    public Location getLocation() {
        return mLocation;
    }

    /**
     * 获取粗略地理位置
     *
     * @param getLastLoc 是否要得到临时的地理位置
     * @return request是否成功
     */
    private boolean requestCoarseLocation(boolean getLastLoc) {
        if (mLocationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            mLocationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, mInterval, mMinDistance, this);
            if (getLastLoc) {
                mLocation = mLocationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            return true;
        }
        Log.w(TAG, "Coarse GPS Provider disabled...");
        return false;
    }


    /**
     * 获取更好的地理位置，目前遴选gps和network模式下
     *
     * @return request是否成功
     */
    private boolean requestBetterLocation() {

        Location gpsLocation = null, coarseLocation = null;
        if (requestFineLocation(false)) {
            gpsLocation = mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (requestCoarseLocation(false)) {
            coarseLocation = mLocationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (null == gpsLocation && null == coarseLocation) {
            if (null != mGPSCallback) {
                mGPSCallback.onGPSFailed();
            }
            return false;
        }

        // If both providers return last known locations, compare the two and use the better
        // one to update the UI.  If only one provider returns a location, use it.
        if (gpsLocation != null && coarseLocation != null) {
            mLocation = getBetterLocation(gpsLocation, coarseLocation);
        } else if (gpsLocation != null) {
            mLocation = gpsLocation;
        } else if (coarseLocation != null) {
            mLocation = coarseLocation;
        }
        return true;
    }


    /**
     * 启动定位
     *
     * @return
     */
    public boolean start() {
        stop();
        switch (mLocateMode) {
            case FINE:
                return requestFineLocation();
            case COARSE:
                return requestCoarseLocation();
            case BOTH:
                return requestBetterLocation();
        }

        return false;
    }

    /**
     * 停止定位
     */
    public void stop() {
        Log.i(TAG, "Stop Get Location!");
        if (null != mLocationMgr) {
            mLocationMgr.removeUpdates(this);
        }
    }

    /**
     * 遴选两个地理位置
     *
     * @param newLocation
     * @param currentBestLocation
     * @return
     */
    private Location getBetterLocation(Location newLocation, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return newLocation;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > DELTA_TIME;
        boolean isSignificantlyOlder = timeDelta < -DELTA_TIME;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved.
        if (isSignificantlyNewer) {
            return newLocation;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return currentBestLocation;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(newLocation.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return newLocation;
        } else if (isNewer && !isLessAccurate) {
            return newLocation;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return newLocation;
        }
        return currentBestLocation;
    }

    /**
     * 检查provider是否相同
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


}
