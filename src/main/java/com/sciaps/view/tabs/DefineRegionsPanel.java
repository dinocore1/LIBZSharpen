package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import com.sciaps.common.data.CalibrationShot;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.spectrum.Spectrum;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.listener.LibzChartMouseListener;
import com.sciaps.common.swing.listener.LibzChartMouseListener.LibzChartMouseListenerCallback;
import com.sciaps.common.swing.view.JFreeChartWrapperPanel;
import com.sciaps.view.tabs.defineregions.RegionsJXCollapsiblePane;
import com.sciaps.view.tabs.defineregions.RegionsJXCollapsiblePane.RegionsJXCollapsiblePaneCallback;
import com.sciaps.common.swing.view.ShotDataJXCollapsiblePane;
import com.sciaps.common.swing.view.ShotDataJXCollapsiblePane.ShotDataJXCollapsiblePaneCallback;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;

/**
 *
 * @author sgowen
 */
public final class DefineRegionsPanel extends AbstractTabPanel
{
    private static final String TAB_NAME = "Define Regions";
    private static final String TOOL_TIP = "Load Spectra and Define Regions here";

    private final JFreeChartWrapperPanel _jFreeChartWrapperPanel;
    private final ShotDataJXCollapsiblePane _shotDataJXCollapsiblePane;
    private final RegionsJXCollapsiblePane _regionsJXCollapsiblePane;

    public DefineRegionsPanel(MainFrame mainFrame)
    {
        super(mainFrame);

        _jFreeChartWrapperPanel = new JFreeChartWrapperPanel();

        _shotDataJXCollapsiblePane = new ShotDataJXCollapsiblePane(JXCollapsiblePane.Direction.RIGHT, new ShotDataJXCollapsiblePaneCallback()
        {
            @Override
            public void shotDataSelected(String calibrationShotId)
            {
                populateSpectrumChartWithLIBZPixelSpectrumForCalibrationShot(calibrationShotId);
            }
        });

        _shotDataJXCollapsiblePane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl S"), JXCollapsiblePane.TOGGLE_ACTION);
        _shotDataJXCollapsiblePane.setCollapsed(false);

        _regionsJXCollapsiblePane = new RegionsJXCollapsiblePane(JXCollapsiblePane.Direction.LEFT, new RegionsJXCollapsiblePaneCallback()
        {
            @Override
            public void addChartMarkers(Marker[] regionMarkers)
            {
                if (_jFreeChartWrapperPanel.getJFreeChart() != null)
                {
                    XYPlot plot = (XYPlot) _jFreeChartWrapperPanel.getJFreeChart().getPlot();
                    for (Marker m : regionMarkers)
                    {
                        if (m instanceof IntervalMarker)
                        {
                            plot.addDomainMarker(m, Layer.BACKGROUND);
                        }
                        else
                        {
                            plot.addDomainMarker(m);
                        }
                    }
                }
            }

            @Override
            public void removeChartMarkers(Marker[] regionMarkers)
            {
                if (_jFreeChartWrapperPanel.getJFreeChart() != null)
                {
                    XYPlot plot = (XYPlot) _jFreeChartWrapperPanel.getJFreeChart().getPlot();
                    for (Marker m : regionMarkers)
                    {
                        if (m instanceof IntervalMarker)
                        {
                            plot.removeDomainMarker(m, Layer.BACKGROUND);
                        }
                        else
                        {
                            plot.removeDomainMarker(m);
                        }

                    }
                }
            }
        });
        _regionsJXCollapsiblePane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl R"), JXCollapsiblePane.TOGGLE_ACTION);
        _regionsJXCollapsiblePane.setCollapsed(false);

        setLayout(new BorderLayout());

        add(_jFreeChartWrapperPanel, BorderLayout.CENTER);
        add(_shotDataJXCollapsiblePane, BorderLayout.WEST);
        add(_regionsJXCollapsiblePane, BorderLayout.EAST);
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
        final JMenuItem showShotDataMenuItem = new JCheckBoxMenuItem("Show Shot Data", true);
        showShotDataMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        showShotDataMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                AbstractButton aButton = (AbstractButton) ae.getSource();
                boolean isSelected = aButton.getModel().isSelected();
                _shotDataJXCollapsiblePane.setCollapsed(!isSelected);
            }
        });
        final JMenuItem showRegionsMenuItem = new JCheckBoxMenuItem("Show Regions", true);
        showRegionsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.ALT_MASK));
        showRegionsMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                AbstractButton aButton = (AbstractButton) ae.getSource();
                boolean isSelected = aButton.getModel().isSelected();
                _regionsJXCollapsiblePane.setCollapsed(!isSelected);
            }
        });

        viewMenu.add(showShotDataMenuItem);
        viewMenu.add(showRegionsMenuItem);

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
        if (_jFreeChartWrapperPanel.getJFreeChart() != null)
        {
            XYPlot plot = (XYPlot) _jFreeChartWrapperPanel.getJFreeChart().getPlot();
            plot.clearDomainMarkers();
        }

        _shotDataJXCollapsiblePane.refresh();
        _regionsJXCollapsiblePane.refresh();
    }

    private void populateSpectrumChartWithLIBZPixelSpectrumForCalibrationShot(String calibrationShotId)
    {
        Map<String, LIBZPixelSpectrum> libzPixelSpectra = LibzUnitManager.getInstance().getLIBZPixelSpectra();
        LIBZPixelSpectrum libzPixelSpectum = libzPixelSpectra.get(calibrationShotId);
        Spectrum spectrum = libzPixelSpectum.createSpectrum();
        double minX = spectrum.getValidRange().getMinimumDouble();
        double maxX = spectrum.getValidRange().getMaximumDouble();

        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries xySeries = new XYSeries("Spectrum");

        for (double x = minX; x < maxX; x += 0.05)
        {
            double y = spectrum.getIntensityFunction().value(x);
            xySeries.add(x, y);
        }

        dataset.addSeries(xySeries);

        CalibrationShot cs = LibzUnitManager.getInstance().getCalibrationShots().get(calibrationShotId);

        _jFreeChartWrapperPanel.populateSpectrumChartWithAbstractXYDataset(dataset, cs.displayName + " / " + cs.standard.name + " / " + cs.timeStamp, "Wavelength", "Intensity");
        addChartMouseListenerAndRefreshUi();
    }

    private void addChartMouseListenerAndRefreshUi()
    {
        _jFreeChartWrapperPanel.getChartPanel().addChartMouseListener(new LibzChartMouseListener(_jFreeChartWrapperPanel.getChartPanel(), _jFreeChartWrapperPanel.getJFreeChart(), _mainFrame, new LibzChartMouseListenerCallback()
        {
            @Override
            public void addRegion(String regionName, double wavelengthMin, double wavelengthMax, Marker... associatedMarkers)
            {
                if (_jFreeChartWrapperPanel.getJFreeChart() != null)
                {
                    XYPlot plot = (XYPlot) _jFreeChartWrapperPanel.getJFreeChart().getPlot();
                    plot.clearDomainMarkers();
                }

                _regionsJXCollapsiblePane.addRegion(regionName, wavelengthMin, wavelengthMax, associatedMarkers);
            }
        }));
        _mainFrame.refreshUI();
    }
}