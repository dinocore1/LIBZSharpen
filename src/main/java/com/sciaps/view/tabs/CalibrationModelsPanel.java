package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import com.sciaps.view.tabs.calibrationmodels.CalibrationModelsJXCollapsiblePane;
import com.sciaps.view.tabs.calibrationmodels.IntensityRatioFormulasAndStandardsJXCollapsiblePane;
import com.sciaps.view.tabs.common.DragDropZonePanel;
import com.sciaps.view.tabs.common.IntensityRatioFormulasTablePanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
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

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Top
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        centerPanel.add(new JPanel(), gbc);

        // Left
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        centerPanel.add(new JPanel(), gbc);

        // Center
        gbc.gridy = 1;
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        DragDropZonePanel panel = new DragDropZonePanel();
        panel.setPreferredSize(new Dimension(panel.getPreferredSize().width, 600));
        centerPanel.add(panel, gbc);

        // Right
        gbc.gridy = 1;
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        centerPanel.add(new JPanel(), gbc);

        // Bottom
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        centerPanel.add(new JPanel(), gbc);

        setLayout(new BorderLayout());

        add(_intensityRatioFormulasAndStandardsJXCollapsiblePane, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(_calibrationModelsJXCollapsiblePane, BorderLayout.EAST);
    }

    @Override
    public String getTabName()
    {
        return TAB_NAME;
    }

    @Override
    public void customizeMenuBar(JMenuBar menuBar)
    {
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);

        final JMenuItem showIRFormulasMenuItem = new JCheckBoxMenuItem("Show Intensity Ratios and Standards", true);
        showIRFormulasMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
        showIRFormulasMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                AbstractButton aButton = (AbstractButton) ae.getSource();
                boolean isSelected = aButton.getModel().isSelected();
                _intensityRatioFormulasAndStandardsJXCollapsiblePane.setCollapsed(!isSelected);
            }
        });

        final JMenuItem showCalibrationModelsMenuItem = new JCheckBoxMenuItem("Show Calibration Models", true);
        showCalibrationModelsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
        showCalibrationModelsMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                AbstractButton aButton = (AbstractButton) ae.getSource();
                boolean isSelected = aButton.getModel().isSelected();
                _calibrationModelsJXCollapsiblePane.setCollapsed(!isSelected);
            }
        });

        viewMenu.add(showIRFormulasMenuItem);
        viewMenu.add(showCalibrationModelsMenuItem);

        menuBar.add(viewMenu);
    }

    @Override
    public void onDisplay()
    {
        _intensityRatioFormulasAndStandardsJXCollapsiblePane.refresh();
        _calibrationModelsJXCollapsiblePane.refresh();
    }
}