/*
 * Copyright (C) 2011-2017 Husor Inc.
 * All Rights Reserved.
 */
package com.husor.android.hbpatch.app.callback;

import android.content.Context;
import android.content.SharedPreferences;

import com.husor.android.hbpatch.app.reporter.TinkerServiceReporter;
import com.husor.android.hbpatch.server.TinkerServerClient;
import com.husor.android.hbpatch.server.utils.NetStatusUtil;
import com.husor.android.hbpatch.server.utils.ServerUtils;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.lib.util.TinkerServiceInternals;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;
import com.tencent.tinker.loader.shareutil.ShareSecurityCheck;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;

import java.io.File;

/**
 * Created by fanguanggcheng on 2018/1/30.
 */
public class DefaultPatchRequestCallback implements PatchRequestCallback {
    private static final String TAG = "Tinker.DefaultPatchRequestCallback";

    public static final String TINKER_DOWNLOAD_FAIL_TIMES = "tinker_download_fail";
    public static final int TINKER_DOWNLOAD_FAIL_MAX_TIMES = 3;

    @Override
    public boolean beforePatchRequest() {
        TinkerServerClient client = TinkerServerClient.get();

        // check network
        if (!NetStatusUtil.isConnected(client.getContext())) {
            TinkerLog.e(TAG, "not connect to internet");
            return false;
        }
        if (TinkerServiceInternals.isTinkerPatchServiceRunning(client.getContext())) {
            TinkerLog.e(TAG, "tinker service is running");
            return false;
        }
        return true;
    }

    @Override
    public boolean onPatchUpgrade(File file, Integer newVersion, Integer currentVersion) {
        TinkerLog.w(TAG, "onPatchUpgrade, file:%s, newVersion:%d, currentVersion:%d",
                file.getPath(), newVersion, currentVersion);
        TinkerServerClient client = TinkerServerClient.get();
        Context context = client.getContext();
        // 上报下载成功
        TinkerServiceReporter.onReportDownload(true);

        ShareSecurityCheck securityCheck = new ShareSecurityCheck(context);
        if (!securityCheck.verifyPatchMetaSignature(file)) {
            TinkerLog.e(TAG, "onPatchUpgrade, signature check fail, file: %s, version: %d", file.getPath(), newVersion);
            if (increaseDownloadError(context)) {
                // update tinker version also, don't request again
                client.updateTinkerVersion(newVersion, SharePatchFileUtil.getMD5(file));
                // 上报检查错误
                TinkerServiceReporter.onApplyPackageCheckFail(ShareConstants.ERROR_PACKAGE_CHECK_SIGNATURE_FAIL);
            }
            SharePatchFileUtil.safeDeleteFile(file);
        }
        tryPatchFile(file, newVersion);
        return false;
    }

    private void tryPatchFile(File patchFile, Integer newVersion) {
        TinkerServerClient client = TinkerServerClient.get();
        Context context = client.getContext();
        String patchMd5 = SharePatchFileUtil.getMD5(patchFile);
        // update version
        client.updateTinkerVersion(newVersion, patchMd5);
        // delete old patch server file
        File serverDir = ServerUtils.getServerDirectory(context);
        if (serverDir != null) {
            File[] files = serverDir.listFiles();
            if (files != null) {
                String currentName = patchFile.getName();
                for (File file : files) {
                    String fileName = file.getName();
                    if (fileName.equals(currentName) || fileName.equals(ServerUtils.TINKER_VERSION_FILE)) {
                        continue;
                    }
                    SharePatchFileUtil.safeDeleteFile(file);
                }
            }
            // try install
            TinkerInstaller.onReceiveUpgradePatch(context, patchFile.getAbsolutePath());
        }
    }

    @Override
    public void onPatchDownloadFail(Exception e, Integer newVersion, Integer currentVersion) {
        TinkerLog.w(TAG, "onPatchDownloadFail e:" + e);
        // check network
        TinkerServerClient client = TinkerServerClient.get();
        if (!NetStatusUtil.isConnected(client.getContext())) {
            TinkerLog.e(TAG, "onPatchDownloadFail, not connect to internet just return");
            return;
        }
        Context context = client.getContext();
        if (increaseDownloadError(context)) {
            // 上报错误
            TinkerServiceReporter.onReportDownload(false);
        }
    }

    @Override
    public void onPatchSyncFail(Exception e) {
        TinkerLog.w(TAG, "onPatchSyncFail error: " + e);
        TinkerLog.printErrStackTrace(TAG, e, "onPatchSyncFail stack:");
    }

    @Override
    public void onPatchRollback() {
        TinkerLog.w(TAG, "onPatchRollback");
        rollbackPatchDirectly();
    }

    public void rollbackPatchDirectly() {
        TinkerServerClient client = TinkerServerClient.get();
        Context context = client.getContext();
        Tinker tinker = client.getTinker();
        //restart now
        tinker.cleanPatch();
//        ShareTinkerInternals.killAllOtherProcess(context);
//        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void updatePatchConditions() {

    }

    public boolean increaseDownloadError(Context context) {
        SharedPreferences sp = context.getSharedPreferences(TinkerServerClient.SHARE_SERVER_PREFERENCE_CONFIG, Context.MODE_PRIVATE);
        int currentCount = sp.getInt(TINKER_DOWNLOAD_FAIL_TIMES, 0);
        TinkerLog.i(TAG, "increaseDownloadError, current count: %d", currentCount);

        if (currentCount >= TINKER_DOWNLOAD_FAIL_MAX_TIMES) {
            sp.edit().putInt(TINKER_DOWNLOAD_FAIL_TIMES, 0).commit();
            return true;
        } else {
            sp.edit().putInt(TINKER_DOWNLOAD_FAIL_TIMES, ++currentCount).commit();
        }
        return false;
    }
}
