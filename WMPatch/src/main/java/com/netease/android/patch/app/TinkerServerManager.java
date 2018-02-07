/*
 * Copyright (C) 1997-2018 NetEase Inc.
 * All Rights Reserved.
 */
package com.netease.android.patch.app;

import android.content.Context;
import android.os.Looper;
import android.os.MessageQueue;

import com.netease.android.patch.app.utils.PreferencesManager;
import com.netease.android.patch.server.TinkerServerClient;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.util.TinkerLog;


/**
 * Created by fanguanggcheng on 2018/1/30.
 */
public class TinkerServerManager {

    private static final String TAG = "Tinker.TinkerServerManager";
    private static final String CONDITION_CHANNEL = "channel";

    private static TinkerServerClient sTinkerServerClient;
    private static String channel;

    /**
     * 初始化
     *
     * @param context    context
     * @param tinker     {@link Tinker}
     * @param hours      访问服务器的时间间隔，单位为小时，应为>=0
     * @param appKey     从apollo中得到的appKey
     * @param appVersion
     * @param channel
     */
    public static void installTinkerServer(Context context, Tinker tinker, int hours,
                                           String appKey, String appVersion, String channel, boolean debug) {
        TinkerLog.i(TAG, String.format("installTinkerServer, debug value: %s appVersion: %s, channel: %s",
                String.valueOf(debug), appVersion, channel));
        sTinkerServerClient = TinkerServerClient.init(context, tinker, appKey, appVersion, channel, debug);
        TinkerServerManager.channel = channel;
        PreferencesManager.context = context;
        PreferencesManager.debug = debug;
    }

    public static void checkTinkerUpdate(final boolean immediately) {
        checkTinkerUpdate(immediately, null);
    }

        /**
         * 检查服务器是否有补丁更新
         */
    public static void checkTinkerUpdate(final boolean immediately, final String config) {
        if (sTinkerServerClient == null) {
            TinkerLog.e(TAG, "checkTinkerUpdate, sTinkerServerClient == null");
            return;
        }
        Tinker tinker = sTinkerServerClient.getTinker();
        // only check at the main process
        if (tinker.isMainProcess()) {
            Looper.getMainLooper().myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
                @Override
                public boolean queueIdle() {
                    sTinkerServerClient.checkTinkerUpdate(immediately, config);
                    return false;
                }
            });
        }
    }

    // 是否是可以热修复的渠道，目前谷歌，360不支持，360 是因为加固原因，不单独打补丁包了
    public static boolean isInValidChannel() {
        return is360Channel() || isGooglePlayChannel();
    }

    public static boolean is360Channel() {
        return channel.contains("360");
    }

    public static boolean isGooglePlayChannel() {
        return channel.contains("google");
    }
}
