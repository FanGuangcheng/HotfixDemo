package com.example.fanguangcheng.hotfixdemo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;

import com.netease.android.patch.app.TinkerManager;
import com.netease.android.patch.app.TinkerServerManager;
import com.netease.android.patch.app.reporter.TinkerServiceReporter;
import com.tencent.tinker.anno.DefaultLifeCycle;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.app.DefaultApplicationLike;
import com.tencent.tinker.loader.shareutil.ShareConstants;

import java.util.Map;

/**
 * Created by fanguangcheng on 2018/1/25.
 */

@SuppressWarnings("unused")
@DefaultLifeCycle(application = "com.example.fanguangcheng.hotfixdemo.SampleApplication",
        flags = ShareConstants.TINKER_ENABLE_ALL,
        loadVerifyFlag = false)
public class SampleApplicationLike extends DefaultApplicationLike {

    private final static String TAG = "Tinker.SampleApplicationLike";

    public SampleApplicationLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
    }

    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        //you must install multiDex whatever tinker is installed!
        MultiDex.install(base);

        TinkerManager.setTinkerApplicationLike(this);
        TinkerManager.initFastCrashProtect();
        TinkerManager.setUpgradeRetryEnable(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化打点系统
        TinkerServiceReporter reporter = new TinkerServiceReporter();
        reporter.setReporter(new TinkerServiceReporter.Reporter() {
            @Override
            public void onReport(String event, Map<String, Object> value) {
                TinkerLog.i(TAG, "event: " + event + ", value: " + value.toString());
//                HusorAnalyzer.getAnalyse().setEvent(event, value);
            }
        });

        // 初始化Tinker
        TinkerManager.installTinker(this);
        // 初始化TinkerPatch SDK
        TinkerServerManager.installTinkerServer(getApplication(), Tinker.with(getApplication()), 3,
        BuildConfig.APP_KEY, BuildConfig.VERSION_NAME, "default", BuildConfig.DEBUG);
        // 开始检查是否有补丁
        TinkerServerManager.checkTinkerUpdate(true);
    }
}
