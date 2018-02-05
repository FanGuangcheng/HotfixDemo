/*
 * Copyright (C) 2011-2017 Husor Inc.
 * All Rights Reserved.
 */
package com.netease.android.patch.app.callback;

import android.content.Context;
import android.content.SharedPreferences;

import com.netease.android.patch.app.TinkerServerManager;
import com.netease.android.patch.app.TinkerServerUtils;
import com.netease.android.patch.server.TinkerServerClient;
import com.netease.android.patch.server.utils.ServerUtils;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.lib.tinker.TinkerLoadResult;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;

import java.io.File;

/**
 * Created by fanguanggcheng on 2018/1/30.
 */
public class TinkerServerPatchRequestCallback extends DefaultPatchRequestCallback {

    private static final String TAG = "Tinker.TinkerServerPatchRequestCallback";

    public static final String TINKER_RETRY_PATCH = "tinker_retry_patch";
    public static final int TINKER_MAX_RETRY_COUNT = 3;

    @Override
    public boolean beforePatchRequest() {
        boolean result = super.beforePatchRequest();
        TinkerServerClient client = TinkerServerClient.get();
        Tinker tinker = client.getTinker();
        Context context = client.getContext();

        if (!tinker.isMainProcess()) {
            TinkerLog.e(TAG, "beforePatchRequest, only request on the main process");
            return false;
        }
        if (TinkerServerManager.isGooglePlayChannel()) {
            TinkerLog.e(TAG, "beforePatchRequest, google play channel, return false");
            return false;
        }

        // check whether it is pending work
        String currentPatchMd5 = client.getCurrentPatchMd5();
        TinkerLoadResult tinkerLoadResult = tinker.getTinkerLoadResultIfPresent();
        if (tinkerLoadResult.currentVersion == null || !currentPatchMd5.equals(tinkerLoadResult.currentVersion)) {
            Integer version = client.getCurrentPatchVersion();
            if (version > 0) {
                File patchFile = ServerUtils.getServerFile(context, client.getAppVersion(), String.valueOf(version));
                if (patchFile.exists() && patchFile.isFile() && handlePatchFile(context, version, patchFile)) {
                    return false;
                }
            }
        }

        return result;
    }

    private boolean handlePatchFile(Context context, Integer version, File patchFile) {
        SharedPreferences sp = context.getSharedPreferences(TinkerServerClient.SHARE_SERVER_PREFERENCE_CONFIG, Context.MODE_PRIVATE);
        int current = sp.getInt(TINKER_RETRY_PATCH, 0);
        if (current >= TINKER_MAX_RETRY_COUNT) {
            SharePatchFileUtil.safeDeleteFile(patchFile);
            sp.edit().putInt(TINKER_RETRY_PATCH, 0).commit();
            TinkerLog.w(TAG, "beforePatchRequest, retry patch install more than %d times, version: %d, patch: %s", current, version, patchFile.getPath());
        } else {
            TinkerLog.w(TAG, "beforePatchRequest, have pending patch to install, version: %d, patch: %s", version, patchFile.getPath());
            sp.edit().putInt(TINKER_RETRY_PATCH, ++current).commit();
            TinkerInstaller.onReceiveUpgradePatch(context, patchFile.getAbsolutePath());
            return true;
        }
        return false;
    }

    @Override
    public void onPatchRollback() {
        TinkerLog.w(TAG, "onPatchRollback");
        TinkerServerClient client = TinkerServerClient.get();

        if (!client.getTinker().isTinkerEnabled()) {
            TinkerLog.w(TAG, "onPatchRollback, tinker is not loaded, just return");
            return;
        }

        if (TinkerServerUtils.isBackground()) {
            TinkerLog.i(TAG, "onPatchRollback, it is in background, just clean patch and kill all process");
            rollbackPatchDirectly();
        } else {
            TinkerLog.i(TAG, "tinker wait kill to clean patch and kill all process");
            rollbackPatchDirectly();
//            new TinkerServerUtils.ScreenState(client.getContext(), new TinkerServerUtils.IOnScreenOff() {
//                @Override
//                public void onScreenOff() {
//                    rollbackPatchDirectly();
//                }
//            });
        }
    }

    @Override
    public void onPatchDownloadFail(Exception e, Integer newVersion, Integer currentVersion) {
        super.onPatchDownloadFail(e, newVersion, currentVersion);
    }

    @Override
    public void onPatchSyncFail(Exception e) {
        super.onPatchSyncFail(e);
    }

    @Override
    public boolean onPatchUpgrade(File file, Integer newVersion, Integer currentVersion) {
        boolean result = super.onPatchUpgrade(file, newVersion, currentVersion);
        if (result) {
            TinkerServerClient client = TinkerServerClient.get();
            Context context = client.getContext();
            SharedPreferences sp = context.getSharedPreferences(TinkerServerClient.SHARE_SERVER_PREFERENCE_CONFIG, Context.MODE_PRIVATE);
            sp.edit().putInt(TINKER_RETRY_PATCH, 0).commit();
        }
        return result;
    }
}
