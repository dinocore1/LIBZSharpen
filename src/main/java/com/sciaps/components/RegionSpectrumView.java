package com.sciaps.components;

import com.devsmart.ThreadUtils;
import com.devsmart.swing.BackgroundTask;
import com.google.inject.Inject;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.algorithms.QuantialBackgroundModel;
import com.sciaps.common.algorithms.SpectrumBackgroundRemoval;
import com.sciaps.common.data.CalibrationShot;
import com.sciaps.common.data.Region;
import com.sciaps.common.data.Standard;
import com.sciaps.common.objtracker.DBObjTracker;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.spectrum.Spectrum;
import com.sciaps.common.swing.SpectrumXYDataset;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.utils.TableColumnAdjuster;
import com.sciaps.common.utils.LIBZPixelShot;
import com.sciaps.common.utils.LIBZPixelShotAvg;
import com.sciaps.uimodel.ElementComboBoxModel;
import com.sciaps.uimodel.RegionWrapper;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
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
    private SpectrumXYDataset mSpectrumDataSet;
    private JTextField mNameTextField;
    private SpinnerNumberModel mMinModel;
    private SpinnerNumberModel mMaxModel;
    private JComboBox<AtomicElement> mElementComboBox;
    private DefaultListModel<RegionWrapper> mRegionModel;
    private StandardTableModel mStandardsModel;
    private JList<RegionWrapper> mRegionList;

    @Inject
    DBObjTracker mObjTracker;

    @Inject
    LibzUnitManager mUnitManager;


    public RegionSpectrumView() {
        setLayout(new BorderLayout());

        mSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftPanel(), createChartPanel());
        mSplitPane.setOneTouchExpandable(true);
        mSplitPane.setContinuousLayout(true);

        add(mSplitPane, BorderLayout.CENTER);

    }

    private JPanel createChartPanel() {

        JPanel centerPanel = new JPanel(new MigLayout("fill"));

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

            mSpectrumDataSet = new SpectrumXYDataset();
            plot.setDataset(0, mSpectrumDataSet);
        }

        mChartPanel = new ChartPanel(chart);
        mChartPanel.setMouseWheelEnabled(true);

        centerPanel.add(mChartPanel, "span, grow, push");

        return centerPanel;
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new MigLayout("fill"));

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

        panel.add(scrollPane, "h 50%, grow, wrap");

        mStandardsModel = new StandardTableModel();
        JTable table = new JTable(mStandardsModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        TableColumnAdjuster tca = new TableColumnAdjuster(table);
        tca.adjustColumns();

        scrollPane = new JScrollPane(table);
        panel.add(scrollPane, "h 50%, grow, wrap");


        return panel;
    }

    private class StandardTableModel extends AbstractTableModel {

        class StandardTableItem {
            Standard standard;
            boolean enabled;
            Spectrum spectrum;
        }

        public StandardTableItem create(Standard standard, boolean enabled) {
            StandardTableItem retval = new StandardTableItem();
            retval.standard = standard;
            retval.enabled = enabled;
            return retval;
        }

        final ArrayList<StandardTableItem> standards = new ArrayList<StandardTableItem>();

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
            StandardTableItem item = standards.get(row);
            switch(column) {
                case 0:
                    return item.enabled;

                case 1:
                    return item.standard.name;

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
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            StandardTableItem item = standards.get(rowIndex);
            boolean newValue = (Boolean) aValue;

            if(item.enabled != newValue) {
                item.enabled = newValue;
                if (item.enabled) {
                    mSpectrumDataSet.addSpectrum(item.spectrum, item.standard.name);
                } else {
                    mSpectrumDataSet.removeSpectrum(item.spectrum);
                }
            }
        }
    }

    public void setStandards(final Collection<Standard> standards) {

        mStandardsModel.standards.clear();
        mStandardsModel.fireTableDataChanged();

        BackgroundTask.runBackgroundTask(new BackgroundTask() {

            ArrayList<StandardTableModel.StandardTableItem> items = new ArrayList<StandardTableModel.StandardTableItem>(standards.size());

            @Override
            public void onBackground() {

                for(Standard standard : standards) {
                    StandardTableModel.StandardTableItem item = mStandardsModel.create(standard, true);
                    items.add(item);

                    LIBZPixelShotAvg shotAvg = new LIBZPixelShotAvg();

                    for (LIBZPixelShot shot : getShotsForStandard(standard)) {
                        shotAvg.addShot(shot);
                    }

                    Spectrum avgSpectrum = shotAvg.getSpectrum();

                    QuantialBackgroundModel backgroundModel = new QuantialBackgroundModel(1, 1);
                    PolynomialSplineFunction bgModel = backgroundModel.getModelBaseline(avgSpectrum);

                    SpectrumBackgroundRemoval bgRm = new SpectrumBackgroundRemoval();
                    item.spectrum = bgRm.doBackgroundRemoval(avgSpectrum, bgModel);

                }

                Collections.sort(mStandardsModel.standards, new Comparator<StandardTableModel.StandardTableItem>() {
                    @Override
                    public int compare(StandardTableModel.StandardTableItem s1, StandardTableModel.StandardTableItem s2) {
                        return s1.standard.name.compareTo(s2.standard.name);
                    }
                });

            }

            @Override
            public void onAfter() {
                mStandardsModel.standards.addAll(items);
                mStandardsModel.fireTableDataChanged();

                for(StandardTableModel.StandardTableItem item : mStandardsModel.standards) {
                    if(item.enabled) {
                        mSpectrumDataSet.addSpectrum(item.spectrum, item.standard.name);
                    }
                }
            }
        }, ThreadUtils.CPUThreads);


    }

    public void selectElement(AtomicElement element) {
        for(int i=0;i<mElementComboBox.getItemCount();i++){
            AtomicElement value = mElementComboBox.getItemAt(i);
            if(value == element) {
                mElementComboBox.setSelectedIndex(i);
                break;
            }
        }
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

                updateGraph();

            } else {
                mNameTextField.setText("");
            }


        }
    };

    private void updateGraph() {
        RegionWrapper selectedRegion = mRegionList.getSelectedValue();
        if(selectedRegion != null) {
            double minX = selectedRegion.mRegion.wavelengthRange.getMinimumDouble();
            final double maxX = selectedRegion.mRegion.wavelengthRange.getMaximumDouble();

            double width = (maxX - minX) * 2;

            final double startX = (minX + maxX)/2 - width/2;
            final double endX = (minX + maxX)/2 + width/2;

            mChartPanel.getChart().getXYPlot().getDomainAxis().setRange(startX, endX);

            final ArrayList<StandardTableModel.StandardTableItem> standards = new ArrayList<StandardTableModel.StandardTableItem>(mStandardsModel.standards);
            BackgroundTask.runBackgroundTask(new BackgroundTask() {

                Max max = new Max();
                Min min = new Min();

                @Override
                public void onBackground() {
                    for(StandardTableModel.StandardTableItem item : standards) {
                        if(item.enabled) {
                            for(double x=startX;x<endX;x+=1/30.0){
                                double y = item.spectrum.getIntensityFunction().value(x);
                                min.increment(y);
                                max.increment(y);
                            }
                        }
                    }
                }

                @Override
                public void onAfter() {
                    double maxy = max.getResult();
                    double miny = min.getResult();
                    double width = (maxy - miny);
                    mChartPanel.getChart().getXYPlot().getRangeAxis().setRange(miny - width*0.1, maxy + width*0.1);
                }
            }, ThreadUtils.CPUThreads);
        }
    }

    private void setSpectrumData(final Collection<Standard> standards) {




    }

    public Collection<LIBZPixelShot> getShotsForStandard(Standard standard) {
        LinkedList<LIBZPixelShot> retval = new LinkedList<LIBZPixelShot>();
        Iterator<CalibrationShot> it = mObjTracker.getAllObjectsOfType(CalibrationShot.class);
        while(it.hasNext()) {
            CalibrationShot shot = it.next();
            if(shot.standard == standard){
                LIBZPixelSpectrum data = mUnitManager.calShotIdCache.get(shot.mId);
                if(data != null) {
                    retval.add(new LIBZPixelShot(data));
                }
            }
        }


        return retval;
    }
}
