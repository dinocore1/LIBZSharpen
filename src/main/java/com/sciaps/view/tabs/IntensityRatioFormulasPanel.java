package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.IRRatio;
import com.sciaps.common.data.Region;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.utils.SwingUtils;
import com.sciaps.common.swing.view.JTextComponentHintLabel;
import com.sciaps.view.tabs.common.DragDropZonePanel;
import com.sciaps.view.tabs.common.IntensityRatioFormulasTablePanel.IntensityRatioFormulasPanelCallback;
import com.sciaps.view.tabs.intensityratioformulas.IntensityRatioFormulasJXCollapsiblePane;
import com.sciaps.view.tabs.intensityratioformulas.RegionsAndOperatorsJXCollapsiblePane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
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
    private final List<Region> _workingIRRatioNumerator;
    private final List<Region> _workingIRRatioDenominator;
    private int _numNumeratorOperators;
    private int _numDenominatorOperators;

    public IntensityRatioFormulasPanel(MainFrame mainFrame)
    {
        super(mainFrame);

        _workingIRRatioNumerator = new ArrayList();
        _workingIRRatioDenominator = new ArrayList();
        _numNumeratorOperators = 0;
        _numDenominatorOperators = 0;

        final JTextField intensityRatioFormulaTextField = new JTextField();
        intensityRatioFormulaTextField.setHorizontalAlignment(SwingConstants.CENTER);
        JTextComponentHintLabel textComponentHintLabel = new JTextComponentHintLabel("Enter Intensity Ratio Formula Name", intensityRatioFormulaTextField);
        textComponentHintLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel nameInputForm = new JPanel(new SpringLayout());
        nameInputForm.setOpaque(false);
        nameInputForm.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        nameInputForm.add(intensityRatioFormulaTextField);
        SwingUtils.makeCompactGrid(nameInputForm, 1, 1, 6, 6, 6, 6);

        JPanel atomicElementForm = new JPanel(new SpringLayout());
        atomicElementForm.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        atomicElementForm.setOpaque(false);
        JLabel atomicElementLabel = new JLabel("Atomic Element:", SwingConstants.TRAILING);
        atomicElementLabel.setForeground(Color.BLACK);
        atomicElementLabel.setOpaque(false);
        JPanel emptyPanel = new JPanel();
        emptyPanel.setOpaque(false);
        atomicElementForm.add(emptyPanel);
        atomicElementForm.add(atomicElementLabel);
        String[] elements = getArrayOfAtomicElements();
        final JComboBox atomicElementComboBox = new JComboBox(elements);
        atomicElementComboBox.setOpaque(false);
        atomicElementLabel.setLabelFor(atomicElementComboBox);
        atomicElementForm.add(atomicElementComboBox);
        SwingUtils.makeCompactGrid(atomicElementForm, 1, 3, 6, 6, 6, 6);

        JPanel inputForm = new JPanel(new SpringLayout());
        inputForm.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        inputForm.setOpaque(false);
        JLabel instructionsLabel = new JLabel("<html><div style=\"text-align: center;\">Drag 'n Drop"
                + "<br>regions and operators"
                + "<br>to create an intensity ratio formula."
                + "</div></html>", SwingConstants.CENTER);
        instructionsLabel.setFont(new Font("Serif", Font.BOLD, 24));
        instructionsLabel.setForeground(Color.BLACK);
        instructionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputForm.add(instructionsLabel);
        final JTextField numeratorTextField = new JTextField();
        numeratorTextField.setEditable(false);
        numeratorTextField.setDragEnabled(true);
        numeratorTextField.setFont(new Font("Serif", Font.BOLD, 24));
        numeratorTextField.setDropTarget(new DropTarget()
        {
            @Override
            public synchronized void drop(DropTargetDropEvent evt)
            {
                processDropTarget(evt, numeratorTextField, _workingIRRatioNumerator, true);
            }
        });
        inputForm.add(numeratorTextField);
        JSeparator divider = new JSeparator();
        divider.setBackground(Color.black);
        inputForm.add(divider);
        final JTextField denominatorTextField = new JTextField();
        denominatorTextField.setDragEnabled(true);
        denominatorTextField.setFont(new Font("Serif", Font.BOLD, 24));
        denominatorTextField.setEditable(false);
        denominatorTextField.setDropTarget(new DropTarget()
        {
            @Override
            public synchronized void drop(DropTargetDropEvent evt)
            {
                processDropTarget(evt, denominatorTextField, _workingIRRatioDenominator, false);
            }
        });
        inputForm.add(denominatorTextField);
        SwingUtils.makeCompactGrid(inputForm, 4, 1, 6, 6, 6, 6);

        JPanel inputPanel = new JPanel();
        inputPanel.setOpaque(false);
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder());
        inputPanel.add(nameInputForm);
        inputPanel.add(atomicElementForm);
        inputPanel.add(inputForm);

        JPanel intensityRatioFormulaPanel = new DragDropZonePanel();
        intensityRatioFormulaPanel.add(inputPanel);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BorderLayout());
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                intensityRatioFormulaTextField.setText("");
                atomicElementComboBox.setSelectedIndex(0);
                numeratorTextField.setText("");
                denominatorTextField.setText("");

                _workingIRRatioNumerator.clear();
                _workingIRRatioDenominator.clear();
                _numNumeratorOperators = 0;
                _numDenominatorOperators = 0;
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
                IRRatio newIRRatio = new IRRatio();

                if (intensityRatioFormulaTextField.getText().trim().length() == 0)
                {
                    JOptionPane.showMessageDialog(new JFrame(), "Please enter a name for the Intensity Ratio Formula first", "Attention", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (_workingIRRatioNumerator.isEmpty())
                {
                    JOptionPane.showMessageDialog(new JFrame(), "Please drag and drop at least 1 region to the numerator field", "Attention", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (_workingIRRatioDenominator.isEmpty())
                {
                    JOptionPane.showMessageDialog(new JFrame(), "Please drag and drop at least 1 region to the denominator field", "Attention", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                newIRRatio.name = intensityRatioFormulaTextField.getText().trim();
                newIRRatio.element = AtomicElement.getElementBySymbol((String) atomicElementComboBox.getSelectedItem());
                newIRRatio.numerator = new ArrayList<Region>();
                newIRRatio.numerator.addAll(_workingIRRatioNumerator);
                newIRRatio.denominator = new ArrayList<Region>();
                newIRRatio.denominator.addAll(_workingIRRatioDenominator);

                LibzUnitManager.getInstance().getIntensityRatios().put(java.util.UUID.randomUUID().toString(), newIRRatio);

                _intensityRatioFormulasJXCollapsiblePane.refresh();
            }
        });

        buttonsPanel.add(clearButton, BorderLayout.WEST);
        buttonsPanel.add(submitButton, BorderLayout.EAST);

        JPanel intensityRatioFormulaContainerPanel = new JPanel();
        intensityRatioFormulaContainerPanel.setLayout(new javax.swing.BoxLayout(intensityRatioFormulaContainerPanel, javax.swing.BoxLayout.Y_AXIS));

        intensityRatioFormulaContainerPanel.add(intensityRatioFormulaPanel);
        intensityRatioFormulaContainerPanel.add(buttonsPanel);

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
        centerPanel.add(intensityRatioFormulaContainerPanel, gbc);

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

        _intensityRatioFormulasJXCollapsiblePane = new IntensityRatioFormulasJXCollapsiblePane(JXCollapsiblePane.Direction.LEFT, new IntensityRatioFormulasPanelCallback()
        {
            @Override
            public void editIntensityRatioFormula(String intensityRatioFormulaId)
            {
                IRRatio irRatioToEdit = LibzUnitManager.getInstance().getIntensityRatios().get(intensityRatioFormulaId);
                if (irRatioToEdit != null)
                {
                    intensityRatioFormulaTextField.setText(irRatioToEdit.name);
                    atomicElementComboBox.setSelectedIndex(irRatioToEdit.element.atomicNumber - 1);
                    
                    _workingIRRatioNumerator.clear();
                    _workingIRRatioNumerator.addAll(irRatioToEdit.numerator);
                    _workingIRRatioDenominator.clear();
                    _workingIRRatioDenominator.addAll(irRatioToEdit.denominator);
                    
                    _numNumeratorOperators = _workingIRRatioNumerator.size();
                    _numDenominatorOperators = _workingIRRatioDenominator.size();

                    StringBuilder sb1 = new StringBuilder();
                    for (Region r : _workingIRRatioNumerator)
                    {
                        sb1.append('[');
                        sb1.append(r.name);
                        sb1.append(']');

                        if (r != _workingIRRatioNumerator.get(_workingIRRatioNumerator.size() - 1))
                        {
                            sb1.append(" + ");
                        }
                    }

                    StringBuilder sb2 = new StringBuilder();
                    for (Region r : _workingIRRatioDenominator)
                    {
                        sb2.append('[');
                        sb2.append(r.name);
                        sb2.append(']');

                        if (r != _workingIRRatioDenominator.get(_workingIRRatioDenominator.size() - 1))
                        {
                            sb2.append(" + ");
                        }
                    }
                    
                    numeratorTextField.setText(sb1.toString());
                    denominatorTextField.setText(sb2.toString());
                }
            }
        });
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

    private void processDropTarget(DropTargetDropEvent evt, JTextField dropTarget, List<Region> regionsList, boolean isNumerator)
    {
        try
        {
            evt.acceptDrop(DnDConstants.ACTION_COPY);
            if (evt.getTransferable().getTransferDataFlavors().length == 1)
            {
                boolean proceed = false;

                if (isNumerator && _numNumeratorOperators < regionsList.size())
                {
                    proceed = true;
                    _numNumeratorOperators++;
                }
                else if (!isNumerator && _numDenominatorOperators < regionsList.size())
                {
                    proceed = true;
                    _numDenominatorOperators++;
                }
                
                if(proceed)
                {
                    dropTarget.setText(dropTarget.getText() + "+ ");
                }
            }
            else
            {
                for (DataFlavor df : evt.getTransferable().getTransferDataFlavors())
                {
                    if (df.getMimeType().equals("application/x-java-serialized-object; class=java.lang.String"))
                    {
                        String regionId = (String) evt.getTransferable().getTransferData(df);
                        if (LibzUnitManager.getInstance().getRegions().containsKey(regionId))
                        {
                            boolean proceed = false;

                            if (isNumerator && _numNumeratorOperators == regionsList.size())
                            {
                                proceed = true;
                            }
                            else if (!isNumerator && _numDenominatorOperators == regionsList.size())
                            {
                                proceed = true;
                            }

                            if (proceed)
                            {
                                Region region = LibzUnitManager.getInstance().getRegions().get(regionId);
                                dropTarget.setText(dropTarget.getText() + "[" + region.name + "] ");
                                regionsList.add(region);
                            }

                            break;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            Logger.getLogger(IntensityRatioFormulasPanel.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private String[] getArrayOfAtomicElements()
    {
        List<String> elements = new ArrayList<String>();
        for (int i = 1; i <= LibzUnitManager.NUM_ATOMIC_ELEMENTS; i++)
        {
            AtomicElement ae = AtomicElement.getElementByAtomicNum(i);
            elements.add(ae.symbol);
        }

        String[] elementsArray = new String[elements.size()];
        elementsArray = elements.toArray(elementsArray);

        return elementsArray;
    }
}