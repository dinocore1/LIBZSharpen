package com.sciaps.utils;

import com.sciaps.common.data.Standard;
import com.sciaps.common.swing.global.LibzUnitManager;
import java.util.Map;
import java.util.Vector;

/**
 *
 * @author sgowen
 */
public final class ShortStandardsTableUtils
{
    public static void fillStandardsData(Vector data)
    {
        if (LibzUnitManager.getInstance().getStandards() != null)
        {
            data.clear();

            for (Map.Entry<String, Standard> entry : LibzUnitManager.getInstance().getStandards().entrySet())
            {
                Vector row = new Vector();

                row.add(entry.getKey());
                row.add(entry.getValue().name);

                data.add(row);
            }
        }
    }

    public static void fillStandardsColumnNames(Vector columnNames)
    {
        columnNames.clear();

        columnNames.add("ID");
        columnNames.add("Standard");
    }

    private ShortStandardsTableUtils()
    {
        // Hide Constructor for Static Utility Class
    }
}