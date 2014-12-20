package com.sciaps.utils;

import com.sciaps.common.data.IRRatio;
import com.sciaps.common.swing.global.LibzUnitManager;
import java.util.Map;
import java.util.Vector;

/**
 *
 * @author sgowen
 */
public final class IntensityRatioFormulaTableUtils
{
    public static void fillIntensityRatioFormulasData(Vector data)
    {
        if (LibzUnitManager.getInstance().getIntensityRatios() != null)
        {
            data.clear();

            for (Map.Entry entry : LibzUnitManager.getInstance().getIntensityRatios().entrySet())
            {
                Vector row = new Vector();

                row.add(entry.getKey());

                IRRatio intensityRatio = (IRRatio) entry.getValue();
                row.add(intensityRatio.name);
                row.add(intensityRatio.element.symbol);

                data.add(row);
            }
        }
    }
    
    public static void fillIntensityRatioFormulasColumnNames(Vector columnNames)
    {
        columnNames.clear();
        
        columnNames.add("ID");
        columnNames.add("Name");
        columnNames.add("Element");
    }

    private IntensityRatioFormulaTableUtils()
    {
        // Hide Constructor for Static Utility Class
    }
}