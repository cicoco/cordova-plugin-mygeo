/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at
         http://www.apache.org/licenses/LICENSE-2.0
       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */


package org.apache.cordova.geolocation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Geolocation extends CordovaPlugin {

    String TAG = "GeolocationPlugin";
    CallbackContext context;
    private LocationClient mLocationClient = null;
    private long timeInterval = 5000L;


    String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};


    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        LOG.d(TAG, "We are entering execute");
        context = callbackContext;
        if (action.equals("getPermission")) {
            if (hasPermisssion()) {
                PluginResult r = new PluginResult(PluginResult.Status.OK);
                context.sendPluginResult(r);
                return true;
            } else {
                PermissionHelper.requestPermissions(this, 0, permissions);
            }
            return true;
        } else if (action.equals("getCurrentPosition2")) {
            if (hasPermisssion()) {
                getLocation();
            } else {
                PermissionHelper.requestPermissions(this, 1, permissions);
            }
            return true;
        }
        return false;
    }


    private void getLocation() {
        if (null == mLocationClient) {
            mLocationClient = new LocationClient(cordova.getContext());
            mLocationClient.setGPSCallback(mGPSListener);
        }

        mLocationClient.setTimeInterval(timeInterval);
        mLocationClient.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (null != mLocationClient) {
            mLocationClient.release();
        }
    }

    private IGPSListener mGPSListener = new IGPSListener() {
        @Override
        public void onGPSSuccess(Location location) {
            if (null != mLocationClient) {
                mLocationClient.removeGPSCallback();
                mLocationClient.stop();
            }
            if (null != context) {
                try {
                    JSONObject r = new JSONObject();
                    r.put("Latitude", location.getLatitude());
                    r.put("Longitude", location.getLongitude());
                    r.put("Altitude", location.getAltitude());
                    r.put("Accuracy", location.getAccuracy());
                    r.put("Speed", location.getSpeed());
                    r.put("Timestamp", location.getTime());
                    context.success(r);
                } catch (Exception e) {
                    context.error("parse failed");
                }
            }
        }

        @Override
        public void onGPSFailed() {
            if (null != context) {
                context.error("cannot get location");
            }
        }
    };

    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        PluginResult result;
        //This is important if we're using Cordova without using Cordova, but we have the geolocation plugin installed
        if (context != null) {
            for (int r : grantResults) {
                if (r == PackageManager.PERMISSION_DENIED) {
                    LOG.d(TAG, "Permission Denied!");
                    result = new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION);
                    context.sendPluginResult(result);
                    return;
                }

            }
            if (0 == requestCode) {
                result = new PluginResult(PluginResult.Status.OK);
                context.sendPluginResult(result);
            } else {
                getLocation();
            }

        }
    }

    public boolean hasPermisssion() {
        for (String p : permissions) {
            if (!PermissionHelper.hasPermission(this, p)) {
                return false;
            }
        }
        return true;
    }

    /*
     * We override this so that we can access the permissions variable, which no longer exists in
     * the parent class, since we can't initialize it reliably in the constructor!
     */

    public void requestPermissions(int requestCode) {
        PermissionHelper.requestPermissions(this, requestCode, permissions);
    }


}
