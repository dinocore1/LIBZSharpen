package com.sciaps.utils;

import com.sciaps.MockWebserver;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.libzunitapi.HttpLibzUnitApiHandler;
import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;
import com.sciaps.global.InstanceManager;
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

        // Give the server time to fully initialize
        Thread.sleep(3000);
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
        assert (_libzUnitApiHandler.connectToLibzUnit());
    }

    @Test
    public void testPullFromLibzUnit()
    {
        assert (_libzUnitApiHandler.pullFromLibzUnit());
    }

    @Test
    public void testPushToLibzUnit()
    {
        assert (_libzUnitApiHandler.pushToLibzUnit());
    }
}