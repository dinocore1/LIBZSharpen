package com.sciaps.utils;

import com.sciaps.common.data.CalibrationShot;
import com.sciaps.common.data.Standard;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.spectrum.Spectrum;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.utils.LIBZPixelShot;

import java.util.*;

/**
 *
 * @author sgowen
 */
public final class SpectraUtils
{
    public static List<Spectrum> getSpectraForStandard(Standard standard)
    {
        final List<Spectrum> spectra = new ArrayList();

        //TODO: putback
        /*
        for (Map.Entry<String, CalibrationShot> entry : LibzUnitManager.getInstance().getCalibrationShots().entrySet())
        {
            if (entry.getValue().standard == standard)
            {
                Map<String, LIBZPixelSpectrum> libzPixelSpectra = LibzUnitManager.getInstance().getLIBZPixelSpectra();
                LIBZPixelSpectrum libzPixelSpectum = libzPixelSpectra.get(entry.getKey());
                if (libzPixelSpectum != null)
                {
                    Spectrum spectrum = libzPixelSpectum.createSpectrum();

                    spectra.add(spectrum);
                }
            }
        }
        */

        return spectra;
    }

    public static Collection<LIBZPixelShot> getShotsForStandard(Standard standard) {

        final List<LIBZPixelShot> shots = new ArrayList<LIBZPixelShot>();
        //TODO: putback
        /*
        for (Map.Entry<String, CalibrationShot> entry : LibzUnitManager.getInstance().getCalibrationShots().entrySet()) {
            if (entry.getValue().standard == standard) {
                Map<String, LIBZPixelSpectrum> libzPixelSpectra = LibzUnitManager.getInstance().getLIBZPixelSpectra();
                LIBZPixelSpectrum libzPixelSpectum = libzPixelSpectra.get(entry.getKey());
                shots.add(new LIBZPixelShot(libzPixelSpectum));
            }
        }
        */
        return shots;
    }

    public static List<String> getCalibrationShotIdsForMissingStandardsShotData(List<Standard> standards)
    {
        final List<String> calibrationShotIds = new ArrayList();

        //TODO: putback
        /*
        for (Map.Entry<String, CalibrationShot> entry : LibzUnitManager.getInstance().getCalibrationShots().entrySet())
        {
            for (Standard s : standards)
            {
                if (entry.getValue().standard == s)
                {
                    calibrationShotIds.add(entry.getKey());
                }
            }
        }
        */

        return calibrationShotIds;
    }

    public static boolean hasCalibrationShotData(Standard standard) {

        //TODO putback
        /*
        for (Map.Entry<String, CalibrationShot> entry : LibzUnitManager.getInstance().getCalibrationShots().entrySet()) {
            if(entry.getValue().standard == standard) {
                return true;
            }
        }
        */
        return false;
    }

    private SpectraUtils()
    {
        // Hide Constructor for Static Utility Class
    }
}