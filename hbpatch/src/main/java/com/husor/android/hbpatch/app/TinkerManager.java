/*
 * Copyright (C) 2011-2017 Husor Inc.
 * All Rights Reserved.
 */
package com.husor.android.hbpatch.app;

import com.husor.android.hbpatch.app.crash.TinkerUnCaughtExceptionHandler;
import com.husor.android.hbpatch.app.reporter.TinkerServerLoadReporter;
import com.husor.android.hbpatch.app.reporter.TinkerServerPatchListener;
import com.husor.android.hbpatch.app.reporter.TinkerServerPatchReporter;
import com.husor.android.hbpatch.app.service.TinkerServerResultService;
import com.tencent.tinker.lib.listener.PatchListener;
import com.tencent.tinker.lib.patch.AbstractPatch;
import com.tencent.tinker.lib.patch.UpgradePatch;
import com.tencent.tinker.lib.reporter.LoadReporter;
import com.tencent.tinker.lib.reporter.PatchReporter;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.lib.util.UpgradePatchRetry;
import com.tencent.tinker.loader.app.ApplicationLike;

/**
 * Created by fanguanggcheng on 2018/1/30.
 */
public class TinkerManager {

    private static final String TAG = "Tinker.TinkerManager";

    private static ApplicationLike applicationLike;
    private static TinkerUnCaughtExceptionHandler uncaughtExceptionHandler;
    private static boolean isInstalled = false;

    public static void setTinkerApplicationLike(ApplicationLike appLike) {
        applicationLike = appLike;
    }

    public static ApplicationLike getTinkerApplicationLike() {
        return applicationLike;
    }

    public static void initFastCrashProtect() {
        if (uncaughtExceptionHandler == null) {
            uncaughtExceptionHandler = new TinkerUnCaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
        }
    }

    public static void setUpgradeRetryEnable(boolean enable) {
        UpgradePatchRetry.getInstance(applicationLike.getApplication()).setRetryEnable(enable);
    }

    /**
     * you can specify all class you want.
     * sometimes, you can only install tinker in some process you want!
     *
     * @param appLike
     */
    public static void installTinker(ApplicationLike appLike) {
        if (isInstalled) {
            TinkerLog.w(TAG, "install tinker, but has installed, ignore");
            return;
        }

        //or you can just use DefaultLoadReporter
        LoadReporter loadReporter = new TinkerServerLoadReporter(appLike.getApplication());
        //or you can just use DefaultPatchReporter
        PatchReporter patchReporter = new TinkerServerPatchReporter(appLike.getApplication());
        //or you can just use DefaultPatchListener
        PatchListener patchListener = new TinkerServerPatchListener(appLike.getApplication());
        //you can set your own upgrade patch if you need
        AbstractPatch upgradePatchProcessor = new UpgradePatch();

        TinkerInstaller.install(appLike,
                loadReporter, patchReporter, patchListener,
                TinkerServerResultService.class, upgradePatchProcessor);

        isInstalled = true;
    }
}
