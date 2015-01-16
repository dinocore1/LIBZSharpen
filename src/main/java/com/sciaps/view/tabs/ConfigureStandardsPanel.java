package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.ChemValue;
import com.sciaps.common.data.Standard;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.listener.TableCellListener;
import com.sciaps.common.swing.utils.NumberUtils;
import com.sciaps.common.swing.utils.SwingUtils;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author sgowen
 */
public final class ConfigureStandardsPanel extends AbstractTabPanel
{
    private static final String TAB_NAME = "Configure Standards";
    private static final String TOOL_TIP = "Add and Edit Standards here";

    private JTable _standardsTable;
    private final Vector _data;
    private final Vector _columnNames;
    private final DefaultTableModel _tableModel;
    private final JTextField _filterTextField;
    private final TableRowSorter<DefaultTableModel> _sorter;

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

                int rowChanged = tcl.getRow();
                int columnChanged = tcl.getColumn() - 1;
                TableModel model = _standardsTable.getModel();

                Object newValue = tcl.getNewValue();

                String standardIdChanged = (String) model.getValueAt(rowChanged, 0);

                JTableHeader th = _standardsTable.getTableHeader();
                TableColumnModel tcm = th.getColumnModel();
                TableColumn tc = tcm.getColumn(columnChanged);

                String elementChanged = (String) tc.getHeaderValue();

                boolean isNewValueInvalid = true;
                if (columnChanged >= 1)
                {
                    if (NumberUtils.isNumber(newValue))
                    {
                        double newPercentageValue = NumberUtils.toDouble(newValue);

                        Standard standard = LibzUnitManager.getInstance().getStandardsManager().getObjects().get(standardIdChanged);
                        boolean chemValueNeedsToBeAdded = true;
                        for (ChemValue cv : standard.spec)
                        {
                            if (cv.element.symbol.equals(elementChanged))
                            {
                                cv.percent = newPercentageValue;
                                chemValueNeedsToBeAdded = false;
                                isNewValueInvalid = false;
                            }
                        }

                        if (chemValueNeedsToBeAdded)
                        {
                            ChemValue cv = createChemValueForElementWithPercentage(elementChanged, newPercentageValue);
                            standard.spec.add(cv);
                        }
                    }
                    else if (StringUtils.isEmpty((String) newValue))
                    {
                        Standard standard = LibzUnitManager.getInstance().getStandardsManager().getObjects().get(standardIdChanged);
                        ChemValue cvToRemove = null;
                        for (ChemValue cv : standard.spec)
                        {
                            if (cv.element.symbol.equals(elementChanged))
                            {
                                cvToRemove = cv;
                                isNewValueInvalid = false;
                            }
                        }

                        if (cvToRemove != null)
                        {
                            standard.spec.remove(cvToRemove);
                        }
                    }
                }
                else if (columnChanged == 0)
                {
                    for (Map.Entry<String, Standard> entry : LibzUnitManager.getInstance().getStandardsManager().getObjects().entrySet())
                    {
                        if (entry.getKey().equals(standardIdChanged))
                        {
                            Standard standard = (Standard) entry.getValue();
                            standard.name = (String) newValue;
                            isNewValueInvalid = false;
                        }
                    }
                }

                if (isNewValueInvalid)
                {
                    model.setValueAt(tcl.getOldValue(), rowChanged, columnChanged + 1);
                }
                else
                {
                    LibzUnitManager.getInstance().getStandardsManager().markObjectAsModified(standardIdChanged);
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
    public String getToolTip()
    {
        return TOOL_TIP;
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

                String newStandardId = persistNewStandardWithName(standardName);

                fillDataAndColumnNames();

                SwingUtils.fitTableToColumns(_standardsTable);
                SwingUtils.refreshTable(_standardsTable);

                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        DefaultTableModel model = (DefaultTableModel) _standardsTable.getModel();
                        SwingUtils.scrollTable(_standardsTable, model.getRowCount() - 1, 0);
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

                SwingUtils.fitTableToColumns(_standardsTable);
                SwingUtils.refreshTable(_standardsTable);

                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        DefaultTableModel model = (DefaultTableModel) _standardsTable.getModel();
                        SwingUtils.scrollTable(_standardsTable, 0, model.getColumnCount() - 1);
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

        SwingUtils.fitTableToColumns(_standardsTable);
        SwingUtils.refreshTable(_standardsTable);
    }

    private void filterTable()
    {
        try
        {
            RowFilter<DefaultTableModel, Object> rowFilter = RowFilter.regexFilter("(?i)" + _filterTextField.getText(), 1);
            _sorter.setRowFilter(rowFilter);
        }
        catch (java.util.regex.PatternSyntaxException e)
        {
            // If current expression doesn't parse, don't update.
            Logger.getLogger(ConfigureStandardsPanel.class.getName()).log(Level.INFO, null, e);
        }
    }

    private void fillDataAndColumnNames()
    {
        _data.clear();
        _columnNames.clear();

        Vector<ChemValue> uniqueChemValues = getUniqueChemValues();
        _columnNames.add("ID");
        _columnNames.add("Standard");
        for (int i = 0; i < uniqueChemValues.size(); i++)
        {
            _columnNames.add(uniqueChemValues.get(i).element.symbol);
        }

        generateStandardsDataForTable(uniqueChemValues);

        _tableModel.setDataVector(_data, _columnNames);
        _standardsTable.setModel(_tableModel);

        _standardsTable.removeColumn(_standardsTable.getColumnModel().getColumn(0));
    }

    private Vector<ChemValue> getUniqueChemValues()
    {
        Vector<ChemValue> uniqueChemValues = new Vector<ChemValue>();

        if (LibzUnitManager.getInstance().getStandardsManager().getObjects() != null)
        {
            for (Map.Entry entry : LibzUnitManager.getInstance().getStandardsManager().getObjects().entrySet())
            {
                Standard standard = (Standard) entry.getValue();
                for (ChemValue chemValue : standard.spec)
                {
                    if (isChemValueUnique(uniqueChemValues, chemValue))
                    {
                        uniqueChemValues.add(chemValue);
                    }
                }
            }
        }

        Collections.sort(uniqueChemValues, new Comparator<ChemValue>()
        {
            @Override
            public int compare(ChemValue o1, ChemValue o2)
            {
                int a = o1.element.atomicNumber;
                int b = o2.element.atomicNumber;

                return a > b ? 1 : a < b ? -1 : 0;
            }
        });

        return uniqueChemValues;
    }

    private String[] getArrayOfElementsNotAlreadyInUse()
    {
        List<String> usedElements = new ArrayList<String>();
        JTableHeader th = _standardsTable.getTableHeader();
        TableColumnModel tcm = th.getColumnModel();
        int columnCount = tcm.getColumnCount();
        for (int i = 2; i < columnCount; i++)
        {
            TableColumn tc = tcm.getColumn(i);

            String element = (String) tc.getHeaderValue();
            usedElements.add(element);
        }

        List<String> availableElements = new ArrayList<String>();
        for (int i = 1; i <= LibzUnitManager.NUM_ATOMIC_ELEMENTS; i++)
        {
            AtomicElement ae = AtomicElement.getElementByAtomicNum(i);
            if (!isAtomicElementAlreadyInUse(ae, usedElements))
            {
                availableElements.add(ae.symbol);
            }
        }

        String[] elementsArray = new String[availableElements.size()];
        elementsArray = availableElements.toArray(elementsArray);

        return elementsArray;
    }

    private void generateStandardsDataForTable(Vector<ChemValue> chemValues)
    {
        if (LibzUnitManager.getInstance().getStandardsManager().getObjects() != null)
        {
            for (Map.Entry entry : LibzUnitManager.getInstance().getStandardsManager().getObjects().entrySet())
            {
                Vector row = new Vector();

                row.add(entry.getKey());

                Standard standard = (Standard) entry.getValue();
                row.add(standard.name);

                for (int j = 0; j < chemValues.size(); j++)
                {
                    ChemValue chemValue = chemValues.get(j);

                    if (!isChemValueUnique(standard.spec, chemValue))
                    {
                        for (ChemValue cv : standard.spec)
                        {
                            if (cv.element.equals(chemValue.element))
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
    }

    private String persistNewStandardWithName(String standardName)
    {
        Standard newStandard = new Standard();
        newStandard.name = standardName;

        return LibzUnitManager.getInstance().getStandardsManager().addObject(newStandard);
    }

    private void addColumnToTableForElement(String element)
    {
        DefaultTableModel model = (DefaultTableModel) _standardsTable.getModel();
        model.addColumn(element);
        for (int i = 0; i < model.getRowCount(); i++)
        {
            model.setValueAt("", i, model.getColumnCount() - 1);
        }

        _standardsTable.removeColumn(_standardsTable.getColumnModel().getColumn(0));
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

    private boolean isAtomicElementAlreadyInUse(AtomicElement ae, List<String> usedElements)
    {
        for (String element : usedElements)
        {
            if (ae != null && element.equals(ae.symbol))
            {
                return true;
            }
        }

        return false;
    }

    private boolean isChemValueUnique(Collection<ChemValue> uniqueChemValues, ChemValue newChemValue)
    {
        for (ChemValue cv : uniqueChemValues)
        {
            if (cv.element.equals(newChemValue.element))
            {
                return false;
            }
        }

        return true;
    }
}