package com.sciaps;


import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.sciaps.common.data.Standard;
import com.sciaps.common.data.utils.StandardsLibrary;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.devsmart.miniweb.Server;
import org.devsmart.miniweb.ServerBuilder;
import org.devsmart.miniweb.handlers.controller.Controller;
import org.devsmart.miniweb.handlers.controller.RequestMapping;
import org.devsmart.miniweb.utils.RequestMethod;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MockWebserver {

    private static Injector injector;

    static class ConfigModule extends AbstractModule {

        private final File baseDir;

        public ConfigModule(File libzAnalysisDir) {
            baseDir = libzAnalysisDir;
        }

        @Override
        protected void configure() {

        }

        private StandardsLibrary standardsLibrary;
        @Provides
        StandardsLibrary provideStandardsLib() throws IOException {


            if(standardsLibrary == null) {
                Gson gson = new GsonBuilder()
                        .create();

                File standardsLibraryFile = new File(baseDir, "assays.json");
                JsonReader reader = new JsonReader(new FileReader(standardsLibraryFile));
                Standard[] standardsArray = gson.fromJson(reader, Standard[].class);
                standardsLibrary = new StandardsLibrary(Arrays.asList(standardsArray));
            }

            return standardsLibrary;
        }
    }

    @Controller
    public static class LIBZMockController {

        @RequestMapping(value = "standards", method = RequestMethod.GET)
        public List<Standard> handleGetStandards() {

            StandardsLibrary library = injector.getInstance(StandardsLibrary.class);

            return library.getStandards();

        }
    }

    public static void main(String[] args) {
        try {

            File libsAnalysisDir = new File(args[0]);

            injector = Guice.createInjector(new ConfigModule(libsAnalysisDir));

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
