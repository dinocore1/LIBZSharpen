package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.IRRatio;
import com.sciaps.common.data.Region;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.utils.RegexUtil;
import com.sciaps.common.swing.utils.SwingUtils;
import com.sciaps.common.swing.view.JTextComponentHintLabel;
import com.sciaps.global.MainFrameListener;
import com.sciaps.global.MainFrameListener.MainFrameListenerCallback;
import com.sciaps.view.tabs.common.IntensityRatioFormulasTablePanel.IntensityRatioFormulasPanelCallback;
import com.sciaps.view.tabs.intensityratioformulas.IntensityRatioFormulasJXCollapsiblePane;
import com.sciaps.view.tabs.intensityratioformulas.RegionsAndOperatorsJXCollapsiblePane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
    private Dimension _mainFrameSize;
    private IRRatio _workingIRRatio;

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

        _mainFrameSize = new Dimension(_mainFrame.getWidth(), _mainFrame.getHeight());
        initUI();

        MainFrameListener.getInstance().addMainFrameListenerCallback(new MainFrameListenerCallback()
        {
            @Override
            public void onMainFrameResized(int width, int height)
            {
                _mainFrameSize = new Dimension(width, height);
                initUI();
            }
        });

        resetWorkingIRRatio();
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

    private void initUI()
    {
        JPanel intensityRatioFormulaBuilderPanel = new JPanel()
        {
            @Override
            protected void paintComponent(Graphics g)
            {
                super.paintComponent(g);

                Dimension arcs = new Dimension(15, 15);
                int width = getWidth();
                int height = getHeight();
                Graphics2D graphics = (Graphics2D) g;
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // paint background
                graphics.setColor(new Color(114, 187, 83, 255));
                graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);

                // paint border
                graphics.setColor(new Color(0, 0, 0, 0));
                graphics.drawRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
            }
        };

        int intensityRatioFormulasPanelWidth = (int) ((float) _mainFrameSize.width * 0.2f);
        int intensityRatioFormulasPanelHeight = (int) ((float) _mainFrameSize.height * 0.3f);
        intensityRatioFormulaBuilderPanel.setOpaque(false);
        intensityRatioFormulaBuilderPanel.setSize(intensityRatioFormulasPanelWidth, intensityRatioFormulasPanelHeight);
        intensityRatioFormulaBuilderPanel.setMaximumSize(new Dimension(intensityRatioFormulasPanelWidth, intensityRatioFormulasPanelHeight));
        intensityRatioFormulaBuilderPanel.setPreferredSize(new Dimension(intensityRatioFormulasPanelWidth, intensityRatioFormulasPanelHeight));

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
                try
                {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    if (evt.getTransferable().getTransferDataFlavors().length == 1)
                    {
                        numeratorTextField.setText(numeratorTextField.getText() + "+ ");
                    }
                    else
                    {
                        for (DataFlavor df : evt.getTransferable().getTransferDataFlavors())
                        {
                            if (df.getMimeType().equals("application/x-java-jvm-local-objectref; class=java.lang.String"))
                            {
                                String rowData = (String) evt.getTransferable().getTransferData(df);
                                String regionName = RegexUtil.findValue(rowData, "(.*?)[\\s\\t]", 1);
                                for (Map.Entry<String, Region> entry : LibzUnitManager.getInstance().getRegions().entrySet())
                                {
                                    Region region = entry.getValue();
                                    if (region.name.equals(regionName))
                                    {
                                        numeratorTextField.setText(numeratorTextField.getText() + "[" + region.name + "] ");
                                        _workingIRRatio.numerator.add(region);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                catch (UnsupportedFlavorException ex)
                {
                    // TODO, handle
                }
                catch (IOException ex)
                {
                    // TODO, handle
                }
            }
        });
        inputForm.add(numeratorTextField);
        JSeparator divider = new JSeparator();
        divider.getInsets().set(0, 40, 0, 40);
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
                try
                {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    if (evt.getTransferable().getTransferDataFlavors().length == 1)
                    {
                        denominatorTextField.setText(denominatorTextField.getText() + "+ ");
                    }
                    else
                    {
                        for (DataFlavor df : evt.getTransferable().getTransferDataFlavors())
                        {
                            if (df.getMimeType().equals("application/x-java-jvm-local-objectref; class=java.lang.String"))
                            {
                                String rowData = (String) evt.getTransferable().getTransferData(df);
                                String regionName = RegexUtil.findValue(rowData, "(.*?)[\\s\\t]", 1);
                                for (Map.Entry<String, Region> entry : LibzUnitManager.getInstance().getRegions().entrySet())
                                {
                                    Region region = entry.getValue();
                                    if (region.name.equals(regionName))
                                    {
                                        denominatorTextField.setText(denominatorTextField.getText() + "[" + region.name + "] ");
                                        _workingIRRatio.denominator.add(region);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                catch (UnsupportedFlavorException ex)
                {
                    // TODO, handle
                }
                catch (IOException ex)
                {
                    // TODO, handle
                }
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
        intensityRatioFormulaBuilderPanel.add(inputPanel);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BorderLayout());
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                resetWorkingIRRatio();

                numeratorTextField.setText("");
                denominatorTextField.setText("");
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

                // TODO, pull name and element from _workingIRRatio
                newIRRatio.name = intensityRatioFormulaTextField.getText().trim();
                newIRRatio.element = AtomicElement.getElementBySymbol((String) atomicElementComboBox.getSelectedItem());
                newIRRatio.numerator = new ArrayList<Region>();
                newIRRatio.numerator.addAll(_workingIRRatio.numerator);
                newIRRatio.denominator = new ArrayList<Region>();
                newIRRatio.denominator.addAll(_workingIRRatio.denominator);

                LibzUnitManager.getInstance().getIntensityRatios().put(java.util.UUID.randomUUID().toString(), newIRRatio);

                _intensityRatioFormulasJXCollapsiblePane.refresh();
            }
        });
        buttonsPanel.add(clearButton, BorderLayout.WEST);
        buttonsPanel.add(submitButton, BorderLayout.EAST);
        buttonsPanel.setMaximumSize(new Dimension(intensityRatioFormulasPanelWidth, clearButton.getPreferredSize().height));
        buttonsPanel.setPreferredSize(new Dimension(intensityRatioFormulasPanelWidth, clearButton.getPreferredSize().height));

        JPanel intensityRatioFormulaBuilderContainerPanel = new JPanel();
        intensityRatioFormulaBuilderContainerPanel.setLayout(new javax.swing.BoxLayout(intensityRatioFormulaBuilderContainerPanel, javax.swing.BoxLayout.Y_AXIS));
        JPanel emptyPanelTop = new JPanel();
        emptyPanelTop.setSize(emptyPanelTop.getWidth(), intensityRatioFormulasPanelWidth / 2);
        JPanel emptyPanelBottom = new JPanel();
        emptyPanelBottom.setSize(emptyPanelBottom.getWidth(), intensityRatioFormulasPanelWidth / 2);

        intensityRatioFormulaBuilderContainerPanel.removeAll();
        intensityRatioFormulaBuilderContainerPanel.add(emptyPanelTop);
        intensityRatioFormulaBuilderContainerPanel.add(intensityRatioFormulaBuilderPanel);
        intensityRatioFormulaBuilderContainerPanel.add(buttonsPanel);
        intensityRatioFormulaBuilderContainerPanel.add(emptyPanelBottom);

        removeAll();
        add(_regionsAndOperatorsJXCollapsiblePane, BorderLayout.WEST);
        add(intensityRatioFormulaBuilderContainerPanel, BorderLayout.CENTER);
        add(_intensityRatioFormulasJXCollapsiblePane, BorderLayout.EAST);
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

    private void resetWorkingIRRatio()
    {
        _workingIRRatio = new IRRatio();
        _workingIRRatio.name = "";
        _workingIRRatio.element = AtomicElement.Hydrogen;
        _workingIRRatio.numerator = new ArrayList<Region>();
        _workingIRRatio.denominator = new ArrayList<Region>();
    }
}