package com.sciaps.utils;

import com.sciaps.common.data.CalibrationShot;
import com.sciaps.common.data.Standard;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.spectrum.Spectrum;
import com.sciaps.common.swing.global.LibzUnitManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sgowen
 */
public final class SpectraUtil
{
    public static List<Spectrum> getSpectraForStandard(Standard standard)
    {
        final List<Spectrum> spectra = new ArrayList();

        for (Map.Entry<String, CalibrationShot> entry : LibzUnitManager.getInstance().getCalibrationShots().entrySet())
        {
            if (entry.getValue().standard.equals(standard))
            {
                Map<String, LIBZPixelSpectrum> libzPixelSpectra = LibzUnitManager.getInstance().getLIBZPixelSpectra();
                LIBZPixelSpectrum libzPixelSpectum = libzPixelSpectra.get(entry.getKey());
                Spectrum spectrum = libzPixelSpectum.createSpectrum();

                spectra.add(spectrum);
            }
        }

        return spectra;
    }
}