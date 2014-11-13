package com.sciaps.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sciaps.common.data.Standard;
import com.sciaps.global.LibzSharpenManager;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        final String urlBaseString = getLibzUnitApiBaseUrl(ipAddress);
        final String urlString = urlBaseString + "isAlive";

        BufferedReader bufferedReader = null;

        try
        {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(10000);

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

    public static boolean pullFromLibzUnit(LibzSharpenManager libzSharpenManager)
    {
        final String urlBaseString = getLibzUnitApiBaseUrl(libzSharpenManager.getIpAddress());

        final String getStandardsUrlString = urlBaseString + "standards";

        BufferedReader bufferedReader = null;

        try
        {
            URL url = new URL(getStandardsUrlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(20000);

            bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            StringBuilder sb = new StringBuilder();

            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null)
            {
                sb.append(inputLine);
            }

            Gson gson = new GsonBuilder().create();

            Standard[] standardsArray = gson.fromJson(sb.toString(), Standard[].class);
            if (standardsArray == null)
            {
                return false;
            }

            System.out.println("# of Standards pulled from LIBZ Unit: " + standardsArray.length);

            List<Standard> standards = new ArrayList<Standard>();
            standards.addAll(Arrays.asList(standardsArray));

            libzSharpenManager.setStandards(standards);
        }
        catch (IOException e)
        {
            Logger.getLogger(DownloadUtils.class.getName()).log(Level.SEVERE, null, e);

            return false;
        }
        finally
        {
            IOUtils.safeClose(bufferedReader);
        }

        return true;
    }

    public static void pushToLibzUnit(LibzSharpenManager libzSharpenManager)
    {
        final String urlBaseString = getLibzUnitApiBaseUrl(libzSharpenManager.getIpAddress());
        // TODO, add the method to the urlBaseString
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

    private static String getLibzUnitApiBaseUrl(String ipAddress)
    {
        final String urlBaseString = "http://" + ipAddress + ":9000/";

        return urlBaseString;
    }
}