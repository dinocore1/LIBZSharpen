package com.sciaps.view.tabs;

import com.devsmart.swing.BackgroundTask;
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
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;

import net.miginfocom.swing.MigLayout;
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
    private final JTextField _filterTextField;
    private final TableRowSorter<StandardsModel> _sorter;
    private final StandardsModel _standardsModel = new StandardsModel();

    private class StandardsModel extends AbstractTableModel {

        private ArrayList<Standard> mStandards;

        public void setStandards(Collection<Standard> standards) {
            mStandards = new ArrayList<Standard>(standards);

            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            if(mStandards == null){
                return 0;
            }
            return mStandards.size();
        }

        @Override
        public int getColumnCount() {
            return AtomicElement.values().length + 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Standard standard = mStandards.get(rowIndex);
            if(columnIndex == 0) {
                return standard.name;
            } else {
                AtomicElement element = AtomicElement.values()[columnIndex-1];
                ChemValue grade = standard.getGradeFor(element);
                if(grade != null) {
                    return grade.percent;
                } else {
                    return null;
                }
            }
        }

        @Override
        public String getColumnName(int column) {
            if(column == 0){
                return "Name";
            } else {
                AtomicElement element = AtomicElement.values()[column-1];
                return String.format("%s (%s)", element.symbol, element);
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex > 0;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            Standard standard = mStandards.get(rowIndex);
            AtomicElement element = AtomicElement.values()[columnIndex-1];
            ChemValue grade = standard.getGradeFor(element);
            if(aValue == null) {
                if(grade != null) {
                    standard.removeGradeFor(element);
                    LibzUnitManager.getInstance().getStandardsManager().markObjectAsModified(standard.mId);
                }
            } else {
                Double percent = (Double) aValue;

                if(percent < 0){
                    JOptionPane.showMessageDialog(_mainFrame, "Invalid Input", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                } else if(percent == 0) {
                    if(grade != null) {
                        standard.removeGradeFor(element);
                        LibzUnitManager.getInstance().getStandardsManager().markObjectAsModified(standard.mId);
                    }
                    return;
                }

                if (grade == null) {
                    grade = new ChemValue();
                    grade.element = element;
                    standard.spec.add(grade);
                }
                if(grade.percent != percent) {
                    grade.percent = percent;
                    LibzUnitManager.getInstance().getStandardsManager().markObjectAsModified(standard.mId);
                }
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            Class<?> retval = null;
            if(columnIndex == 0) {
                retval = String.class;
            } else {
                retval = Double.class;
            }
            return retval;
        }
    }

    public ConfigureStandardsPanel(MainFrame mainFrame)
    {
        super(mainFrame);

        setLayout(new MigLayout("fill",
                "",
                "[][grow]"));


        JLabel standardsFilterLabel = new JLabel("Search:");
        add(standardsFilterLabel, "split");

        _filterTextField = new JTextField();
        _filterTextField.getDocument().addDocumentListener(new DocumentListener() {
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

        MouseMotionListener doScrollRectToVisible = new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e)
            {
                Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
                ((JTableHeader)e.getSource()).scrollRectToVisible(r);
                _standardsTable.scrollRectToVisible(r);
            }
        };
        _standardsTable.getTableHeader().addMouseMotionListener(doScrollRectToVisible);

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
        addStandardMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                final String standardName = JOptionPane.showInputDialog(_mainFrame, "Enter name for new Standard:");
                String newStandardId = persistNewStandardWithName(standardName);
                fillDataAndColumnNames();
                _filterTextField.setText(standardName);

            }
        });
        tableMenu.add(addStandardMenuItem);


        menuBar.add(tableMenu);
    }

    @Override
    public void onDisplay(){
        fillDataAndColumnNames();


        //SwingUtils.fitTableToColumns(_standardsTable);
        //SwingUtils.refreshTable(_standardsTable);
    }

    private void filterTable()
    {
        try {
            RowFilter<StandardsModel, Object> rowFilter = RowFilter.regexFilter("(?i)" + _filterTextField.getText(), 0);
            _sorter.setRowFilter(rowFilter);
        }
        catch (java.util.regex.PatternSyntaxException e)
        {
            // If current expression doesn't parse, don't update.
            Logger.getLogger(ConfigureStandardsPanel.class.getName()).log(Level.INFO, null, e);
        }
    }

    private void fillDataAndColumnNames() {
        _standardsModel.setStandards(LibzUnitManager.getInstance().getStandardsManager().getObjects().values());

    }

    private String persistNewStandardWithName(String standardName)
    {
        Standard newStandard = new Standard();
        newStandard.name = standardName;

        return LibzUnitManager.getInstance().getStandardsManager().addObject(newStandard);
    }

}