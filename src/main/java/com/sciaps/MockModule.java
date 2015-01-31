package com.sciaps;

import com.google.inject.AbstractModule;
import com.sciaps.common.calculation.libs.SpectrumAverager;
import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;
import com.sciaps.common.swing.libzunitapi.MockedLibzUnitApiHandler;
import com.sciaps.common.utils.LIBZPixelShotAvg;


public class MockModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(LibzUnitApiHandler.class).to(MockedLibzUnitApiHandler.class);
        bind(SpectrumAverager.class).to(LIBZPixelShotAvg.class);
    }
}
