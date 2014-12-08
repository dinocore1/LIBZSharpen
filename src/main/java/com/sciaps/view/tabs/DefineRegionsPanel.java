package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import com.sciaps.common.data.CalibrationShot;
import com.sciaps.common.data.Standard;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.swing.async.DownloadFileSwingWorker;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.listener.LibzChartMouseListener;
import com.sciaps.common.swing.listener.LibzChartMouseListener.LibzChartMouseListenerCallback;
import com.sciaps.common.swing.model.CSV;
import com.sciaps.common.swing.utils.CSVFileFilter;
import com.sciaps.common.swing.utils.CSVReader;
import com.sciaps.common.swing.utils.CSVUtils;
import com.sciaps.common.swing.utils.IOUtils;
import com.sciaps.common.swing.utils.RegexUtil;
import com.sciaps.common.swing.view.JFreeChartWrapperPanel;
import com.sciaps.view.LIBZUnitConnectedPanel;
import com.sciaps.view.tabs.defineregions.RegionsJXCollapsiblePane;
import com.sciaps.view.tabs.defineregions.RegionsJXCollapsiblePane.RegionsJXCollapsiblePaneCallback;
import com.sciaps.common.swing.view.ShotDataJXCollapsiblePane;
import com.sciaps.common.swing.view.ShotDataJXCollapsiblePane.ShotDataJXCollapsiblePaneCallback;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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
    private static final String CSV_FILE_URL_REGEX = "(^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]\\.csv)";
    private static final String TAB_NAME = "Define Regions";

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
                CalibrationShot cs = LibzUnitManager.getInstance().getCalibrationShots().get(calibrationShotId);
                populateSpectrumChartWithLIBZPixelSpectrumForCalibrationShot(cs);
            }
        });

        _shotDataJXCollapsiblePane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl S"), JXCollapsiblePane.TOGGLE_ACTION);
        _shotDataJXCollapsiblePane.setCollapsed(false);

        _regionsJXCollapsiblePane = new RegionsJXCollapsiblePane(JXCollapsiblePane.Direction.LEFT, new RegionsJXCollapsiblePaneCallback()
        {
            @Override
            public void removeChartMarkers(Marker[] regionMarkers)
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
    public void customizeMenuBar(JMenuBar menuBar)
    {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem loadCSVMenuItem = new JMenuItem("Load CSV", KeyEvent.VK_L);
        loadCSVMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));
        loadCSVMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new CSVFileFilter());
                int result = chooser.showDialog(_mainFrame, "Load");
                if (result == JFileChooser.APPROVE_OPTION)
                {
                    populateSpectrumChartWithContentsOfCSVFile(chooser.getSelectedFile());
                }
            }
        });
        JMenuItem downloadCSVMenuItem = new JMenuItem("Download CSV", KeyEvent.VK_D);
        downloadCSVMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK));
        downloadCSVMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                final String url = JOptionPane.showInputDialog(_mainFrame, "Enter the URL to download the CSV file:");
                if (url != null)
                {
                    if (RegexUtil.findValue(url, CSV_FILE_URL_REGEX, 1) != null)
                    {
                        DownloadFileSwingWorker downloadFileSwingWorker = new DownloadFileSwingWorker(url, new DownloadFileSwingWorker.DownloadFileSwingWorkerCallback()
                        {
                            @Override
                            public void onComplete(File downloadedFile)
                            {
                                populateSpectrumChartWithContentsOfCSVFile(downloadedFile);
                            }

                            @Override
                            public void onFail()
                            {
                                JOptionPane.showMessageDialog(null, "Failed to download csv file from url:\n" + url);
                            }
                        });

                        downloadFileSwingWorker.start();
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(_mainFrame, "Please enter a valid URL with a \".csv\" extension!");
                    }
                }
            }
        });

        fileMenu.add(loadCSVMenuItem);
        fileMenu.add(downloadCSVMenuItem);

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

        menuBar.add(fileMenu);
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

    private void populateSpectrumChartWithLIBZPixelSpectrumForCalibrationShot(CalibrationShot cs)
    {
        List<LIBZPixelSpectrum> libzPixelSpectra = LibzUnitManager.getInstance().getLIBZPixelSpectra();
        String shotNumberString = RegexUtil.findValue(cs.displayName, ".*?([0-9]+)", 1);
        int shotNumber = Integer.parseInt(shotNumberString);
        LIBZPixelSpectrum libzPixelSpectum = libzPixelSpectra.get(shotNumber - 1);
        double minX = libzPixelSpectum.getValidRange().getMinimumDouble();
        double maxX = libzPixelSpectum.getValidRange().getMaximumDouble();

        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries xySeries = new XYSeries("Spectrum");

        for (double x = minX; x < maxX; x += 0.05)
        {
            double y = libzPixelSpectum.getIntensityFunction().value(x);
            xySeries.add(x, y);
        }

        dataset.addSeries(xySeries);

        Standard shotStandard = LibzUnitManager.getInstance().getStandards().get(cs.standardId);
        _jFreeChartWrapperPanel.populateSpectrumChartWithAbstractXYDataset(dataset, cs.displayName + " / " + shotStandard.name + " / " + cs.timeStamp, "Wavelength", "Intensity");
        addChartMouseListenerAndRefreshUi();
    }

    private void populateSpectrumChartWithContentsOfCSVFile(File csvFile)
    {
        InputStream is = null;
        try
        {
            is = new BufferedInputStream(new FileInputStream(csvFile));
            CSV csv = CSVReader.readCSVFromInputStream(is);

            XYSeriesCollection dataset = new XYSeriesCollection();
            XYSeries xySeries = new XYSeries("Spectrum");

            int numRows = CSVUtils.getNumRowsInCSV(csv);
            for (int i = 1; i < numRows; i++)
            {
                double x = CSVUtils.readDoubleFromCSV(csv, i, 0);
                double y = CSVUtils.readDoubleFromCSV(csv, i, 1);
                xySeries.add(x, y);
            }

            dataset.addSeries(xySeries);

            String xAxisName = CSVUtils.readValueFromCSV(csv, 0, 0);
            String yAxisName = CSVUtils.readValueFromCSV(csv, 0, 1);

            _jFreeChartWrapperPanel.populateSpectrumChartWithAbstractXYDataset(dataset, csvFile.getName(), xAxisName, yAxisName);
            addChartMouseListenerAndRefreshUi();
        }
        catch (IOException e)
        {
            Logger.getLogger(LIBZUnitConnectedPanel.class.getName()).log(Level.SEVERE, null, e);
        }
        finally
        {
            IOUtils.safeClose(is);
        }
    }

    private void addChartMouseListenerAndRefreshUi()
    {
        _jFreeChartWrapperPanel.getChartPanel().addChartMouseListener(new LibzChartMouseListener(_jFreeChartWrapperPanel.getChartPanel(), _jFreeChartWrapperPanel.getJFreeChart(), _mainFrame, new LibzChartMouseListenerCallback()
        {
            @Override
            public void addRegion(String regionName, double wavelengthMin, double wavelengthMax, Marker... associatedMarkers)
            {
                _regionsJXCollapsiblePane.addRegion(regionName, wavelengthMin, wavelengthMax, associatedMarkers);
            }
        }));
        _mainFrame.refreshUI();
    }
}