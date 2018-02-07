/*
 * Copyright (C) 1997-2018 NetEase Inc.
 * All Rights Reserved.
 */
package com.netease.android.patch.server;

import android.content.Context;
import android.content.SharedPreferences;

import com.netease.android.patch.app.callback.PatchRequestCallback;
import com.netease.android.patch.app.callback.TinkerServerPatchRequestCallback;
import com.netease.android.patch.server.client.TinkerClientAPI;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;

/**
 * Created by fanguanggcheng on 2018/1/30.
 */
public class TinkerServerClient {
    private static final String TAG = "Tinker.ServerClient";

    public static final String SHARE_SERVER_PREFERENCE_CONFIG = "tinker_server_config";
    public static final String TINKER_LAST_CHECK = "tinker_last_check";

    private static final long DEFAULT_CHECK_INTERVAL = 1 * 3600 * 1000;
    private static final long NEVER_CHECK_UPDATE = -1;
    private long checkInterval = DEFAULT_CHECK_INTERVAL;
    private long checkConfigInterval = DEFAULT_CHECK_INTERVAL;

    private static volatile TinkerServerClient client;
    private final Tinker tinker;
    private final Context context;
    private final PatchRequestCallback patchRequestCallback;
    private final TinkerClientAPI clientAPI;

    public TinkerServerClient(Context context, Tinker tinker, String appKey, String appVersion,
                              String channel, Boolean debug, PatchRequestCallback patchRequestCallback) {
        this.tinker = tinker;
        this.context = context;
        this.clientAPI = TinkerClientAPI.init(context, appKey, appVersion, channel, debug);
        this.patchRequestCallback = patchRequestCallback;
    }

    public static TinkerServerClient get() {
        if (client == null) {
            throw new RuntimeException("Please invoke init Tinker Client first");
        }
        return client;
    }

    public static TinkerServerClient init(Context context, Tinker tinker, String appKey, String appVersion,
                                          String channel, Boolean debug) {
        if (client == null) {
            synchronized (TinkerClientAPI.class) {
                if (client == null) {
                    client = new TinkerServerClient(context, tinker, appKey, appVersion, channel, debug, new TinkerServerPatchRequestCallback());
                }
            }
        }
        return client;
    }

    /**
     * 检查服务器是否有补丁更新
     */
    public void checkTinkerUpdate(boolean immediately, String config) {
        if (!tinker.isTinkerEnabled() || !ShareTinkerInternals.isTinkerEnableWithSharedPreferences(context)) {
            TinkerLog.e(TAG, "tinker is disable, just return");
            return;
        }

        SharedPreferences sp = context.getSharedPreferences(SHARE_SERVER_PREFERENCE_CONFIG, Context.MODE_PRIVATE);
        long last = sp.getLong(TINKER_LAST_CHECK, 0);
        if (last == NEVER_CHECK_UPDATE) {
            TinkerLog.i(TAG, "tinker update is disabled, with never check flag!");
            return;
        }
        long interval = System.currentTimeMillis() - last;
        if (immediately || interval >= checkInterval) {
            sp.edit().putLong(TINKER_LAST_CHECK, System.currentTimeMillis()).commit();
            clientAPI.update(context, config, patchRequestCallback);
        } else {
            TinkerLog.i(TAG, "tinker sync should wait interval %ss", (checkInterval - interval) / 1000);
        }
    }

    public void updateTinkerVersion(Integer newVersion, String patchMd5) {
        clientAPI.updateTinkerVersion(newVersion, patchMd5);
    }

    public Tinker getTinker() {
        return tinker;
    }

    public Context getContext() {
        return context;
    }

    public String getAppVersion() {
        return clientAPI.getAppVersion();
    }

    public Integer getCurrentPatchVersion() {
        return clientAPI.getCurrentPatchVersion();
    }

    public String getCurrentPatchMd5() {
        return clientAPI.getCurrentPatchMd5();
    }
}
