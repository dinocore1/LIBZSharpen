package com.sciaps.view.tabs.calibrationmodels;

import com.sciaps.MainFrame;
import com.sciaps.common.swing.utils.SwingUtils;
import com.sciaps.utils.ShortStandardsTableUtils;
import com.sciaps.view.tabs.common.IntensityRatioFormulasTablePanel;
import com.sciaps.view.tabs.common.IntensityRatioFormulasTablePanel.IntensityRatioFormulasPanelCallback;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
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
import javax.swing.table.TableRowSorter;
import org.jdesktop.swingx.JXCollapsiblePane;

/**
 *
 * @author sgowen
 */
public final class IntensityRatioFormulasAndStandardsJXCollapsiblePane extends JXCollapsiblePane
{
    private final IntensityRatioFormulasPanelCallback _callback;
    private final IntensityRatioFormulasTablePanel _intensityRatioFormulasPanel;

    private JTable _standardsTable;
    private final Vector _data;
    private final Vector _columnNames;
    private final DefaultTableModel _tableModel;
    private final JTextField _filterTextField;
    private final TableRowSorter<DefaultTableModel> _sorter;

    public IntensityRatioFormulasAndStandardsJXCollapsiblePane(MainFrame mainFrame, JXCollapsiblePane.Direction direction, IntensityRatioFormulasPanelCallback callback)
    {
        super(direction);

        _callback = callback;

        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        _intensityRatioFormulasPanel = new IntensityRatioFormulasTablePanel(_callback);
        _intensityRatioFormulasPanel.getIntensityRatioFormulasTable().setPreferredScrollableViewportSize(new Dimension(_intensityRatioFormulasPanel.getIntensityRatioFormulasTable().getPreferredScrollableViewportSize().width, (int) ((float) mainFrame.getHeight() * 0.36f)));
        _intensityRatioFormulasPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 60, 0));

        add(_intensityRatioFormulasPanel);

        JLabel standardsLabel = new JLabel("Standards");
        standardsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        standardsLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        standardsLabel.setFont(new Font("Serif", Font.BOLD, 24));
        standardsLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, standardsLabel.getPreferredSize().height));
        standardsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        add(standardsLabel);

        JPanel filterForm = new JPanel(new SpringLayout());
        JLabel standardsFilterLabel = new JLabel("Filter:", SwingConstants.TRAILING);
        filterForm.add(standardsFilterLabel);

        _filterTextField = new JTextField();
        _filterTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, _filterTextField.getPreferredSize().height));
        _filterTextField.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void changedUpdate(DocumentEvent e)
            {
                filterStandardsTable();
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                filterStandardsTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                filterStandardsTable();
            }
        });

        standardsFilterLabel.setLabelFor(_filterTextField);
        filterForm.add(_filterTextField);
        SwingUtils.makeCompactGrid(filterForm, 1, 2, 6, 6, 6, 6);
        add(filterForm);

        _standardsTable = new JTable();
        _standardsTable.setFont(new Font("Serif", Font.BOLD, 18));
        _standardsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _standardsTable.setPreferredScrollableViewportSize(new Dimension(_intensityRatioFormulasPanel.getIntensityRatioFormulasTable().getPreferredSize().width, (int) ((float) mainFrame.getHeight() * 0.36f)));
        _standardsTable.setFillsViewportHeight(true);
        _standardsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _standardsTable.setDragEnabled(true);

        _data = new Vector();
        _columnNames = new Vector();
        _tableModel = new DefaultTableModel();

        fillDataAndColumnNames();

        _sorter = new TableRowSorter<DefaultTableModel>(_tableModel);
        _standardsTable.setRowSorter(_sorter);

        JScrollPane scrollPane = new JScrollPane(_standardsTable);
        scrollPane.setPreferredSize(new Dimension(scrollPane.getPreferredSize().width, (int) ((float) mainFrame.getHeight() * 0.36f)));
        scrollPane.setMaximumSize(new Dimension(scrollPane.getPreferredSize().width, (int) ((float) mainFrame.getHeight() * 0.36f)));

        add(scrollPane);
    }

    public void refresh()
    {
        _intensityRatioFormulasPanel.refresh();

        fillDataAndColumnNames();

        SwingUtils.refreshTable(_standardsTable);
        SwingUtils.fitTableToColumns(_standardsTable);
    }

    private void fillDataAndColumnNames()
    {
        _columnNames.clear();

        ShortStandardsTableUtils.fillStandardsColumnNames(_columnNames);
        ShortStandardsTableUtils.fillStandardsData(_data);

        _tableModel.setDataVector(_data, _columnNames);
        _standardsTable.setModel(_tableModel);

        _standardsTable.removeColumn(_standardsTable.getColumnModel().getColumn(0));
    }

    private void filterStandardsTable()
    {
        try
        {
            RowFilter<DefaultTableModel, Object> rowFilter = RowFilter.regexFilter("(?i)" + _filterTextField.getText(), 1);
            _sorter.setRowFilter(rowFilter);
        }
        catch (java.util.regex.PatternSyntaxException e)
        {
            // If current expression doesn't parse, don't update.
            Logger.getLogger(IntensityRatioFormulasAndStandardsJXCollapsiblePane.class.getName()).log(Level.INFO, null, e);
        }
    }
}