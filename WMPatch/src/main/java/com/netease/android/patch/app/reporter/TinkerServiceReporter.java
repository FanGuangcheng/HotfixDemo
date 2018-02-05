/*
 * Copyright (C) 2011-2017 Husor Inc.
 * All Rights Reserved.
 */
package com.netease.android.patch.app.reporter;

import com.netease.android.patch.app.utils.PreferencesManager;
import com.netease.android.patch.app.utils.Utils;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fanguanggcheng on 2018/1/30.
 */
public class TinkerServiceReporter {
    private static final String TAG = "Tinker.TinkerServiceReporter";


    // 开机引起的崩溃
    public static final String EVENT_CRASH_CAUSE_CHECK = "patch_crash_cause";
    public static final int KEY_CRASH_FAST_PROTECT = 10;
    public static final int KEY_CRASH_CAUSE_XPOSED_DALVIK = 11;
    public static final int KEY_CRASH_CAUSE_XPOSED_ART = 12;

    // 检查补丁包错误
    public static final String EVENT_TRY_APPLY_CHECK = "patch_try_apply";
    public static final int KEY_TRY_APPLY_SUCCESS = 20;
    public static final int KEY_TRY_APPLY_FAIL = 21;
    public static final int KEY_TRY_APPLY_DISABLE = 22;
    public static final int KEY_TRY_APPLY_RUNNING = 23;
    public static final int KEY_TRY_APPLY_INSERVICE = 24;
    public static final int KEY_TRY_APPLY_NOT_EXIST = 25;
    public static final int KEY_TRY_APPLY_GOOGLEPLAY = 26;
    public static final int KEY_TRY_APPLY_ROM_SPACE = 27;
    public static final int KEY_TRY_APPLY_ALREADY_APPLY = 28;
    public static final int KEY_TRY_APPLY_MEMORY_LIMIT = 29;
    public static final int KEY_TRY_APPLY_CRASH_LIMIT = 30;
    public static final int KEY_TRY_APPLY_CONDITION_NOT_SATISFIED = 31;

    // patch下载状态 1: 成功 0: 失败
    public static final String EVENT_DOWNLOAD = "patch_download";
    public static final int KEY_DOWNLOAD_SUCCESS = 1;
    public static final int KEY_DOWNLOAD_FAIL = 0;

    // 加载补丁包耗时
    public static final String EVENT_LOADED = "patch_loaded";

    // patch时检查补丁包失败
    public static final String EVENT_LOADED_FAIL = "patch_loaded_fail";
    public static final int KEY_LOADED_MISMATCH_DEX = 100;
    public static final int KEY_LOADED_MISMATCH_LIB = 101;
    public static final int KEY_LOADED_MISMATCH_RESOURCE = 102;
    public static final int KEY_LOADED_MISSING_DEX = 103;
    public static final int KEY_LOADED_MISSING_LIB = 104;
    public static final int KEY_LOADED_MISSING_PATCH_FILE = 105;
    public static final int KEY_LOADED_MISSING_PATCH_INFO = 106;
    public static final int KEY_LOADED_MISSING_DEX_OPT = 107;
    public static final int KEY_LOADED_MISSING_RES = 108;
    public static final int KEY_LOADED_INFO_CORRUPTED = 109;
    public static final int KEY_LOADED_INTERPRET_GET_INSTRUCTION_SET_ERROR = 110;
    public static final int KEY_LOADED_INTERPRET_INTERPRET_COMMAND_ERROR = 111;
    public static final int KEY_LOADED_INTERPRET_TYPE_INTERPRET_OK = 112;

    public static final int KEY_LOADED_PACKAGE_CHECK_SIGNATURE = 150;
    public static final int KEY_LOADED_PACKAGE_CHECK_DEX_META = 151;
    public static final int KEY_LOADED_PACKAGE_CHECK_LIB_META = 152;
    public static final int KEY_LOADED_PACKAGE_CHECK_APK_TINKER_ID_NOT_FOUND = 153;
    public static final int KEY_LOADED_PACKAGE_CHECK_PATCH_TINKER_ID_NOT_FOUND = 154;
    public static final int KEY_LOADED_PACKAGE_CHECK_TINKER_ID_NOT_EQUAL = 155;
    public static final int KEY_LOADED_PACKAGE_CHECK_PACKAGE_META_NOT_FOUND = 156;
    public static final int KEY_LOADED_PACKAGE_CHECK_RES_META = 157;
    public static final int KEY_LOADED_PACKAGE_CHECK_TINKERFLAG_NOT_SUPPORT = 158;

    public static final int KEY_LOADED_UNKNOWN_EXCEPTION = 160;
    public static final int KEY_LOADED_UNCAUGHT_EXCEPTION = 161;
    public static final int KEY_LOADED_EXCEPTION_DEX = 162;
    public static final int KEY_LOADED_EXCEPTION_DEX_CHECK = 163;
    public static final int KEY_LOADED_EXCEPTION_RESOURCE = 164;
    public static final int KEY_LOADED_EXCEPTION_RESOURCE_CHECK = 165;


    // 合成补丁包耗时
    public static final String EVENT_APPLIED = "patch_applied";
    public static final int KEY_APPLIED_START = 2;
    public static final int KEY_APPLIED_UPGRADE_SUCCESS = 1;
    public static final int KEY_APPLIED_UPGRADE_FAIL = 0;

    // patch时检查补丁包失败
    public static final String EVENT_APPLIED_FAIL = "patch_applied_fail";
    public static final int KEY_APPLIED_PACKAGE_CHECK_SIGNATURE = 200;
    public static final int KEY_APPLIED_PACKAGE_CHECK_DEX_META = 201;
    public static final int KEY_APPLIED_PACKAGE_CHECK_LIB_META = 202;
    public static final int KEY_APPLIED_PACKAGE_CHECK_APK_TINKER_ID_NOT_FOUND = 203;
    public static final int KEY_APPLIED_PACKAGE_CHECK_PATCH_TINKER_ID_NOT_FOUND = 204;
    public static final int KEY_APPLIED_PACKAGE_CHECK_META_NOT_FOUND = 205;
    public static final int KEY_APPLIED_PACKAGE_CHECK_TINKER_ID_NOT_EQUAL = 206;
    public static final int KEY_APPLIED_PACKAGE_CHECK_RES_META = 207;
    public static final int KEY_APPLIED_PACKAGE_CHECK_TINKERFLAG_NOT_SUPPORT = 208;

    public static final int KEY_APPLIED_VERSION_CHECK = 210;
    public static final int KEY_APPLIED_PATCH_FILE_EXTRACT = 211;
    public static final int KEY_APPLIED_DEX_EXTRACT = 212;
    public static final int KEY_APPLIED_LIB_EXTRACT = 213;
    public static final int KEY_APPLIED_RESOURCE_EXTRACT = 214;

    public static final int KEY_APPLIED_EXCEPTION = 270;
    public static final int KEY_APPLIED_DEXOPT = 271;
    public static final int KEY_APPLIED_INFO_CORRUPTED = 272;

    // 重试次数
    public static final String EVENT_APPLY_RETRY_CHECK = "patch_apply_retry";
    public static final int KEY_APPLY_RETRY = 300;

    public interface Reporter {
        void onReport(String event, Map<String, Object> value);
    }

    private static Reporter reporter = null;

    public void setReporter(Reporter reporter) {
        this.reporter = reporter;
    }

    /**
     * 上报下载状态
     */
    public static void onReportDownload(boolean success) {
        if (reporter == null) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("status", success ? KEY_DOWNLOAD_SUCCESS : KEY_DOWNLOAD_FAIL);
        map.put("patch_version", PreferencesManager.getPatchVersion());
        reporter.onReport(EVENT_DOWNLOAD, map);
    }

    public static void onTryApply(boolean success) {
        if (reporter == null) {
            return;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("type", success ? KEY_TRY_APPLY_SUCCESS : KEY_TRY_APPLY_FAIL);
        map.put("patch_version", PreferencesManager.getPatchVersion());
        reporter.onReport(EVENT_TRY_APPLY_CHECK, map);
    }

    /**
     * 上传、补丁包错误，不存在
     */
    public static void onTryApplyFail(int errorCode) {
        if (reporter == null) {
            return;
        }
        int type = -1;
        switch (errorCode) {
            case ShareConstants.ERROR_PATCH_NOTEXIST:
                type = KEY_TRY_APPLY_NOT_EXIST;
                break;
            case ShareConstants.ERROR_PATCH_DISABLE:
                type = KEY_TRY_APPLY_DISABLE;
                break;
            case ShareConstants.ERROR_PATCH_INSERVICE:
                type = KEY_TRY_APPLY_INSERVICE;
                break;
            case ShareConstants.ERROR_PATCH_RUNNING:
                type = KEY_TRY_APPLY_RUNNING;
                break;
            case Utils.ERROR_PATCH_ROM_SPACE:
                type = KEY_TRY_APPLY_ROM_SPACE;
                break;
            case Utils.ERROR_PATCH_GOOGLEPLAY_CHANNEL:
                type = KEY_TRY_APPLY_GOOGLEPLAY;
                break;
            case Utils.ERROR_PATCH_ALREADY_APPLY:
                type = KEY_TRY_APPLY_ALREADY_APPLY;
                break;
            case Utils.ERROR_PATCH_CRASH_LIMIT:
                type = KEY_TRY_APPLY_CRASH_LIMIT;
                break;
            case Utils.ERROR_PATCH_MEMORY_LIMIT:
                type = KEY_TRY_APPLY_MEMORY_LIMIT;
                break;
            case Utils.ERROR_PATCH_CONDITION_NOT_SATISFIED:
                type = KEY_TRY_APPLY_CONDITION_NOT_SATISFIED;
                break;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("patch_version", PreferencesManager.getPatchVersion());
        reporter.onReport(EVENT_TRY_APPLY_CHECK, map);
    }

    /**
     * 上报检查包失败
     */
    public static void onLoadPackageCheckFail(int errorCode) {
        if (reporter == null) {
            return;
        }
        int type = -1;
        switch (errorCode) {
            case ShareConstants.ERROR_PACKAGE_CHECK_SIGNATURE_FAIL:
                type = KEY_LOADED_PACKAGE_CHECK_SIGNATURE;
                break;
            case ShareConstants.ERROR_PACKAGE_CHECK_DEX_META_CORRUPTED:
                type = KEY_LOADED_PACKAGE_CHECK_DEX_META;
                break;
            case ShareConstants.ERROR_PACKAGE_CHECK_LIB_META_CORRUPTED:
                type = KEY_LOADED_PACKAGE_CHECK_LIB_META;
                break;
            case ShareConstants.ERROR_PACKAGE_CHECK_PATCH_TINKER_ID_NOT_FOUND:
                type = KEY_LOADED_PACKAGE_CHECK_PATCH_TINKER_ID_NOT_FOUND;
                break;
            case ShareConstants.ERROR_PACKAGE_CHECK_APK_TINKER_ID_NOT_FOUND:
                type = KEY_LOADED_PACKAGE_CHECK_APK_TINKER_ID_NOT_FOUND;
                break;
            case ShareConstants.ERROR_PACKAGE_CHECK_TINKER_ID_NOT_EQUAL:
                type = KEY_LOADED_PACKAGE_CHECK_TINKER_ID_NOT_EQUAL;
                break;
            case ShareConstants.ERROR_PACKAGE_CHECK_PACKAGE_META_NOT_FOUND:
                type = KEY_LOADED_PACKAGE_CHECK_PACKAGE_META_NOT_FOUND;
                break;
            case ShareConstants.ERROR_PACKAGE_CHECK_RESOURCE_META_CORRUPTED:
                type = KEY_LOADED_PACKAGE_CHECK_RES_META;
                break;
            case ShareConstants.ERROR_PACKAGE_CHECK_TINKERFLAG_NOT_SUPPORT:
                type = KEY_LOADED_PACKAGE_CHECK_TINKERFLAG_NOT_SUPPORT;
                break;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("patch_version", PreferencesManager.getPatchVersion());
        reporter.onReport(EVENT_LOADED_FAIL, map);
    }

    /**
     * 上报加载patch包耗时
     */
    public static void onLoaded(long cost) {
        if (reporter == null) {
            return;
        }

        if (cost < 0L) {
            TinkerLog.e(TAG, "hp_report report load cost failed, invalid cost");
            return;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("cost", cost);
        map.put("patch_version", PreferencesManager.getPatchVersion());
        reporter.onReport(EVENT_LOADED, map);
    }

    public static void onLoadInfoCorrupted() {
        if (reporter == null) {
            return;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("type", KEY_LOADED_INFO_CORRUPTED);
        map.put("patch_version", PreferencesManager.getPatchVersion());
        reporter.onReport(EVENT_LOADED_FAIL, map);
    }

    /**
     * 上传文件不见
     * @param fileType
     */
    public static void onLoadFileNotFound(int fileType) {
        if (reporter == null) {
            return;
        }
        int type = -1;
        switch (fileType) {
            case ShareConstants.TYPE_DEX_OPT:
                type = KEY_LOADED_MISSING_DEX_OPT;
                break;
            case ShareConstants.TYPE_DEX:
                type = KEY_LOADED_MISSING_DEX;
                break;
            case ShareConstants.TYPE_LIBRARY:
                type = KEY_LOADED_MISSING_LIB;
                break;
            case ShareConstants.TYPE_PATCH_FILE:
                type = KEY_LOADED_MISSING_PATCH_FILE;
                break;
            case ShareConstants.TYPE_PATCH_INFO:
                type = KEY_LOADED_MISSING_PATCH_INFO;
                break;
            case ShareConstants.TYPE_RESOURCE:
                type = KEY_LOADED_MISSING_RES;
                break;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("patch_version", PreferencesManager.getPatchVersion());
        reporter.onReport(EVENT_LOADED_FAIL, map);
    }

    /**
     * 上传OTA时的错误
     * @param e
     */
    public static void onLoadInterpretReport(int fileType, Throwable e) {
        if (reporter == null) {
            return;
        }
        int type = -1;
        Map<String, Object> map = new HashMap<>();
        switch (fileType) {
            case ShareConstants.TYPE_INTERPRET_GET_INSTRUCTION_SET_ERROR:
                type = KEY_LOADED_INTERPRET_GET_INSTRUCTION_SET_ERROR;
                map.put("message", "Tinker Exception:interpret occur exception " + Utils.getExceptionCauseString(e));
                break;
            case ShareConstants.TYPE_INTERPRET_COMMAND_ERROR:
                type = KEY_LOADED_INTERPRET_INTERPRET_COMMAND_ERROR;
                map.put("message", "Tinker Exception:interpret occur exception " + Utils.getExceptionCauseString(e));
                break;
            case ShareConstants.TYPE_INTERPRET_OK:
                type = KEY_LOADED_INTERPRET_TYPE_INTERPRET_OK;
                break;
        }
        map.put("type", type);
        map.put("patch_version", PreferencesManager.getPatchVersion());
        reporter.onReport(EVENT_LOADED_FAIL, map);
    }

    /**
     * 上传文件不匹配
     */
    public static void onLoadFileMisMatch(int fileType) {
        if (reporter == null) {
            return;
        }
        int type = -1;
        switch (fileType) {
            case ShareConstants.TYPE_DEX:
                type = KEY_LOADED_MISMATCH_DEX;
                break;
            case ShareConstants.TYPE_LIBRARY:
                type = KEY_LOADED_MISMATCH_LIB;
                break;
            case ShareConstants.TYPE_RESOURCE:
                type = KEY_LOADED_MISMATCH_RESOURCE;
                break;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("patch_version", PreferencesManager.getPatchVersion());
        reporter.onReport(EVENT_LOADED_FAIL, map);
    }

    /**
     * 上传补丁包加载异常
     */
    public static void onLoadException(Throwable throwable, int errorCode) {
        if (reporter == null) {
            return;
        }
        boolean isCheckFail = false;
        int type = -1;
        switch (errorCode) {
            case ShareConstants.ERROR_LOAD_EXCEPTION_DEX:
                if (throwable.getMessage().contains(ShareConstants.CHECK_DEX_INSTALL_FAIL)) {
                    type = KEY_LOADED_EXCEPTION_DEX_CHECK;
                    isCheckFail = true;
                    TinkerLog.e(TAG, "tinker dex check fail:" + throwable.getMessage());
                } else {
                    type = KEY_LOADED_EXCEPTION_DEX;
                    TinkerLog.e(TAG, "tinker dex reflect fail:" + throwable.getMessage());
                }
                break;
            case ShareConstants.ERROR_LOAD_EXCEPTION_RESOURCE:
                if (throwable.getMessage().contains(ShareConstants.CHECK_RES_INSTALL_FAIL)) {
                    type = KEY_LOADED_EXCEPTION_RESOURCE_CHECK;
                    isCheckFail = true;
                    TinkerLog.e(TAG, "tinker res check fail:" + throwable.getMessage());
                } else {
                    type = KEY_LOADED_EXCEPTION_RESOURCE;
                    TinkerLog.e(TAG, "tinker res reflect fail:" + throwable.getMessage());
                }
                break;
            case ShareConstants.ERROR_LOAD_EXCEPTION_UNCAUGHT:
                type = KEY_LOADED_UNCAUGHT_EXCEPTION;
                break;
            case ShareConstants.ERROR_LOAD_EXCEPTION_UNKNOWN:
                type = KEY_LOADED_UNKNOWN_EXCEPTION;
                break;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("patch_version", PreferencesManager.getPatchVersion());
        map.put("message", "Tinker Exception:load tinker occur exception " + Utils.getExceptionCauseString(throwable));
        reporter.onReport(EVENT_LOADED_FAIL, map);
    }

    /**
     * 上报修复补丁开始
     */
    public static void onApplyPatchServiceStart() {
        if (reporter == null) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("type", KEY_APPLIED_START);
        map.put("patch_version", PreferencesManager.getPatchVersion());
        reporter.onReport(EVENT_APPLIED, map);
    }

    /**
     * 上传应用过程中失败
     */
    public static void onApplyDexOptFail(Throwable throwable) {
        if (reporter == null) {
            return;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("type", KEY_APPLIED_DEXOPT);
        map.put("patch_version", PreferencesManager.getPatchVersion());
        map.put("message", "Tinker Exception:apply tinker occur exception " + Utils.getExceptionCauseString(throwable));
        reporter.onReport(EVENT_APPLIED_FAIL, map);
    }

    /**
     * 上传应用过程中失败
     */
    public static void onApplyInfoCorrupted() {
        if (reporter == null) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("type", KEY_APPLIED_INFO_CORRUPTED);
        map.put("patch_version", PreferencesManager.getPatchVersion());
        reporter.onReport(EVENT_APPLIED_FAIL, map);
    }

    /**
     * 上传文件解压失败
     */
    public static void onApplyVersionCheckFail() {
        if (reporter == null) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("type", KEY_APPLIED_VERSION_CHECK);
        map.put("patch_version", PreferencesManager.getPatchVersion());
        reporter.onReport(EVENT_APPLIED_FAIL, map);
    }

    /**
     * 上传文件解压失败
     */
    public static void onApplyExtractFail(int fileType) {
        if (reporter == null) {
            return;
        }
        int type = -1;
        switch (fileType) {
            case ShareConstants.TYPE_DEX:
                type = KEY_APPLIED_DEX_EXTRACT;
                break;
            case ShareConstants.TYPE_LIBRARY:
                type = KEY_APPLIED_LIB_EXTRACT;
                break;
            case ShareConstants.TYPE_PATCH_FILE:
                type = KEY_APPLIED_PATCH_FILE_EXTRACT;
                break;
            case ShareConstants.TYPE_RESOURCE:
                type = KEY_APPLIED_RESOURCE_EXTRACT;
                break;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("patch_version", PreferencesManager.getPatchVersion());
        reporter.onReport(EVENT_APPLIED_FAIL, map);
    }

    /**
     * 合成包上报
     */
    public static void onApplied(long cost, boolean success) {
        if (reporter == null) {
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("status", success ? KEY_APPLIED_UPGRADE_SUCCESS : KEY_APPLIED_UPGRADE_FAIL);
        map.put("cost", cost);
        map.put("patch_version", PreferencesManager.getPatchVersion());
        reporter.onReport(EVENT_APPLIED, map);

        TinkerLog.i(TAG, "hp_report report apply cost = %d", cost);
    }

    /**
     * patch时检查补丁包失败
     */
    public static void onApplyPackageCheckFail(int errorCode) {
        if (reporter == null) {
            return;
        }
        TinkerLog.i(TAG, "hp_report package check failed, error = %d", errorCode);
        int type = -1;
        switch (errorCode) {
            case ShareConstants.ERROR_PACKAGE_CHECK_SIGNATURE_FAIL:
                type = KEY_APPLIED_PACKAGE_CHECK_SIGNATURE;
                break;
            case ShareConstants.ERROR_PACKAGE_CHECK_DEX_META_CORRUPTED:
                type = KEY_APPLIED_PACKAGE_CHECK_DEX_META;
                break;
            case ShareConstants.ERROR_PACKAGE_CHECK_LIB_META_CORRUPTED:
                type = KEY_APPLIED_PACKAGE_CHECK_LIB_META;
                break;
            case ShareConstants.ERROR_PACKAGE_CHECK_PATCH_TINKER_ID_NOT_FOUND:
                type = KEY_APPLIED_PACKAGE_CHECK_PATCH_TINKER_ID_NOT_FOUND;
                break;
            case ShareConstants.ERROR_PACKAGE_CHECK_APK_TINKER_ID_NOT_FOUND:
                type = KEY_APPLIED_PACKAGE_CHECK_APK_TINKER_ID_NOT_FOUND;
                break;
            case ShareConstants.ERROR_PACKAGE_CHECK_TINKER_ID_NOT_EQUAL:
                type = KEY_APPLIED_PACKAGE_CHECK_TINKER_ID_NOT_EQUAL;
                break;
            case ShareConstants.ERROR_PACKAGE_CHECK_PACKAGE_META_NOT_FOUND:
                type = KEY_APPLIED_PACKAGE_CHECK_META_NOT_FOUND;
                break;
            case ShareConstants.ERROR_PACKAGE_CHECK_RESOURCE_META_CORRUPTED:
                type = KEY_APPLIED_PACKAGE_CHECK_RES_META;
                break;
            case ShareConstants.ERROR_PACKAGE_CHECK_TINKERFLAG_NOT_SUPPORT:
                type = KEY_APPLIED_PACKAGE_CHECK_TINKERFLAG_NOT_SUPPORT;
                break;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("patch_version", PreferencesManager.getPatchVersion());
        reporter.onReport(EVENT_APPLIED_FAIL, map);
    }

    public static void onApplyCrash(Throwable throwable) {
        if (reporter == null) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("type", KEY_APPLIED_EXCEPTION);
        map.put("patch_version", PreferencesManager.getPatchVersion());
        map.put("message", "Tinker Exception:apply tinker occur exception " + Utils.getExceptionCauseString(throwable));
        reporter.onReport(EVENT_CRASH_CAUSE_CHECK, map);
    }

    /**
     * 上传启动时引起的奔溃
     */
    public static void onFastCrashProtect() {
        if (reporter == null) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("type", KEY_CRASH_FAST_PROTECT);
        map.put("patch_version", PreferencesManager.getPatchVersion());
        reporter.onReport(EVENT_CRASH_CAUSE_CHECK, map);
    }

    /**
     * 上传系统引起的奔溃
     */
    public static void onXposedCrash() {
        if (reporter == null) {
            return;
        }
        int type = -1;
        if (ShareTinkerInternals.isVmArt()) {
            type = KEY_CRASH_CAUSE_XPOSED_ART;
        } else {
            type = KEY_CRASH_CAUSE_XPOSED_DALVIK;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("patch_version", PreferencesManager.getPatchVersion());
        reporter.onReport(EVENT_CRASH_CAUSE_CHECK, map);
    }

    /**
     * 上传重试的次数
     */
    public static void onReportRetryPatch() {
        if (reporter == null) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("type", KEY_APPLY_RETRY);
        map.put("patch_version", PreferencesManager.getPatchVersion());
        reporter.onReport(EVENT_APPLY_RETRY_CHECK, map);
    }
}
