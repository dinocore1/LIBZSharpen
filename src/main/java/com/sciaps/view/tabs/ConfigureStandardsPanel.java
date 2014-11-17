package com.sciaps.view.tabs;

import com.sciaps.Main;
import com.sciaps.MainFrame;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.ChemValue;
import com.sciaps.common.data.Standard;
import com.sciaps.global.LibzSharpenManager;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author sgowen
 */
public final class ConfigureStandardsPanel extends AbstractTabPanel
{
    private static final String TAB_NAME = "Configure Standards";
    private static final int NUM_ATOMIC_ELEMENTS = 118;

    private JTable _standardsTable;
    private Vector _data;
    private Vector _columnNames;
    private DefaultTableModel _tableModel;

    public ConfigureStandardsPanel(MainFrame mainFrame)
    {
        super(mainFrame);

        _standardsTable = new JTable();
        _standardsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _standardsTable.setPreferredScrollableViewportSize(new Dimension((int) ((float) _mainFrame.getWidth() * 0.96f), (int) ((float) _mainFrame.getHeight() * 0.84f)));
        _standardsTable.setFillsViewportHeight(true);
        _standardsTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                printDebugData(_standardsTable);
            }
        });

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(_standardsTable);

        //Add the scroll pane to this panel.
        add(scrollPane);

        _data = new Vector();
        _columnNames = new Vector();
        _tableModel = new DefaultTableModel();
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
                final String standardName = JOptionPane.showInputDialog(_mainFrame, "Enter name for new Standard:");

                persistStandardWithName(standardName);
                addRowToTableForStandard(standardName);

                refreshTable();

                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        DefaultTableModel model = (DefaultTableModel) _standardsTable.getModel();
                        scrollTable(model.getRowCount() - 1, 0);
                    }
                });
            }
        });
        JMenuItem addElementMenuItem = new JMenuItem("Add Element", KeyEvent.VK_E);
        addElementMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK));
        addElementMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                String[] elements = getArrayOfElementsNotAlreadyInUse();
                String element = (String) JOptionPane.showInputDialog(_mainFrame, "Please select an element:", "Elements", JOptionPane.INFORMATION_MESSAGE, null, elements, "Tennis");

                persistElementIntoStandards(element);
                addColumnToTableForElement(element);

                refreshTable();

                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        DefaultTableModel model = (DefaultTableModel) _standardsTable.getModel();
                        scrollTable(0, model.getColumnCount() - 1);
                    }
                });
            }
        });

        tableMenu.add(addStandardMenuItem);
        tableMenu.add(addElementMenuItem);

        menuBar.add(tableMenu);
    }

    @Override
    public void onDisplay()
    {
        fillDataAndColumnNames();

        refreshTable();
    }

    private void printDebugData(JTable table)
    {
        JTableHeader th = table.getTableHeader();
        TableColumnModel tcm = th.getColumnModel();
        System.out.println("Columns");
        for (int x = 1; x < tcm.getColumnCount(); x++)
        {
            TableColumn tc = tcm.getColumn(x);
            System.out.print(tc.getHeaderValue() + ", ");
        }

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

    private void scrollTable(final int row, final int column)
    {
        Thread counter = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException e)
                {
                    Logger.getLogger(ConfigureStandardsPanel.class.getName()).log(Level.SEVERE, null, e);
                };

                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Rectangle bottomRect = _standardsTable.getCellRect(row, column, true);
                        _standardsTable.scrollRectToVisible(bottomRect);
                    }
                });
            }
        };

        counter.start();
    }

    private void fillDataAndColumnNames()
    {
        _data.clear();
        _columnNames.clear();

        Vector<ChemValue> uniqueChemValues = getUniqueChemValues();
        _columnNames.add("Standard");
        for (int i = 0; i < uniqueChemValues.size(); i++)
        {
            _columnNames.add(uniqueChemValues.get(i).element.symbol);
        }

        generateStandardsDataForTable(uniqueChemValues);

        _tableModel.setDataVector(_data, _columnNames);
        _standardsTable.setModel(_tableModel);
    }

    private void refreshTable()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                for (int column = 0; column < _standardsTable.getColumnCount(); column++)
                {
                    TableColumn tableColumn = _standardsTable.getColumnModel().getColumn(column);
                    int preferredWidth = tableColumn.getMinWidth();
                    int maxWidth = tableColumn.getMaxWidth();

                    JTableHeader th = _standardsTable.getTableHeader();
                    TableColumnModel tcm = th.getColumnModel();

                    for (int x = 1; x < tcm.getColumnCount(); x++)
                    {
                        TableColumn tc = tcm.getColumn(x);
                        preferredWidth = Math.max(preferredWidth, tc.getWidth());
                    }

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

                _standardsTable.invalidate();
            }
        });
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

    private String[] getArrayOfElementsNotAlreadyInUse()
    {
        Vector<ChemValue> uniqueChemValues = getUniqueChemValues();

        List<String> elements = new ArrayList<String>();
        for (int i = 1; i < NUM_ATOMIC_ELEMENTS; i++)
        {
            AtomicElement ae = AtomicElement.getElementByAtomicNum(i);
            if (!isAtomicElementAlreadyInUse(ae, uniqueChemValues))
            {
                elements.add(ae.symbol);
            }
        }

        String[] elementsArray = new String[elements.size()];
        elementsArray = elements.toArray(elementsArray);

        return elementsArray;
    }

    private void generateStandardsDataForTable(Vector<ChemValue> chemValues)
    {
        final LibzSharpenManager libzSharpenManager = LibzSharpenManager.getInstance();

        for (Standard standard : libzSharpenManager.getStandards())
        {
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

            _data.add(row);
        }
    }

    private void persistStandardWithName(String standardName)
    {
        Standard newStandard = new Standard();
        newStandard.name = standardName;

        JTableHeader th = _standardsTable.getTableHeader();
        TableColumnModel tcm = th.getColumnModel();
        for (int x = 1; x < tcm.getColumnCount(); x++)
        {
            TableColumn tc = tcm.getColumn(x);
            String element = (String) tc.getHeaderValue();
            AtomicElement ae = AtomicElement.getElementBySymbol(element);
            ChemValue chemValue = new ChemValue();
            chemValue.element = ae;
            chemValue.percent = 0.0;
            chemValue.error = 0.0;

            newStandard.spec.add(chemValue);
        }

        final LibzSharpenManager libzSharpenManager = LibzSharpenManager.getInstance();
        libzSharpenManager.getStandards().add(newStandard);
    }

    private void persistElementIntoStandards(String elementAbbreviation)
    {
        AtomicElement ae = AtomicElement.getElementBySymbol(elementAbbreviation);
        ChemValue cv = new ChemValue();
        cv.element = ae;
        cv.percent = 0.0;
        cv.error = 0.0;

        final LibzSharpenManager libzSharpenManager = LibzSharpenManager.getInstance();
        for (Standard standard : libzSharpenManager.getStandards())
        {
            standard.spec.add(cv);
        }
    }

    private void addRowToTableForStandard(String standardName)
    {
        DefaultTableModel model = (DefaultTableModel) _standardsTable.getModel();
        Object[] newStandard = new Object[model.getColumnCount()];
        newStandard[0] = standardName;
        for (int i = 1; i < newStandard.length; i++)
        {
            newStandard[i] = "0.0";
        }

        model.addRow(newStandard);
    }

    private void addColumnToTableForElement(String element)
    {
        DefaultTableModel model = (DefaultTableModel) _standardsTable.getModel();
        model.addColumn(element);
        for (int i = 0; i < model.getRowCount(); i++)
        {
            model.setValueAt("0.0", i, model.getColumnCount() - 1);
        }
    }

    private boolean isAtomicElementAlreadyInUse(AtomicElement ae, Vector<ChemValue> uniqueChemValues)
    {
        for (ChemValue cv : uniqueChemValues)
        {
            if (cv.element.equals(ae))
            {
                return true;
            }
        }

        return false;
    }
}