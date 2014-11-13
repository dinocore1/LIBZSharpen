package com.sciaps.utils;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class LIBZServer {

    CloseableHttpClient httpclient;

    final String urlBase;

    public LIBZServer(String urlBase) {
        httpclient = HttpClients.createDefault();
        this.urlBase = urlBase;
    }


    public LIBZPixelSpectrum downloadSpectraFile(String spectraId) throws IOException {

        HttpGet httpget = new HttpGet(urlBase + "/spectra/" + spectraId);

        CloseableHttpResponse response = httpclient.execute(httpget);
        InputStream in = response.getEntity().getContent();

        in = new GZIPInputStream(in);
        JsonReader reader = new JsonReader(new InputStreamReader(in));
        try {
            Gson gson = new GsonBuilder().create();
            final LIBZPixelSpectrum.SerializationObj obj = gson.fromJson(reader, LIBZPixelSpectrum.SerializationObj.class);
            return new LIBZPixelSpectrum(obj);
        } finally {
            reader.close();
        }
    }
}
