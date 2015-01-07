package com.sciaps.view.tabs.common;

import com.sciaps.common.data.Model;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.utils.LibzTableUtils;
import com.sciaps.common.swing.utils.SwingUtils;
import com.sciaps.common.swing.view.ImmutableTable;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author sgowen
 */
public final class CalibrationModelsTablePanel extends JPanel
{
    public interface CalibrationModelsPanelCallback
    {
        void onCalibrationModelSelected(String calibrationModelId);
    }

    private final CalibrationModelsPanelCallback _callback;
    private final JTable _calibrationModelsTable;
    private final Vector _columnNames;
    private final Vector _data;
    private final DefaultTableModel _tableModel;
    private final JTextField _filterTextField;
    private final TableRowSorter<DefaultTableModel> _sorter;

    public CalibrationModelsTablePanel(CalibrationModelsPanelCallback callback)
    {
        _callback = callback;

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        _calibrationModelsTable = new ImmutableTable();
        _calibrationModelsTable.setFont(new Font("Serif", Font.BOLD, 18));
        _calibrationModelsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _calibrationModelsTable.setFillsViewportHeight(true);
        _calibrationModelsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _calibrationModelsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting() && _calibrationModelsTable.getModel().getRowCount() > 0 && _calibrationModelsTable.getSelectedRow() != -1)
                {
                    if (_callback != null)
                    {
                        _callback.onCalibrationModelSelected(LibzTableUtils.getSelectedObjectId(_calibrationModelsTable));
                    }
                }
            }
        });

        _columnNames = new Vector();
        _columnNames.add("ID");
        _columnNames.add("Name");
        _data = new Vector();
        _tableModel = new DefaultTableModel();

        _sorter = new TableRowSorter<DefaultTableModel>(_tableModel);
        _calibrationModelsTable.setRowSorter(_sorter);

        refresh();

        JLabel title = new JLabel("Calibration Models");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        title.setFont(new Font("Serif", Font.BOLD, 24));
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, title.getPreferredSize().height));

        add(title);

        JPanel filterForm = new JPanel(new SpringLayout());
        JLabel calibrationModelsFilterLabel = new JLabel("Filter:", SwingConstants.TRAILING);
        filterForm.add(calibrationModelsFilterLabel);

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

        calibrationModelsFilterLabel.setLabelFor(_filterTextField);
        _filterTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, _filterTextField.getPreferredSize().height));
        filterForm.add(_filterTextField);

        JButton deleteRegionButton = new JButton("Delete");
        deleteRegionButton.setAlignmentX(JButton.RIGHT_ALIGNMENT);
        deleteRegionButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String calModelRatioId = LibzTableUtils.getSelectedObjectId(_calibrationModelsTable);
                if (calModelRatioId != null)
                {
                    LibzUnitManager.getInstance().getModelsManager().removeObject(calModelRatioId);
                    refresh();
                }
            }
        });

        filterForm.add(deleteRegionButton);

        SwingUtils.makeCompactGrid(filterForm, 1, 3, 6, 6, 6, 6);
        add(filterForm);

        JScrollPane scrollPane = new JScrollPane(_calibrationModelsTable);

        add(scrollPane);
    }

    public void refresh()
    {
        fillCalibrationModelsData();

        _tableModel.setDataVector(_data, _columnNames);
        _calibrationModelsTable.setModel(_tableModel);

        SwingUtils.refreshTable(_calibrationModelsTable);
        SwingUtils.fitTableToColumns(_calibrationModelsTable);

        _calibrationModelsTable.removeColumn(_calibrationModelsTable.getColumnModel().getColumn(0));
    }

    private void fillCalibrationModelsData()
    {
        if (LibzUnitManager.getInstance().getModelsManager().getObjects() != null)
        {
            _data.clear();

            for (Map.Entry<String, Model> entry : LibzUnitManager.getInstance().getModelsManager().getObjects().entrySet())
            {
                Vector row = new Vector();

                row.add(entry.getKey());
                row.add(entry.getValue().name);

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
            Logger.getLogger(IntensityRatioFormulasTablePanel.class.getName()).log(Level.INFO, null, e);
        }
    }
}