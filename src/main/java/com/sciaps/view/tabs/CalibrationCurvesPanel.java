package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.calculation.libs.EmpiricalCurveCreator;
import com.sciaps.common.data.IRCurve;
import com.sciaps.common.data.Model;
import com.sciaps.common.data.Shot;
import com.sciaps.common.data.Standard;
import com.sciaps.common.spectrum.Spectrum;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.view.JFreeChartWrapperPanel;
import com.sciaps.common.swing.view.LabeledXYDataset;
import com.sciaps.utils.SpectraUtil;
import com.sciaps.view.tabs.calibrationcurves.CalibrationModelsInspectorJXCollapsiblePane;
import com.sciaps.view.tabs.calibrationcurves.CalibrationModelsInspectorJXCollapsiblePane.CalibrationModelsInspectorCallback;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author sgowen
 */
public final class CalibrationCurvesPanel extends AbstractTabPanel
{
    private static final String TAB_NAME = "Calibration Curves";
    private static final String TOOL_TIP = "Display Calibration Curves here";

    private final CalibrationModelsInspectorJXCollapsiblePane _calibrationModelsAndElementsJXCollapsiblePane;
    private final JFreeChartWrapperPanel _jFreeChartWrapperPanel;
    private String _currentlyLoadedModelId;
    private AtomicElement _currentlyLoadedAtomicElement;
    private IRCurve _currentlyLoadedIRCurve;
    private List<Standard> _currentlyLoadedStandards;
    private double _minX;
    private double _maxX;
    private double[][] _points;

    public CalibrationCurvesPanel(MainFrame mainFrame)
    {
        super(mainFrame);

        _calibrationModelsAndElementsJXCollapsiblePane = new CalibrationModelsInspectorJXCollapsiblePane(JXCollapsiblePane.Direction.RIGHT, new CalibrationModelsInspectorCallback()
        {
            @Override
            public void onModelElementSelected(String modelId, AtomicElement element, List<Standard> standards)
            {
                populateSpectrumChartWithModelAndElement(modelId, element, standards);
            }
        });
        _calibrationModelsAndElementsJXCollapsiblePane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl C"), JXCollapsiblePane.TOGGLE_ACTION);
        _calibrationModelsAndElementsJXCollapsiblePane.setCollapsed(false);

        _jFreeChartWrapperPanel = new JFreeChartWrapperPanel();

        setLayout(new BorderLayout());

        add(_calibrationModelsAndElementsJXCollapsiblePane, BorderLayout.WEST);
        add(_jFreeChartWrapperPanel, BorderLayout.CENTER);
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
                _calibrationModelsAndElementsJXCollapsiblePane.setCollapsed(!isSelected);
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

        JMenu curveMenu = new JMenu("Curve");
        curveMenu.setMnemonic(KeyEvent.VK_P);
        final JMenuItem forceThroughZeroMenuItem = new JCheckBoxMenuItem("Force Through Zero", false);
        forceThroughZeroMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.ALT_MASK));
        forceThroughZeroMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                AbstractButton aButton = (AbstractButton) ae.getSource();
                boolean isSelected = aButton.getModel().isSelected();

                if (_jFreeChartWrapperPanel.isChartLoaded() && _currentlyLoadedIRCurve != null)
                {
                    _currentlyLoadedIRCurve.forceZero = isSelected;
                    Model currentlyLoadedModel = LibzUnitManager.getInstance().getModelsManager().getObjects().get(_currentlyLoadedModelId);
                    populateSpectrumChartWithModelAndCurve(currentlyLoadedModel, _currentlyLoadedIRCurve, _currentlyLoadedAtomicElement, _currentlyLoadedStandards, false);
                }
            }
        });
        final JMenuItem changeDegreeMenuItem = new JMenuItem("Change Degree", KeyEvent.VK_S);
        changeDegreeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK));
        changeDegreeMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                if (_jFreeChartWrapperPanel.isChartLoaded() && _currentlyLoadedAtomicElement != null)
                {
                    final String degree = JOptionPane.showInputDialog(_mainFrame, "Enter the Degree:");
                    if (NumberUtils.isNumber(degree))
                    {
                        LibzUnitManager.getInstance().getModelsManager().markObjectAsModified(_currentlyLoadedModelId);
                        _currentlyLoadedIRCurve.degree = Integer.parseInt(degree);
                        Model currentlyLoadedModel = LibzUnitManager.getInstance().getModelsManager().getObjects().get(_currentlyLoadedModelId);
                        populateSpectrumChartWithModelAndCurve(currentlyLoadedModel, _currentlyLoadedIRCurve, _currentlyLoadedAtomicElement, _currentlyLoadedStandards, false);
                    }
                }
            }
        });

        curveMenu.add(forceThroughZeroMenuItem);
        curveMenu.add(changeDegreeMenuItem);

        menuBar.add(viewMenu);
        menuBar.add(chartMenu);
        menuBar.add(curveMenu);
    }

    @Override
    public void onDisplay()
    {
        _calibrationModelsAndElementsJXCollapsiblePane.refresh();
    }

    private void populateSpectrumChartWithModelAndElement(String modelId, AtomicElement ae, List<Standard> standards)
    {
        if (standards.isEmpty())
        {
            return;
        }

        Model currentlyLoadedModel = LibzUnitManager.getInstance().getModelsManager().getObjects().get(_currentlyLoadedModelId);
        Model newModel = LibzUnitManager.getInstance().getModelsManager().getObjects().get(modelId);
        IRCurve irCurve = newModel.irs.get(ae);

        boolean refreshStandardsPoints = false;
        if (newModel != currentlyLoadedModel || ae != _currentlyLoadedAtomicElement)
        {
            refreshStandardsPoints = true;
        }

        _currentlyLoadedModelId = modelId;
        _currentlyLoadedAtomicElement = ae;
        _currentlyLoadedIRCurve = irCurve;
        _currentlyLoadedStandards = standards;

        populateSpectrumChartWithModelAndCurve(newModel, irCurve, ae, standards, refreshStandardsPoints);
    }

    private void populateSpectrumChartWithModelAndCurve(Model model, IRCurve irCurve, AtomicElement ae, List<Standard> standards, boolean refreshStandardsPoints)
    {
        if (standards.isEmpty())
        {
            return;
        }

        EmpiricalCurveCreator ecc = new EmpiricalCurveCreator(irCurve.degree, irCurve.forceZero);

        List<EmpiricalCurveCreator.Sample> samples = new ArrayList<EmpiricalCurveCreator.Sample>();
        for (Standard standard : standards)
        {
            EmpiricalCurveCreator.Sample sample = new EmpiricalCurveCreator.Sample();
            sample.standard = standard;

            Collection<Shot> shots = new ArrayList<Shot>();

            final List<Spectrum> spectra = SpectraUtil.getSpectraForStandard(standard);
            if (spectra != null && spectra.size() > 0)
            {
                for (final Spectrum spectrum : spectra)
                {
                    shots.add(new Shot()
                    {
                        @Override
                        public Spectrum getSpectrum()
                        {
                            return spectrum;
                        }
                    });
                }
            }

            sample.shots = shots;

            samples.add(sample);
        }

        PolynomialFunction polynomialFunction = ecc.createCurve(irCurve, samples);

        _minX = 0;
        _maxX = 0;
        _points = ecc.getPoints;

        LabeledXYDataset labeledXYDataset = new LabeledXYDataset();
        for (int i = 0; i < _points[0].length; i++)
        {
            double x = _points[0][i];
            double y = _points[1][i];
            String label = samples.get(i).standard.name;

            labeledXYDataset.add(x, y, label);

            if (x < _minX)
            {
                _minX = x;
            }
            else if (x > _maxX)
            {
                _maxX = x;
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries xySeries = new XYSeries("Calibration Curve");

        double width = _maxX - _minX;
        double stepSize = width / 200;
        for (double x = _minX - width * 0.1; x < _maxX + width * 0.1; x += stepSize)
        {
            double y = polynomialFunction.value(x);
            xySeries.add(x, y);
        }

        dataset.addSeries(xySeries);

        _jFreeChartWrapperPanel.populateCurveChart(model.name + " / " + ae.symbol, "IR Ratio", "Concentration", dataset, labeledXYDataset);

        _mainFrame.refreshUI();
    }
}