package com.sciaps.view.tabs.intensityratioformulas;

import com.sciaps.common.AtomicElement;
import com.sciaps.view.tabs.common.IntensityRatioFormulasTablePanel;
import com.sciaps.view.tabs.common.IntensityRatioFormulasTablePanel.IntensityRatioFormulasPanelCallback;
import javax.swing.BorderFactory;
import org.jdesktop.swingx.JXCollapsiblePane;

/**
 *
 * @author sgowen
 */
public final class IntensityRatioFormulasJXCollapsiblePane extends JXCollapsiblePane
{
    private final IntensityRatioFormulasPanelCallback _callback;
    private final IntensityRatioFormulasTablePanel _intensityRatioFormulasPanel;

    public IntensityRatioFormulasJXCollapsiblePane(Direction direction, IntensityRatioFormulasPanelCallback callback)
    {
        super(direction);

        _callback = callback;

        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        _intensityRatioFormulasPanel = new IntensityRatioFormulasTablePanel(_callback);
        _intensityRatioFormulasPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        add(_intensityRatioFormulasPanel);
    }

    public void refresh()
    {
        _intensityRatioFormulasPanel.refresh();
    }

    public void addIntensityRatioFormula(String intensityRatioFormulaName, AtomicElement element, double[][] numerator, double[][] denominator)
    {
        // TODO
    }
}