package com.sciaps.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.sciaps.common.data.Standard;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.global.LibzSharpenManager;
import com.sciaps.model.IsAlive;
import com.sciaps.model.SpectraFile;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author sgowen
 */
public final class LibzUnitApiUtils
{
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
            Logger.getLogger(LibzUnitApiUtils.class.getName()).log(Level.SEVERE, null, e);

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

        List<Standard> standards = getStandards(urlBaseString + "standards");
        libzSharpenManager.setStandards(standards);

        List<SpectraFile> spectraFiles = getSpectraFiles(urlBaseString + "spectra");
        libzSharpenManager.setSpectraFiles(spectraFiles);

        if (spectraFiles != null)
        {
            List<LIBZPixelSpectrum> libzPixelSpectra = new ArrayList<LIBZPixelSpectrum>();

            for (SpectraFile sf : spectraFiles)
            {
                LIBZPixelSpectrum libzPixelSpectum = getLIBZPixelSpectrum(urlBaseString + "spectra", sf.id);
                if (libzPixelSpectum == null)
                {
                    Logger.getLogger(LibzUnitApiUtils.class.getName()).log(Level.WARNING, "LIBZPixelSpectrum retrieved via id: {0} was NULL! Continuing to download the other LIBZPixelSpectrum objects...", sf.id);
                }

                libzPixelSpectra.add(libzPixelSpectum);
            }

            libzSharpenManager.setLIBZPixelSpectra(libzPixelSpectra);
        }

        return libzSharpenManager.isValidAfterPull();
    }

    public static void pushToLibzUnit(LibzSharpenManager libzSharpenManager)
    {
        // TODO
    }

    private static List<Standard> getStandards(final String getStandardsUrlString)
    {
        String jsonResponse = downloadJson(getStandardsUrlString);
        List<Standard> standards = JsonUtils.deserializeJsonIntoListOfType(jsonResponse, Standard[].class);

        System.out.println("# of Standards pulled from LIBZ Unit: " + standards.size());

        return standards;
    }

    private static List<SpectraFile> getSpectraFiles(final String getSpectraFilesUrlString)
    {
        String jsonResponse = downloadJson(getSpectraFilesUrlString);
        List<SpectraFile> spectraFiles = JsonUtils.deserializeJsonIntoListOfType(jsonResponse, SpectraFile[].class);

        System.out.println("# of Spectra Files pulled from LIBZ Unit: " + spectraFiles.size());

        return spectraFiles;
    }

    private static LIBZPixelSpectrum getLIBZPixelSpectrum(final String getLIBZPixelSpectrumUrlString, final String spectraId)
    {
        JsonReader jsonReader = null;

        try
        {
            URL url = new URL(getLIBZPixelSpectrumUrlString + "/" + spectraId);

            BufferedInputStream bis = new BufferedInputStream(url.openStream());
            GZIPInputStream gzis = new GZIPInputStream(bis);
            jsonReader = new JsonReader(new InputStreamReader(gzis));

            Gson gson = new GsonBuilder().create();
            final LIBZPixelSpectrum.SerializationObj obj = gson.fromJson(jsonReader, LIBZPixelSpectrum.SerializationObj.class);
            if (obj == null)
            {
                return null;
            }

            LIBZPixelSpectrum libzPixelSpectrum = new LIBZPixelSpectrum(obj);

            return libzPixelSpectrum;
        }
        catch (IOException e)
        {
            Logger.getLogger(LibzUnitApiUtils.class.getName()).log(Level.SEVERE, null, e);

            return null;
        }
        finally
        {
            IOUtils.safeClose(jsonReader);
        }
    }

    private static String downloadJson(final String getJsonUrlString)
    {
        BufferedReader bufferedReader = null;

        try
        {
            URL url = new URL(getJsonUrlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(20000);

            bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            StringBuilder sb = new StringBuilder();

            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null)
            {
                sb.append(inputLine);
            }

            return sb.toString();
        }
        catch (IOException e)
        {
            Logger.getLogger(LibzUnitApiUtils.class.getName()).log(Level.SEVERE, null, e);

            return null;
        }
        finally
        {
            IOUtils.safeClose(bufferedReader);
        }
    }

    private static String getLibzUnitApiBaseUrl(String ipAddress)
    {
        final String urlBaseString = "http://" + ipAddress + "/";

        return urlBaseString;
    }
}