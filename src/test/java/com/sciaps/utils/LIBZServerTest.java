package com.sciaps.utils;


import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.sciaps.MockWebserver;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import org.devsmart.miniweb.Server;
import org.devsmart.miniweb.ServerBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LIBZServerTest {

    private Injector injector;
    private Server mServer;

    private static class TestModule extends AbstractModule {

        private final File baseDir;

        public TestModule() {
            baseDir = new File("mockdata");
        }

        @Override
        protected void configure() {

        }

        @Provides
        @Named(MockWebserver.SpectraController.SPECTRA_LIBRARY)
        Map<String, MockWebserver.SpectraController.InternalSpectraFile> provideSpectraFile(){
            Map<String, MockWebserver.SpectraController.InternalSpectraFile> retval = new HashMap<String, MockWebserver.SpectraController.InternalSpectraFile>();
            int i = 0;
            final File spectraFolder = new File(baseDir, "spectra");

            for(File f : spectraFolder.listFiles()){
                if(f.getName().endsWith(".json.gz")) {
                    MockWebserver.SpectraController.InternalSpectraFile spectra = new MockWebserver.SpectraController.InternalSpectraFile();
                    spectra.id = String.format("myid%d", i);
                    spectra.displayName = String.format("My friendly name %d", i++);
                    spectra.file = f;
                    retval.put(spectra.id, spectra);
                }
            }

            return retval;
        }


    }


    @Before
    public void setupMockServer() throws IOException {

        injector = Guice.createInjector(new TestModule());

        MockWebserver.SpectraController spectraController = injector.getInstance(MockWebserver.SpectraController.class);


        mServer = new ServerBuilder()
                .port(9100)
                .mapController("/", spectraController)
                .create();

        mServer.start();
    }

    @After
    public void stopServer() {
        mServer.shutdown();
    }

    @Test
    public void testDownloadLIBZSpectra() throws IOException {

        LIBZServer server = new LIBZServer("http://localhost:9100");
        LIBZPixelSpectrum file = server.downloadSpectraFile("myid2");
        assertNotNull(file);

    }
}
