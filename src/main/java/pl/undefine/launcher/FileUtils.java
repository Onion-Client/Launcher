package pl.undefine.launcher;

import java.io.*;

public class FileUtils
{
    public static void EnsureExistence(File file)
    {
        if (!file.exists()) file.mkdirs();
    }

    public static String ReadFile(File file)
    {
        try
        {
            FileInputStream inputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null)
            {
                builder.append(line);
            }

            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();

            return builder.toString();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "";
        }
    }
}
