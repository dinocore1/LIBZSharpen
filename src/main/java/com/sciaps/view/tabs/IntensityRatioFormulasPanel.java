package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import com.sciaps.common.data.IRRatio;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.view.tabs.common.IntensityRatioFormulaContainerPanel;
import com.sciaps.view.tabs.common.IntensityRatioFormulaContainerPanel.IntensityRatioFormulaContainerPanelCallback;
import com.sciaps.view.tabs.common.IntensityRatioFormulasTablePanel.IntensityRatioFormulasPanelCallback;
import com.sciaps.view.tabs.intensityratioformulas.IntensityRatioFormulasJXCollapsiblePane;
import com.sciaps.view.tabs.intensityratioformulas.RegionsAndOperatorsJXCollapsiblePane;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.jdesktop.swingx.JXCollapsiblePane;

/**
 *
 * @author sgowen
 */
public final class IntensityRatioFormulasPanel extends AbstractTabPanel
{
    private static final String TAB_NAME = "Intensity Ratio Formulas";
    private static final String TOOL_TIP = "Create Intensity Ratio Formulas here";

    private final RegionsAndOperatorsJXCollapsiblePane _regionsAndOperatorsJXCollapsiblePane;
    private final IntensityRatioFormulaContainerPanel _intensityRatioFormulaContainerPanel;
    private final IntensityRatioFormulasJXCollapsiblePane _intensityRatioFormulasJXCollapsiblePane = null;

    public IntensityRatioFormulasPanel(MainFrame mainFrame)
    {
        super(mainFrame);

        _intensityRatioFormulaContainerPanel = new IntensityRatioFormulaContainerPanel(new IntensityRatioFormulaContainerPanelCallback()
        {
            @Override
            public void onIntensityRatioSaved()
            {
                _intensityRatioFormulasJXCollapsiblePane.refresh();
            }
        });

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Top
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 5;
        centerPanel.add(new JPanel(), gbc);

        // Left
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        centerPanel.add(new JPanel(), gbc);

        // Center
        gbc.gridy = 1;
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        centerPanel.add(_intensityRatioFormulaContainerPanel, gbc);

        // Right
        gbc.gridy = 1;
        gbc.gridx = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        centerPanel.add(new JPanel(), gbc);

        // Bottom
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 5;
        centerPanel.add(new JPanel(), gbc);

        _regionsAndOperatorsJXCollapsiblePane = new RegionsAndOperatorsJXCollapsiblePane(JXCollapsiblePane.Direction.RIGHT);
        _regionsAndOperatorsJXCollapsiblePane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl R"), JXCollapsiblePane.TOGGLE_ACTION);
        _regionsAndOperatorsJXCollapsiblePane.setCollapsed(false);

        /*
        _intensityRatioFormulasJXCollapsiblePane = new IntensityRatioFormulasJXCollapsiblePane(JXCollapsiblePane.Direction.LEFT, new IntensityRatioFormulasPanelCallback()
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
        */
        _intensityRatioFormulasJXCollapsiblePane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl I"), JXCollapsiblePane.TOGGLE_ACTION);
        _intensityRatioFormulasJXCollapsiblePane.setCollapsed(false);

        setLayout(new BorderLayout());

        add(_regionsAndOperatorsJXCollapsiblePane, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(_intensityRatioFormulasJXCollapsiblePane, BorderLayout.EAST);
    }

    @Override
    public String getTabName()
    {
        return TAB_NAME;
    }

    @Override
    public String getToolTip()
    {
        return TOOL_TIP;
    }

    @Override
    public void customizeMenuBar(JMenuBar menuBar)
    {
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);

        final JMenuItem showRegionsMenuItem = new JCheckBoxMenuItem("Show Regions and Operators", true);
        showRegionsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.ALT_MASK));
        showRegionsMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                AbstractButton aButton = (AbstractButton) ae.getSource();
                boolean isSelected = aButton.getModel().isSelected();
                _regionsAndOperatorsJXCollapsiblePane.setCollapsed(!isSelected);
            }
        });

        final JMenuItem showIRFormulasMenuItem = new JCheckBoxMenuItem("Show Intensity Ratios", true);
        showIRFormulasMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
        showIRFormulasMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                AbstractButton aButton = (AbstractButton) ae.getSource();
                boolean isSelected = aButton.getModel().isSelected();
                _intensityRatioFormulasJXCollapsiblePane.setCollapsed(!isSelected);
            }
        });

        viewMenu.add(showRegionsMenuItem);
        viewMenu.add(showIRFormulasMenuItem);

        menuBar.add(viewMenu);
    }

    @Override
    public void onDisplay()
    {
        _regionsAndOperatorsJXCollapsiblePane.refresh();
        _intensityRatioFormulasJXCollapsiblePane.refresh();
    }
}