/*
 * Copyright (c) 2021, Undefine <cqundefine@gmail.com>
 *
 * SPDX-License-Identifier: BSD-2-Clause
 */

package pl.undefine.launcher;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class MinecraftUtils
{
    public static File root = new File(System.getenv("APPDATA"), ".onionclient");
    public static File libraries = new File(root, "libraries");

    public static void EnsureRootIntegrity()
    {
        try
        {
            if (!root.exists()) root.mkdirs();
            if (!libraries.exists()) libraries.mkdirs();

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("http://s3.amazonaws.com/Minecraft.Download/versions/1.8.9/1.8.9.json")
                    .build();

            String json = Objects.requireNonNull(client.newCall(request).execute().body()).string();

            JSONObject obj = (JSONObject) new JSONParser().parse(json);
            JSONArray array = (JSONArray) obj.get("libraries");
            Iterator<JSONObject> iterator = array.iterator();
            while (iterator.hasNext())
            {
                JSONObject lib = iterator.next();
                JSONObject downloads = (JSONObject) lib.get("downloads");
                JSONObject artifact = (JSONObject) downloads.get("artifact");
                System.out.println("Downloading " + artifact.get("url").toString() + "...");
                FileUtils.DownloadFile(artifact.get("url").toString(), new File(libraries, artifact.get("path").toString()));
            }
        }
        catch (ParseException | IOException e)
        {
            e.printStackTrace();
        }
    }
}
