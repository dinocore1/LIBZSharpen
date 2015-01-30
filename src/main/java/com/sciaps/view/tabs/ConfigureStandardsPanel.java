package com.sciaps.view.tabs;

import com.google.inject.Inject;
import com.sciaps.MainFrame;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.ChemValue;
import com.sciaps.common.data.Standard;
import com.sciaps.common.objtracker.DBObjTracker;
import com.sciaps.common.swing.global.LibzUnitManager;
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.*;
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

    @Inject
    DBObjTracker mObjTracker;

    private class StandardsModel extends AbstractTableModel {
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
                    mObjTracker.markModified(standard);
                }
            }
            else
            {
                Double percent = (Double) aValue;

                if (percent < 0)
                {
                    JOptionPane.showMessageDialog(ConfigureStandardsPanel.this, "Invalid Input", "Error", JOptionPane.ERROR_MESSAGE);

                    return;
                }
                else if (percent == 0)
                {
                    if (grade != null)
                    {
                        standard.removeGradeFor(element);
                        mObjTracker.markModified(standard);
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
                    mObjTracker.markModified(standard);
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

    public ConfigureStandardsPanel() {

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
                int row = _standardsTable.rowAtPoint(e.getPoint());
                int column = _standardsTable.columnAtPoint(e.getPoint());


                if (row >= 0 && row < _standardsTable.getRowCount()) {
                    _standardsTable.setRowSelectionInterval(row, row);
                } else {
                    _standardsTable.clearSelection();
                }

                final int modelRow = _standardsTable.convertRowIndexToModel(row);
                final int modelColumn = _standardsTable.convertColumnIndexToModel(column);

                if(SwingUtilities.isRightMouseButton(e)){

                    final Standard standard = _standardsModel.mStandards.get(modelRow);

                    JPopupMenu balanceMenu = new JPopupMenu();
                    JMenuItem item = new JMenuItem("Set Balance");
                    item.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent actionEvent) {
                            double remainder = getPercentRemainder(standard);
                            _standardsModel.setValueAt(remainder, modelRow, modelColumn);
                        }
                    });

                    double remainder = getPercentRemainder(standard);
                    if(remainder <= 0) {
                        item.setEnabled(false);
                    }

                    balanceMenu.add(item);
                    balanceMenu.show(e.getComponent(), e.getX(), e.getY());
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
                final String standardName = JOptionPane.showInputDialog(ConfigureStandardsPanel.this, "Enter name for new Standard:");
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

    private double getPercentRemainder(Standard standard) {
        double sum = 0;
        for(ChemValue cv : standard.spec) {
            sum += cv.percent;
        }

        return 100 - sum;
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
        LinkedList<Standard> list = new LinkedList<Standard>();
        Iterator<Standard> it = mObjTracker.getAllObjectsOfType(Standard.class);
        while(it.hasNext()){
            list.add(it.next());
        }
        _standardsModel.setStandards(list);
    }

    private void persistNewStandardWithName(String standardName)
    {
        Standard newStandard = new Standard();
        newStandard.name = standardName;

        mObjTracker.markCreated(newStandard);
    }
}