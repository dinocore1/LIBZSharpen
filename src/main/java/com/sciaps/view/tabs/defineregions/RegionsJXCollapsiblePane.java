package com.sciaps.view.tabs.defineregions;

import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.EmissionLine;
import com.sciaps.common.data.Region;
import com.sciaps.global.LibzUnitManager;
import com.sciaps.listener.TableCellListener;
import com.sciaps.utils.NumberUtils;
import com.sciaps.utils.SwingUtils;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.apache.commons.lang.math.DoubleRange;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jfree.chart.plot.Marker;

/**
 *
 * @author sgowen
 */
public final class RegionsJXCollapsiblePane extends JXCollapsiblePane implements ListSelectionListener
{
    public interface RegionsJXCollapsiblePaneCallback
    {
        void removeChartMarkers(Marker[] regionMarkers);
    }

    private JTable _regionsTable;
    private Vector _columnNames;
    private Vector _data;
    private DefaultTableModel _tableModel;
    private final RegionsJXCollapsiblePaneCallback _callback;
    private final Map<String, Marker[]> regionAndAssociatedMarkersMap = new HashMap<String, Marker[]>();

    public RegionsJXCollapsiblePane(Direction direction, RegionsJXCollapsiblePaneCallback callback)
    {
        super(direction);

        _callback = callback;

        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        _regionsTable = new JTable();
        _regionsTable.setFont(new Font("Serif", Font.BOLD, 18));
        _regionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _regionsTable.setFillsViewportHeight(true);
        _regionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableCellListener tcl = new TableCellListener(_regionsTable, new AbstractAction()
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
                TableModel model = _regionsTable.getModel();

                Object newValue = tcl.getNewValue();

                String regionChanged = (String) model.getValueAt(rowIndexChanged, 0);

                JTableHeader th = _regionsTable.getTableHeader();
                TableColumnModel tcm = th.getColumnModel();
                TableColumn tc = tcm.getColumn(columnIndexChanged);

                final String columnChanged = (String) tc.getHeaderValue();
                boolean isNewValueInvalid = false;
                if (columnChanged.equals("Name"))
                {
                    // Can't change Name value, because logic relies on it
                    isNewValueInvalid = true;
                }
                else if (columnChanged.equals("Element"))
                {
                    // Can't change Element value either, because it is tied to the name
                    isNewValueInvalid = true;
                }
                else if (columnChanged.equals("Min"))
                {
                    if (NumberUtils.isNumber(newValue))
                    {
                        double newPercentageValue = NumberUtils.toDouble(newValue);

                        // We could let the user change these, but then they would want to change the name, which we don't allow, so...
                        isNewValueInvalid = true;
                    }
                    else
                    {
                        isNewValueInvalid = true;
                    }
                }
                else if (columnChanged.equals("Max"))
                {
                    if (NumberUtils.isNumber(newValue))
                    {
                        double newPercentageValue = NumberUtils.toDouble(newValue);
                        // We could let the user change these, but then they would want to change the name, which we don't allow, so...
                        isNewValueInvalid = true;
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
            }
        });

        _columnNames = new Vector();
        _columnNames.add("Name");
        _columnNames.add("Element");
        _columnNames.add("Min");
        _columnNames.add("Max");
        _data = new Vector();
        _tableModel = new DefaultTableModel();

        refresh();

        initElementColumn(_regionsTable, _regionsTable.getColumnModel().getColumn(1));

        JLabel title = new JLabel(" Regions ");
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        title.setFont(new Font("Serif", Font.BOLD, 24));

        add(title);

        JButton deleteRegionButton = new JButton("Delete");
        title.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        deleteRegionButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int selectedRowIndex = _regionsTable.getSelectedRow();
                String regionName = (String) _regionsTable.getModel().getValueAt(selectedRowIndex, 0);

                Region regionToRemove = null;
                for (Region region : LibzUnitManager.getInstance().getRegions())
                {
                    if (region.name.name.equals(regionName))
                    {
                        regionToRemove = region;
                    }
                }

                if (regionToRemove != null)
                {
                    LibzUnitManager.getInstance().getRegions().remove(regionToRemove);
                    refresh();
                }

                Marker[] markersAssociatedWithRegion = regionAndAssociatedMarkersMap.get(regionName);

                if (markersAssociatedWithRegion != null)
                {
                    _callback.removeChartMarkers(markersAssociatedWithRegion);
                }
            }
        });

        add(deleteRegionButton);

        JScrollPane scrollPane = new JScrollPane(_regionsTable);

        add(scrollPane);
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        System.out.println(e.toString());
    }

    public void refresh()
    {
        fillRegionsData();

        _tableModel.setDataVector(_data, _columnNames);
        _regionsTable.setModel(_tableModel);

        SwingUtils.refreshTable(_regionsTable);
        SwingUtils.fitTableToColumns(_regionsTable);
    }

    public void addRegion(String regionName, int wavelengthMin, int wavelengthMax, Marker... associatedMarkers)
    {
        try
        {
            Region region = new Region();
            region.wavelengthRange = new DoubleRange(wavelengthMin, wavelengthMax);
            region.name = EmissionLine.parse(regionName);

            LibzUnitManager.getInstance().getRegions().add(region);

            Marker[] markers = new Marker[associatedMarkers.length];
            System.arraycopy(associatedMarkers, 0, markers, 0, associatedMarkers.length);

            regionAndAssociatedMarkersMap.put(region.name.name, markers);

            refresh();
        }
        catch (Exception ex)
        {
            Logger.getLogger(RegionsJXCollapsiblePane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initElementColumn(JTable table, TableColumn elementColumn)
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

    private void fillRegionsData()
    {
        _data.clear();

        for (Region region : LibzUnitManager.getInstance().getRegions())
        {
            Vector row = new Vector();
            row.add(region.name.name);
            row.add(region.name.element.symbol);
            row.add(region.wavelengthRange.getMinimumDouble());
            row.add(region.wavelengthRange.getMaximumDouble());

            _data.add(row);
        }
    }
}