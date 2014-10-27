package com.sciaps.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sciaps.common.data.Standard;
import com.sciaps.listener.DownloadListener;
import com.sciaps.model.IsAlive;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sgowen
 */
public final class DownloadUtils
{
    private static final String EXTRACT_FILE_FROM_URL_REGEX = "^(https?|ftp|file)://[^/]+/(?:[^/]+/)*((?:[^/.]+\\.)+[^/.]+)$";

    public static IsAlive connectToLibzUnit(String ipAddress)
    {
        String urlString = "http://" + ipAddress + ":9000/isAlive";

        BufferedReader bufferedReader = null;

        try
        {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            StringBuilder sb = new StringBuilder();

            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null)
            {
                sb.append(inputLine);
            }

            Gson gson = new GsonBuilder().create();

            IsAlive isAlive = gson.fromJson(sb.toString(), IsAlive.class);

            return isAlive;
        }
        catch (IOException e)
        {
            Logger.getLogger(DownloadUtils.class.getName()).log(Level.SEVERE, null, e);

            return null;
        }
        finally
        {
            IOUtils.safeClose(bufferedReader);
        }
    }

    public static File downloadFileFromUrl(String urlString, DownloadListener downloadListener)
    {
        InputStream in = null;
        FileOutputStream fos = null;

        String fileName = RegexUtil.findValue(urlString, EXTRACT_FILE_FROM_URL_REGEX, 2);
        File downloadedFile = new File(fileName);

        boolean isDownloadSuccessful = false;

        try
        {
            URL link = new URL(urlString);

            in = new BufferedInputStream(link.openStream());
            fos = new FileOutputStream(fileName);

            byte[] buf = new byte[1024];
            int n;
            while ((n = in.read(buf)) != -1)
            {
                fos.write(buf, 0, n);
                downloadListener.onBytesDownloaded(n);
            }

            IOUtils.safeClose(fos);

            isDownloadSuccessful = true;

            return downloadedFile;
        }
        catch (IOException e)
        {
            Logger.getLogger(DownloadUtils.class.getName()).log(Level.SEVERE, null, e);
        }
        finally
        {
            IOUtils.safeClose(in);
            IOUtils.safeClose(fos);

            if (!isDownloadSuccessful)
            {
                downloadedFile.delete();
            }
        }

        return null;
    }

    public static long getFileSize(String urlString)
    {
        HttpURLConnection conn = null;
        try
        {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.getInputStream();
            return conn.getContentLengthLong();
        }
        catch (IOException e)
        {
            Logger.getLogger(DownloadUtils.class.getName()).log(Level.SEVERE, null, e);
            return -1;
        }
        finally
        {
            if (conn != null)
            {
                conn.disconnect();
            }
        }
    }
}