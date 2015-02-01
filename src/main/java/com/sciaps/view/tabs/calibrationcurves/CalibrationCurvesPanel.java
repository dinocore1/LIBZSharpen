package com.sciaps.view.tabs.calibrationcurves;

import com.devsmart.swing.BackgroundTask;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.sciaps.Main;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.calculation.libs.EmpiricalCurveCreator;
import com.sciaps.common.calculation.libs.ShotAvgSlider;
import com.sciaps.common.data.CalibrationShot;
import com.sciaps.common.data.IRCurve;
import com.sciaps.common.data.Shot;
import com.sciaps.common.data.Standard;
import com.sciaps.common.objtracker.DBObjTracker;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.swing.FramePanel;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.view.LabelGenerator;
import com.sciaps.common.swing.view.LabeledXYDataset;
import com.sciaps.common.utils.LIBZPixelShot;
import com.sciaps.view.tabs.AbstractTabPanel;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public final class CalibrationCurvesPanel extends AbstractTabPanel {

    static Logger logger = LoggerFactory.getLogger(CalibrationCurvesPanel.class);

    public final FramePanel mFramePanel;
    private final JSplitPane mSplitPane;
    ChartPanel mChartPanel;

    @Inject
    DBObjTracker mObjTracker;

    @Inject
    LibzUnitManager mUnitManager;

    private LeftPanel mLeftPanel;

    EventBus mGlobalEventBus;
    private XYSeriesCollection mCurveDataset;
    private LabeledXYDataset mPointsDataset;

    @Inject
    void setGlobalEventBus(EventBus eventBus) {
        mGlobalEventBus = eventBus;
        mGlobalEventBus.register(this);
    }

    public CalibrationCurvesPanel() {

        setLayout(new BorderLayout());
        mFramePanel = new FramePanel();
        add(mFramePanel, BorderLayout.CENTER);

        createChart();

        mLeftPanel = Main.mInjector.getInstance(LeftPanel.class);
        mLeftPanel.setCalCurvesPanel(this);

        mSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mLeftPanel, mChartPanel);
        mSplitPane.setOneTouchExpandable(true);
        mSplitPane.setContinuousLayout(true);

        mFramePanel.add(mSplitPane, new Integer(0));
    }

    private void createChart() {

        JFreeChart chart = ChartFactory.createXYLineChart("", "Intensity Ratio", "Concentration (%)", null);
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

            mCurveDataset = new XYSeriesCollection();
            plot.setDataset(0, mCurveDataset);
        }

        {
            //points renderer
            XYItemRenderer pointRenderer = new XYLineAndShapeRenderer(false, true);
            pointRenderer.setBaseItemLabelGenerator(new LabelGenerator());
            pointRenderer.setBaseItemLabelPaint(Color.LIGHT_GRAY);
            pointRenderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.BOTTOM_CENTER));
            pointRenderer.setBaseItemLabelsVisible(true);
            pointRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
            pointRenderer.setSeriesPaint(0, Color.GREEN);
            pointRenderer.setSeriesPaint(1, Color.RED);

            plot.setRenderer(1, pointRenderer);

            mPointsDataset = new LabeledXYDataset();
            plot.setDataset(1, mPointsDataset);

        }

        mChartPanel = new ChartPanel(chart);
        mChartPanel.setMouseWheelEnabled(true);
    }

    @Override
    public void onDisplay() {
    }

    @Override
    public void onHide() {

    }

    private List<EmpiricalCurveCreator.Sample> createSamples(Set<Standard> standards, AtomicElement element) {
        final List<EmpiricalCurveCreator.Sample> samples = new ArrayList<EmpiricalCurveCreator.Sample>();
        for (final Standard standard : standards) {
            if (standard.getGradeFor(element) != null) {

                final EmpiricalCurveCreator.Sample sample = new EmpiricalCurveCreator.Sample();
                sample.standard = standard;
                sample.shots = new ArrayList<Shot>();

                ShotAvgSlider slider = new ShotAvgSlider(1, 1);
                Main.mInjector.injectMembers(slider);

                slider.setOnShotCallback(new ShotAvgSlider.Callback() {
                    @Override
                    public void shotReady(Shot shot) {
                        sample.shots.add(shot);
                    }
                });

                for (LIBZPixelShot shot : getShotsForStandard(standard)) {
                    slider.addShot(shot);
                }
                samples.add(sample);
            }
        }

        return samples;
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

    public void populateSpectrumChartWithModelAndElement(final IRCurve irCurve, final Set<Standard> enabledStandards, final Set<Standard> disabledStandards) {
        BackgroundTask.runBackgroundTask(new BackgroundTask() {

            LabeledXYDataset.LabeledXYSeries enabledXYDataset = new LabeledXYDataset.LabeledXYSeries("Enabled");
            LabeledXYDataset.LabeledXYSeries disabledXYDataset= new LabeledXYDataset.LabeledXYSeries("Disabled");
            XYSeries curveXYSeries = new XYSeries("Calibration Curve");

            @Override
            public void onBefore() {
                mCurveDataset.removeAllSeries();
                mPointsDataset.removeAllSeriese();
            }

            @Override
            public void onBackground() {
                try {
                    EmpiricalCurveCreator ecc = new EmpiricalCurveCreator(irCurve.degree, irCurve.forceZero);

                    List<EmpiricalCurveCreator.Sample> enabledSamples = createSamples(enabledStandards, irCurve.element);
                    double[][] enabledPoints = ecc.getPoints(irCurve, enabledSamples);

                    PolynomialFunction polynomialFunction = ecc.createCurve(enabledPoints[0], enabledPoints[1]);
                    irCurve.coefficients = polynomialFunction.getCoefficients();
                    irCurve.irRange = ecc.getIRRange();

                    List<EmpiricalCurveCreator.Sample> disabledStaples = createSamples(disabledStandards, irCurve.element);
                    double[][] disabledPoints = ecc.getPoints(irCurve, disabledStaples);

                    for (int i = 0; i < enabledSamples.size(); i++) {
                        String label = enabledSamples.get(i).standard.name;
                        double x = enabledPoints[0][i];
                        double y = enabledPoints[1][i];

                        enabledXYDataset.add(x, y, label);
                    }

                    for (int i = 0; i < disabledStaples.size(); i++) {
                        String label = disabledStaples.get(i).standard.name;
                        double x = disabledPoints[0][i];
                        double y = disabledPoints[1][i];

                        disabledXYDataset.add(x, y, label);
                    }


                    double min = irCurve.irRange.getMinimumDouble();
                    double max = irCurve.irRange.getMaximumDouble();

                    double width = max - min;
                    if(!Double.isInfinite(width) && !Double.isNaN(width)) {
                        double stepSize = width / 200;
                        for (double x = min; x < max; x += stepSize) {
                            double y = polynomialFunction.value(x);
                            curveXYSeries.add(x, y);
                        }
                    }


                } catch (Exception e) {
                    logger.error("", e);
                }
            }

            @Override
            public void onAfter() {
                mChartPanel.getChart().setTitle(irCurve.name);

                mCurveDataset.addSeries(curveXYSeries);

                mPointsDataset.addSeries(enabledXYDataset);
                mPointsDataset.addSeries(disabledXYDataset);
            }
        });
    }
}