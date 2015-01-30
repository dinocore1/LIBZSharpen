package com.sciaps;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.sciaps.common.objtracker.DBObjTracker;
import com.sciaps.common.swing.global.LibzUnitManager;

public class BaseModule extends AbstractModule {

    private String mIpAddress;

    @Override
    protected void configure() {
        bind(DBObjTracker.class).in(Singleton.class);
        bind(LibzUnitManager.class).in(Singleton.class);
    }


    public void setIpaddress(String ipaddress) {
        mIpAddress = ipaddress;
    }

    public static final String IPADDRESS = "ipaddress";

    @Provides @Named(IPADDRESS)
    public String provideIPAddress() {
        return mIpAddress;
    }
}
