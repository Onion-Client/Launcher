/*
 * Copyright (c) 2021-2022, Undefine <cqundefine@gmail.com>
 *
 * SPDX-License-Identifier: BSD-2-Clause
 */

package pl.undefine.launcher;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils
{
    public static final int READ_BUFFER_SIZE = 1024;

    public static void EnsureExistence(File file)
    {
        if (!file.exists()) file.mkdirs();
    }

    public static String ReadFile(File file)
    {
        try
        {
            return new String(IOUtils.toByteArray(new FileInputStream(file)));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] ReadFileBinary(File file)
    {
        try
        {
            return IOUtils.toByteArray(new FileInputStream(file));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static String SHA1Sum(byte[] value)
    {
        return DigestUtils.sha1Hex(value);
    }

    public static String SHA1SumFile(File file)
    {
        return SHA1Sum(ReadFileBinary(file));
    }

    public static void VerifyFile(File file, String sum)
    {
        if(!SHA1SumFile(file).equals(sum))
        {
            System.err.printf("Error downloading %s, found SHA1: %s, expected %s%n", file.getAbsoluteFile(), SHA1SumFile(file), sum);
            System.exit(1);
        }
    }

    public static boolean CheckFile(File file, String sum)
    {
        return file.exists() && SHA1SumFile(file).equals(sum);
    }

    public static void UnzipFile(File file)
    {
        try
        {
            byte[] buffer = new byte[READ_BUFFER_SIZE];
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
            ZipEntry entry = zipInputStream.getNextEntry();
            while (entry != null)
            {
                File fileEntry = new File(file.getParentFile(), entry.getName());
                fileEntry.createNewFile();
                FileOutputStream outputStream = new FileOutputStream(fileEntry);
                int len;
                while ((len = zipInputStream.read(buffer)) > 0)
                {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.close();
                entry = zipInputStream.getNextEntry();
            }
            zipInputStream.closeEntry();
            zipInputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
