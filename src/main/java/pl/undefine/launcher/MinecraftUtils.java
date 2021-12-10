/*
 * Copyright (c) 2021, Undefine <cqundefine@gmail.com>
 *
 * SPDX-License-Identifier: BSD-2-Clause
 */

package pl.undefine.launcher;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
    public static File resourcepacks = new File(root, "resourcepacks");
    public static File saves = new File(root, "saves");
    public static File screenshots = new File(root, "screenshots");
    public static File versions = new File(root, "versions");
    public static File versionsOnion = new File(versions, "OnionClient");
    public static File clientJAR = new File(versionsOnion, "OnionClient.jar");
    public static File clientJSON = new File(versionsOnion, "OnionClient.json");

    public static List<String> libraryList = new ArrayList<>();

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
            FileUtils.EnsureExistence(resourcepacks);
            FileUtils.EnsureExistence(saves);
            FileUtils.EnsureExistence(screenshots);
            FileUtils.EnsureExistence(versions);
            FileUtils.EnsureExistence(versionsOnion);

            if (!clientJAR.exists())
            {
                System.out.println("Downloading https://raw.githubusercontent.com/Onion-Client/LauncherFiles/main/OnionClient.jar...");
                NetUtils.DownloadFile("https://raw.githubusercontent.com/Onion-Client/LauncherFiles/main/OnionClient.jar", clientJAR.getAbsoluteFile());
            }

            if (!clientJSON.exists())
            {
                System.out.println("Downloading https://raw.githubusercontent.com/Onion-Client/LauncherFiles/main/Version.json...");
                NetUtils.DownloadFile("https://raw.githubusercontent.com/Onion-Client/LauncherFiles/main/Version.json", clientJSON.getAbsoluteFile());
            }

            if (!natives.exists())
            {
                System.out.println("Downloading https://raw.githubusercontent.com/Onion-Client/LauncherFiles/main/Natives.zip...");
                NetUtils.DownloadFile("https://raw.githubusercontent.com/Onion-Client/LauncherFiles/main/Natives.zip", new File(natives, "Natives.zip"));
                byte[] buffer = new byte[1024];
                ZipInputStream ZIPInputStream = new ZipInputStream(new FileInputStream(new File(natives, "Natives.zip")));
                ZipEntry Entry = ZIPInputStream.getNextEntry();
                while (Entry != null)
                {
                    System.out.println(Entry.getName());
                    File NativeDLL = new File(natives, Entry.getName());
                    NativeDLL.createNewFile();
                    FileOutputStream OutputSream = new FileOutputStream(NativeDLL);
                    int len;
                    while ((len = ZIPInputStream.read(buffer)) > 0)
                    {
                        OutputSream.write(buffer, 0, len);
                    }
                    OutputSream.close();
                    Entry = ZIPInputStream.getNextEntry();
                }
                ZIPInputStream.closeEntry();
                ZIPInputStream.close();
            }

            String versionJSON = FileUtils.ReadFile(clientJSON);

            JSONObject VersionJSONRoot = (JSONObject) parser.parse(versionJSON);
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
                    libraryList.add(Path);
                    if (!new File(libraries, Path).exists())
                    {
                        System.out.println("Downloading " + URL + "...");
                        NetUtils.DownloadFile(URL, new File(libraries, Path));
                    }
                }
            }

            JSONObject AssetIndex = (JSONObject) VersionJSONRoot.get("assetIndex");

            if (!assetsIndex.exists())
            {
                System.out.println("Downloading " + AssetIndex.get("url").toString() + "...");
                NetUtils.DownloadFile(AssetIndex.get("url").toString(), new File(assetsIndexes, "1.8.json"));
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
        catch (ParseException | IOException e)
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
                libs.append(libraries.getAbsolutePath() + "\\" + string + ";");
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
                    "  --uuid b8e2cbd659f711ecbf630242ac130002" +
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
}
