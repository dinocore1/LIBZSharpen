package com.sciaps.components;

import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.Region;
import com.sciaps.uimodel.ElementComboBoxModel;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

public class RegionBox extends JPanel {


    private JSplitPane mSplitPane;
    private ChartPanel mChartPanel;
    private XYSeriesCollection mSpectrumDataSet;
    private JTextField mNameTextField;
    private SpinnerNumberModel mMinModel;
    private SpinnerNumberModel mMaxModel;
    private JComboBox<AtomicElement> mElementComboBox;
    private ElementComboBoxModel mElementModel;
    private DefaultListModel<Region> mRegionModel;

    public RegionBox() {
        setLayout(new BorderLayout());

        mSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftPanel(), createChartPanel());
        mSplitPane.setOneTouchExpandable(true);
        mSplitPane.setContinuousLayout(true);

        add(mSplitPane, BorderLayout.CENTER);

    }

    private JPanel createChartPanel() {

        JPanel centerPanel = new JPanel(new MigLayout("fill"));

        JLabel label = new JLabel("Name: ");
        centerPanel.add(label, "");

        mNameTextField = new JTextField();
        centerPanel.add(mNameTextField, "growx");

        label = new JLabel("Nm Range: ");
        centerPanel.add(label, "");

        mMinModel = new SpinnerNumberModel(new Double(0), new Double(0), new Double(1000), new Double(0.05));
        JSpinner spinner = new JSpinner(mMinModel);
        add(spinner, "");

        label = new JLabel("-");
        add(label, "");

        mMaxModel = new SpinnerNumberModel(new Double(0), new Double(0), new Double(1000), new Double(0.05));
        spinner = new JSpinner(mMaxModel);
        add(spinner, "wrap");


        JFreeChart chart = ChartFactory.createXYLineChart("", "Wavelength", "Intensity", null);

        XYPlot plot = chart.getXYPlot();

        plot.setBackgroundPaint(Color.DARK_GRAY);

        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.BLACK);

        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.BLACK);

        {
            //curve renderer
            XYSplineRenderer renderer = new XYSplineRenderer();
            renderer.setShapesVisible(false);

            plot.setRenderer(0, renderer);

            mSpectrumDataSet = new XYSeriesCollection();
            plot.setDataset(0, mSpectrumDataSet);
        }

        mChartPanel = new ChartPanel(chart);
        mChartPanel.setMouseWheelEnabled(true);

        centerPanel.add(mChartPanel, "span, grow");

        return centerPanel;
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new MigLayout());

        JLabel label = new JLabel("Element");
        panel.add(label, "wrap");

        mElementComboBox = new JComboBox<AtomicElement>(new ElementComboBoxModel());
        panel.add(mElementComboBox, "growx");

        mRegionModel = new DefaultListModel<Region>();
        JList<Region> regionList = new JList<Region>(mRegionModel);
        regionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(regionList);
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true), "Regions"));

        panel.add(scrollPane, "grow, wrap");




        return panel;
    }
}
