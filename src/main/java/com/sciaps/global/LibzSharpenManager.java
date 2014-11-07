package com.sciaps.global;

import com.sciaps.common.data.Standard;
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

    private LibzSharpenManager()
    {
        // Hide Constructor
    }
}