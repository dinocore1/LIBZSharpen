package com.sciaps.view.tabs.defineregions;

import com.google.gson.reflect.TypeToken;
import com.sciaps.common.algorithms.OneIntensityValue;
import com.sciaps.common.algorithms.SimpleBaseLine;
import com.sciaps.common.data.Region;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.listener.TableCellListener;
import com.sciaps.common.swing.utils.JsonUtils;
import com.sciaps.common.swing.utils.NumberUtils;
import com.sciaps.common.swing.utils.SwingUtils;
import com.sciaps.utils.RegionFinderUtils;
import com.sciaps.utils.TableUtils;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import static javax.swing.TransferHandler.COPY;
import javax.swing.border.BevelBorder;
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
import org.apache.commons.lang.math.DoubleRange;

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

        void onRegionEdited(Object regionId, double wavelengthMin, double wavelengthMax);
    }

    private final RegionsPanelCallback _callback;
    private JTable _regionsTable;
    private Vector _columnNames;
    private Vector _data;
    private DefaultTableModel _tableModel;
    private JTextField _filterTextField;
    private TableRowSorter<DefaultTableModel> _sorter;
    private int[] _selectedRowIndices;

    public RegionsPanel(RegionsPanelCallback callback)
    {
        _callback = callback;

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        _regionsTable = new JTable()
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                int actualRow = _regionsTable.convertRowIndexToModel(row);
                String regionId = (String) _regionsTable.getModel().getValueAt(actualRow, 0);
                Region region = LibzUnitManager.getInstance().getRegionsManager().getObjects().get(regionId);

                return !RegionFinderUtils.isPropsOnlyRegion(region);
            }
        };
        _regionsTable.setFont(new Font("Serif", Font.BOLD, 18));
        _regionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        _regionsTable.setFillsViewportHeight(true);
        _regionsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        _regionsTable.setDragEnabled(true);
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
                if (selectedRowIndices.length >= 1)
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
        _regionsTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                int r = _regionsTable.rowAtPoint(e.getPoint());
                if (r >= 0 && r < _regionsTable.getRowCount())
                {
                    _regionsTable.setRowSelectionInterval(r, r);
                }
                else
                {
                    _regionsTable.clearSelection();
                }

                int rawRow = _regionsTable.getSelectedRow();
                int actualRow = _regionsTable.convertRowIndexToModel(rawRow);
                if (actualRow >= 0)
                {
                    TableModel model = _regionsTable.getModel();
                    String regionId = (String) model.getValueAt(actualRow, 0);
                    final Region region;
                    if ((region = LibzUnitManager.getInstance().getRegionsManager().getObjects().get(regionId)) != null)
                    {
                        if (SwingUtilities.isRightMouseButton(e))
                        {
                            JPopupMenu popupMenu = new JPopupMenu();
                            JMenuItem item = new JMenuItem("Edit Additional Properties for " + region.name);
                            item.addActionListener(new ActionListener()
                            {
                                @Override
                                public void actionPerformed(ActionEvent event)
                                {
                                    JTextArea regionPropertiesTextArea = new JTextArea();
                                    regionPropertiesTextArea.setText(JsonUtils.serializeJson(region.params));
                                    int optionChosen = JOptionPane.showConfirmDialog(new JFrame(),
                                            regionPropertiesTextArea,
                                            "Enter custom JSON for Region",
                                            JOptionPane.OK_CANCEL_OPTION);

                                    if (optionChosen == JOptionPane.OK_OPTION)
                                    {
                                        try
                                        {
                                            String jsonString = regionPropertiesTextArea.getText().trim();
                                            Type type = new TypeToken<HashMap<String, Object>>()
                                            {
                                            }.getType();
                                            HashMap<String, String> regionParams = JsonUtils.deserializeJsonIntoType(jsonString, type);
                                            if (regionParams == null)
                                            {
                                                throw new IOException("regionParams is NULL!");
                                            }

                                            region.params = regionParams;
                                            LibzUnitManager.getInstance().getRegionsManager().markObjectAsModified(region.mId);
                                        }
                                        catch (Exception e)
                                        {
                                            JOptionPane.showMessageDialog(new JFrame(), "JSON is invalid", "Attention", JOptionPane.ERROR_MESSAGE);
                                        }
                                    }
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

        TableCellListener tcl = new TableCellListener(_regionsTable, new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                TableCellListener tcl = (TableCellListener) e.getSource();

                int rowIndexChanged = tcl.getRow();
                int columnIndexChanged = tcl.getColumn() - 1;
                TableModel model = _regionsTable.getModel();

                Object newValue = tcl.getNewValue();
                String newValueAsString = (String) newValue;

                String regionId = (String) model.getValueAt(rowIndexChanged, 0);

                JTableHeader th = _regionsTable.getTableHeader();
                TableColumnModel tcm = th.getColumnModel();
                TableColumn tc = tcm.getColumn(columnIndexChanged);

                final String columnChanged = (String) tc.getHeaderValue();
                Region region = LibzUnitManager.getInstance().getRegionsManager().getObjects().get(regionId);
                boolean isNewValueInvalid = false;
                if (region == null)
                {
                    isNewValueInvalid = true;
                }
                else
                {
                    if (columnChanged.equals("Min"))
                    {
                        if (NumberUtils.isNumber(newValueAsString))
                        {
                            double min = Double.parseDouble(newValueAsString);
                            region.wavelengthRange = new DoubleRange(min, region.wavelengthRange.getMaximumDouble());
                        }
                        else
                        {
                            isNewValueInvalid = true;
                        }
                    }
                    else if (columnChanged.equals("Max"))
                    {
                        if (NumberUtils.isNumber(newValueAsString))
                        {
                            double max = Double.parseDouble(newValueAsString);
                            region.wavelengthRange = new DoubleRange(region.wavelengthRange.getMinimumDouble(), max);
                        }
                        else
                        {
                            isNewValueInvalid = true;
                        }
                    }
                    else
                    {
                        // Until the Region class becomes more flexible, we cannot safely modify the name
                        isNewValueInvalid = true;
                    }
                }

                if (isNewValueInvalid)
                {
                    model.setValueAt(tcl.getOldValue(), rowIndexChanged, columnIndexChanged);
                }
                else
                {
                    LibzUnitManager.getInstance().getRegionsManager().markObjectAsModified(regionId);

                    if (_callback != null)
                    {
                        _callback.onRegionEdited(regionId, region.wavelengthRange.getMinimumDouble(), region.wavelengthRange.getMaximumDouble());
                    }
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

        refreshData();
        refreshUI();

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
                        String regionToRemoveId = (String) _regionsTable.getModel().getValueAt(selectedRowIndex, 0);
                        if (regionToRemoveId != null)
                        {
                            LibzUnitManager.getInstance().getRegionsManager().removeObject(regionToRemoveId);
                        }

                        if (_callback != null)
                        {
                            _callback.onRegionDeleted(regionToRemoveId);
                        }
                    }

                    refreshData();
                }
            }
        });

        filterForm.add(deleteRegionButton);

        SwingUtils.makeCompactGrid(filterForm, 1, 3, 6, 6, 6, 6);
        add(filterForm);

        JScrollPane scrollPane = new JScrollPane(_regionsTable);

        add(scrollPane);
    }

    public void refreshData()
    {
        _selectedRowIndices = new int[]
        {
        };

        fillRegionsData();

        _tableModel.setDataVector(_data, _columnNames);
        _regionsTable.setModel(_tableModel);

        TableUtils.initElementComboBoxForColumn(_regionsTable.getColumnModel().getColumn(2));

        _regionsTable.removeColumn(_regionsTable.getColumnModel().getColumn(0));
    }

    public void refreshUI()
    {
        SwingUtils.fitTableToColumns(_regionsTable);
    }

    public JTable getTable()
    {
        return _regionsTable;
    }

    private void fillRegionsData()
    {
        if (LibzUnitManager.getInstance().getRegionsManager().getObjects() != null)
        {
            _data.clear();

            for (Map.Entry<String, Region> entry : LibzUnitManager.getInstance().getRegionsManager().getObjects().entrySet())
            {
                Vector row = new Vector();

                row.add(entry.getKey());

                Region region = entry.getValue();
                row.add(region.name);
                row.add(region.getElement() == null ? "XX" : region.getElement().symbol);
                row.add(region.wavelengthRange == null ? "N/A" : region.wavelengthRange.getMinimumDouble());
                row.add(region.wavelengthRange == null ? "N/A" : region.wavelengthRange.getMaximumDouble());

                _data.add(row);
            }


            Region hardCodedOneRegion = null;

            for(Region r : LibzUnitManager.getInstance().getRegionsManager().getObjects().values()){
                Object v = null;
                if(r.params != null && r.params.size() == 1 && (v = r.params.get("name")) != null) {
                    if(Objects.equals(OneIntensityValue.class.getName(), v)){
                        hardCodedOneRegion = r;
                        break;
                    }
                }
            }
            if (hardCodedOneRegion == null)  {
                hardCodedOneRegion = new Region();
                hardCodedOneRegion.params.put("name", OneIntensityValue.class.getName());

                Vector row = new Vector();

                row.add(LibzUnitManager.getInstance().getRegionsManager().addObject(hardCodedOneRegion));
                row.add(hardCodedOneRegion.name);
                row.add("XX");
                row.add("N/A");
                row.add("N/A");

                _data.add(row);
            }
            hardCodedOneRegion.name = "ONE";
            hardCodedOneRegion.wavelengthRange = new DoubleRange(0, 1000);


            Region baselineRegion = null;
            for(Region r : LibzUnitManager.getInstance().getRegionsManager().getObjects().values()){
                Object v = null;
                if(r.params != null && r.params.size() == 1 && (v = r.params.get("name")) != null) {
                    if(Objects.equals(SimpleBaseLine.class.getName(), v)){
                        baselineRegion = r;
                        break;
                    }
                }
            }
            if (baselineRegion == null)
            {
                baselineRegion = new Region();
                baselineRegion.params.put("name", SimpleBaseLine.class.getName());

                Vector row = new Vector();

                row.add(LibzUnitManager.getInstance().getRegionsManager().addObject(baselineRegion));
                row.add(baselineRegion.name);
                row.add("XX");
                row.add("N/A");
                row.add("N/A");

                _data.add(row);
            }
            baselineRegion.name = "Baseline";
            baselineRegion.wavelengthRange = new DoubleRange(0, 1000);
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