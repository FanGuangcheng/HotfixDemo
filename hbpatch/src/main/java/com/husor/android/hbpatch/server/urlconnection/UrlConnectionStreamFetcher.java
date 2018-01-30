/*
 * Copyright (C) 2011-2017 Husor Inc.
 * All Rights Reserved.
 */
package com.husor.android.hbpatch.server.urlconnection;

import com.husor.android.hbpatch.server.model.DataFetcher;
import com.husor.android.hbpatch.server.model.TinkerClientUrl;
import com.husor.android.hbpatch.server.utils.Preconditions;
import com.husor.android.hbpatch.server.utils.ServerUtils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tencent.tinker.lib.util.TinkerLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by fanguanggcheng on 2018/1/30.
 */
public class UrlConnectionStreamFetcher implements DataFetcher<InputStream> {

    private static final String TAG = "Tinker.UrlConnectionStreamFetcher";
    private final TinkerClientUrl tkUrl;
    private final Executor executor;
    private InputStream stream;

    public UrlConnectionStreamFetcher(Executor executor, TinkerClientUrl tkUrl) {
        this.tkUrl = tkUrl;
        this.executor = executor;
    }

    @Override
    public void loadData(final DataCallback<? super InputStream> callback) {
        ConnectionWorker worker = new ConnectionWorker(tkUrl, new DataCallback<InputStream>() {
            @Override
            public void onDataReady(InputStream data) {
                stream = data;
                callback.onDataReady(data);
            }

            @Override
            public void onLoadFailed(Exception e) {
                callback.onLoadFailed(e);
            }
        });
        executor.execute(worker);
    }

    @Override
    public void cleanup() {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cancel() {

    }

    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    private static class ConnectionWorker implements Runnable {

        private final DataCallback<? super InputStream> callback;
        private final TinkerClientUrl url;

        ConnectionWorker(TinkerClientUrl url, DataCallback<? super InputStream> callback) {
            this.callback = Preconditions.checkNotNull(callback);
            this.url = Preconditions.checkNotNull(url);
        }

        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
                final Request request = new Request.Builder()
                        .url(this.url.toURL())
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        callback.onLoadFailed(e);
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        TinkerLog.d(TAG, "response code " + response.code() + " msg: " + response.message());
                        callback.onDataReady(response.body().byteStream());
                    }
                });


//                HttpURLConnection conn = (HttpURLConnection) url.toURL().openConnection();
//                conn.setRequestMethod(url.getMethod());
//                conn.setDoOutput(true);
//                conn.setReadTimeout(10000 /* milliseconds */);
//                conn.setConnectTimeout(15000 /* milliseconds */);
//                conn.setInstanceFollowRedirects(false);
//                conn.setUseCaches(false);
//                for (Map.Entry<String, String> entry : url.getHeaders().entrySet()) {
//                    conn.setRequestProperty(entry.getKey(), entry.getValue());
//                }
//                switch (url.getMethod()) {
//                    case "GET":
//                        break;
//                    case "POST":
//                        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), ServerUtils.CHARSET);
//                        writer.write(url.getBody());
//                        writer.flush();
//                        writer.close();
//                        break;
//                    default:
//                        throw new RuntimeException("Unsupported request method" + url.getMethod());
//                }
//                conn.connect();
//                TinkerLog.d(TAG, "response code " + conn.getResponseCode() + " msg: " + conn.getResponseMessage());
//                InputStream inputStream = conn.getInputStream();
//                this.callback.onDataReady(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
                this.callback.onLoadFailed(e);
            }
        }
    }
}
