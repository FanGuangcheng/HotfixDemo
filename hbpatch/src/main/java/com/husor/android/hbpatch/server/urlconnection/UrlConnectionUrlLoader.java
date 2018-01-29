/*
 * Copyright (C) 2011-2017 Husor Inc.
 * All Rights Reserved.
 */
package com.husor.android.hbpatch.server.urlconnection;

import com.husor.android.hbpatch.server.model.DataFetcher;
import com.husor.android.hbpatch.server.model.TinkerClientUrl;
import com.tencent.tinker.lib.util.TinkerLog;

import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by fanchao on 17/4/17.
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
