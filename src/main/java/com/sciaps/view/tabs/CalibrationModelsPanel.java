package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import com.sciaps.common.data.IRRatio;
import com.sciaps.view.tabs.common.IntensityRatioFormulasTablePanel.IntensityRatioFormulasPanelCallback;
import com.sciaps.view.tabs.intensityratioformulas.IntensityRatioFormulasJXCollapsiblePane;
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

    private final IntensityRatioFormulasJXCollapsiblePane _intensityRatioFormulasJXCollapsiblePane;

    public CalibrationModelsPanel(MainFrame mainFrame)
    {
        super(mainFrame);

        _intensityRatioFormulasJXCollapsiblePane = new IntensityRatioFormulasJXCollapsiblePane(JXCollapsiblePane.Direction.RIGHT, new IntensityRatioFormulasPanelCallback()
        {
            @Override
            public void editIntensityRatioFormula(Object intensityRatioFormulaId)
            {
                // TODO
            }
        });
        _intensityRatioFormulasJXCollapsiblePane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl I"), JXCollapsiblePane.TOGGLE_ACTION);
        _intensityRatioFormulasJXCollapsiblePane.setCollapsed(false);

        setLayout(new BorderLayout());

        add(_intensityRatioFormulasJXCollapsiblePane, BorderLayout.WEST);
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
        _intensityRatioFormulasJXCollapsiblePane.refresh();
    }
}