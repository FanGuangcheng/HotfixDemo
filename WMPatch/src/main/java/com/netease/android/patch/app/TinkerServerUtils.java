/*
 * Copyright (C) 1997-2018 NetEase Inc.
 * All Rights Reserved.
 */
package com.netease.android.patch.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.tencent.tinker.lib.util.TinkerLog;

/**
 * Created by fanguanggcheng on 2018/1/30.
 */
public final class TinkerServerUtils {
    private static final String TAG = "Tinker.TinkerServerUtils";
    private static boolean background = false;

    public interface IOnScreenOff {
        void onScreenOff();
    }

    private TinkerServerUtils() {

    }

    public static boolean isBackground() {
        return background;
    }

    public static void setBackground(boolean back) {
        background = back;
    }

    public static class ScreenState {
        public ScreenState(Context context, final IOnScreenOff onScreenOffInterface) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent == null ? "" : intent.getAction();
                    TinkerLog.i(TAG, "ScreenReceiver action [%s] ", action);
                    if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                        context.unregisterReceiver(this);
                        if (onScreenOffInterface != null) {
                            onScreenOffInterface.onScreenOff();
                        }
                    }
                }
            }, filter);
        }
    }
}
