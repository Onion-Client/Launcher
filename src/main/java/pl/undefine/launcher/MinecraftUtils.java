/*
 * Copyright (c) 2021-2022, Undefine <cqundefine@gmail.com>
 *
 * SPDX-License-Identifier: BSD-2-Clause
 */

package pl.undefine.launcher;

import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class MinecraftUtils
{
    private static final JSONParser parser = new JSONParser();

    public static File root = new File(System.getenv("APPDATA"), ".onionclient");
    public static File assets = new File(root, "assets");
    public static File assetsIndexes = new File(assets, "indexes");
    public static File assetsIndex = new File(assetsIndexes, "1.8.json");
    public static File assetsObjects = new File(assets, "objects");
    public static File libraries = new File(root, "libraries");
    public static File logs = new File(root, "logs");
    public static File natives = new File(root, "natives");
    public static File nativesZIP = new File(natives, "Natives.zip");
    public static File resourcepacks = new File(root, "resourcepacks");
    public static File saves = new File(root, "saves");
    public static File screenshots = new File(root, "screenshots");
    public static File versions = new File(root, "versions");
    public static File versionsOnion = new File(versions, "OnionClient");
    public static File clientJAR = new File(versionsOnion, "OnionClient.jar");
    public static File clientJSON = new File(versionsOnion, "OnionClient.json");

    public static List<String> libraryList = new ArrayList<>();

    public static final String VERSION_URL = "https://raw.githubusercontent.com/Onion-Client/LauncherFiles/main/Version.json";
    public static final String VERSION_URL_SHA1 = "https://raw.githubusercontent.com/Onion-Client/LauncherFiles/main/Version.json.sha1";

    public static final String NATIVES_URL = "https://raw.githubusercontent.com/Onion-Client/LauncherFiles/main/Natives.zip";
    public static final String NATIVES_URL_SHA1 = "https://raw.githubusercontent.com/Onion-Client/LauncherFiles/main/Natives.zip.sha1";

    public static MinecraftUserInfo LoginWithMojang(String username, String password)
    {
        try
        {
            /*
            "agent": {
                "name": "Minecraft",
                "version": 1
            },
            "username": "username",
            "password": "password"
            */

            String json = "{" +
                    "   \"agent\": {\n" +
                    "      \"name\": \"Minecraft\",\n" +
                    "      \"version\": 1\n" +
                    "   },\n" +
                    "   \"username\": \"" + username + "\",\n" +
                    "   \"password\": \"" + password + "\"\n" +
                    "}";

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), json);


            Request request = new Request.Builder()
                    .url("https://authserver.mojang.com/authenticate")
                    .post(body)
                    .build();

            Call call = new OkHttpClient().newCall(request);
            Response response = call.execute();

            System.out.println(response.body().string());

            return null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean CheckNatives()
    {
        for(String line : NetUtils.GetFileToString(NATIVES_URL_SHA1).split("\n"))
        {
            String sum = line.split(" ")[0];
            String file = line.split(" ")[1];
            if(!FileUtils.CheckFile(new File(natives, file), sum))
            {
                return false;
            }
        }
        return true;
    }

    private static void VerifyNatives()
    {
        for(String line : NetUtils.GetFileToString(NATIVES_URL_SHA1).split("\n"))
        {
            String sum = line.split(" ")[0];
            String file = line.split(" ")[1];
            FileUtils.VerifyFile(new File(natives, file), sum);
        }
    }

    public static void EnsureRootIntegrity()
    {
        try
        {
            FileUtils.EnsureExistence(root);
            FileUtils.EnsureExistence(assets);
            FileUtils.EnsureExistence(assetsIndexes);
            FileUtils.EnsureExistence(assetsObjects);
            FileUtils.EnsureExistence(libraries);
            FileUtils.EnsureExistence(logs);
            FileUtils.EnsureExistence(natives);
            FileUtils.EnsureExistence(resourcepacks);
            FileUtils.EnsureExistence(saves);
            FileUtils.EnsureExistence(screenshots);
            FileUtils.EnsureExistence(versions);
            FileUtils.EnsureExistence(versionsOnion);

            String clientJSONSHA1 = NetUtils.GetFileToString(VERSION_URL_SHA1);
            if (!FileUtils.CheckFile(clientJSON, clientJSONSHA1))
            {
                System.out.println("Downloading " + VERSION_URL + "...");
                NetUtils.DownloadFile(VERSION_URL, clientJSON);
                FileUtils.VerifyFile(clientJSON, clientJSONSHA1);
            }

            String versionJSON = FileUtils.ReadFile(clientJSON);
            JSONObject VersionJSONRoot = (JSONObject) parser.parse(versionJSON);
            JSONObject client = (JSONObject) ((JSONObject) VersionJSONRoot.get("downloads")).get("client");

            String clientJARSHA1 = client.get("sha1").toString();
            if (!FileUtils.CheckFile(clientJSON, clientJSONSHA1))
            {
                System.out.println("Downloading " + client.get("url").toString() + "...");
                NetUtils.DownloadFile(client.get("url").toString(), clientJAR);
                FileUtils.VerifyFile(clientJAR, clientJARSHA1);
            }

            if (!CheckNatives())
            {
                System.out.println("Downloading " + NATIVES_URL + "...");
                NetUtils.DownloadFile(NATIVES_URL, nativesZIP);
                FileUtils.UnzipFile(nativesZIP);
                nativesZIP.delete();
                VerifyNatives();
            }

            JSONArray LibrariesRoot = (JSONArray) VersionJSONRoot.get("libraries");
            Iterator<JSONObject> LibraryIterator = LibrariesRoot.iterator();

            while (LibraryIterator.hasNext())
            {
                JSONObject Library = LibraryIterator.next();
                JSONObject Downloads = (JSONObject) Library.get("downloads");
                if (Downloads.containsKey("artifact"))
                {
                    JSONObject Artifact = (JSONObject) Downloads.get("artifact");
                    String Path = Artifact.get("path").toString();
                    String URL = Artifact.get("url").toString();
                    String Sum = Artifact.get("sha1").toString();
                    File File = new File(libraries, Path);
                    libraryList.add(Path);
                    if (!FileUtils.CheckFile(File, Sum))
                    {
                        System.out.println("Downloading " + URL + "...");
                        NetUtils.DownloadFile(URL, new File(libraries, Path));
                        FileUtils.VerifyFile(File, Sum);
                    }
                }
            }

            JSONObject AssetIndex = (JSONObject) VersionJSONRoot.get("assetIndex");
            String URL = AssetIndex.get("url").toString();
            String Sum = AssetIndex.get("sha1").toString();

            if (!FileUtils.CheckFile(assetsIndex, Sum))
            {
                System.out.println("Downloading " + URL + "...");
                NetUtils.DownloadFile(URL, assetsIndex);
                FileUtils.VerifyFile(assetsIndex, Sum);
            }

            String AssetIndexJSON = FileUtils.ReadFile(assetsIndex);
            JSONObject AssetIndexJSONRoot = (JSONObject) parser.parse(AssetIndexJSON);
            JSONObject Objects = (JSONObject) AssetIndexJSONRoot.get("objects");

            for (Object string : Objects.keySet())
            {
                String path = string.toString();
                JSONObject Asset = (JSONObject) Objects.get(path);
                String hash = Asset.get("hash").toString();
                String url = String.format("http://resources.download.minecraft.net/%s/%s", hash.substring(0, 2), hash);
                path = String.format("%s/%s", hash.substring(0, 2), hash);
                if (!new File(assetsObjects, path).exists())
                {
                    System.out.println("Downloading " + url + "...");
                    NetUtils.DownloadFile(url, new File(assetsObjects, path));
                }
            }
            //TODO: Download log4j2.xml

        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    public static void LaunchMinecraft(String username)
    {
        try
        {
            StringBuilder libs = new StringBuilder();
            for (String string : libraryList)
            {
                libs.append(libraries.getAbsolutePath()).append("\\").append(string).append(";");
            }

            Process p = Runtime.getRuntime().exec("java.exe " +
                    "\"-Dos.name=Windows 10\"" +
                    "  -Dos.version=10.0" +
                    "  -Djava.library.path=" + natives +
                    "  -Dminecraft.client.jar=" + clientJAR +
                    "  -cp " + libs + clientJAR +
                    "  -Xmx4G" +
                    "  -XX:+UnlockExperimentalVMOptions" +
                    "  -XX:+UseG1GC" +
                    "  -XX:G1NewSizePercent=20" +
                    "  -XX:G1ReservePercent=20" +
                    "  -XX:MaxGCPauseMillis=50" +
                    "  -XX:G1HeapRegionSize=32M" +
                    "  net.minecraft.client.main.Main" +
                    "  --username " + username +
                    "  --version OnionClient" +
                    "  --gameDir " + root +
                    "  --assetsDir " + assets +
                    "  --assetIndex 1.8" +
                    "  --uuid " + UUID.randomUUID() +
                    "  --accessToken 123456789" +
                    "  --userProperties \"{\\\"preferredLanguage\\\" : [\\\"pl-pl\\\"],\\\"registrationCountry\\\" : [\\\"PL\\\"]}\"" +
                    "  --userType mojang");
            BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String errorString = error.readLine();
            System.out.println(errorString);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static class MinecraftUserInfo
    {
        public UUID uuid;
        public String username;
        public String accessToken;
        public String preferredLanguage;
        public String registrationCountry;

        public MinecraftUserInfo(UUID uuid, String username, String accessToken, String preferredLanguage, String registrationCountry)
        {
            this.uuid = uuid;
            this.username = username;
            this.accessToken = accessToken;
            this.preferredLanguage = preferredLanguage;
            this.registrationCountry = registrationCountry;
        }
    }
}
