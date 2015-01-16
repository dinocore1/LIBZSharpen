package com.sciaps.utils;

import com.sciaps.common.data.Region;
import com.sciaps.common.swing.global.LibzUnitManager;
import java.util.Map;

/**
 *
 * @author sgowen
 */
public final class RegionFinderUtils
{
    public static Region findOneIntensityValueRegion()
    {
        for (Map.Entry<String, Region> entry : LibzUnitManager.getInstance().getRegionsManager().getObjects().entrySet())
        {
            if (entry.getValue().params != null && entry.getValue().params.size() > 0)
            {
                if (entry.getValue().params.containsKey("name"))
                {
                    Object name = entry.getValue().params.get("name");
                    if (name.equals("com.sciaps.common.algorithms.OneIntensityValue"))
                    {
                        return entry.getValue();
                    }
                }
            }
        }

        return null;
    }

    private RegionFinderUtils()
    {
        // Hide Constructor for Static Utility Class
    }
}