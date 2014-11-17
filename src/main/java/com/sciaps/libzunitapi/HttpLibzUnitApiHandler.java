package com.sciaps.libzunitapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.sciaps.common.data.Standard;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.global.LibzUnitManager;
import com.sciaps.model.IsAlive;
import com.sciaps.model.SpectraFile;
import com.sciaps.utils.IOUtils;
import com.sciaps.utils.JsonUtils;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
public final class HttpLibzUnitApiHandler implements LibzUnitApiHandler
{
    @Override
    public boolean connectToLibzUnit(LibzUnitManager libzUnitManager)
    {
        final String urlBaseString = getLibzUnitApiBaseUrl(libzUnitManager.getIpAddress());
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

            String jsonResponse = sb.toString();
            if (jsonResponse != null)
            {
                Gson gson = new GsonBuilder().create();

                IsAlive isAlive = gson.fromJson(sb.toString(), IsAlive.class);
                libzUnitManager.setLibzUnitUniqueIdentifier(isAlive.libzUnitUniqueIdentifier);

                return true;
            }
        }
        catch (IOException e)
        {
            Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        finally
        {
            IOUtils.safeClose(bufferedReader);
        }

        return false;
    }

    @Override
    public boolean pullFromLibzUnit(LibzUnitManager libzUnitManager)
    {
        final String urlBaseString = getLibzUnitApiBaseUrl(libzUnitManager.getIpAddress());

        List<Standard> standards = getStandards(urlBaseString + "standards");
        libzUnitManager.setStandards(standards);

        List<SpectraFile> spectraFiles = getSpectraFiles(urlBaseString + "spectra");
        libzUnitManager.setSpectraFiles(spectraFiles);

        if (spectraFiles != null)
        {
            List<LIBZPixelSpectrum> libzPixelSpectra = new ArrayList<LIBZPixelSpectrum>();

            for (SpectraFile sf : spectraFiles)
            {
                LIBZPixelSpectrum libzPixelSpectum = getLIBZPixelSpectrum(urlBaseString + "spectra", sf.id);
                if (libzPixelSpectum == null)
                {
                    Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.WARNING, "LIBZPixelSpectrum retrieved via id: {0} was NULL! Continuing to download the other LIBZPixelSpectrum objects...", sf.id);
                }

                libzPixelSpectra.add(libzPixelSpectum);
            }

            libzUnitManager.setLIBZPixelSpectra(libzPixelSpectra);
        }

        return libzUnitManager.isValidAfterPull();
    }

    @Override
    public boolean pushToLibzUnit(LibzUnitManager libzUnitManager)
    {
        final String urlBaseString = getLibzUnitApiBaseUrl(libzUnitManager.getIpAddress());
        if (!putStandards(urlBaseString + "standards", libzUnitManager.getStandards()))
        {
            return false;
        }

        return true;
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
            Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.SEVERE, null, e);

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
            Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.SEVERE, null, e);

            return null;
        }
        finally
        {
            IOUtils.safeClose(bufferedReader);
        }
    }

    private static boolean putStandards(final String putStandardsUrlString, List<Standard> standards)
    {
        try
        {
            URL url = new URL(putStandardsUrlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("PUT");
            con.setRequestProperty("Content-Type", "application/json");
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());

            Standard[] standardsArray = standards.toArray(new Standard[standards.size()]);
            Gson gson = new GsonBuilder().create();
            gson.toJson(standardsArray, Standard[].class, out);

            IOUtils.safeClose(out);

            con.connect();

            return con.getResponseCode() == 200;
        }
        catch (IOException ex)
        {
            Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    private static String getLibzUnitApiBaseUrl(String ipAddress)
    {
        final String urlBaseString = "http://" + ipAddress + "/";

        return urlBaseString;
    }
}