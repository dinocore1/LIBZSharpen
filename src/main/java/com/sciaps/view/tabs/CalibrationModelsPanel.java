package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import com.sciaps.view.tabs.calibrationmodels.CalibrationModelsJXCollapsiblePane;
import com.sciaps.view.tabs.calibrationmodels.IntensityRatioFormulasAndStandardsJXCollapsiblePane;
import com.sciaps.view.tabs.common.IntensityRatioFormulasTablePanel;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import org.jdesktop.swingx.JXCollapsiblePane;

/**
 *
 * @author sgowen
 */
public final class CalibrationModelsPanel extends AbstractTabPanel
{
    private static final String TAB_NAME = "Calibration Models";

    private final IntensityRatioFormulasAndStandardsJXCollapsiblePane _intensityRatioFormulasAndStandardsJXCollapsiblePane;
    private final CalibrationModelsJXCollapsiblePane _calibrationModelsJXCollapsiblePane;

    public CalibrationModelsPanel(MainFrame mainFrame)
    {
        super(mainFrame);

        _intensityRatioFormulasAndStandardsJXCollapsiblePane = new IntensityRatioFormulasAndStandardsJXCollapsiblePane(mainFrame, JXCollapsiblePane.Direction.RIGHT, new IntensityRatioFormulasTablePanel.IntensityRatioFormulasPanelCallback()
        {
            @Override
            public void editIntensityRatioFormula(Object intensityRatioFormulaId)
            {
                // TODO
            }
        });
        _intensityRatioFormulasAndStandardsJXCollapsiblePane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl I"), JXCollapsiblePane.TOGGLE_ACTION);
        _intensityRatioFormulasAndStandardsJXCollapsiblePane.setCollapsed(false);
        
        _calibrationModelsJXCollapsiblePane = new CalibrationModelsJXCollapsiblePane(JXCollapsiblePane.Direction.LEFT);
        _calibrationModelsJXCollapsiblePane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl C"), JXCollapsiblePane.TOGGLE_ACTION);
        _calibrationModelsJXCollapsiblePane.setCollapsed(false);

        setLayout(new BorderLayout());

        add(_intensityRatioFormulasAndStandardsJXCollapsiblePane, BorderLayout.WEST);
        add(_calibrationModelsJXCollapsiblePane, BorderLayout.EAST);
        
        mainFrame.refreshUI();
    }

    @Override
    public String getTabName()
    {
        return TAB_NAME;
    }

    @Override
    public void customizeMenuBar(JMenuBar menuBar)
    {
        // Empty
    }

    @Override
    public void onDisplay()
    {
        _intensityRatioFormulasAndStandardsJXCollapsiblePane.refresh();
    }
}