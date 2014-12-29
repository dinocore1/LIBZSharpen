package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.calculation.libs.EmpiricalCurve;
import com.sciaps.common.calculation.libs.EmpiricalCurveCreator;
import com.sciaps.common.calculation.libs.EmpiricalCurveCreator.AssaysSamples;
import com.sciaps.common.data.CalibrationShot;
import com.sciaps.common.data.ChemValue;
import com.sciaps.common.data.IRCurve;
import com.sciaps.common.data.Model;
import com.sciaps.common.data.Region;
import com.sciaps.common.data.Standard;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.utils.RegexUtil;
import com.sciaps.common.swing.view.JFreeChartWrapperPanel;
import com.sciaps.view.tabs.calibrationcurves.CalibrationModelsAndElementsJXCollapsiblePane;
import com.sciaps.view.tabs.calibrationcurves.CalibrationModelsAndElementsJXCollapsiblePane.ModelElementSelectedCallback;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
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

    private final CalibrationModelsAndElementsJXCollapsiblePane _calibrationModelsAndElementsJXCollapsiblePane;
    private final JFreeChartWrapperPanel _jFreeChartWrapperPanel;

    public CalibrationCurvesPanel(MainFrame mainFrame)
    {
        super(mainFrame);

        _calibrationModelsAndElementsJXCollapsiblePane = new CalibrationModelsAndElementsJXCollapsiblePane(JXCollapsiblePane.Direction.RIGHT, new ModelElementSelectedCallback()
        {
            @Override
            public void onModelElementSelected(Model model, AtomicElement element)
            {
                populateSpectrumChartWithModelAndElement(model, element);
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

    // This entire conversion is completely horrible
    // I am fairly certain Paul has developed a much cleaner way to load up the data,
    // but I can't seem to find it
    private void populateSpectrumChartWithModelAndElement(Model model, AtomicElement ae)
    {
        IRCurve irCurve = model.irs.get(ae);

        EmpiricalCurve.Region[] numoratorlines = new EmpiricalCurve.Region[irCurve.numerator.size()];
        int i = 0;
        for (Region region : irCurve.numerator)
        {
            EmpiricalCurve.Region empiricalCurveRegion = new EmpiricalCurve.Region();
            empiricalCurveRegion.min = region.wavelengthRange.getMinimumDouble();
            empiricalCurveRegion.max = region.wavelengthRange.getMaximumDouble();

            numoratorlines[i] = empiricalCurveRegion;

            i++;
        }

        EmpiricalCurve.Region[] denomonatorlines = new EmpiricalCurve.Region[irCurve.denominator.size()];
        int j = 0;
        for (Region region : irCurve.denominator)
        {
            EmpiricalCurve.Region empiricalCurveRegion = new EmpiricalCurve.Region();
            empiricalCurveRegion.min = region.wavelengthRange.getMinimumDouble();
            empiricalCurveRegion.max = region.wavelengthRange.getMaximumDouble();

            denomonatorlines[j] = empiricalCurveRegion;

            j++;
        }

        List<AssaysSamples> assaysSamplesList = new ArrayList();
        for (Standard standard : model.standardList)
        {
            AssaysSamples as = new AssaysSamples();
            as.name = standard.name;
            ChemValue cv;
            if ((cv = standard.getGradeFor(ae)) != null)
            {
                as.knownvalue = cv.percent;
            }
            else
            {
                as.knownvalue = 0;
            }

            for (Map.Entry<String, CalibrationShot> entry : LibzUnitManager.getInstance().getCalibrationShots().entrySet())
            {
                if (entry.getValue().standard.equals(standard))
                {
                    List<LIBZPixelSpectrum> libzPixelSpectra = LibzUnitManager.getInstance().getLIBZPixelSpectra();
                    String shotNumberString = RegexUtil.findValue(entry.getValue().displayName, ".*?([0-9]+)", 1);
                    int shotNumber = Integer.parseInt(shotNumberString);
                    LIBZPixelSpectrum libzPixelSpectum = libzPixelSpectra.get(shotNumber - 1);
                    as.sample = libzPixelSpectum;

                    assaysSamplesList.add(as);

                    break;
                }
            }
        }

        EmpiricalCurveCreator ecc = new EmpiricalCurveCreator();
        EmpiricalCurve ec = ecc.createCurveFor(ae, numoratorlines, denomonatorlines, assaysSamplesList);
        ec.degree = irCurve.degree;
        ec.forceZero = irCurve.forceZero;
        double minX = -1;
        double maxX = 100;

        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries xySeries = new XYSeries("Spectrum");

        for (double x = minX; x < maxX; x += 0.05)
        {
            double y = ec.curveFunction.value(x);
            xySeries.add(x, y);
        }

        dataset.addSeries(xySeries);

        _jFreeChartWrapperPanel.populateSpectrumChartWithAbstractXYDataset(dataset, model.name + " / " + ae.symbol, "IR Ratio", "Concentration");

        _mainFrame.refreshUI();
    }
}