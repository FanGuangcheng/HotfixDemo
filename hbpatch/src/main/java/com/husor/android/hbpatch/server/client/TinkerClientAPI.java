/*
 * Copyright (C) 2011-2017 Husor Inc.
 * All Rights Reserved.
 */
package com.husor.android.hbpatch.server.client;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.husor.android.hbpatch.app.callback.PatchRequestCallback;
import com.husor.android.hbpatch.app.utils.PreferencesManager;
import com.husor.android.hbpatch.server.model.DataFetcher;
import com.husor.android.hbpatch.server.model.TinkerClientUrl;
import com.husor.android.hbpatch.server.model.response.SyncResponse;
import com.husor.android.hbpatch.server.urlconnection.UrlConnectionUrlLoader;
import com.husor.android.hbpatch.server.utils.Constants;
import com.husor.android.hbpatch.server.utils.Preconditions;
import com.husor.android.hbpatch.server.utils.ServerUtils;
import com.husor.android.hbpatch.server.utils.VersionUtils;
import com.tencent.tinker.lib.util.TinkerLog;

import java.io.File;
import java.io.InputStream;

import static com.husor.android.hbpatch.server.model.response.SyncResponse.fromJson;

/**
 * Created by fanguanggcheng on 2018/1/30.
 */
public class TinkerClientAPI {

    private static final String TAG = "Tinker.TinkerClientAPI";

    private static volatile TinkerClientAPI clientAPI;
    private final VersionUtils versionUtils;
    private final String appVersion;
    private final String appKey;
    private final String host;
    private final boolean debug;
    private final UrlConnectionUrlLoader loader;

    public TinkerClientAPI(String appKey, String appVersion, String host,
                           Boolean debug, UrlConnectionUrlLoader loader, VersionUtils versionUtils) {
        this.appVersion = appVersion;
        this.appKey = appKey;
        this.host = host;
        this.debug = debug;
        this.loader = loader;
        this.versionUtils = versionUtils;
    }

    public static TinkerClientAPI init(Context context, String appKey, String appVersion,
                                       Boolean debug) {
        if (clientAPI == null) {
            synchronized (TinkerClientAPI.class) {
                if (clientAPI == null) {
                    clientAPI = new Builder()
                            .appKey(appKey)
                            .appVersion(appVersion)
                            .debug(debug)
                            .versionUtils(new VersionUtils(context, appVersion))
                            .build();
                }
            }
        }
        return clientAPI;
    }

    public void updateTinkerVersion(Integer newVersion, String md5) {
        versionUtils.updateVersionProperty(getAppVersion(), newVersion, md5, versionUtils.grayValue(), versionUtils.id());
    }

    public void update(final Context context, String config, final PatchRequestCallback callback) {
        if (callback == null) {
            throw new RuntimeException("callback can't be null");
        }
        if (!callback.beforePatchRequest()) {
            return;
        }

        //  如果没有config,保护措施
        if (TextUtils.isEmpty(config)) {
            sync(new DataFetcher.DataCallback<String>() {
                @Override
                public void onDataReady(String data) {
                    SyncResponse response = fromJson(data);
                    // 存取version
                    PreferencesManager.setPatchVersion(response.version);
                    if (response == null) {
                        callback.onPatchSyncFail(new RuntimeException("Can't sync with version: response == null"));
                    } else {
                        TinkerLog.d(TAG, "sync response in update: " + response);

                        // 下载补丁
                        downloadHotPatch(context, response, callback);

                    }
                }

                @Override
                public void onLoadFailed(Exception e) {
                    callback.onPatchSyncFail(e);
                }
            });
        } else {
            // 如果有config
            SyncResponse response = fromJson(config);
            // 存取version
            PreferencesManager.setPatchVersion(response.version);
            TinkerLog.d(TAG, "sync response in config: " + response);
            if (response != null) {
                downloadHotPatch(context, response, callback);
            }

        }
    }

    public void downloadHotPatch(final Context context, final SyncResponse response, final PatchRequestCallback callback) {
        if (response.isRollback) {
            callback.onPatchRollback();
            return;
        }

        if (response.isPaused) {
            TinkerLog.d(TAG, "Needn't update, sync response is: " + response.toString());
            return;
        }

        if (!TextUtils.isEmpty(response.conditions)) {
            callback.updatePatchConditions();
        }

        final Integer newVersion = Integer.parseInt(response.version);
        if (versionUtils.isUpdate(newVersion, getAppVersion())) {
            DataFetcher.DataCallback<File> downloadCallback = new DataFetcher.DataCallback<File>() {
                @Override
                public void onDataReady(File data) {
                    callback.onPatchUpgrade(data, newVersion, getCurrentPatchVersion());
                }

                @Override
                public void onLoadFailed(Exception e) {
                    callback.onPatchDownloadFail(e, newVersion, getCurrentPatchVersion());
                }
            };
            String patchPath = ServerUtils.getServerFile(context, getAppVersion(), response.version).getAbsolutePath();
            download(response, patchPath, downloadCallback);
        } else {
            TinkerLog.d(TAG, "Need't update, sync response is: " + response.toString() + "\ngray: " + versionUtils.grayValue());
        }
    }

    public void sync(final DataFetcher.DataCallback<String> callback) {
        Uri.Builder urlBuilder = Uri.parse(this.host).buildUpon();
        if (clientAPI.debug) {
            urlBuilder.appendPath("dev");
        }

        String url = urlBuilder.appendPath(this.appKey)
                .appendPath(this.appVersion)
                .appendQueryParameter("d", versionUtils.id())
                .appendQueryParameter("v", String.valueOf(System.currentTimeMillis()))
                .build().toString();

        TinkerClientUrl tkClientUrl = new TinkerClientUrl.Builder().url(url).build();
        final DataFetcher<InputStream> dataFetcher = loader.buildLoadData(tkClientUrl);
        dataFetcher.loadData(new DataFetcher.DataCallback<InputStream>() {
            @Override
            public void onDataReady(InputStream data) {
                if (callback == null) {
                    return;
                }
                try {
                    String response = ServerUtils.readStreamToString(data, ServerUtils.CHARSET);
                    TinkerLog.d(TAG, "tinker server sync respond:" + response);

                    SyncResponse.fromJson(response);
                    callback.onDataReady(response);
                } catch (Exception e) {
                    callback.onLoadFailed(e);
                } finally {
                    dataFetcher.cleanup();
                }
            }

            @Override
            public void onLoadFailed(Exception e) {
                if (callback == null) {
                    return;
                }
                try {
                    callback.onLoadFailed(e);
                } finally {
                    dataFetcher.cleanup();
                }
            }
        });
    }

    public void download(SyncResponse response, final String filePath, final DataFetcher.DataCallback<? super File> callback) {
        Preconditions.checkNotEmpty(response.version);
        String url = response.url;
        if (TextUtils.isEmpty(url)) {
            url = Uri.parse(this.host)
                    .buildUpon()
                    .appendPath(this.appKey)
                    .appendPath(this.appVersion)
                    .appendPath(String.format("file%s", response.version))
                    .appendQueryParameter("d", versionUtils.id())
                    .appendQueryParameter("v", String.valueOf(System.currentTimeMillis()))
                    .build().toString();
        }

        TinkerClientUrl tkClientUrl = new TinkerClientUrl.Builder().url(url).build();
        final DataFetcher<InputStream> dataFetcher = loader.buildLoadData(tkClientUrl);
        dataFetcher.loadData(new DataFetcher.DataCallback<InputStream>() {
            @Override
            public void onDataReady(InputStream data) {
                if (callback == null) {
                    return;
                }

                try {
                    callback.onDataReady(ServerUtils.readStreamToFile(data, filePath));
                } catch (Exception e) {
                    callback.onLoadFailed(e);
                } finally {
                    dataFetcher.cleanup();
                }
            }

            @Override
            public void onLoadFailed(Exception e) {
                if (callback == null) {
                    return;
                }

                try {
                    callback.onLoadFailed(e);
                } finally {
                    dataFetcher.cleanup();
                }
            }
        });
    }

    public Integer getCurrentPatchVersion() {
        return versionUtils.getPatchVersion();
    }

    public String getCurrentPatchMd5() {
        return versionUtils.getPatchMd5();
    }

    public String getAppVersion() {
        return appVersion;
    }

    static class Builder {

        private String appVersion;
        private String appKey;
        private String host;
        private Boolean debug;
        private UrlConnectionUrlLoader loader;
        private VersionUtils versionUtils;

        Builder host(String host) {
            this.host = host;
            return this;
        }

        Builder appKey(String appKey) {
            this.appKey = appKey;
            return this;
        }

        Builder appVersion(String appVersion) {
            this.appVersion = appVersion;
            return this;
        }

        Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        Builder urlLoader(UrlConnectionUrlLoader loader) {
            this.loader = loader;
            return this;
        }

        Builder versionUtils(VersionUtils versionUtils) {
            this.versionUtils = versionUtils;
            return this;
        }

        void makeDefault() {
            if (TextUtils.isEmpty(host)) {
                this.host = Constants.HOST_URL;
            }
            if (this.loader == null) {
                this.loader = new UrlConnectionUrlLoader();
            }
            if (TextUtils.isEmpty(this.appVersion)) {
                throw new RuntimeException("You need setup appkey and appversion");
            }
        }

        public TinkerClientAPI build() {
            makeDefault();
            return new TinkerClientAPI(this.appKey, this.appVersion, this.host, this.debug, this.loader, this.versionUtils);
        }
    }
}
