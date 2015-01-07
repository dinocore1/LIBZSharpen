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
import javax.swing.KeyStroke;
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

        menuBar.add(viewMenu);
        menuBar.add(chartMenu);
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
        EmpiricalCurveCreator ecc = new EmpiricalCurveCreator(irCurve.degree, irCurve.forceZero);

        Collection<EmpiricalCurveCreator.Sample> samples = new ArrayList();
        for (Standard standard : standards)
        {
            EmpiricalCurveCreator.Sample sample = new EmpiricalCurveCreator.Sample();
            sample.standard = standard;

            Collection<Shot> shots = new ArrayList();

            final Spectrum spectrum = getSpectrumForStandard(standard);
            if (spectrum != null)
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

    private Spectrum getSpectrumForStandard(Standard standard)
    {
        System.out.println("getSpectrumForStandard: " + standard.toString());

        for (Map.Entry<String, CalibrationShot> entry : LibzUnitManager.getInstance().getCalibrationShots().entrySet())
        {
            if (entry.getValue().standard.equals(standard))
            {
                System.out.println("We have a match");
                Map<String, LIBZPixelSpectrum> libzPixelSpectra = LibzUnitManager.getInstance().getLIBZPixelSpectra();
                LIBZPixelSpectrum libzPixelSpectum = libzPixelSpectra.get(entry.getKey());
                Spectrum spectrum = libzPixelSpectum.createSpectrum();

                return spectrum;
            }
        }

        System.out.println("Returning NULL");

        return null;
    }
}