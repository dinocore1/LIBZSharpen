package com.sciaps.utils;

import com.sciaps.MockWebserver;
import com.sciaps.global.InstanceManager;
import com.sciaps.global.LibzUnitManager;
import com.sciaps.libzunitapi.HttpLibzUnitApiHandler;
import com.sciaps.libzunitapi.LibzUnitApiHandler;
import org.devsmart.miniweb.Server;
import org.junit.Test;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author sgowen
 */
public final class LibzUnitApiHandlerTest
{
    private static Server s_server;
    private LibzUnitApiHandler _libzUnitApiHandler;

    @BeforeClass
    public static void initMockWebserver() throws Exception
    {
        s_server = MockWebserver.init("mockdata", 9100);

        InstanceManager.getInstance().storeInstance(LibzUnitApiHandler.class, new HttpLibzUnitApiHandler());
        LibzUnitManager.getInstance().setIpAddress("localhost:9100");

        Thread.sleep(3000); // Give the server time to fully initialize
    }

    @AfterClass
    public static void stopMockWebserver()
    {
        s_server.shutdown();
    }

    @Before
    public void setup()
    {
        _libzUnitApiHandler = InstanceManager.getInstance().retrieveInstance(LibzUnitApiHandler.class);
    }

    @Test
    public void testConnectToLibzUnit()
    {
        boolean isConnectSuccessful = _libzUnitApiHandler.connectToLibzUnit(LibzUnitManager.getInstance());

        assert (isConnectSuccessful);
    }

    @Test
    public void testPullFromLibzUnit()
    {
        boolean isPullSuccessful = _libzUnitApiHandler.pullFromLibzUnit(LibzUnitManager.getInstance());

        assert (isPullSuccessful);
    }

    @Test
    public void testPushToLibzUnit()
    {
        boolean isPushSuccessful = _libzUnitApiHandler.pushToLibzUnit(LibzUnitManager.getInstance());

        assert (isPushSuccessful);
    }
}