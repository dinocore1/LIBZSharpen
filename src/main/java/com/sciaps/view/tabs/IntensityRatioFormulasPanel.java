package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import com.sciaps.view.tabs.common.IntensityRatioFormulasTablePanel.IntensityRatioFormulasPanelCallback;
import com.sciaps.view.tabs.intensityratioformulas.IntensityRatioFormulasJXCollapsiblePane;
import com.sciaps.view.tabs.intensityratioformulas.RegionsAndOperatorsJXCollapsiblePane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
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
        intensityRatioFormulaBuilderPanel.setOpaque(false);
        intensityRatioFormulaBuilderPanel.setSize(300, 300);
        intensityRatioFormulaBuilderPanel.setMaximumSize(new Dimension(300, 300));
        intensityRatioFormulaBuilderPanel.setPreferredSize(new Dimension(300, 300));
        
        JPanel intensityRatioFormulaBuilderContainerPanel = new JPanel();
        intensityRatioFormulaBuilderContainerPanel.setLayout(new javax.swing.BoxLayout(intensityRatioFormulaBuilderContainerPanel, javax.swing.BoxLayout.Y_AXIS));
        JPanel emptyPanelTop = new JPanel();
        emptyPanelTop.setSize(emptyPanelTop.getWidth(), 300);
        JPanel emptyPanelBottom = new JPanel();
        emptyPanelBottom.setSize(emptyPanelTop.getWidth(), 300);
        intensityRatioFormulaBuilderContainerPanel.add(emptyPanelTop);
        intensityRatioFormulaBuilderContainerPanel.add(intensityRatioFormulaBuilderPanel);
        intensityRatioFormulaBuilderContainerPanel.add(emptyPanelBottom);

        add(_regionsAndOperatorsJXCollapsiblePane, BorderLayout.WEST);
        add(intensityRatioFormulaBuilderContainerPanel, BorderLayout.CENTER);
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