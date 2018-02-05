/*
 * Copyright (C) 2011-2017 Husor Inc.
 * All Rights Reserved.
 */
package com.netease.android.patch.server.urlconnection;

import com.netease.android.patch.server.model.DataFetcher;
import com.netease.android.patch.server.model.TinkerClientUrl;
import com.tencent.tinker.lib.util.TinkerLog;

import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by fanguanggcheng on 2018/1/30.
 */
public class UrlConnectionUrlLoader {

    public static final String TAG = "Tinker.UrlLoader";
    private final Executor executor;

    public UrlConnectionUrlLoader() {
        executor = Executors.newSingleThreadExecutor();
    }

    public DataFetcher<InputStream> buildLoadData(TinkerClientUrl url) {
        TinkerLog.i(TAG, "loadData from: %s", url.toStringUrl());
        return new UrlConnectionStreamFetcher(executor, url);
    }

}
