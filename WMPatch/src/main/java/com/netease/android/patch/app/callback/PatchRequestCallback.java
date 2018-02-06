/*
 * Copyright (C) 1997-2018 NetEase Inc.
 * All Rights Reserved.
 */
package com.netease.android.patch.app.callback;

import java.io.File;

/**
 * Created by fanguanggcheng on 2018/1/30.
 */
public interface PatchRequestCallback {

    /**
     * 在请求补丁前，我们可以在这个接口拦截请求
     * @return 返回false，即不会请求服务器
     */
    boolean beforePatchRequest();

    /**
     * 服务器有新的补丁，并且已经成功的下载
     * @param file 下载好的补丁版本，存放在/data/data/app_name/tinker_server/
     * @param newVersion 新的补丁版本
     * @param currentVersion 当前的补丁版本
     * @return
     */
    boolean onPatchUpgrade(File file, Integer newVersion, Integer currentVersion);

    /**
     * 向服务器请求新的补丁时，下载补丁失败
     * @param e 错误类型
     * @param newVersion 下载失败的新补丁版本
     * @param currentVersion 当前的补丁版本
     */
    void onPatchDownloadFail(Exception e, Integer newVersion, Integer currentVersion);

    /**
     * 与服务器同步时失败
     * @param e 失败类型
     */
    void onPatchSyncFail(Exception e);

    /**
     * 收到服务器清除补丁的请求
     */
    void onPatchRollback();

    /**
     * 若使用条件下发方式发布补丁，某些动态改变的条件，可以在这个接口更新。例如是否为wifi
     */
    void updatePatchConditions();
}
