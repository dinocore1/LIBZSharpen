package com.sciaps.view.tabs.common;

import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.IRRatio;
import com.sciaps.common.data.Region;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.utils.SwingUtils;
import com.sciaps.common.swing.view.JTextComponentHintLabel;
import com.sciaps.utils.RegionFinderUtils;
import com.sciaps.view.tabs.IntensityRatioFormulasPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import org.apache.commons.lang.math.DoubleRange;

/**
 *
 * @author sgowen
 */
public final class IntensityRatioFormulaContainerPanel extends JPanel
{
    public interface IntensityRatioFormulaContainerPanelCallback
    {
        void onIntensityRatioSaved();
    }

    private final IntensityRatioFormulaContainerPanelCallback _callback;
    private final JTextField _intensityRatioFormulaTextField;
    private final JComboBox _atomicElementComboBox;
    private final JTextField _numeratorTextField;
    private final JTextField _denominatorTextField;
    private final List<Region> _workingIRRatioNumerator;
    private final List<Region> _workingIRRatioDenominator;

    private int _numNumeratorOperators;
    private int _numDenominatorOperators;

    public IntensityRatioFormulaContainerPanel(IntensityRatioFormulaContainerPanelCallback callback)
    {
        _callback = callback;
        _workingIRRatioNumerator = new ArrayList();
        _workingIRRatioDenominator = new ArrayList();
        _numNumeratorOperators = 0;
        _numDenominatorOperators = 0;

        _intensityRatioFormulaTextField = new JTextField();
        _intensityRatioFormulaTextField.setHorizontalAlignment(SwingConstants.CENTER);
        JTextComponentHintLabel textComponentHintLabel = new JTextComponentHintLabel("Enter Intensity Ratio Formula Name", _intensityRatioFormulaTextField);
        textComponentHintLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel nameInputForm = new JPanel(new SpringLayout());
        nameInputForm.setOpaque(false);
        nameInputForm.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        nameInputForm.add(_intensityRatioFormulaTextField);
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
        _atomicElementComboBox = new JComboBox(elements);
        _atomicElementComboBox.setOpaque(false);
        atomicElementLabel.setLabelFor(_atomicElementComboBox);
        atomicElementForm.add(_atomicElementComboBox);
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
        _numeratorTextField = new JTextField();
        _numeratorTextField.setEditable(false);
        _numeratorTextField.setDragEnabled(true);
        _numeratorTextField.setFont(new Font("Serif", Font.BOLD, 24));
        _numeratorTextField.setDropTarget(new DropTarget()
        {
            @Override
            public synchronized void drop(DropTargetDropEvent evt)
            {
                processDropTarget(evt, _numeratorTextField, _workingIRRatioNumerator, true);
            }
        });
        inputForm.add(_numeratorTextField);
        JSeparator divider = new JSeparator();
        divider.setBackground(Color.black);
        inputForm.add(divider);
        _denominatorTextField = new JTextField();
        _denominatorTextField.setDragEnabled(true);
        _denominatorTextField.setFont(new Font("Serif", Font.BOLD, 24));
        _denominatorTextField.setEditable(false);
        _denominatorTextField.setDropTarget(new DropTarget()
        {
            @Override
            public synchronized void drop(DropTargetDropEvent evt)
            {
                processDropTarget(evt, _denominatorTextField, _workingIRRatioDenominator, false);
            }
        });
        inputForm.add(_denominatorTextField);
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
                _intensityRatioFormulaTextField.setText("");
                _atomicElementComboBox.setSelectedIndex(0);
                _numeratorTextField.setText("");
                _denominatorTextField.setText("");

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

                if (_intensityRatioFormulaTextField.getText().trim().length() == 0)
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
                    Region hardCodedOneRegion = RegionFinderUtils.findOneIntensityValueRegion();
                    if (hardCodedOneRegion == null)
                    {
                        hardCodedOneRegion = new Region();
                        hardCodedOneRegion.name = "ONE";
                        hardCodedOneRegion.wavelengthRange = new DoubleRange(0, 0);
                        hardCodedOneRegion.params.put("name", "com.sciaps.common.algorithms.OneIntensityValue");

                        LibzUnitManager.getInstance().getRegionsManager().addObject(hardCodedOneRegion);
                    }

                    _workingIRRatioDenominator.add(hardCodedOneRegion);
                }

                newIRRatio.name = _intensityRatioFormulaTextField.getText().trim();
                newIRRatio.element = AtomicElement.getElementBySymbol((String) _atomicElementComboBox.getSelectedItem());
                newIRRatio.numerator = new ArrayList<Region>();
                newIRRatio.numerator.addAll(_workingIRRatioNumerator);
                newIRRatio.denominator = new ArrayList<Region>();
                newIRRatio.denominator.addAll(_workingIRRatioDenominator);

                LibzUnitManager.getInstance().getIRRatiosManager().addObject(newIRRatio);

                _callback.onIntensityRatioSaved();
            }
        });

        buttonsPanel.add(clearButton, BorderLayout.WEST);
        buttonsPanel.add(submitButton, BorderLayout.EAST);

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        add(intensityRatioFormulaPanel);
        add(buttonsPanel);
    }

    public void editIntensityRatioFormula(IRRatio irRatioToEdit)
    {
        _intensityRatioFormulaTextField.setText(irRatioToEdit.name);
        _atomicElementComboBox.setSelectedIndex(irRatioToEdit.element.atomicNumber - 1);

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

        _numeratorTextField.setText(sb1.toString());
        _denominatorTextField.setText(sb2.toString());
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

                if (proceed)
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
                        if (LibzUnitManager.getInstance().getRegionsManager().getObjects().containsKey(regionId))
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
                                Region region = LibzUnitManager.getInstance().getRegionsManager().getObjects().get(regionId);
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