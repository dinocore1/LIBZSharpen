package com.sciaps.view.tabs;

import com.devsmart.swing.BackgroundTask;
import com.sciaps.Main;
import com.sciaps.MainFrame;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.calculation.libs.EmpiricalCurveCreator;
import com.sciaps.common.calculation.libs.ShotAvgSlider;
import com.sciaps.common.data.IRCurve;
import com.sciaps.common.data.Shot;
import com.sciaps.common.data.Standard;
import com.sciaps.common.spectrum.Spectrum;
import com.sciaps.common.swing.view.JFreeChartWrapperPanel;
import com.sciaps.common.swing.view.LabeledXYDataset;
import com.sciaps.common.utils.LIBZPixelShot;
import com.sciaps.utils.SpectraUtils;
import com.sciaps.view.tabs.calibrationcurves.LeftPanel;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class CalibrationCurvesPanel extends AbstractTabPanel {

    static Logger logger = LoggerFactory.getLogger(CalibrationCurvesPanel.class);

    private static final String TAB_NAME = "Calibration Curves";
    private static final String TOOL_TIP = "Display Calibration Curves here";

    private final JSplitPane _splitPane;
    private final LeftPanel mLeftPanel;
    private final JFreeChartWrapperPanel _jFreeChartWrapperPanel;
    public final JLayeredPane mLayeredPane;

    public CalibrationCurvesPanel() {

        mLayeredPane = new JLayeredPane();
        add(mLayeredPane);

        mLeftPanel = new LeftPanel(this);

        _jFreeChartWrapperPanel = new JFreeChartWrapperPanel();

        _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mLeftPanel, _jFreeChartWrapperPanel);
        _splitPane.setOneTouchExpandable(true);
        _splitPane.setContinuousLayout(true);

        mLayeredPane.add(_splitPane);
    }

    @Override
    public void doLayout() {

        mLayeredPane.setBounds(0, 0, getWidth(), getHeight());
        for(Component child : mLayeredPane.getComponents()) {
            child.setBounds(0, 0, getWidth(), getHeight());
        }


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
        JMenu chartMenu = new JMenu("Chart");
        chartMenu.setMnemonic(KeyEvent.VK_C);
        final JMenuItem zoomWavelengthMenuItem = new JCheckBoxMenuItem("Zoom IR Ratio", true);
        zoomWavelengthMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
        zoomWavelengthMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                AbstractButton aButton = (AbstractButton) ae.getSource();
                boolean isSelected = aButton.getModel().isSelected();

                if (_jFreeChartWrapperPanel.isChartLoaded())
                {
                    _jFreeChartWrapperPanel.getChartPanel().setDomainZoomable(isSelected);
                }
            }
        });
        final JMenuItem zoomIntensityMenuItem = new JCheckBoxMenuItem("Zoom Concentration", true);
        zoomIntensityMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
        zoomIntensityMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                AbstractButton aButton = (AbstractButton) ae.getSource();
                boolean isSelected = aButton.getModel().isSelected();

                if (_jFreeChartWrapperPanel.isChartLoaded())
                {
                    _jFreeChartWrapperPanel.getChartPanel().setRangeZoomable(isSelected);
                }
            }
        });

        chartMenu.add(zoomWavelengthMenuItem);
        chartMenu.add(zoomIntensityMenuItem);

        menuBar.add(chartMenu);
    }

    @Override
    public void onDisplay()
    {
        mLeftPanel.refresh();
    }

    private List<EmpiricalCurveCreator.Sample> createSamples(List<Standard> standards, AtomicElement element) {
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

                for (LIBZPixelShot shot : SpectraUtils.getShotsForStandard(standard)) {
                    slider.addShot(shot);
                }
                samples.add(sample);
            }
        }

        return samples;
    }

    public void populateSpectrumChartWithModelAndElement(final IRCurve irCurve, final List<Standard> enabledStandards, final List<Standard> disabledStandards)
    {
        BackgroundTask.runBackgroundTask(new BackgroundTask()
        {
            XYSeriesCollection dataset = new XYSeriesCollection();
            LabeledXYDataset pointsDataset = new LabeledXYDataset();

            @Override
            public void onBackground()
            {
                try {
                    EmpiricalCurveCreator ecc = new EmpiricalCurveCreator(irCurve.degree, irCurve.forceZero);

                    List<EmpiricalCurveCreator.Sample> enabledSamples = createSamples(enabledStandards, irCurve.element);
                    double[][] enabledPoints = ecc.getPoints(irCurve, enabledSamples);

                    PolynomialFunction polynomialFunction = ecc.createCurve(enabledPoints[0], enabledPoints[1]);
                    irCurve.coefficients = polynomialFunction.getCoefficients();
                    irCurve.irRange = ecc.getIRRange();

                    List<EmpiricalCurveCreator.Sample> disabledStaples = createSamples(disabledStandards, irCurve.element);
                    double[][] disabledPoints = ecc.getPoints(irCurve, disabledStaples);

                    LabeledXYDataset.LabeledXYSeries enabledXYDataset = new LabeledXYDataset.LabeledXYSeries("Enabled");
                    pointsDataset.addSeries(enabledXYDataset);
                    for (int i = 0; i < enabledSamples.size(); i++)
                    {
                        String label = enabledSamples.get(i).standard.name;
                        double x = enabledPoints[0][i];
                        double y = enabledPoints[1][i];

                        enabledXYDataset.add(x, y, label);
                    }

                    LabeledXYDataset.LabeledXYSeries disabledXYDataset = new LabeledXYDataset.LabeledXYSeries("Disabled");
                    pointsDataset.addSeries(disabledXYDataset);
                    for (int i = 0; i < disabledStaples.size(); i++)
                    {
                        String label = disabledStaples.get(i).standard.name;
                        double x = disabledPoints[0][i];
                        double y = disabledPoints[1][i];

                        disabledXYDataset.add(x, y, label);
                    }


                    double min = irCurve.irRange.getMinimumDouble();
                    double max = irCurve.irRange.getMaximumDouble();

                    double width = max - min;
                    if(!Double.isInfinite(width) && !Double.isNaN(width)) {
                        XYSeries xySeries = new XYSeries("Calibration Curve");
                        double stepSize = width / 200;
                        for (double x = min; x < max; x += stepSize) {
                            double y = polynomialFunction.value(x);
                            xySeries.add(x, y);
                        }

                        dataset.addSeries(xySeries);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("", e);
                }
            }

            @Override
            public void onAfter() {
                _jFreeChartWrapperPanel.populateCurveChart(irCurve.name, "IR Ratio", "Concentration (%)", dataset, pointsDataset);
            }
        });
    }
}