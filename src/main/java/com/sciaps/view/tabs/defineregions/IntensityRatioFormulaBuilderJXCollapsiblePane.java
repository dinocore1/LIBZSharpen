package com.sciaps.view.tabs.defineregions;

import com.sciaps.common.data.IRRatio;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.view.tabs.common.IntensityRatioFormulaContainerPanel;
import com.sciaps.view.tabs.common.IntensityRatioFormulasTablePanel;
import com.sciaps.view.tabs.common.OperatorsPanel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;

/**
 *
 * @author sgowen
 */
public final class IntensityRatioFormulaBuilderJXCollapsiblePane extends JXCollapsiblePane
{
    private final IntensityRatioFormulaContainerPanel _intensityRatioFormulaContainerPanel;
    private final IntensityRatioFormulasTablePanel _intensityRatioFormulasPanel;

    public IntensityRatioFormulaBuilderJXCollapsiblePane(Direction direction)
    {
        super(direction);

        _intensityRatioFormulasPanel = new IntensityRatioFormulasTablePanel(new IntensityRatioFormulasTablePanel.IntensityRatioFormulasPanelCallback()
        {
            @Override
            public void editIntensityRatioFormula(String intensityRatioFormulaId)
            {
                IRRatio irRatioToEdit = LibzUnitManager.getInstance().getIRRatiosManager().getObjects().get(intensityRatioFormulaId);
                if (irRatioToEdit != null)
                {
                    _intensityRatioFormulaContainerPanel.editIntensityRatioFormula(irRatioToEdit);
                }
            }
        });
        _intensityRatioFormulasPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        _intensityRatioFormulaContainerPanel = new IntensityRatioFormulaContainerPanel(new IntensityRatioFormulaContainerPanel.IntensityRatioFormulaContainerPanelCallback()
        {
            @Override
            public void onIntensityRatioSaved()
            {
                _intensityRatioFormulasPanel.refreshData();

                if (!isCollapsed())
                {
                    _intensityRatioFormulasPanel.refreshUI();
                }
            }
        });
        _intensityRatioFormulaContainerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // OperatorsPanel
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 3;
        gbc.weightx = 0;
        gbc.weighty = 0;
        centerPanel.add(new OperatorsPanel(), gbc);

        // IntensityRatioFormulaContainerPanel
        gbc.gridy = 0;
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 3;
        gbc.weightx = 0;
        gbc.weighty = 0;
        centerPanel.add(_intensityRatioFormulaContainerPanel, gbc);

        // IntensityRatioFormulasTablePanel
        gbc.gridy = 0;
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 3;
        gbc.weightx = 0;
        gbc.weighty = 0;
        centerPanel.add(_intensityRatioFormulasPanel, gbc);

        setLayout(new BorderLayout());

        add(centerPanel, BorderLayout.CENTER);
    }
}