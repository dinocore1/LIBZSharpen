package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.ChemValue;
import com.sciaps.common.data.Standard;
import com.sciaps.common.swing.global.LibzUnitManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.miginfocom.swing.MigLayout;

/**
 *
 * @author sgowen
 */
public final class ConfigureStandardsPanel extends AbstractTabPanel
{
    private static final String TAB_NAME = "Configure Standards";
    private static final String TOOL_TIP = "Add and Edit Standards here";

    private JTable _standardsTable;
    private final JTextField _filterTextField;
    private final TableRowSorter<StandardsModel> _sorter;
    private final StandardsModel _standardsModel = new StandardsModel();

    private class StandardsModel extends AbstractTableModel
    {
        private ArrayList<Standard> mStandards;

        public void setStandards(Collection<Standard> standards)
        {
            mStandards = new ArrayList<Standard>(standards);

            fireTableDataChanged();
        }

        public List<Standard> getStandards()
        {
            return mStandards;
        }

        @Override
        public int getRowCount()
        {
            if (mStandards == null)
            {
                return 0;
            }

            return mStandards.size();
        }

        @Override
        public int getColumnCount()
        {
            return AtomicElement.values().length + 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            Standard standard = mStandards.get(rowIndex);
            if (columnIndex == 0)
            {
                return standard.name;
            }
            else
            {
                AtomicElement element = AtomicElement.values()[columnIndex - 1];
                ChemValue grade = standard.getGradeFor(element);
                if (grade != null)
                {
                    return grade.percent;
                }
                else
                {
                    return null;
                }
            }
        }

        @Override
        public String getColumnName(int column)
        {
            if (column == 0)
            {
                return "Name";
            }
            else
            {
                AtomicElement element = AtomicElement.values()[column - 1];
                return String.format("%s (%s)", element.symbol, element);
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return columnIndex > 0;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            Standard standard = mStandards.get(rowIndex);
            AtomicElement element = AtomicElement.values()[columnIndex - 1];
            ChemValue grade = standard.getGradeFor(element);
            if (aValue == null)
            {
                if (grade != null)
                {
                    standard.removeGradeFor(element);
                    LibzUnitManager.getInstance().getStandardsManager().markObjectAsModified(standard.mId);
                }
            }
            else
            {
                Double percent = (Double) aValue;

                if (percent < 0)
                {
                    JOptionPane.showMessageDialog(_mainFrame, "Invalid Input", "Error", JOptionPane.ERROR_MESSAGE);

                    return;
                }
                else if (percent == 0)
                {
                    if (grade != null)
                    {
                        standard.removeGradeFor(element);
                        LibzUnitManager.getInstance().getStandardsManager().markObjectAsModified(standard.mId);
                    }

                    return;
                }

                if (grade == null)
                {
                    grade = new ChemValue();
                    grade.element = element;
                    standard.spec.add(grade);
                }
                if (grade.percent != percent)
                {
                    grade.percent = percent;
                    LibzUnitManager.getInstance().getStandardsManager().markObjectAsModified(standard.mId);
                }
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            Class<?> retval = null;
            if (columnIndex == 0)
            {
                retval = String.class;
            }
            else
            {
                retval = Double.class;
            }

            return retval;
        }
    }

    public ConfigureStandardsPanel(MainFrame mainFrame)
    {
        super(mainFrame);

        setLayout(new MigLayout("fill", "", "[][grow]"));

        JLabel standardsFilterLabel = new JLabel("Search:");
        add(standardsFilterLabel, "split");

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
        add(_filterTextField, "growx, wrap");

        _standardsTable = new JTable(_standardsModel);
        JScrollPane scrollPane = new JScrollPane(_standardsTable);
        add(scrollPane, "grow");

        MouseMotionListener doScrollRectToVisible = new MouseMotionAdapter()
        {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
                ((JTableHeader) e.getSource()).scrollRectToVisible(r);
                _standardsTable.scrollRectToVisible(r);
            }
        };

        _standardsTable.getTableHeader().addMouseMotionListener(doScrollRectToVisible);
        _standardsTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                int r = _standardsTable.rowAtPoint(e.getPoint());
                if (r >= 0 && r < _standardsTable.getRowCount())
                {
                    _standardsTable.setRowSelectionInterval(r, r);
                }
                else
                {
                    _standardsTable.clearSelection();
                }

                int rawRow = _standardsTable.getSelectedRow();
                int actualRow = _standardsTable.convertRowIndexToModel(rawRow);
                if (actualRow >= 0)
                {
                    StandardsModel model = (StandardsModel) _standardsTable.getModel();
                    String standardId = (String) model.getStandards().get(actualRow).mId;
                    final Standard standard;
                    if ((standard = LibzUnitManager.getInstance().getStandardsManager().getObjects().get(standardId)) != null)
                    {
                        if (SwingUtilities.isRightMouseButton(e))
                        {
                            JPopupMenu popupMenu = new JPopupMenu();
                            JMenuItem item = new JMenuItem("Balance " + standard.name);
                            item.addActionListener(new ActionListener()
                            {
                                @Override
                                public void actionPerformed(ActionEvent event)
                                {
                                    String[] elements = getArrayOfElementNamesForStandard(standard);
                                    String element = (String) JOptionPane.showInputDialog(_mainFrame, "Please select an element:", "Elements", JOptionPane.INFORMATION_MESSAGE, null, elements, null);
                                    AtomicElement ae = AtomicElement.getElementBySymbol(element);
                                    balanceStandardUsingElement(standard, ae);
                                }
                            });
                            popupMenu.add(item);
                            popupMenu.setBorder(new BevelBorder(BevelBorder.RAISED));

                            popupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
            }
        });

        _sorter = new TableRowSorter<StandardsModel>(_standardsModel);

        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        _sorter.setSortKeys(sortKeys);

        _standardsTable.setRowSorter(_sorter);

        _standardsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _standardsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _standardsTable.getColumnModel().getColumn(0).setPreferredWidth(150);
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
                persistNewStandardWithName(standardName);
                fillDataAndColumnNames();
                _filterTextField.setText(standardName);
            }
        });
        tableMenu.add(addStandardMenuItem);

        menuBar.add(tableMenu);
    }

    @Override
    public void onDisplay()
    {
        fillDataAndColumnNames();
    }

    private String[] getArrayOfElementNamesForStandard(Standard standard)
    {
        Set<String> availableElements = new HashSet<String>();
        for (ChemValue cv : standard.spec)
        {
            availableElements.add(cv.element.symbol);
        }

        String[] elementsArray = new String[availableElements.size()];
        elementsArray = availableElements.toArray(elementsArray);

        return elementsArray;
    }

    private void balanceStandardUsingElement(Standard standard, AtomicElement ae)
    {
        ChemValue balanceChemValue = null;
        double runningConcentrationTotal = 0;
        for (ChemValue cv : standard.spec)
        {
            if (cv.element.equals(ae))
            {
                balanceChemValue = cv;
                continue;
            }

            runningConcentrationTotal += cv.percent;
        }

        if (runningConcentrationTotal >= 100)
        {
            JOptionPane.showMessageDialog(new JFrame(), "The sum concentration of all elements has already exceeded 100%", "Attention", JOptionPane.ERROR_MESSAGE);
        }
        else if (balanceChemValue != null)
        {
            balanceChemValue.percent = 100 - runningConcentrationTotal;

            LibzUnitManager.getInstance().getStandardsManager().markObjectAsModified(standard.mId);
        }
    }

    private void filterTable()
    {
        try
        {
            RowFilter<StandardsModel, Object> rowFilter = RowFilter.regexFilter("(?i)" + _filterTextField.getText(), 0);
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
        _standardsModel.setStandards(LibzUnitManager.getInstance().getStandardsManager().getObjects().values());
    }

    private void persistNewStandardWithName(String standardName)
    {
        Standard newStandard = new Standard();
        newStandard.name = standardName;

        LibzUnitManager.getInstance().getStandardsManager().addObject(newStandard);
    }
}