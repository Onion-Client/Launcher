/*
 * Copyright (c) 2021, Undefine <cqundefine@gmail.com>
 *
 * SPDX-License-Identifier: BSD-2-Clause
 */

package pl.undefine.launcher;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.util.Objects;

public class NetUtils
{
    private static final OkHttpClient client = new OkHttpClient();

    private static final int DATA_DOWNLOAD_BUFFER = 1024;

    public static void DownloadFile(String url, File path)
    {
        try
        {
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();

            InputStream is = Objects.requireNonNull(response.body()).byteStream();

            BufferedInputStream input = new BufferedInputStream(is);
            if(!path.exists()) path.mkdirs();
            if(path.exists()) path.delete();
            OutputStream output = new FileOutputStream(path);

            byte[] data = new byte[DATA_DOWNLOAD_BUFFER];
            int count;

            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        }
        catch (IOException | NullPointerException e)
        {
            //TODO: Error out with a message box
            e.printStackTrace();
        }
    }

    public static String GetFileToString(String url)
    {
        try
        {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            return Objects.requireNonNull(client.newCall(request).execute().body()).string();
        }
        catch (IOException e)
        {
            //TODO: Error out with a message box
            e.printStackTrace();
            return null;
        }
    }
}
