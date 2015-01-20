package com.sciaps.view.tabs;

import com.devsmart.swing.BackgroundTask;
import com.sciaps.MainFrame;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.calculation.libs.EmpiricalCurveCreator;
import com.sciaps.common.data.IRCurve;
import com.sciaps.common.data.Model;
import com.sciaps.common.data.Shot;
import com.sciaps.common.data.Standard;
import com.sciaps.common.spectrum.Spectrum;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.utils.SwingUtils;
import com.sciaps.common.swing.view.JFreeChartWrapperPanel;
import com.sciaps.common.swing.view.LabeledXYDataset;
import com.sciaps.utils.SpectraUtils;
import com.sciaps.view.tabs.calibrationcurves.CalibrationModelsInspectorJXCollapsiblePane;
import com.sciaps.view.tabs.calibrationcurves.CalibrationModelsInspectorJXCollapsiblePane.CalibrationModelsInspectorCallback;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.jdesktop.swingx.JXCollapsiblePane;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author sgowen
 */
public final class CalibrationCurvesPanel extends AbstractTabPanel
{

    static Logger logger = LoggerFactory.getLogger(CalibrationCurvesPanel.class);

    private static final String TAB_NAME = "Calibration Curves";
    private static final String TOOL_TIP = "Display Calibration Curves here";

    private JSplitPane _splitPane;

    private final CalibrationModelsInspectorJXCollapsiblePane _calibrationModelsAndElementsJXCollapsiblePane;
    private final JFreeChartWrapperPanel _jFreeChartWrapperPanel;


    public CalibrationCurvesPanel(MainFrame mainFrame)
    {
        super(mainFrame);


        _calibrationModelsAndElementsJXCollapsiblePane = new CalibrationModelsInspectorJXCollapsiblePane(new CalibrationModelsInspectorCallback()
        {
            @Override
            public void onModelElementSelected(IRCurve curve, List<Standard> enabledStandards, List<Standard> disabledStandards)
            {
                populateSpectrumChartWithModelAndElement(curve, enabledStandards, disabledStandards);
            }
        });
        _calibrationModelsAndElementsJXCollapsiblePane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl C"), JXCollapsiblePane.TOGGLE_ACTION);


        _jFreeChartWrapperPanel = new JFreeChartWrapperPanel();

        _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                _calibrationModelsAndElementsJXCollapsiblePane,
                _jFreeChartWrapperPanel);
        _splitPane.setOneTouchExpandable(true);
        _splitPane.setContinuousLayout(true);


        setLayout(new BorderLayout());
        add(_splitPane, BorderLayout.CENTER);

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

        final JMenuItem showCalibrationModelsMenuItem = new JCheckBoxMenuItem("Show Calibration Models", true);
        showCalibrationModelsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.ALT_MASK));
        showCalibrationModelsMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                AbstractButton aButton = (AbstractButton) ae.getSource();
                boolean isSelected = aButton.getModel().isSelected();
                //_calibrationModelsAndElementsJXCollapsiblePane.setCollapsed(!isSelected);
            }
        });

        viewMenu.add(showCalibrationModelsMenuItem);

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

        menuBar.add(viewMenu);
        menuBar.add(chartMenu);
    }

    @Override
    public void onDisplay()
    {
        _calibrationModelsAndElementsJXCollapsiblePane.refresh();
    }

    private static List<EmpiricalCurveCreator.Sample> createSamples(List<Standard> standards, AtomicElement element) {
        List<EmpiricalCurveCreator.Sample> samples = new ArrayList<EmpiricalCurveCreator.Sample>();
        for (Standard standard : standards) {
            if(standard.getGradeFor(element) != null) {
                EmpiricalCurveCreator.Sample sample = new EmpiricalCurveCreator.Sample();
                sample.standard = standard;

                Collection<Shot> shots = new ArrayList<Shot>();

                final List<Spectrum> spectra = SpectraUtils.getSpectraForStandard(standard);
                if (spectra != null && spectra.size() > 0) {
                    for (final Spectrum spectrum : spectra) {
                        shots.add(new Shot() {
                            @Override
                            public Spectrum getSpectrum() {
                                return spectrum;
                            }
                        });
                    }
                }

                sample.shots = shots;

                samples.add(sample);
            }
        }

        return samples;
    }

    private void populateSpectrumChartWithModelAndElement(final IRCurve irCurve, final List<Standard> enabledStandards, final List<Standard> disabledStandards)
    {

        BackgroundTask.runBackgroundTask(new BackgroundTask() {

            XYSeriesCollection dataset = new XYSeriesCollection();
            LabeledXYDataset pointsDataset = new LabeledXYDataset();

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

                    Min min = new Min();
                    Max max = new Max();

                    LabeledXYDataset.LabeledXYSeries enabledXYDataset = new LabeledXYDataset.LabeledXYSeries("Enabled");
                    pointsDataset.addSeries(enabledXYDataset);
                    for (int i = 0; i < enabledSamples.size(); i++) {
                        String label = enabledSamples.get(i).standard.name;
                        double x = enabledPoints[0][i];
                        double y = enabledPoints[1][i];

                        enabledXYDataset.add(x, y, label);

                        min.increment(x);
                        max.increment(x);
                    }

                    LabeledXYDataset.LabeledXYSeries disabledXYDataset = new LabeledXYDataset.LabeledXYSeries("Disabled");
                    pointsDataset.addSeries(disabledXYDataset);
                    for (int i = 0; i < disabledStaples.size(); i++) {
                        String label = disabledStaples.get(i).standard.name;
                        double x = disabledPoints[0][i];
                        double y = disabledPoints[1][i];

                        disabledXYDataset.add(x, y, label);

                        min.increment(x);
                        max.increment(x);
                    }


                    XYSeries xySeries = new XYSeries("Calibration Curve");


                    double width = max.getResult() - min.getResult();
                    if(width == 0){
                        width = 1;
                    }
                    double stepSize = width / 200;
                    for (double x = min.getResult() - width * 0.1; x < max.getResult() + width * 0.1; x += stepSize) {
                        double y = polynomialFunction.value(x);
                        xySeries.add(x, y);
                    }

                    dataset.addSeries(xySeries);



                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("", e);
                }
            }

            @Override
            public void onAfter() {
                _jFreeChartWrapperPanel.populateCurveChart(irCurve.name, "IR Ratio", "Concentration (%)", dataset, pointsDataset);
                _mainFrame.refreshUI();
            }
        });


    }



}