package com.sciaps;

import com.google.inject.AbstractModule;
import com.sciaps.common.calculation.libs.SpectrumAverager;
import com.sciaps.common.utils.LIBZPixelShotAvg;


public class HttpModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SpectrumAverager.class).to(LIBZPixelShotAvg.class);
    }
}
