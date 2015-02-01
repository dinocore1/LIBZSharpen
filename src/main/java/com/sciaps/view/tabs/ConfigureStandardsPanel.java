package com.sciaps.view.tabs;

import com.devsmart.StringUtils;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.ChemValue;
import com.sciaps.common.data.Standard;
import com.sciaps.common.objtracker.DBObjTracker;

import java.awt.Rectangle;
import java.awt.event.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import com.sciaps.events.PullEvent;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author sgowen
 */
public final class ConfigureStandardsPanel extends AbstractTabPanel {

    private JTable mStandardsTable;
    private final JTextField mSearchTextField;
    private final TableRowSorter<StandardsModel> mTableRowSorted;
    private final StandardsModel mStandardsModel = new StandardsModel();

    @Inject
    DBObjTracker mObjTracker;

    EventBus mGlobalEventBus;
    private JMenu mTableMenu;

    @Inject
    void setGlobalEventBus(EventBus eventBus) {
        mGlobalEventBus = eventBus;
        mGlobalEventBus.register(this);
    }


    private class StandardsModel extends AbstractTableModel {
        private ArrayList<Standard> mStandards;

        public void setStandards(Collection<Standard> standards) {
            mStandards = new ArrayList<Standard>(standards);

            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            if (mStandards == null) {
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
        public Object getValueAt(int rowIndex, int columnIndex) {
            Standard standard = mStandards.get(rowIndex);
            if (columnIndex == 0) {
                return standard.name;
            } else {
                AtomicElement element = AtomicElement.values()[columnIndex - 1];
                ChemValue grade = standard.getGradeFor(element);
                if (grade != null) {
                    return grade.percent;
                } else {
                    return null;
                }
            }
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0) {
                return "Name";
            } else {
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
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            Standard standard = mStandards.get(rowIndex);
            AtomicElement element = AtomicElement.values()[columnIndex - 1];
            ChemValue grade = standard.getGradeFor(element);
            if (aValue == null) {
                if (grade != null) {
                    standard.removeGradeFor(element);
                    mObjTracker.markModified(standard);
                }
            } else {
                Double percent = (Double) aValue;

                if (percent < 0) {
                    JOptionPane.showMessageDialog(ConfigureStandardsPanel.this, "Invalid Input", "Error", JOptionPane.ERROR_MESSAGE);

                    return;
                } else if (percent == 0) {
                    if (grade != null) {
                        standard.removeGradeFor(element);
                        mObjTracker.markModified(standard);
                    }

                    return;
                }

                if (grade == null) {
                    grade = new ChemValue();
                    grade.element = element;
                    standard.spec.add(grade);
                }
                if (grade.percent != percent) {
                    grade.percent = percent;
                    mObjTracker.markModified(standard);
                }
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            Class<?> retval = null;
            if (columnIndex == 0) {
                retval = String.class;
            } else {
                retval = Double.class;
            }

            return retval;
        }
    }

    public ConfigureStandardsPanel() {

        setLayout(new MigLayout("fill", "", "[][grow]"));

        JLabel standardsFilterLabel = new JLabel("Search:");
        add(standardsFilterLabel, "split");

        mSearchTextField = new JTextField();
        mSearchTextField.getDocument().addDocumentListener(new DocumentListener() {
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

        standardsFilterLabel.setLabelFor(mSearchTextField);
        add(mSearchTextField, "growx, wrap");

        mStandardsTable = new JTable(mStandardsModel);
        JScrollPane scrollPane = new JScrollPane(mStandardsTable);
        add(scrollPane, "grow");

        MouseMotionListener doScrollRectToVisible = new MouseMotionAdapter()
        {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
                ((JTableHeader) e.getSource()).scrollRectToVisible(r);
                mStandardsTable.scrollRectToVisible(r);
            }
        };

        mStandardsTable.getTableHeader().addMouseMotionListener(doScrollRectToVisible);
        mStandardsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int row = mStandardsTable.rowAtPoint(e.getPoint());
                int column = mStandardsTable.columnAtPoint(e.getPoint());


                if (row >= 0 && row < mStandardsTable.getRowCount()) {
                    mStandardsTable.setRowSelectionInterval(row, row);
                } else {
                    mStandardsTable.clearSelection();
                }

                final int modelRow = mStandardsTable.convertRowIndexToModel(row);
                final int modelColumn = mStandardsTable.convertColumnIndexToModel(column);

                if (SwingUtilities.isRightMouseButton(e)) {

                    final Standard standard = mStandardsModel.mStandards.get(modelRow);

                    JPopupMenu balanceMenu = new JPopupMenu();
                    JMenuItem item = new JMenuItem("Set Balance");
                    item.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent actionEvent) {
                            double remainder = getPercentRemainder(standard);
                            mStandardsModel.setValueAt(remainder, modelRow, modelColumn);
                        }
                    });

                    double remainder = getPercentRemainder(standard);
                    if (remainder <= 0) {
                        item.setEnabled(false);
                    }

                    balanceMenu.add(item);
                    balanceMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        mTableRowSorted = new TableRowSorter<StandardsModel>(mStandardsModel);

        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        mTableRowSorted.setSortKeys(sortKeys);

        mStandardsTable.setRowSorter(mTableRowSorted);

        mStandardsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        mStandardsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mStandardsTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        mStandardsTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        setupMenu();
    }

    private void setupMenu() {
        mTableMenu = new JMenu("Table");
        mTableMenu.setMnemonic(KeyEvent.VK_T);

        JMenuItem addStandardMenuItem = new JMenuItem("Add Standard", KeyEvent.VK_S);
        addStandardMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        addStandardMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                final String standardName = JOptionPane.showInputDialog(ConfigureStandardsPanel.this, "Enter name for new Standard:");
                if(!StringUtils.isEmptyString(standardName)) {
                    persistNewStandardWithName(standardName);
                    mSearchTextField.setText(standardName);
                }
            }
        });
        mTableMenu.add(addStandardMenuItem);
    }

    @Override
    public void onDisplay() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        JMenuBar menuBar = topFrame.getJMenuBar();
        menuBar.add(mTableMenu);
        menuBar.revalidate();
        menuBar.repaint();
    }

    @Override
    public void onHide() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        JMenuBar menuBar = topFrame.getJMenuBar();
        menuBar.remove(mTableMenu);
        menuBar.revalidate();
        menuBar.repaint();
    }


    private double getPercentRemainder(Standard standard) {
        double sum = 0;
        for(ChemValue cv : standard.spec) {
            sum += cv.percent;
        }

        return 100 - sum;
    }

    private void filterTable() {
        try {
            RowFilter<StandardsModel, Object> rowFilter = RowFilter.regexFilter("(?i)" + mSearchTextField.getText(), 0);
            mTableRowSorted.setRowFilter(rowFilter);
        } catch (java.util.regex.PatternSyntaxException e) {
            // If current expression doesn't parse, don't update.
            Logger.getLogger(ConfigureStandardsPanel.class.getName()).log(Level.INFO, null, e);
        }
    }

    @Subscribe
    public void onPullEvent(PullEvent event) {
        if(event.mSuccess) {
            LinkedList<Standard> list = new LinkedList<Standard>();
            Iterator<Standard> it = mObjTracker.getAllObjectsOfType(Standard.class);
            while (it.hasNext()) {
                list.add(it.next());
            }
            mStandardsModel.setStandards(list);
        }
    }

    private void persistNewStandardWithName(String standardName)
    {
        Standard newStandard = new Standard();
        newStandard.name = standardName;
        mObjTracker.markCreated(newStandard);
        mStandardsModel.mStandards.add(newStandard);
        mStandardsModel.fireTableRowsInserted(mStandardsModel.mStandards.size()-1, mStandardsModel.mStandards.size()-1);
    }
}