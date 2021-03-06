package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.IRCurve;
import com.sciaps.common.data.IRRatio;
import com.sciaps.common.data.Model;
import com.sciaps.common.data.Standard;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.utils.SwingUtils;
import com.sciaps.common.swing.view.ImmutableTable;
import com.sciaps.common.swing.view.JTextComponentHintLabel;
import com.sciaps.view.tabs.calibrationmodels.CalibrationModelsJXCollapsiblePane;
import com.sciaps.view.tabs.calibrationmodels.IntensityRatioFormulasAndStandardsJXCollapsiblePane;
import com.sciaps.view.tabs.common.CalibrationModelsTablePanel.CalibrationModelsPanelCallback;
import com.sciaps.view.tabs.common.DragDropZonePanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.swingx.JXCollapsiblePane;

/**
 *
 * @author sgowen
 */
public final class CalibrationModelsPanel extends AbstractTabPanel
{
    private static final String TAB_NAME = "Calibration Models";
    private static final String TOOL_TIP = "Create Calibration Models here";
    private static final String DROP_TYPE = "application/x-java-serialized-object; class=java.lang.String";

    private final IntensityRatioFormulasAndStandardsJXCollapsiblePane _intensityRatioFormulasAndStandardsJXCollapsiblePane;
    private final CalibrationModelsJXCollapsiblePane _calibrationModelsJXCollapsiblePane;
    private final Map<AtomicElement, IRCurve> _workingIRRatios;
    private final List<Standard> _workingStandards;
    private final JTable _intensityRatioFormulasTable;
    private final Vector _intensityRatioFormulasColumnNames;
    private final Vector _intensityRatioFormulasData;
    private final DefaultTableModel _intensityRatioFormulasTableModel;
    private final JTable _shortStandardsTable;
    private final Vector _shortStandardsColumnNames;
    private final Vector _shortStandardsData;
    private final DefaultTableModel _shortStandardsTableModel;

    private Model _currentlySelectedModel;

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

        JPanel irForm = new JPanel(new SpringLayout());
        irForm.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        irForm.setOpaque(false);
        JLabel irLabel = new JLabel("<html><div style=\"text-align: center;\">"
                + "Drag 'n Drop"
                + "<br>Intensity Ratios here"
                + "</div></html>", SwingConstants.CENTER);
        irLabel.setFont(new Font("Serif", Font.BOLD, 24));
        irLabel.setForeground(Color.BLACK);
        irLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        irForm.add(irLabel);
        SwingUtils.makeCompactGrid(irForm, 1, 1, 6, 6, 6, 6);

        JPanel irDeleteButtonForm = new JPanel(new SpringLayout());
        irDeleteButtonForm.setOpaque(false);
        JButton deleteIrButton = new JButton("Delete");
        deleteIrButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int selectedIrRatioIndex = _intensityRatioFormulasTable.getSelectedRow();
                String irName = (String) _intensityRatioFormulasTable.getModel().getValueAt(selectedIrRatioIndex, 0);
                String irElementSymbol = (String) _intensityRatioFormulasTable.getModel().getValueAt(selectedIrRatioIndex, 1);

                AtomicElement irCurveToRemoveKey = null;
                for (Map.Entry<AtomicElement, IRCurve> entry : _workingIRRatios.entrySet())
                {
                    IRCurve irCurve = entry.getValue();
                    if (irCurve.name.equals(irName) && irCurve.element.symbol.equals(irElementSymbol))
                    {
                        irCurveToRemoveKey = irCurve.element;
                        break;
                    }
                }

                if (irCurveToRemoveKey != null)
                {
                    _workingIRRatios.remove(irCurveToRemoveKey);
                }

                refreshWorkingCalModelData();
            }
        });
        irDeleteButtonForm.add(createFillerPanel());
        irDeleteButtonForm.add(createFillerPanel());
        irDeleteButtonForm.add(deleteIrButton);
        SwingUtils.makeCompactGrid(irDeleteButtonForm, 1, 3, 6, 6, 6, 6);

        _intensityRatioFormulasTable = new ImmutableTable();
        _intensityRatioFormulasTable.setFont(new Font("Serif", Font.BOLD, 18));
        _intensityRatioFormulasTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        _intensityRatioFormulasTable.setFillsViewportHeight(true);
        _intensityRatioFormulasTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        _intensityRatioFormulasColumnNames = new Vector();
        _intensityRatioFormulasColumnNames.add("Name");
        _intensityRatioFormulasColumnNames.add("Element");
        _intensityRatioFormulasData = new Vector();
        _intensityRatioFormulasTableModel = new DefaultTableModel();
        _intensityRatioFormulasTableModel.setDataVector(_intensityRatioFormulasData, _intensityRatioFormulasColumnNames);
        _intensityRatioFormulasTable.setModel(_intensityRatioFormulasTableModel);
        _intensityRatioFormulasTable.setDropTarget(new DropTarget()
        {
            @Override
            public synchronized void drop(DropTargetDropEvent evt)
            {
                try
                {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    for (DataFlavor df : evt.getTransferable().getTransferDataFlavors())
                    {
                        if (df.getMimeType().equals(DROP_TYPE))
                        {
                            String intensityRatioIdsString = (String) evt.getTransferable().getTransferData(df);
                            if (intensityRatioIdsString == null)
                            {
                                return;
                            }

                            String[] intensityRatioIds = intensityRatioIdsString.split(",");
                            if (intensityRatioIds == null)
                            {
                                return;
                            }

                            for (String intensityRatioId : intensityRatioIds)
                            {
                                if (!LibzUnitManager.getInstance().getIRRatiosManager().getObjects().containsKey(intensityRatioId))
                                {
                                    JOptionPane.showMessageDialog(new JFrame(), "Only drop Intensity Ratios here", "Attention", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            }

                            for (String intensityRatioId : intensityRatioIds)
                            {
                                IRRatio irRatio = LibzUnitManager.getInstance().getIRRatiosManager().getObjects().get(intensityRatioId);
                                IRCurve irCurve = new IRCurve();
                                irCurve.name = irRatio.name;
                                irCurve.element = irRatio.element;
                                irCurve.numerator = irRatio.numerator;
                                irCurve.denominator = irRatio.denominator;
                                _workingIRRatios.put(irRatio.element, irCurve);

                                refreshWorkingCalModelData();
                            }

                            break;
                        }
                    }
                }
                catch (Exception e)
                {
                    Logger.getLogger(CalibrationModelsPanel.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        });

        SwingUtils.enforceMinWidthForColumns(_intensityRatioFormulasTable, 40);

        JScrollPane intensityRatiosScrollPane = new JScrollPane(_intensityRatioFormulasTable);
        intensityRatiosScrollPane.setPreferredSize(new Dimension(intensityRatiosScrollPane.getPreferredSize().width, (int) ((float) mainFrame.getHeight() * 0.2f)));
        intensityRatiosScrollPane.setMaximumSize(new Dimension(intensityRatiosScrollPane.getPreferredSize().width, (int) ((float) mainFrame.getHeight() * 0.2f)));

        JPanel sForm = new JPanel(new SpringLayout());
        sForm.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        sForm.setOpaque(false);
        JLabel sLabel = new JLabel("<html><div style=\"text-align: center;\">"
                + "Drag 'n Drop"
                + "<br>Standards here"
                + "</div></html>", SwingConstants.CENTER);
        sLabel.setFont(new Font("Serif", Font.BOLD, 24));
        sLabel.setForeground(Color.BLACK);
        sLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sForm.add(sLabel);
        SwingUtils.makeCompactGrid(sForm, 1, 1, 6, 6, 6, 6);

        JPanel sDeleteButtonForm = new JPanel(new SpringLayout());
        sDeleteButtonForm.setOpaque(false);
        JButton deleteSButton = new JButton("Delete");
        deleteSButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int selectedStandardIndex = _shortStandardsTable.getSelectedRow();
                _workingStandards.remove(selectedStandardIndex);

                refreshWorkingCalModelData();
            }
        });
        sDeleteButtonForm.add(createFillerPanel());
        sDeleteButtonForm.add(createFillerPanel());
        sDeleteButtonForm.add(deleteSButton);
        SwingUtils.makeCompactGrid(sDeleteButtonForm, 1, 3, 6, 6, 6, 6);

        _shortStandardsTable = new ImmutableTable();
        _shortStandardsTable.setFont(new Font("Serif", Font.BOLD, 18));
        _shortStandardsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        _shortStandardsTable.setFillsViewportHeight(true);
        _shortStandardsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        _shortStandardsColumnNames = new Vector();
        _shortStandardsColumnNames.add("Name");
        _shortStandardsData = new Vector();
        _shortStandardsTableModel = new DefaultTableModel();
        _shortStandardsTableModel.setDataVector(_shortStandardsData, _shortStandardsColumnNames);
        _shortStandardsTable.setModel(_shortStandardsTableModel);
        _shortStandardsTable.setDropTarget(new DropTarget()
        {
            @Override
            public synchronized void drop(DropTargetDropEvent evt)
            {
                try
                {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    for (DataFlavor df : evt.getTransferable().getTransferDataFlavors())
                    {
                        if (df.getMimeType().equals(DROP_TYPE))
                        {
                            String standardIdsString = (String) evt.getTransferable().getTransferData(df);
                            if (standardIdsString == null)
                            {
                                return;
                            }

                            String[] standardIds = standardIdsString.split(",");
                            if (standardIds == null)
                            {
                                return;
                            }

                            for (String standardId : standardIds)
                            {
                                if (!LibzUnitManager.getInstance().getStandardsManager().getObjects().containsKey(standardId))
                                {
                                    JOptionPane.showMessageDialog(new JFrame(), "Only drop Standards here", "Attention", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            }

                            for (String standardId : standardIds)
                            {
                                Standard standard = LibzUnitManager.getInstance().getStandardsManager().getObjects().get(standardId);
                                if (!_workingStandards.contains(standard))
                                {
                                    _workingStandards.add(standard);

                                    refreshWorkingCalModelData();
                                }
                            }

                            break;
                        }
                    }
                }
                catch (Exception e)
                {
                    Logger.getLogger(CalibrationModelsPanel.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        });

        SwingUtils.enforceMinWidthForColumns(_shortStandardsTable, 40);

        JScrollPane standardsScrollPane = new JScrollPane(_shortStandardsTable);
        standardsScrollPane.setPreferredSize(new Dimension(intensityRatiosScrollPane.getPreferredSize().width, (int) ((float) mainFrame.getHeight() * 0.2f)));
        standardsScrollPane.setMaximumSize(new Dimension(intensityRatiosScrollPane.getPreferredSize().width, (int) ((float) mainFrame.getHeight() * 0.2f)));

        JPanel inputPanel = new JPanel();
        inputPanel.setOpaque(false);
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder());
        inputPanel.add(nameInputForm);
        inputPanel.add(irForm);
        inputPanel.add(irDeleteButtonForm);
        inputPanel.add(intensityRatiosScrollPane);
        inputPanel.add(sForm);
        inputPanel.add(sDeleteButtonForm);
        inputPanel.add(standardsScrollPane);

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
                _currentlySelectedModel = null;

                calibrationModelTextField.setText("");

                _workingIRRatios.clear();
                _workingStandards.clear();

                refreshWorkingCalModelData();
            }
        });
        clearButton.setBackground(Color.RED);
        clearButton.setContentAreaFilled(true);
        JButton submitButton = new JButton("Save");
        submitButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Model model;
                if (_currentlySelectedModel == null)
                {
                    model = new Model();
                }
                else
                {
                    model = _currentlySelectedModel;
                }

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

                model.name = calibrationModelTextField.getText().trim();
                model.irs = new HashMap();
                model.irs.putAll(_workingIRRatios);
                model.standardList = new ArrayList();
                model.standardList.addAll(_workingStandards);

                if (_currentlySelectedModel == null)
                {
                    LibzUnitManager.getInstance().getModelsManager().addObject(model);
                }
                else
                {
                    LibzUnitManager.getInstance().getModelsManager().markObjectAsModified(_currentlySelectedModel.mId);
                }

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
                Model model = LibzUnitManager.getInstance().getModelsManager().getObjects().get(calibrationModelId);
                if (model != null)
                {
                    _currentlySelectedModel = model;

                    calibrationModelTextField.setText(model.name);

                    _workingIRRatios.clear();
                    _workingIRRatios.putAll(model.irs);

                    _workingStandards.clear();
                    _workingStandards.addAll(model.standardList);

                    refreshWorkingCalModelData();
                }
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
    public String getToolTip()
    {
        return TOOL_TIP;
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

    private void refreshWorkingCalModelData()
    {
        _intensityRatioFormulasData.clear();

        for (Map.Entry<AtomicElement, IRCurve> entry : _workingIRRatios.entrySet())
        {
            Vector row = new Vector();

            IRCurve irCurve = entry.getValue();
            row.add(irCurve.name);
            row.add(irCurve.element.symbol);

            _intensityRatioFormulasData.add(row);
        }

        SwingUtils.fitTableToColumns(_intensityRatioFormulasTable);

        _shortStandardsData.clear();

        for (Standard standard : _workingStandards)
        {
            Vector row = new Vector();

            row.add(standard.name);

            _shortStandardsData.add(row);
        }

        _shortStandardsTableModel.setDataVector(_shortStandardsData, _shortStandardsColumnNames);
        _shortStandardsTable.setModel(_shortStandardsTableModel);

        SwingUtils.fitTableToColumns(_shortStandardsTable);

        _mainFrame.refreshUI();
    }

    private JPanel createFillerPanel()
    {
        JPanel fillerPanel = new JPanel();
        fillerPanel.setOpaque(false);

        return fillerPanel;
    }
}