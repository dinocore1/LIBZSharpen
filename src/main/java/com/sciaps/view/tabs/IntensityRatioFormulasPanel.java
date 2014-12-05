package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import com.sciaps.view.tabs.common.IntensityRatioFormulasTablePanel.IntensityRatioFormulasPanelCallback;
import com.sciaps.view.tabs.intensityratioformulas.IntensityRatioFormulasJXCollapsiblePane;
import com.sciaps.view.tabs.intensityratioformulas.RegionsAndOperatorsJXCollapsiblePane;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import org.jdesktop.swingx.JXCollapsiblePane;

/**
 *
 * @author sgowen
 */
public final class IntensityRatioFormulasPanel extends AbstractTabPanel
{
    private static final String TAB_NAME = "Intensity Ratio Formulas";

    private final RegionsAndOperatorsJXCollapsiblePane _regionsAndOperatorsJXCollapsiblePane;
    private final IntensityRatioFormulasJXCollapsiblePane _intensityRatioFormulasJXCollapsiblePane;

    public IntensityRatioFormulasPanel(MainFrame mainFrame)
    {
        super(mainFrame);

        _regionsAndOperatorsJXCollapsiblePane = new RegionsAndOperatorsJXCollapsiblePane(JXCollapsiblePane.Direction.RIGHT);
        _regionsAndOperatorsJXCollapsiblePane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl R"), JXCollapsiblePane.TOGGLE_ACTION);
        _regionsAndOperatorsJXCollapsiblePane.setCollapsed(false);

        _intensityRatioFormulasJXCollapsiblePane = new IntensityRatioFormulasJXCollapsiblePane(JXCollapsiblePane.Direction.LEFT, new IntensityRatioFormulasPanelCallback()
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

        add(_regionsAndOperatorsJXCollapsiblePane, BorderLayout.WEST);
        add(_intensityRatioFormulasJXCollapsiblePane, BorderLayout.EAST);
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
        _regionsAndOperatorsJXCollapsiblePane.refresh();
        _intensityRatioFormulasJXCollapsiblePane.refresh();
    }
}