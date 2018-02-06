/*
 * Copyright (C) 1997-2018 NetEase Inc.
 * All Rights Reserved.
 */
package com.netease.android.patch.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by fanchao on 17/5/18.
 */
public class PreferencesManager {

    public static final String SHARE_SERVER_PREFERENCE_CONFIG = "tinker_server_config";
    public static final String PATCH_VERSION = "tinker_patch_version";

    public static Context context;
    public static boolean debug;

    /**
     * 存patchversion
     *
     * @param version
     */
    public static void setPatchVersion(String version) {
        if (context != null) {
            SharedPreferences sp = context.getSharedPreferences(SHARE_SERVER_PREFERENCE_CONFIG, Context.MODE_PRIVATE);
            sp.edit().putString(PATCH_VERSION, version).commit();
        }
    }

    /**
     * 取出patchversion
     *
     * @return
     */
    public static String getPatchVersion() {
        if (context != null) {
            SharedPreferences sp = context.getSharedPreferences(SHARE_SERVER_PREFERENCE_CONFIG, Context.MODE_PRIVATE);
            return sp.getString(PATCH_VERSION, "0");
        } else {
            return "-1";
        }
    }
}
