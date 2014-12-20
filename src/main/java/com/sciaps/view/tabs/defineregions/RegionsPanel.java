package com.sciaps.view.tabs.defineregions;

import com.sciaps.common.data.Region;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.utils.SwingUtils;
import com.sciaps.common.swing.view.ImmutableTable;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
import javax.swing.table.TableRowSorter;

/**
 *
 * @author sgowen
 */
public final class RegionsPanel extends JPanel
{
    public interface RegionsPanelCallback
    {
        void onRegionSelected(Object regionId);

        void onRegionUnselected(Object regionId);

        void onRegionDeleted(Object regionId);
    }

    private final RegionsPanelCallback _callback;
    private JTable _regionsTable;
    private Vector _columnNames;
    private Vector _data;
    private DefaultTableModel _tableModel;
    private JTextField _filterTextField;
    private TableRowSorter<DefaultTableModel> _sorter;
    private int[] _selectedRowIndices;

    public RegionsPanel(RegionsPanelCallback callback, boolean useDragNDrop)
    {
        _callback = callback;

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        _regionsTable = new ImmutableTable();
        _regionsTable.setFont(new Font("Serif", Font.BOLD, 18));
        _regionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _regionsTable.setFillsViewportHeight(true);
        _regionsTable.setSelectionMode(useDragNDrop ? ListSelectionModel.SINGLE_SELECTION : ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        _regionsTable.setDragEnabled(useDragNDrop);
        _regionsTable.setTransferHandler(new TransferHandler()
        {
            @Override
            public int getSourceActions(JComponent c)
            {
                return COPY;
            }

            @Override
            protected Transferable createTransferable(JComponent c)
            {
                int[] selectedRowIndices = _regionsTable.getSelectedRows();
                if (selectedRowIndices.length == 1)
                {
                    int rawRow = selectedRowIndices[0];
                    int actualRow = _regionsTable.convertRowIndexToModel(rawRow);
                    String regionId = (String) _regionsTable.getModel().getValueAt(actualRow, 0);

                    return new StringSelection(regionId);
                }

                return null;
            }
        });
        _regionsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting() && _regionsTable.getModel().getRowCount() > 0)
                {
                    int[] selectedRowIndices = _regionsTable.getSelectedRows();
                    if (selectedRowIndices.length == 0)
                    {
                        return;
                    }

                    for (int i = 0; i < selectedRowIndices.length; i++)
                    {
                        int rawRow = selectedRowIndices[i];
                        int actualRow = _regionsTable.convertRowIndexToModel(rawRow);
                        selectedRowIndices[i] = actualRow;
                    }

                    if (_callback != null)
                    {
                        for (int row : selectedRowIndices)
                        {
                            if (!isRowIndexContainedInArray(row, _selectedRowIndices))
                            {
                                String regionId = (String) _regionsTable.getModel().getValueAt(row, 0);
                                _callback.onRegionSelected(regionId);
                            }
                        }

                        for (int row : _selectedRowIndices)
                        {
                            if (!isRowIndexContainedInArray(row, selectedRowIndices))
                            {
                                String regionId = (String) _regionsTable.getModel().getValueAt(row, 0);
                                _callback.onRegionUnselected(regionId);
                            }
                        }
                    }

                    _selectedRowIndices = selectedRowIndices;
                }
            }
        });

        _columnNames = new Vector();
        _columnNames.add("ID");
        _columnNames.add("Name");
        _columnNames.add("Element");
        _columnNames.add("Min");
        _columnNames.add("Max");
        _data = new Vector();
        _tableModel = new DefaultTableModel();

        _sorter = new TableRowSorter<DefaultTableModel>(_tableModel);
        _regionsTable.setRowSorter(_sorter);

        refresh();

        JLabel title = new JLabel("Regions");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        title.setFont(new Font("Serif", Font.BOLD, 24));
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, title.getPreferredSize().height));

        add(title);

        JPanel filterForm = new JPanel(new SpringLayout());
        JLabel regionsFilterLabel = new JLabel("Filter:", SwingConstants.TRAILING);
        filterForm.add(regionsFilterLabel);

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

        regionsFilterLabel.setLabelFor(_filterTextField);
        _filterTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, _filterTextField.getPreferredSize().height));
        filterForm.add(_filterTextField);

        JButton deleteRegionButton = new JButton("Delete");
        deleteRegionButton.setAlignmentX(JButton.RIGHT_ALIGNMENT);
        deleteRegionButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (_selectedRowIndices.length == 0)
                {
                    return;
                }

                boolean proceedWithDeletion = _selectedRowIndices.length == 1;
                if (!proceedWithDeletion)
                {
                    proceedWithDeletion = JOptionPane.showOptionDialog(
                            null,
                            "Delete " + _selectedRowIndices.length + " regions?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            new String[]
                            {
                                "Cancel", "Yes"
                            },
                            "Cancel") == 1;
                }

                if (proceedWithDeletion)
                {
                    for (int selectedRowIndex : _selectedRowIndices)
                    {
                        Object regionToRemoveId = _regionsTable.getModel().getValueAt(selectedRowIndex, 0);
                        if (regionToRemoveId != null)
                        {
                            LibzUnitManager.getInstance().getRegions().remove(regionToRemoveId);
                        }

                        if (_callback != null)
                        {
                            _callback.onRegionDeleted(regionToRemoveId);
                        }
                    }

                    refresh();
                }
            }
        });

        filterForm.add(deleteRegionButton);

        SwingUtils.makeCompactGrid(filterForm, 1, 3, 6, 6, 6, 6);
        add(filterForm);

        JScrollPane scrollPane = new JScrollPane(_regionsTable);

        add(scrollPane);
    }

    public void refresh()
    {
        _selectedRowIndices = new int[]
        {
        };

        fillRegionsData();

        _tableModel.setDataVector(_data, _columnNames);
        _regionsTable.setModel(_tableModel);

        SwingUtils.refreshTable(_regionsTable);
        SwingUtils.fitTableToColumns(_regionsTable);

        _regionsTable.removeColumn(_regionsTable.getColumnModel().getColumn(0));
    }

    public JTable getTable()
    {
        return _regionsTable;
    }

    private void fillRegionsData()
    {
        if (LibzUnitManager.getInstance().getRegions() != null)
        {
            _data.clear();

            for (Map.Entry<String, Region> entry : LibzUnitManager.getInstance().getRegions().entrySet())
            {
                Vector row = new Vector();

                row.add(entry.getKey());

                Region region = entry.getValue();
                row.add(region.name);
                row.add(region.getElement().symbol);
                row.add(region.wavelengthRange.getMinimumDouble());
                row.add(region.wavelengthRange.getMaximumDouble());

                _data.add(row);
            }
        }
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
            Logger.getLogger(RegionsPanel.class.getName()).log(Level.INFO, null, e);
        }
    }

    private boolean isRowIndexContainedInArray(int rowIndex, int[] rows)
    {
        for (int row : rows)
        {
            if (row == rowIndex)
            {
                return true;
            }
        }

        return false;
    }
}