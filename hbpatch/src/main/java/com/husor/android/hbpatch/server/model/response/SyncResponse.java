/*
 * Copyright (C) 2011-2017 Husor Inc.
 * All Rights Reserved.
 */
package com.husor.android.hbpatch.server.model.response;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by fanchao on 17/4/17.
 */
public final class SyncResponse {

    private static final String KEY_VERSION = "v";
    private static final String KEY_GRAY = "g";
    private static final String KEY_CONDITIONS = "c";
    private static final String KEY_PAUSE = "p";
    private static final String KEY_ROLLBACK = "e";
    private static final String KEY_URL = "url";

    public final String version;
    public Integer grayValue;
    public final String conditions;
    public final Boolean isPaused;
    public final Boolean isRollback;
    public final String url;

    public SyncResponse(String version, Integer grayValue, String conditions, String url, Boolean isPaused, Boolean isRollback) {
        this.version = version;
        this.grayValue = grayValue;
        this.conditions = conditions;
        this.url = url;
        this.isPaused = isPaused;
        this.isRollback = isRollback;
        if (grayValue == 0) {
            this.grayValue = null;
        } else {
            this.grayValue = grayValue;
        }
    }

    public static SyncResponse fromJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            String version = jsonObject.optString(KEY_VERSION);
            String conditions = jsonObject.optString(KEY_CONDITIONS);
            Integer grayValue = jsonObject.optInt(KEY_GRAY);
            Integer pauseFlag = jsonObject.optInt(KEY_PAUSE);
            Integer rollbackFlag = jsonObject.optInt(KEY_ROLLBACK);
            String url = jsonObject.optString(KEY_URL);

            return new SyncResponse(version, grayValue, conditions, url, pauseFlag == 1, rollbackFlag == 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "version:" + version + "\ngrayValue:" + grayValue + "\nconditions:" + conditions + "\nurl: " + url
                + "\npause:" + isPaused + "\nrollback:" + isRollback;
    }
}
