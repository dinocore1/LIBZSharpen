package com.sciaps;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.sciaps.common.data.Standard;
import com.sciaps.common.data.utils.StandardsLibrary;
import com.sciaps.common.swing.model.IsAlive;
import com.sciaps.common.swing.model.SpectraFile;
import com.sciaps.common.webserver.FSRegionController;
import com.sciaps.common.webserver.FSStandardsController;
import org.apache.http.HttpResponse;
import org.apache.http.entity.FileEntity;
import org.devsmart.miniweb.Server;
import org.devsmart.miniweb.ServerBuilder;
import org.devsmart.miniweb.handlers.controller.Body;
import org.devsmart.miniweb.handlers.controller.Controller;
import org.devsmart.miniweb.handlers.controller.PathVariable;
import org.devsmart.miniweb.handlers.controller.RequestMapping;
import org.devsmart.miniweb.utils.RequestMethod;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MockWebserver
{
    private static Injector injector;
    private static File baseDir;
    private static StandardsLibrary standardsLibrary;

    static class ConfigModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            // Empty
        }

        @Provides
        StandardsLibrary provideStandardsLib() throws IOException
        {
            if (standardsLibrary == null)
            {
                Gson gson = new GsonBuilder().create();

                File jsonFile = new File(baseDir, "assays.json");
                JsonReader reader = new JsonReader(new FileReader(jsonFile));
                try
                {
                    Standard[] standardsArray = gson.fromJson(reader, Standard[].class);
                    standardsLibrary = new StandardsLibrary(Arrays.asList(standardsArray));
                }
                finally
                {
                    reader.close();
                }
            }

            return standardsLibrary;
        }

        @Provides
        @Named(SpectraController.SPECTRA_LIBRARY)
        Map<String, SpectraController.InternalSpectraFile> provideSpectraFile()
        {
            Map<String, SpectraController.InternalSpectraFile> retval = new HashMap<String, SpectraController.InternalSpectraFile>();
            int i = 0;
            final File spectraFolder = new File(baseDir, "spectra");

            for (File f : spectraFolder.listFiles())
            {
                if (f.getName().endsWith(".json.gz"))
                {
                    SpectraController.InternalSpectraFile spectra = new SpectraController.InternalSpectraFile();
                    spectra.id = UUID.randomUUID().toString();
                    spectra.displayName = String.format("My friendly name %d", i++);
                    spectra.file = f;
                    retval.put(spectra.id, spectra);
                }
            }

            return retval;
        }
    }

    @Controller
    public static class LIBZMockController
    {
        @RequestMapping(value = "standards", method = RequestMethod.GET)
        @Body
        public List<Standard> handleGetStandards()
        {
            StandardsLibrary library = injector.getInstance(StandardsLibrary.class);

            return library.getStandards();
        }

        @RequestMapping(value = "isAlive", method = RequestMethod.GET)
        @Body
        public IsAlive handleIsAlive()
        {
            IsAlive isAlive = new IsAlive();
            isAlive.libzUnitUniqueIdentifier = "LIBZ_UNIT_UNIQUE_ID";

            return isAlive;
        }

        @RequestMapping(value = "standards", method = RequestMethod.PUT)
        public void handleSetStandards(@Body Standard[] standards) throws IOException
        {
            standardsLibrary = new StandardsLibrary(Arrays.asList(standards));

            // save to disk
            final File standardsLibraryFile = new File(baseDir, "assays.json");
            final File tmpFile = File.createTempFile("tmp", "json", baseDir);
            JsonWriter writer = new JsonWriter(new FileWriter(tmpFile));
            Gson gson = new GsonBuilder().create();

            gson.toJson(standards, Standard[].class, writer);
            writer.close();
            tmpFile.renameTo(standardsLibraryFile);
        }
    }

    @Controller
    public static class SpectraController
    {
        private static class InternalSpectraFile
        {
            public String id;
            public String displayName;
            public File file;
        }

        public static final String SPECTRA_LIBRARY = "spectralibrary";

        @Inject
        @Named(SPECTRA_LIBRARY)
        Map<String, InternalSpectraFile> spectraLibrary;

        @RequestMapping(value = "spectra", method = RequestMethod.GET)
        @Body
        public List<SpectraFile> handleGetSpectraList()
        {
            return new ArrayList<SpectraFile>(Collections2.transform(spectraLibrary.values(), new Function<InternalSpectraFile, SpectraFile>()
            {
                @Override
                public SpectraFile apply(InternalSpectraFile input)
                {
                    SpectraFile f = new SpectraFile();
                    f.id = input.id;
                    f.displayName = input.displayName;
                    return f;
                }
            }));
        }

        @RequestMapping(value = "spectra/{id}", method = RequestMethod.GET)
        public void handleGetSpectraFile(@PathVariable("id") String spectraId, HttpResponse response)
        {
            InternalSpectraFile spectraFile = spectraLibrary.get(spectraId);

            if (spectraFile == null)
            {
                response.setStatusCode(404);
            }
            else
            {
                FileEntity fileEntity = new FileEntity(spectraFile.file, "application/x-sciaps-spectra");
                response.setEntity(fileEntity);
            }
        }
    }

    public static Server init(final String baseDirPath, int portNumber) throws IOException
    {
        baseDir = new File(baseDirPath);
        injector = Guice.createInjector(new ConfigModule());

        SpectraController spectraController = injector.getInstance(SpectraController.class);

        FSStandardsController fsStandardsController = new FSStandardsController(new File(baseDir, "standards.json"));
        FSRegionController fsRegionController = new FSRegionController(new File(baseDir, "regions.json"));

        Server server = new ServerBuilder()
                .port(portNumber)
                .mapController("/", spectraController)
                .mapController("/data", fsStandardsController, fsRegionController)
                .create();

        server.start();

        return server;
    }

    public static void main(String[] args)
    {
        try
        {
            Server mockWebServer = init(args[0], 9000);

            System.out.println("Press the enter key to shut down the server...");

            // Press enter key to shut down server
            Scanner exitInput = new Scanner(System.in);
            exitInput.nextLine();

            mockWebServer.shutdown();
        }
        catch (IOException e)
        {
            Logger.getLogger(MockWebserver.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}