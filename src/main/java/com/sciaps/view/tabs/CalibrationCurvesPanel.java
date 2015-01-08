package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.calculation.libs.EmpiricalCurveCreator;
import com.sciaps.common.data.CalibrationShot;
import com.sciaps.common.data.IRCurve;
import com.sciaps.common.data.Model;
import com.sciaps.common.data.Shot;
import com.sciaps.common.data.Standard;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.spectrum.Spectrum;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.view.JFreeChartWrapperPanel;
import com.sciaps.view.tabs.calibrationcurves.CalibrationModelsInspectorJXCollapsiblePane;
import com.sciaps.view.tabs.calibrationcurves.CalibrationModelsInspectorJXCollapsiblePane.CalibrationModelsInspectorCallback;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author sgowen
 */
public final class CalibrationCurvesPanel extends AbstractTabPanel
{
    private static final String TAB_NAME = "Calibration Curves";

    private final CalibrationModelsInspectorJXCollapsiblePane _calibrationModelsAndElementsJXCollapsiblePane;
    private final JFreeChartWrapperPanel _jFreeChartWrapperPanel;
    private Model _currentlyLoadedModel;
    private AtomicElement _currentlyLoadedAtomicElement;
    private IRCurve _currentlyLoadedIRCurve;
    private List<Standard> _currentlyLoadedStandards;

    public CalibrationCurvesPanel(MainFrame mainFrame)
    {
        super(mainFrame);

        _calibrationModelsAndElementsJXCollapsiblePane = new CalibrationModelsInspectorJXCollapsiblePane(JXCollapsiblePane.Direction.RIGHT, new CalibrationModelsInspectorCallback()
        {
            @Override
            public void onModelElementSelected(Model model, AtomicElement element, List<Standard> standards)
            {
                populateSpectrumChartWithModelAndElement(model, element, standards);
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
    public void customizeMenuBar(JMenuBar menuBar)
    {
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);

        final JMenuItem showCalibrationModelsMenuItem = new JCheckBoxMenuItem("Show Calibration Models", true);
        showCalibrationModelsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
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
        final JMenuItem zoomWavelengthMenuItem = new JCheckBoxMenuItem("Zoom Wavelength", true);
        zoomWavelengthMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.ALT_MASK));
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
        final JMenuItem zoomIntensityMenuItem = new JCheckBoxMenuItem("Zoom Intensity", true);
        zoomIntensityMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
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
                    populateSpectrumChartWithModelAndCurve(_currentlyLoadedModel, _currentlyLoadedIRCurve, _currentlyLoadedAtomicElement, _currentlyLoadedStandards);
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
                        _currentlyLoadedIRCurve.degree = Integer.parseInt(degree);
                        populateSpectrumChartWithModelAndCurve(_currentlyLoadedModel, _currentlyLoadedIRCurve, _currentlyLoadedAtomicElement, _currentlyLoadedStandards);
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

    private void populateSpectrumChartWithModelAndElement(Model model, AtomicElement ae, List<Standard> standards)
    {
        if (standards.isEmpty())
        {
            return;
        }

        IRCurve irCurve = model.irs.get(ae);

        _currentlyLoadedModel = model;
        _currentlyLoadedAtomicElement = ae;
        _currentlyLoadedIRCurve = irCurve;
        _currentlyLoadedStandards = standards;

        populateSpectrumChartWithModelAndCurve(model, irCurve, ae, standards);
    }

    private void populateSpectrumChartWithModelAndCurve(Model model, IRCurve irCurve, AtomicElement ae, List<Standard> standards)
    {
        if (standards.isEmpty())
        {
            return;
        }

        EmpiricalCurveCreator ecc = new EmpiricalCurveCreator(irCurve.degree, irCurve.forceZero);

        Collection<EmpiricalCurveCreator.Sample> samples = new ArrayList();
        for (Standard standard : standards)
        {
            EmpiricalCurveCreator.Sample sample = new EmpiricalCurveCreator.Sample();
            sample.standard = standard;

            Collection<Shot> shots = new ArrayList();

            final List<Spectrum> spectra = getSpectraForStandard(standard);
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

        double minX = 0;
        double maxX = 100;

        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries xySeries = new XYSeries("Spectrum");

        for (double x = minX; x < maxX; x += 0.05)
        {
            double y = polynomialFunction.value(x);
            xySeries.add(x, y);
        }

        dataset.addSeries(xySeries);

        _jFreeChartWrapperPanel.populateSpectrumChartWithAbstractXYDataset(dataset, model.name + " / " + ae.symbol, "IR Ratio", "Concentration");

        _mainFrame.refreshUI();
    }

    private List<Spectrum> getSpectraForStandard(Standard standard)
    {
        final List<Spectrum> spectra = new ArrayList();

        for (Map.Entry<String, CalibrationShot> entry : LibzUnitManager.getInstance().getCalibrationShots().entrySet())
        {
            if (entry.getValue().standard.equals(standard))
            {
                Map<String, LIBZPixelSpectrum> libzPixelSpectra = LibzUnitManager.getInstance().getLIBZPixelSpectra();
                LIBZPixelSpectrum libzPixelSpectum = libzPixelSpectra.get(entry.getKey());
                Spectrum spectrum = libzPixelSpectum.createSpectrum();

                spectra.add(spectrum);
            }
        }

        return spectra;
    }
}