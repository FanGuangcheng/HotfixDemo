/*
 * Copyright (C) 2011-2017 Husor Inc.
 * All Rights Reserved.
 */
package com.husor.android.hbpatch.server.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by fanguanggcheng on 2018/1/30.
 */
public final class NetStatusUtil {

    private static ConnectivityManager connectivityManager = null;

    private NetStatusUtil() {
    }

    public static boolean isConnected(Context context) {
        if (connectivityManager == null) {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        boolean connect = false;
        try {
            connect = activeNetInfo.isConnected();
        } catch (Exception e) {
            // do noting
        }
        return connect;
    }

    public static boolean isWifi(Context context) {
        if (connectivityManager == null) {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo == null) {
            return false;
        }

        return activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }
}
