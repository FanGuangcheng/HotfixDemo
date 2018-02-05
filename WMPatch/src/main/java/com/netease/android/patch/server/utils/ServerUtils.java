/*
 * Copyright (C) 2011-2017 Husor Inc.
 * All Rights Reserved.
 */
package com.netease.android.patch.server.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by fanguanggcheng on 2018/1/30.
 */
public final class ServerUtils {
    public static final String CHARSET = "UTF-8";
    public static final int BUFFER_SIZE = 4096;
    public static final String TINKER_SERVER_DIR = "tinker_server";
    public static final String TINKER_VERSION_FILE = "version.info";


    private ServerUtils() {
        // A TinkerServerUtils Class
    }

    public static File readStreamToFile(InputStream inputStream, String filePath) throws IOException {
        if (inputStream == null) {
            return null;
        }

        File file = new File(filePath);
        File parent = file.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException(String.format("Can't create folder %s", parent.getAbsolutePath()));
        }
        FileOutputStream fileOutput = new FileOutputStream(file);
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bufferLength;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
            }
        } finally {
            try {
                fileOutput.flush();
                fileOutput.close();
            } catch (IOException ignored) {
                // ignored
            }
        }
        return file;
    }

    public static Integer stringToInteger(String string) {
        if (string == null) {
            return null;
        }
        return Integer.parseInt(string);
    }

    public static String readStreamToString(InputStream inputStream, String charset) {
        if (inputStream == null) {
            return null;
        }
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int bufferLength;

        String result;
        try {
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                bo.write(buffer, 0, bufferLength);
            }
            result = bo.toString(charset);
        } catch (Throwable e) {
            result = null;
        }
        return result;
    }

    public static File getServerDirectory(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        if (applicationInfo == null) {
            // Looks like running on a test Context, so just return without patching.
            return null;
        }
        return new File(applicationInfo.dataDir, TINKER_SERVER_DIR);
    }

    public static File getServerFile(Context context, String appVersion, String currentVersion) {
        return new File(getServerDirectory(context), appVersion + "_" + currentVersion + ".apk");
    }
}
