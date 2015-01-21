package com.sciaps.view.tabs.common;

import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.IRRatio;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.listener.TableCellListener;
import com.sciaps.common.swing.utils.SwingUtils;
import com.sciaps.utils.IntensityRatioFormulaTableUtils;
import com.sciaps.common.swing.utils.LibzTableUtils;
import com.sciaps.utils.TableUtils;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import static javax.swing.TransferHandler.COPY;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
public final class IntensityRatioFormulasTablePanel extends JPanel
{
    public interface IntensityRatioFormulasPanelCallback
    {
        void editIntensityRatioFormula(String intensityRatioFormulaId);
    }

    private final IntensityRatioFormulasPanelCallback _callback;
    private final JTable _intensityRatioFormulasTable;
    private final Vector _columnNames;
    private final Vector _data;
    private final DefaultTableModel _tableModel;
    private final JTextField _filterTextField;
    private final TableRowSorter<DefaultTableModel> _sorter;

    public IntensityRatioFormulasTablePanel(IntensityRatioFormulasPanelCallback callback)
    {
        _callback = callback;

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        _intensityRatioFormulasTable = new JTable();
        _intensityRatioFormulasTable.setDragEnabled(true);
        _intensityRatioFormulasTable.setTransferHandler(new TransferHandler()
        {
            @Override
            public int getSourceActions(JComponent c)
            {
                return COPY;
            }

            @Override
            protected Transferable createTransferable(JComponent c)
            {
                int[] selectedRowIndices = _intensityRatioFormulasTable.getSelectedRows();
                if (selectedRowIndices.length == 1)
                {
                    int rawRow = selectedRowIndices[0];
                    int actualRow = _intensityRatioFormulasTable.convertRowIndexToModel(rawRow);
                    String intensityRatioId = (String) _intensityRatioFormulasTable.getModel().getValueAt(actualRow, 0);

                    return new StringSelection(intensityRatioId);
                }

                return null;
            }
        });
        _intensityRatioFormulasTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting() && _intensityRatioFormulasTable.getModel().getRowCount() > 0 && _intensityRatioFormulasTable.getSelectedRow() != -1)
                {
                    String intensityRatioId = LibzTableUtils.getSelectedObjectId(_intensityRatioFormulasTable);
                    if (_callback != null)
                    {
                        _callback.editIntensityRatioFormula(intensityRatioId);
                    }
                }
            }
        });

        TableCellListener tcl = new TableCellListener(_intensityRatioFormulasTable, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                TableCellListener tcl = (TableCellListener) e.getSource();

                int rowIndexChanged = tcl.getRow();
                int columnIndexChanged = tcl.getColumn() - 1;
                TableModel model = _intensityRatioFormulasTable.getModel();

                Object newValue = tcl.getNewValue();
                String newValueAsString = (String) newValue;

                String intensityRatioId = (String) model.getValueAt(rowIndexChanged, 0);

                JTableHeader th = _intensityRatioFormulasTable.getTableHeader();
                TableColumnModel tcm = th.getColumnModel();
                TableColumn tc = tcm.getColumn(columnIndexChanged);

                final String columnChanged = (String) tc.getHeaderValue();
                IRRatio intensityRatio = LibzUnitManager.getInstance().getIRRatiosManager().getObjects().get(intensityRatioId);
                boolean isNewValueInvalid = false;
                if (intensityRatio == null)
                {
                    isNewValueInvalid = true;
                }
                else
                {
                    if (columnChanged.equals("Name"))
                    {
                        if (StringUtils.isEmpty(newValueAsString))
                        {
                            isNewValueInvalid = true;
                        }
                        else
                        {
                            intensityRatio.name = newValueAsString;
                        }
                    }
                    else if (columnChanged.equals("Element"))
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
                    else
                    {
                        isNewValueInvalid = true;
                    }
                }

                if (isNewValueInvalid)
                {
                    model.setValueAt(tcl.getOldValue(), rowIndexChanged, columnIndexChanged);
                }
                else
                {
                    LibzUnitManager.getInstance().getIRRatiosManager().markObjectAsModified(intensityRatioId);
                }
            }
        });

        _intensityRatioFormulasTable.setFont(new Font("Serif", Font.BOLD, 18));
        _intensityRatioFormulasTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        _intensityRatioFormulasTable.setFillsViewportHeight(true);
        _intensityRatioFormulasTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        _columnNames = new Vector();
        IntensityRatioFormulaTableUtils.fillIntensityRatioFormulasColumnNames(_columnNames);
        _data = new Vector();
        _tableModel = new DefaultTableModel();

        _sorter = new TableRowSorter<DefaultTableModel>(_tableModel);
        _intensityRatioFormulasTable.setRowSorter(_sorter);

        refresh();

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
                String intensityRatioId = LibzTableUtils.getSelectedObjectId(_intensityRatioFormulasTable);
                if (intensityRatioId != null)
                {
                    LibzUnitManager.getInstance().getIRRatiosManager().removeObject(intensityRatioId);
                    refresh();
                }
            }
        });

        filterForm.add(deleteRegionButton);

        SwingUtils.makeCompactGrid(filterForm, 1, 3, 6, 6, 6, 6);
        add(filterForm);

        JScrollPane scrollPane = new JScrollPane(_intensityRatioFormulasTable);

        add(scrollPane);
    }

    public void refresh()
    {
        IntensityRatioFormulaTableUtils.fillIntensityRatioFormulasData(_data);

        _tableModel.setDataVector(_data, _columnNames);
        _intensityRatioFormulasTable.setModel(_tableModel);

        TableUtils.initElementComboBoxForColumn(_intensityRatioFormulasTable.getColumnModel().getColumn(2));

        SwingUtils.fitTableToColumns(_intensityRatioFormulasTable);

        _intensityRatioFormulasTable.removeColumn(_intensityRatioFormulasTable.getColumnModel().getColumn(0));
    }

    public JTable getIntensityRatioFormulasTable()
    {
        return _intensityRatioFormulasTable;
    }

    private void filterTable()
    {
        try
        {
            RowFilter<DefaultTableModel, Object> rowFilter = RowFilter.regexFilter("(?i)" + _filterTextField.getText(), 1, 2);
            _sorter.setRowFilter(rowFilter);
        }
        catch (java.util.regex.PatternSyntaxException e)
        {
            // If current expression doesn't parse, don't update.
            Logger.getLogger(IntensityRatioFormulasTablePanel.class.getName()).log(Level.INFO, null, e);
        }
    }
}