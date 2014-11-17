package com.sciaps.utils;

import com.sciaps.MockWebserver;
import com.sciaps.global.LibzSharpenManager;
import com.sciaps.model.IsAlive;
import org.devsmart.miniweb.Server;
import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author sgowen
 */
public final class LibzUnitApiUtilsTest
{
    private static final String IP_ADDRESS = "localhost:9100";
    private static Server _server;

    @BeforeClass
    public static void initMockWebserver() throws Exception
    {
        _server = MockWebserver.init("mockdata", 9100);

        LibzSharpenManager.getInstance().setIpAddress(IP_ADDRESS);

        Thread.sleep(3000); // Give the server time to fully initialize
    }

    @AfterClass
    public static void stopMockWebserver()
    {
        _server.shutdown();
    }

    @Test
    public void testConnectToLibzUnit()
    {
        IsAlive isAlive = LibzUnitApiUtils.connectToLibzUnit(IP_ADDRESS);

        assertNotNull("IsAlive response should NOT be null!", isAlive);
        assertEquals("LIBZ_UNIT_UNIQUE_ID", isAlive.libzUnitUniqueIdentifier);

        LibzSharpenManager.getInstance().setLibzUnitUniqueIdentifier(isAlive.libzUnitUniqueIdentifier);
    }

    @Test
    public void testPullFromLibzUnit()
    {
        boolean isPullSuccessful = LibzUnitApiUtils.pullFromLibzUnit(LibzSharpenManager.getInstance());

        assert (isPullSuccessful);
    }

    @Test
    public void testPushToLibzUnit()
    {
        boolean isPushSuccessful = LibzUnitApiUtils.pushToLibzUnit(LibzSharpenManager.getInstance());

        assert (isPushSuccessful);
    }
}