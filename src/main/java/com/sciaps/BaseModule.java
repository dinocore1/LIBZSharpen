package com.sciaps;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.sciaps.common.objtracker.DBObjTracker;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.libzunitapi.HttpLibzUnitApiHandler;

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



    @Provides @Named(HttpLibzUnitApiHandler.IPADDRESS)
    public String provideIPAddress() {
        return mIpAddress;
    }

    @Provides @Singleton
    public EventBus provideEventBus() {
        return new EventBus();
    }
}
