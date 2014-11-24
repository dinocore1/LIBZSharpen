package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.ChemValue;
import com.sciaps.common.data.Standard;
import com.sciaps.global.LibzUnitManager;
import com.sciaps.listener.TableCellListener;
import com.sciaps.utils.NumberUtils;
import com.sciaps.utils.SwingUtils;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author sgowen
 */
public final class ConfigureStandardsPanel extends AbstractTabPanel
{
    private static final String TAB_NAME = "Configure Standards";

    private JTable _standardsTable;
    private Vector _data;
    private Vector _columnNames;
    private DefaultTableModel _tableModel;
    private JTextField _filterTextField;
    private TableRowSorter<DefaultTableModel> _sorter;

    public ConfigureStandardsPanel(MainFrame mainFrame)
    {
        super(mainFrame);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        _standardsTable = new JTable();
        _standardsTable.setFont(new Font("Serif", Font.BOLD, 18));
        _standardsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _standardsTable.setPreferredScrollableViewportSize(new Dimension((int) ((float) _mainFrame.getWidth() * 0.96f), (int) ((float) _mainFrame.getHeight() * 0.80f)));
        _standardsTable.setFillsViewportHeight(true);
        _standardsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableCellListener tcl = new TableCellListener(_standardsTable, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                TableCellListener tcl = (TableCellListener) e.getSource();

                System.out.println("Row   : " + tcl.getRow());
                System.out.println("Column: " + tcl.getColumn());
                System.out.println("Old   : " + tcl.getOldValue());
                System.out.println("New   : " + tcl.getNewValue());

                int rowChanged = tcl.getRow();
                int columnChanged = tcl.getColumn();
                TableModel model = _standardsTable.getModel();

                Object newValue = tcl.getNewValue();

                if (NumberUtils.isNumber(newValue))
                {
                    double newPercentageValue = NumberUtils.toDouble(newValue);

                    String standardChanged = (String) model.getValueAt(rowChanged, 0);

                    JTableHeader th = _standardsTable.getTableHeader();
                    TableColumnModel tcm = th.getColumnModel();
                    TableColumn tc = tcm.getColumn(columnChanged);

                    String elementChanged = (String) tc.getHeaderValue();

                    final LibzUnitManager libzSharpenManager = LibzUnitManager.getInstance();
                    for (Standard standard : libzSharpenManager.getStandards())
                    {
                        if (standard.name.equals(standardChanged))
                        {
                            boolean chemValueNeedsToBeAdded = true;
                            for (ChemValue cv : standard.spec)
                            {
                                if (cv.element.symbol.equals(elementChanged))
                                {
                                    cv.percent = newPercentageValue;
                                    chemValueNeedsToBeAdded = false;
                                }
                            }

                            if (chemValueNeedsToBeAdded)
                            {
                                ChemValue cv = createChemValueForElementWithPercentage(elementChanged, newPercentageValue);
                                standard.spec.add(cv);
                            }
                        }
                    }
                }
                else
                {
                    model.setValueAt(tcl.getOldValue(), rowChanged, columnChanged);
                }
            }
        });

        _data = new Vector();
        _columnNames = new Vector();
        _tableModel = new DefaultTableModel();

        _sorter = new TableRowSorter<DefaultTableModel>(_tableModel);
        _standardsTable.setRowSorter(_sorter);

        JPanel filterForm = new JPanel(new SpringLayout());
        JLabel standardsFilterLabel = new JLabel("Standards Filter:", SwingConstants.TRAILING);
        filterForm.add(standardsFilterLabel);

        _filterTextField = new JTextField();
        _filterTextField.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void changedUpdate(DocumentEvent e)
            {
                filterTable();
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                filterTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                filterTable();
            }
        });

        standardsFilterLabel.setLabelFor(_filterTextField);
        filterForm.add(_filterTextField);
        SwingUtils.makeCompactGrid(filterForm, 1, 2, 6, 6, 6, 6);
        add(filterForm);

        JScrollPane scrollPane = new JScrollPane(_standardsTable);

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
                final String standardName = JOptionPane.showInputDialog(_mainFrame, "Enter name for new Standard:");

                persistStandardWithName(standardName);
                addRowToTableForStandard(standardName);

                SwingUtils.refreshTable(_standardsTable);

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
                String element = (String) JOptionPane.showInputDialog(_mainFrame, "Please select an element:", "Elements", JOptionPane.INFORMATION_MESSAGE, null, elements, null);

                addColumnToTableForElement(element);

                SwingUtils.refreshTable(_standardsTable);

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

        SwingUtils.refreshTable(_standardsTable);
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

    private void filterTable()
    {
        try
        {
            RowFilter<DefaultTableModel, Object> rowFilter = RowFilter.regexFilter("(?i)" + _filterTextField.getText(), 0);
            _sorter.setRowFilter(rowFilter);
        }
        catch (java.util.regex.PatternSyntaxException e)
        {
            // If current expression doesn't parse, don't update.
        }
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

    private Vector<ChemValue> getUniqueChemValues()
    {
        Vector<ChemValue> uniqueChemValues = new Vector<ChemValue>();

        final LibzUnitManager libzSharpenManager = LibzUnitManager.getInstance();

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
        for (int i = 1; i <= LibzUnitManager.NUM_ATOMIC_ELEMENTS; i++)
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
        final LibzUnitManager libzSharpenManager = LibzUnitManager.getInstance();

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
                    row.add("");
                }
            }

            _data.add(row);
        }
    }

    private void persistStandardWithName(String standardName)
    {
        Standard newStandard = new Standard();
        newStandard.name = standardName;

        final LibzUnitManager libzSharpenManager = LibzUnitManager.getInstance();
        libzSharpenManager.getStandards().add(newStandard);
    }

    private void addRowToTableForStandard(String standardName)
    {
        DefaultTableModel model = (DefaultTableModel) _standardsTable.getModel();
        Object[] newStandard = new Object[model.getColumnCount()];
        newStandard[0] = standardName;
        for (int i = 1; i < newStandard.length; i++)
        {
            newStandard[i] = "";
        }

        model.addRow(newStandard);
    }

    private void addColumnToTableForElement(String element)
    {
        DefaultTableModel model = (DefaultTableModel) _standardsTable.getModel();
        model.addColumn(element);
        for (int i = 0; i < model.getRowCount(); i++)
        {
            model.setValueAt("", i, model.getColumnCount() - 1);
        }
    }

    private ChemValue createChemValueForElementWithPercentage(String element, double percent)
    {
        AtomicElement ae = AtomicElement.getElementBySymbol(element);
        ChemValue cv = new ChemValue();
        cv.element = ae;
        cv.percent = percent;
        cv.error = 0.0;

        return cv;
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
