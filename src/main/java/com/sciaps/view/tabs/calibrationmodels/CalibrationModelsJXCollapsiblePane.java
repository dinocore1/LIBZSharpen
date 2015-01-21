package com.sciaps.view.tabs.calibrationmodels;

import com.sciaps.view.tabs.common.CalibrationModelsTablePanel;
import javax.swing.BorderFactory;
import org.jdesktop.swingx.JXCollapsiblePane;

/**
 *
 * @author sgowen
 */
public final class CalibrationModelsJXCollapsiblePane extends JXCollapsiblePane
{
    private final CalibrationModelsTablePanel _calibrationModelsTablePanel;

    public CalibrationModelsJXCollapsiblePane(Direction direction, CalibrationModelsTablePanel.CalibrationModelsPanelCallback callback)
    {
        super(direction);

        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        _calibrationModelsTablePanel = new CalibrationModelsTablePanel(callback);
        _calibrationModelsTablePanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        add(_calibrationModelsTablePanel);
    }

    public void refresh()
    {
        _calibrationModelsTablePanel.refreshData();

        if (!isCollapsed())
        {
            _calibrationModelsTablePanel.refreshUI();
        }
    }
}