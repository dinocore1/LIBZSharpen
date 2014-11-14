package com.sciaps.global;

import com.sciaps.common.data.Standard;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.model.SpectraFile;
import java.util.List;

/**
 *
 * @author sgowen
 */
public final class LibzSharpenManager
{
    private static final Object LOCK = new Object();

    private static LibzSharpenManager instance;

    private String _ipAddress;
    private String _libzUnitUniqueIdentifier;
    private List<Standard> _standards;
    private List<SpectraFile> _spectraFiles;
    private List<LIBZPixelSpectrum> _libzPixelSpectra;

    public static LibzSharpenManager getInstance()
    {
        synchronized (LOCK)
        {
            if (instance == null)
            {
                instance = new LibzSharpenManager();
            }

            return instance;
        }
    }

    public boolean isValidAfterPull()
    {
        return _standards != null && _spectraFiles != null && _libzPixelSpectra != null;
    }

    public String getIpAddress()
    {
        return _ipAddress;
    }

    public void setIpAddress(String ipAddress)
    {
        _ipAddress = ipAddress;
    }

    public String getLibzUnitUniqueIdentifier()
    {
        return _libzUnitUniqueIdentifier;
    }

    public void setLibzUnitUniqueIdentifier(String libzUnitUniqueIdentifier)
    {
        _libzUnitUniqueIdentifier = libzUnitUniqueIdentifier;
    }

    public List<Standard> getStandards()
    {
        return _standards;
    }

    public void setStandards(List<Standard> standards)
    {
        _standards = standards;
    }

    public List<SpectraFile> getSpectraFiles()
    {
        return _spectraFiles;
    }

    public void setSpectraFiles(List<SpectraFile> spectraFiles)
    {
        _spectraFiles = spectraFiles;
    }

    public List<LIBZPixelSpectrum> getLIBZPixelSpectra()
    {
        return _libzPixelSpectra;
    }

    public void setLIBZPixelSpectra(List<LIBZPixelSpectrum> libzPixelSpectra)
    {
        _libzPixelSpectra = libzPixelSpectra;
    }

    private LibzSharpenManager()
    {
        // Hide Constructor
    }
}