package com.sciaps.components;

import com.google.inject.Inject;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.Region;
import com.sciaps.common.data.Standard;
import com.sciaps.common.objtracker.DBObjTracker;
import com.sciaps.common.swing.utils.TableColumnAdjuster;
import com.sciaps.uimodel.ElementComboBoxModel;
import com.sciaps.uimodel.RegionWrapper;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class RegionSpectrumView extends JPanel {


    private JSplitPane mSplitPane;
    private ChartPanel mChartPanel;
    private XYSeriesCollection mSpectrumDataSet;
    private JTextField mNameTextField;
    private SpinnerNumberModel mMinModel;
    private SpinnerNumberModel mMaxModel;
    private JComboBox<AtomicElement> mElementComboBox;
    private ElementComboBoxModel mElementModel;
    private DefaultListModel<RegionWrapper> mRegionModel;
    private StandardTableModel mStandardsModel;
    private JList<RegionWrapper> mRegionList;

    @Inject
    DBObjTracker mObjTracker;


    public RegionSpectrumView() {
        setLayout(new BorderLayout());

        mSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftPanel(), createChartPanel());
        mSplitPane.setOneTouchExpandable(true);
        mSplitPane.setContinuousLayout(true);

        add(mSplitPane, BorderLayout.CENTER);

    }

    private JPanel createChartPanel() {

        JPanel centerPanel = new JPanel(new MigLayout("fill", "", "[][grow]"));

        JLabel label = new JLabel("Name: ");
        centerPanel.add(label, "split");

        mNameTextField = new JTextField();
        centerPanel.add(mNameTextField, "growx");

        label = new JLabel("Nm Range: ");
        centerPanel.add(label, "gapx 5mm, split");

        mMinModel = new SpinnerNumberModel(new Double(0), new Double(0), new Double(1000), new Double(0.05));
        JSpinner spinner = new JSpinner(mMinModel);
        centerPanel.add(spinner, "");

        label = new JLabel("-");
        centerPanel.add(label, "");

        mMaxModel = new SpinnerNumberModel(new Double(0), new Double(0), new Double(1000), new Double(0.05));
        spinner = new JSpinner(mMaxModel);
        centerPanel.add(spinner, "wrap");


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
        mElementComboBox.addActionListener(mOnElementSelected);
        panel.add(mElementComboBox, "growx, wrap");

        mRegionModel = new DefaultListModel<RegionWrapper>();
        mRegionList = new JList<RegionWrapper>(mRegionModel);
        mRegionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mRegionList.addListSelectionListener(mOnRegionSelected);

        JScrollPane scrollPane = new JScrollPane(mRegionList);
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true), "Regions"));

        panel.add(scrollPane, "h 100::, grow, wrap");

        mStandardsModel = new StandardTableModel();
        JTable table = new JTable(mStandardsModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        TableColumnAdjuster tca = new TableColumnAdjuster(table);
        tca.adjustColumns();

        scrollPane = new JScrollPane(table);
        panel.add(scrollPane, "grow, wrap");


        return panel;
    }

    private class StandardTableModel extends AbstractTableModel {

        final ArrayList<Standard> standards = new ArrayList<Standard>();
        final ArrayList<Boolean> enabled = new ArrayList<Boolean>();

        @Override
        public int getRowCount() {
            return standards.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int row, int column) {
            switch(column) {
                case 0:
                    return enabled.get(row);

                case 1:
                    return standards.get(row).name;

                default:
                    return null;
            }
        }

        @Override
        public String getColumnName(int column) {
            switch(column){
                case 0:
                    return "Show";
                case 1:
                    return "Standard";

                default:
                    return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Boolean.class;

                case 1:
                    return String.class;

                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            boolean bval = (Boolean) aValue;
            if(enabled.get(rowIndex) != bval) {
                enabled.set(rowIndex, bval);
            }
        }
    }

    public void setStandards(Collection<Standard> standards) {

        mStandardsModel.standards.clear();
        mStandardsModel.enabled.clear();

        mStandardsModel.standards.addAll(standards);
        Collections.sort(mStandardsModel.standards, new Comparator<Standard>() {
            @Override
            public int compare(Standard s1, Standard s2) {
                return s1.name.compareTo(s2.name);
            }
        });

        for(int i=0;i<standards.size();i++){
            mStandardsModel.enabled.add(true);
        }
        mStandardsModel.fireTableDataChanged();
    }

    private ActionListener mOnElementSelected = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            mRegionModel.removeAllElements();

            AtomicElement element = (AtomicElement) mElementComboBox.getSelectedItem();
            Iterator<Region> it = mObjTracker.getAllObjectsOfType(Region.class);
            while(it.hasNext()) {
                Region r = it.next();
                AtomicElement regionElement = r.getElement();
                if(regionElement == null) {
                    mRegionModel.addElement(new RegionWrapper(r));
                } else if(regionElement == element) {
                    mRegionModel.add(0, new RegionWrapper(r));
                }
            }

            if(mRegionModel.size() > 0) {
                mRegionList.setSelectedIndex(0);
            }
        }
    };

    private ListSelectionListener mOnRegionSelected = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            RegionWrapper selectedRegion = mRegionList.getSelectedValue();
            if(selectedRegion != null) {
                mNameTextField.setText(selectedRegion.toString());
                mMinModel.setValue(selectedRegion.mRegion.wavelengthRange.getMinimumDouble());
                mMaxModel.setValue(selectedRegion.mRegion.wavelengthRange.getMaximumDouble());
            } else {
                mNameTextField.setText("");
            }

            System.out.println("");
        }
    };
}
