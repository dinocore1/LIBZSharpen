package com.sciaps;

import com.google.inject.AbstractModule;
import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;
import com.sciaps.common.swing.libzunitapi.MockedLibzUnitApiHandler;


public class MockModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(LibzUnitApiHandler.class).to(MockedLibzUnitApiHandler.class);
    }
}
