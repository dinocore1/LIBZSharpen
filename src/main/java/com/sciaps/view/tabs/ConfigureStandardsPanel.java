package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import com.sciaps.common.data.ChemValue;
import com.sciaps.common.data.Standard;
import com.sciaps.global.LibzSharpenManager;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 *
 * @author sgowen
 */
public final class ConfigureStandardsPanel extends AbstractTabPanel
{
    private static final String TAB_NAME = "Configure Standards";

    private JTable _standardsTable;

    public ConfigureStandardsPanel(MainFrame mainFrame)
    {
        super(mainFrame);

        Vector<ChemValue> uniqueChemValues = getUniqueChemValues();
        Vector<String> columnNames = new Vector<String>();
        columnNames.add("Standard");
        for (int i = 0; i < uniqueChemValues.size(); i++)
        {
            columnNames.add(uniqueChemValues.get(i).element.symbol);
        }

        Vector data = generateStandardsDataForTable(uniqueChemValues);

        _standardsTable = new JTable(data, columnNames);
        _standardsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _standardsTable.setPreferredScrollableViewportSize(new Dimension((int) ((float) mainFrame.getWidth() * 0.96f), (int) ((float) mainFrame.getHeight() * 0.84f)));
        _standardsTable.setFillsViewportHeight(true);

        _standardsTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                printDebugData(_standardsTable);
            }
        });

        refreshTable();

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(_standardsTable);

        //Add the scroll pane to this panel.
        add(scrollPane);
    }

    @Override
    public String getTabName()
    {
        return TAB_NAME;
    }

    @Override
    public void customizeMenuBar(JMenuBar menuBar)
    {
        JMenu tableMenu = new JMenu("Table");
        tableMenu.setMnemonic(KeyEvent.VK_T);
        JMenuItem addStandardMenuItem = new JMenuItem("Add Standard", KeyEvent.VK_S);
        addStandardMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        addStandardMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                DefaultTableModel model = (DefaultTableModel) _standardsTable.getModel();
                Object[] newStandard = new Object[model.getColumnCount()];
                newStandard[0] = "New_Standard";
                for (int i = 1; i < newStandard.length; i++)
                {
                    newStandard[i] = "0.0";
                }

                model.addRow(newStandard);

                refreshTable();
            }
        });
        JMenuItem addElementMenuItem = new JMenuItem("Add Element", KeyEvent.VK_E);
        addElementMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK));
        addElementMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                DefaultTableModel model = (DefaultTableModel) _standardsTable.getModel();
                model.addColumn("XX");
                for (int i = 0; i < model.getRowCount(); i++)
                {
                    model.setValueAt("0.0", i, model.getColumnCount() - 1);
                }
            }
        });

        tableMenu.add(addStandardMenuItem);
        tableMenu.add(addElementMenuItem);

        menuBar.add(tableMenu);
    }

    private void printDebugData(JTable table)
    {
        int numRows = table.getRowCount();
        int numCols = table.getColumnCount();
        javax.swing.table.TableModel model = table.getModel();

        System.out.println("Value of data: ");
        for (int i = 0; i < numRows; i++)
        {
            System.out.print("    row " + i + ":");
            for (int j = 0; j < numCols; j++)
            {
                System.out.print("  " + model.getValueAt(i, j));
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }

    private void refreshTable()
    {
        for (int column = 0; column < _standardsTable.getColumnCount(); column++)
        {
            TableColumn tableColumn = _standardsTable.getColumnModel().getColumn(column);
            int preferredWidth = tableColumn.getMinWidth();
            int maxWidth = tableColumn.getMaxWidth();

            for (int row = 0; row < _standardsTable.getRowCount(); row++)
            {
                TableCellRenderer cellRenderer = _standardsTable.getCellRenderer(row, column);
                Component c = _standardsTable.prepareRenderer(cellRenderer, row, column);
                int width = c.getPreferredSize().width + _standardsTable.getIntercellSpacing().width;
                preferredWidth = Math.max(preferredWidth, width);

                //  We've exceeded the maximum width, no need to check other rows
                if (preferredWidth >= maxWidth)
                {
                    preferredWidth = maxWidth;
                    break;
                }
            }

            tableColumn.setPreferredWidth(preferredWidth);
        }
    }

    private Vector<ChemValue> getUniqueChemValues()
    {
        Vector<ChemValue> uniqueChemValues = new Vector<ChemValue>();

        final LibzSharpenManager libzSharpenManager = LibzSharpenManager.getInstance();

        for (Standard standard : libzSharpenManager.getStandards())
        {
            for (ChemValue chemValue : standard.spec)
            {
                if (!uniqueChemValues.contains(chemValue))
                {
                    uniqueChemValues.add(chemValue);
                }
            }
        }

        return uniqueChemValues;
    }

    private Vector generateStandardsDataForTable(Vector<ChemValue> chemValues)
    {
        final LibzSharpenManager libzSharpenManager = LibzSharpenManager.getInstance();
        Vector data = new Vector();//[libzSharpenManager.getStandards().size()][chemValues.length + 1];

        for (int i = 0; i < libzSharpenManager.getStandards().size(); i++)
        {
            Standard standard = libzSharpenManager.getStandards().get(i);
            Vector row = new Vector();
            row.add(standard.name);

            for (int j = 0; j < chemValues.size(); j++)
            {
                ChemValue chemValue = chemValues.get(j);

                if (standard.spec.contains(chemValue))
                {
                    for (ChemValue cv : standard.spec)
                    {
                        if (cv.equals(chemValue))
                        {
                            row.add(cv.percent);
                            break;
                        }
                    }
                }
                else
                {
                    row.add("0.0");
                }
            }

            data.add(row);
        }

        return data;
    }
}