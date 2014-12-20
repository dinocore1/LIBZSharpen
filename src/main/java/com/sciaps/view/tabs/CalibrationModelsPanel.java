package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.IRCurve;
import com.sciaps.common.data.Model;
import com.sciaps.common.data.Standard;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.utils.SwingUtils;
import com.sciaps.common.swing.view.JTextComponentHintLabel;
import com.sciaps.view.tabs.calibrationmodels.CalibrationModelsJXCollapsiblePane;
import com.sciaps.view.tabs.calibrationmodels.IntensityRatioFormulasAndStandardsJXCollapsiblePane;
import com.sciaps.view.tabs.common.CalibrationModelsTablePanel.CalibrationModelsPanelCallback;
import com.sciaps.view.tabs.common.DragDropZonePanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
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
    private final Map<AtomicElement, IRCurve> _workingIRRatios;
    private final List<Standard> _workingStandards;

    public CalibrationModelsPanel(MainFrame mainFrame)
    {
        super(mainFrame);

        _workingIRRatios = new HashMap();
        _workingStandards = new ArrayList();

        final JTextField calibrationModelTextField = new JTextField();
        calibrationModelTextField.setHorizontalAlignment(SwingConstants.CENTER);
        JTextComponentHintLabel textComponentHintLabel = new JTextComponentHintLabel("Enter Calibration Model Name", calibrationModelTextField);
        textComponentHintLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel nameInputForm = new JPanel(new SpringLayout());
        nameInputForm.setOpaque(false);
        nameInputForm.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        nameInputForm.add(calibrationModelTextField);
        SwingUtils.makeCompactGrid(nameInputForm, 1, 1, 6, 6, 6, 6);

        JPanel inputForm = new JPanel(new SpringLayout());
        inputForm.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        inputForm.setOpaque(false);
        JLabel instructionsLabel = new JLabel("<html><div style=\"text-align: center;\">"
                + "Drag 'n Drop"
                + "<br>intensity ratio formulas here"
                + "</div></html>", SwingConstants.CENTER);
        instructionsLabel.setFont(new Font("Serif", Font.BOLD, 24));
        instructionsLabel.setForeground(Color.BLACK);
        instructionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputForm.add(instructionsLabel);
        SwingUtils.makeCompactGrid(inputForm, 1, 1, 6, 6, 6, 6);

        JPanel inputPanel = new JPanel();
        inputPanel.setOpaque(false);
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder());
        inputPanel.add(nameInputForm);
        inputPanel.add(inputForm);

        JPanel calibrationModelPanel = new DragDropZonePanel();
        calibrationModelPanel.add(inputPanel);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BorderLayout());
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                calibrationModelTextField.setText("");

                _workingIRRatios.clear();
                _workingStandards.clear();
            }
        });
        clearButton.setBackground(Color.RED);
        clearButton.setContentAreaFilled(true);
        JButton submitButton = new JButton("Add");
        submitButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Model newModel = new Model();

                if (calibrationModelTextField.getText().trim().length() == 0)
                {
                    JOptionPane.showMessageDialog(new JFrame(), "Please enter a name for the Calibration Model first", "Attention", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (_workingIRRatios.isEmpty())
                {
                    JOptionPane.showMessageDialog(new JFrame(), "Please drag and drop at least 1 Intensity Ratio", "Attention", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (_workingStandards.isEmpty())
                {
                    JOptionPane.showMessageDialog(new JFrame(), "Please drag and drop at least 1 Standard", "Attention", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                newModel.name = calibrationModelTextField.getText().trim();
                newModel.irs = new HashMap();
                newModel.irs.putAll(_workingIRRatios);
                newModel.standardList = new ArrayList();
                newModel.standardList.addAll(_workingStandards);

                LibzUnitManager.getInstance().getCalibrationModels().put(java.util.UUID.randomUUID().toString(), newModel);

                _calibrationModelsJXCollapsiblePane.refresh();
            }
        });

        buttonsPanel.add(clearButton, BorderLayout.WEST);
        buttonsPanel.add(submitButton, BorderLayout.EAST);

        JPanel calibrationModelContainerPanel = new JPanel();
        calibrationModelContainerPanel.setLayout(new javax.swing.BoxLayout(calibrationModelContainerPanel, javax.swing.BoxLayout.Y_AXIS));

        calibrationModelContainerPanel.add(calibrationModelPanel);
        calibrationModelContainerPanel.add(buttonsPanel);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridheight = 1;
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
        gbc.weightx = 0;
        gbc.weighty = 0;
        centerPanel.add(calibrationModelContainerPanel, gbc);

        // Right
        gbc.gridy = 1;
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        centerPanel.add(new JPanel(), gbc);

        // Bottom
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        centerPanel.add(new JPanel(), gbc);

        _intensityRatioFormulasAndStandardsJXCollapsiblePane = new IntensityRatioFormulasAndStandardsJXCollapsiblePane(mainFrame, JXCollapsiblePane.Direction.RIGHT, null);
        _intensityRatioFormulasAndStandardsJXCollapsiblePane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl I"), JXCollapsiblePane.TOGGLE_ACTION);
        _intensityRatioFormulasAndStandardsJXCollapsiblePane.setCollapsed(false);

        _calibrationModelsJXCollapsiblePane = new CalibrationModelsJXCollapsiblePane(JXCollapsiblePane.Direction.LEFT, new CalibrationModelsPanelCallback()
        {
            @Override
            public void onCalibrationModelSelected(String calibrationModelId)
            {
                // TODO
            }
        });
        _calibrationModelsJXCollapsiblePane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl C"), JXCollapsiblePane.TOGGLE_ACTION);
        _calibrationModelsJXCollapsiblePane.setCollapsed(false);

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