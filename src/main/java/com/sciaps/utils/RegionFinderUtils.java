package com.sciaps.utils;

import com.sciaps.common.algorithms.OneIntensityValue;
import com.sciaps.common.algorithms.SimpleBaseLine;
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
        return findRegionWithNameValue(OneIntensityValue.class.getName());
    }

    public static Region findBaselineRegion()
    {
        return findRegionWithNameValue(SimpleBaseLine.class.getName());
    }

    public static boolean isPropsOnlyRegion(Region region)
    {
        return doesRegionContainNameValue(region, OneIntensityValue.class.getName()) || doesRegionContainNameValue(region, SimpleBaseLine.class.getName());
    }

    private static Region findRegionWithNameValue(String nameValue)
    {
        for (Map.Entry<String, Region> entry : LibzUnitManager.getInstance().getRegionsManager().getObjects().entrySet())
        {
            if (doesRegionContainNameValue(entry.getValue(), nameValue))
            {
                return entry.getValue();
            }
        }

        return null;
    }

    private static boolean doesRegionContainNameValue(Region region, String nameValue)
    {
        if (region.params != null && region.params.size() > 0)
        {
            if (region.params.containsKey("name"))
            {
                Object name = region.params.get("name");
                if (name.equals(nameValue))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private RegionFinderUtils()
    {
        // Hide Constructor for Static Utility Class
    }
}