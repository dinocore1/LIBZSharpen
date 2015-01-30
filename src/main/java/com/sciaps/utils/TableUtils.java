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
        AtomicElement[] allElement = AtomicElement.values();
        for (int i = 0; i <= allElement.length; i++) {
            comboBox.addItem(allElement[i].symbol);
        }

        elementColumn.setCellEditor(new DefaultCellEditor(comboBox));
    }
}