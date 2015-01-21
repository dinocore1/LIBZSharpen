package com.sciaps.utils;

import com.sciaps.common.AtomicElement;
import com.sciaps.common.swing.global.LibzUnitManager;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableColumn;

/**
 *
 * @author sgowen
 */
public final class TableUtils
{
    public static void initElementComboBoxForColumn(TableColumn elementColumn)
    {
        JComboBox comboBox = new JComboBox();
        for (int i = 1; i <= LibzUnitManager.NUM_ATOMIC_ELEMENTS; i++)
        {
            AtomicElement ae = AtomicElement.getElementByAtomicNum(i);
            comboBox.addItem(ae.symbol);
        }

        elementColumn.setCellEditor(new DefaultCellEditor(comboBox));
    }
}