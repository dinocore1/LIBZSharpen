package com.sciaps;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.sciaps.common.objtracker.DBObjTracker;
import com.sciaps.common.swing.global.LibzUnitManager;

public class BaseModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DBObjTracker.class).in(Singleton.class);
        bind(LibzUnitManager.class).in(Singleton.class);
    }
}
