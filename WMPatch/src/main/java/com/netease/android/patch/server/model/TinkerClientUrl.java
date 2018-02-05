/*
 * Copyright (C) 2011-2017 Husor Inc.
 * All Rights Reserved.
 */
package com.netease.android.patch.server.model;

import android.net.Uri;
import android.text.TextUtils;

import com.netease.android.patch.server.utils.Preconditions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fanguanggcheng on 2018/1/30.
 */
public class TinkerClientUrl {

    private static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";

    private final Headers headers;
    private final String stringUrl;
    private final String body;
    private final String method;

    private String safeStringUrl;
    private URL safeUrl;

    public TinkerClientUrl(String stringUrl, Headers headers, String body, String method) {
        this.stringUrl = Preconditions.checkNotEmpty(stringUrl);
        this.method = Preconditions.checkNotEmpty(method);
        this.headers = headers;
        this.body = body;
    }

    public URL toURL() throws MalformedURLException {
        return getSafeUrl();
    }

    public String toStringUrl() {
        return getSafeStringUrl();
    }

    public Map<String, String> getHeaders() {
        return headers.getHeaders();
    }

    public String getMethod() {
        return method;
    }

    public String getBody() {
        return body;
    }

    // See http://stackoverflow.com/questions/3286067/url-encoding-in-android. Although the answer
    // using URI would work, using it would require both decoding and encoding each string which is
    // more complicated, slower and generates more objects than the solution below. See also issue
    // #133.
    private URL getSafeUrl() throws MalformedURLException {
        if (safeUrl == null) {
            safeUrl = new URL(getSafeStringUrl());
        }
        return safeUrl;
    }

    private String getSafeStringUrl() {
        if (TextUtils.isEmpty(safeStringUrl)) {
            safeStringUrl = Uri.encode(stringUrl, ALLOWED_URI_CHARS);
        }
        return safeStringUrl;
    }

    public static class Builder {
        private String url;
        private HashMap<String, String> params;
        private String body;
        private String method;
        private Headers headers;

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder param(String key, Object value) {
            if (params == null) {
                this.params = new HashMap<>();
            }
            this.params.put(key, String.valueOf(value));
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder method(String method) {
            switch (method) {
                case "GET":
                case "POST":
                    this.method = method;
                    break;
                default:
                    throw new RuntimeException("Didn't Supported Method, Please pass the correct method");
            }
            return this;
        }

        public Builder headers(Headers headers) {
            this.headers = headers;
            return this;
        }

        private void makeDefault() {
            Uri.Builder urlBuilder = Uri.parse(this.url).buildUpon();
            if (TextUtils.isEmpty(this.method)) {
                this.method = "GET";
            }
            if (this.headers == null) {
                this.headers = Headers.DEFAULT;
            }
            if (this.params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    urlBuilder.appendQueryParameter(entry.getKey(), entry.getValue());
                }
            }
            this.url = urlBuilder.build().toString();
        }

        public TinkerClientUrl build() {
            makeDefault();
            return new TinkerClientUrl(this.url, this.headers, this.body, this.method);
        }
    }
}