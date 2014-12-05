package com.sciaps.view.tabs.common;

import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.IRRatio;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.listener.TableCellListener;
import com.sciaps.common.swing.utils.SwingUtils;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
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
public final class IntensityRatioFormulasTablePanel extends JPanel
{
    public interface IntensityRatioFormulasPanelCallback
    {
        void editIntensityRatioFormula(Object intensityRatioFormulaId);
    }

    private final IntensityRatioFormulasPanelCallback _callback;
    private JTable _intensityRatioFormulasTable;
    private Vector _columnNames;
    private Vector _data;
    private DefaultTableModel _tableModel;
    private JTextField _filterTextField;
    private TableRowSorter<DefaultTableModel> _sorter;

    public IntensityRatioFormulasTablePanel(IntensityRatioFormulasPanelCallback callback)
    {
        _callback = callback;

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        _intensityRatioFormulasTable = new JTable();
        _intensityRatioFormulasTable.setFont(new Font("Serif", Font.BOLD, 18));
        _intensityRatioFormulasTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _intensityRatioFormulasTable.setFillsViewportHeight(true);
        _intensityRatioFormulasTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableCellListener tcl = new TableCellListener(_intensityRatioFormulasTable, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                TableCellListener tcl = (TableCellListener) e.getSource();

                System.out.println("Row   : " + tcl.getRow());
                System.out.println("Column: " + tcl.getColumn());
                System.out.println("Old   : " + tcl.getOldValue());
                System.out.println("New   : " + tcl.getNewValue());

                int rowIndexChanged = tcl.getRow();
                int columnIndexChanged = tcl.getColumn();
                TableModel model = _intensityRatioFormulasTable.getModel();

                Object newValue = tcl.getNewValue();
                String newValueAsString = (String) newValue;

                String intensityRatioFormulaChanged = (String) model.getValueAt(rowIndexChanged, 0);

                JTableHeader th = _intensityRatioFormulasTable.getTableHeader();
                TableColumnModel tcm = th.getColumnModel();
                TableColumn tc = tcm.getColumn(columnIndexChanged);

                final String columnChanged = (String) tc.getHeaderValue();
                boolean isNewValueInvalid = false;
                if (columnChanged.equals("Name"))
                {
                    // Can't change the name, since we
                    isNewValueInvalid = true;
                }
                else if (columnChanged.equals("Element"))
                {
                    for (Map.Entry entry : LibzUnitManager.getInstance().getIntensityRatios().entrySet())
                    {
                        IRRatio intensityRatio = (IRRatio) entry.getValue();
                        if (intensityRatio.name.equals(intensityRatioFormulaChanged))
                        {
                            AtomicElement ae = AtomicElement.getElementBySymbol(newValueAsString);
                            if (ae == null)
                            {
                                isNewValueInvalid = true;
                            }
                            else
                            {
                                intensityRatio.element = ae;
                            }
                        }
                    }
                }

                if (isNewValueInvalid)
                {
                    model.setValueAt(tcl.getOldValue(), rowIndexChanged, columnIndexChanged);
                }
            }
        });

        _columnNames = new Vector();
        _columnNames.add("Name");
        _columnNames.add("Element");
        _data = new Vector();
        _tableModel = new DefaultTableModel();

        _sorter = new TableRowSorter<DefaultTableModel>(_tableModel);
        _intensityRatioFormulasTable.setRowSorter(_sorter);

        refresh();

        initElementColumn(_intensityRatioFormulasTable.getColumnModel().getColumn(1));

        JLabel title = new JLabel("Intensity Ratio Formulas");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        title.setFont(new Font("Serif", Font.BOLD, 24));
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, title.getPreferredSize().height));

        add(title);

        JPanel filterForm = new JPanel(new SpringLayout());
        JLabel standardsFilterLabel = new JLabel("Filter:", SwingConstants.TRAILING);
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
        _filterTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, _filterTextField.getPreferredSize().height));
        filterForm.add(_filterTextField);

        JButton deleteRegionButton = new JButton("Delete");
        deleteRegionButton.setAlignmentX(JButton.RIGHT_ALIGNMENT);
        deleteRegionButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Object intensityRatioToRemove = getSelectedIntensityRatioId();
                if (intensityRatioToRemove != null)
                {
                    LibzUnitManager.getInstance().getIntensityRatios().remove(intensityRatioToRemove);
                    refresh();
                }
            }
        });

        filterForm.add(deleteRegionButton);

        JButton editRegionButton = new JButton("Edit");
        editRegionButton.setAlignmentX(JButton.RIGHT_ALIGNMENT);
        editRegionButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Object intensityRatioToRemove = getSelectedIntensityRatioId();
                if (intensityRatioToRemove != null)
                {
                    _callback.editIntensityRatioFormula(intensityRatioToRemove);
                }
            }
        });

        filterForm.add(editRegionButton);

        SwingUtils.makeCompactGrid(filterForm, 2, 2, 6, 6, 6, 6);
        add(filterForm);

        JScrollPane scrollPane = new JScrollPane(_intensityRatioFormulasTable);

        add(scrollPane);
    }

    public void refresh()
    {
        fillIntensityRatioFormulasData();

        _tableModel.setDataVector(_data, _columnNames);
        _intensityRatioFormulasTable.setModel(_tableModel);

        initElementColumn(_intensityRatioFormulasTable.getColumnModel().getColumn(1));

        SwingUtils.refreshTable(_intensityRatioFormulasTable);
        SwingUtils.fitTableToColumns(_intensityRatioFormulasTable);
    }

    private void initElementColumn(TableColumn elementColumn)
    {
        //Set up the editor for the element cells.
        JComboBox comboBox = new JComboBox();
        for (int i = 1; i <= LibzUnitManager.NUM_ATOMIC_ELEMENTS; i++)
        {
            AtomicElement ae = AtomicElement.getElementByAtomicNum(i);
            comboBox.addItem(ae.symbol);
        }

        elementColumn.setCellEditor(new DefaultCellEditor(comboBox));
    }

    private void fillIntensityRatioFormulasData()
    {
        if (LibzUnitManager.getInstance().getIntensityRatios() != null)
        {
            _data.clear();

            for (Map.Entry entry : LibzUnitManager.getInstance().getIntensityRatios().entrySet())
            {
                IRRatio intensityRatio = (IRRatio) entry.getValue();
                Vector row = new Vector();
                row.add(intensityRatio.name);
                row.add(intensityRatio.element.symbol);

                _data.add(row);
            }
        }
    }

    private void filterTable()
    {
        try
        {
            RowFilter<DefaultTableModel, Object> rowFilter = RowFilter.regexFilter("(?i)" + _filterTextField.getText(), 0, 1);
            _sorter.setRowFilter(rowFilter);
        }
        catch (java.util.regex.PatternSyntaxException e)
        {
            // If current expression doesn't parse, don't update.
            Logger.getLogger(IntensityRatioFormulasTablePanel.class.getName()).log(Level.INFO, null, e);
        }
    }

    private Object getSelectedIntensityRatioId()
    {
        int selectedRowIndex = _intensityRatioFormulasTable.getSelectedRow();
        if (selectedRowIndex != -1)
        {
            String intensityRatioFormula = (String) _intensityRatioFormulasTable.getModel().getValueAt(selectedRowIndex, 0);

            for (Map.Entry entry : LibzUnitManager.getInstance().getIntensityRatios().entrySet())
            {
                IRRatio intensityRatio = (IRRatio) entry.getValue();
                if (intensityRatio.name.equals(intensityRatioFormula))
                {
                    return entry.getKey();
                }
            }
        }

        return null;
    }
}