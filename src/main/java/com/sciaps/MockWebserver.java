package com.sciaps;


import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.sciaps.common.ThreadUtils;
import com.sciaps.common.data.Standard;
import com.sciaps.common.data.utils.StandardsLibrary;
import org.devsmart.miniweb.Server;
import org.devsmart.miniweb.ServerBuilder;
import org.devsmart.miniweb.handlers.controller.Body;
import org.devsmart.miniweb.handlers.controller.Controller;
import org.devsmart.miniweb.handlers.controller.RequestMapping;
import org.devsmart.miniweb.utils.RequestMethod;
import sun.misc.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MockWebserver {

    private static Injector injector;
    private static File baseDir;
    private static StandardsLibrary standardsLibrary;

    static class ConfigModule extends AbstractModule {


        @Override
        protected void configure() {

        }

        @Provides
        StandardsLibrary provideStandardsLib() throws IOException {
            if(standardsLibrary == null) {
                Gson gson = new GsonBuilder()
                        .create();

                File standardsLibraryFile = new File(baseDir, "assays.json");
                JsonReader reader = new JsonReader(new FileReader(standardsLibraryFile));
                try {
                    Standard[] standardsArray = gson.fromJson(reader, Standard[].class);
                    standardsLibrary = new StandardsLibrary(Arrays.asList(standardsArray));
                } finally {
                    reader.close();
                }
            }

            return standardsLibrary;
        }
    }

    @Controller
    public static class LIBZMockController {

        @RequestMapping(value = "standards", method = RequestMethod.GET)
        @Body
        public List<Standard> handleGetStandards() {

            StandardsLibrary library = injector.getInstance(StandardsLibrary.class);

            return library.getStandards();

        }

        @RequestMapping(value = "standards", method = RequestMethod.PUT)
        public void handleSetStandards(@Body Standard[] standards) throws IOException {

            standardsLibrary = new StandardsLibrary(Arrays.asList(standards));

            //save to disk
            final File standardsLibraryFile = new File(baseDir, "assays.json");
            final File tmpFile = File.createTempFile("tmp", "json", baseDir);
            JsonWriter writer = new JsonWriter(new FileWriter(tmpFile));
            Gson gson = new GsonBuilder()
                    .create();

            gson.toJson(standardsLibrary.getStandards(), Standard[].class, writer);
            writer.close();
            tmpFile.renameTo(standardsLibraryFile);


        }
    }

    public static void main(String[] args) {
        try {

            baseDir = new File(args[0]);
            injector = Guice.createInjector(new ConfigModule());

            Server server = new ServerBuilder()
                    .port(9000)
                    .mapController("/", new LIBZMockController())
                    .create();

            server.start();

        } catch (Throwable t) {
            Throwables.propagate(t);
        }
    }
}
