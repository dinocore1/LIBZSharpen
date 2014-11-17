package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import com.sciaps.async.DownloadFileSwingWorker;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.global.LibzSharpenManager;
import com.sciaps.model.CSV;
import com.sciaps.utils.CSVFileFilter;
import com.sciaps.utils.CSVReader;
import com.sciaps.utils.CSVUtils;
import com.sciaps.utils.IOUtils;
import com.sciaps.utils.RegexUtil;
import com.sciaps.view.LIBZUnitConnectedPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author sgowen
 */
public final class DefineRegionsPanel extends AbstractTabPanel
{
    private static final String CSV_FILE_URL_REGEX = "(^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]\\.csv)";
    private static final String TAB_NAME = "Define Regions";

    private JPanel _chartPanel;
    private boolean _isChartLoaded = false;

    public DefineRegionsPanel(MainFrame mainFrame)
    {
        super(mainFrame);

        _chartPanel = new JPanel();

        setLayout(new BorderLayout());

        add(_chartPanel, BorderLayout.CENTER);
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

                if (_isChartLoaded)
                {
                    ((ChartPanel) _chartPanel).setDomainZoomable(isSelected);
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

                if (_isChartLoaded)
                {
                    ((ChartPanel) _chartPanel).setRangeZoomable(isSelected);
                }
            }
        });

        chartMenu.add(zoomWavelengthMenuItem);
        chartMenu.add(zoomIntensityMenuItem);

        // This Shot Data menu is temporary but we might end up liking it
        JMenu shotDataMenu = new JMenu("Shot Data");
        shotDataMenu.setMnemonic(KeyEvent.VK_S);
        List<LIBZPixelSpectrum> libzPixelSpectra = LibzSharpenManager.getInstance().getLIBZPixelSpectra();
        for (int i = 0; i < libzPixelSpectra.size(); i++)
        {
            final int libzPixelSpectrumIndex = i;
            JMenuItem menuItem = new JMenuItem("Shot Data " + libzPixelSpectrumIndex);
            menuItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    populateSpectrumChartWithLIBZPixelSpectrumIndex(libzPixelSpectrumIndex);
                }
            });

            shotDataMenu.add(menuItem);
        }

        menuBar.add(fileMenu);
        menuBar.add(chartMenu);
        menuBar.add(shotDataMenu);
    }
    
    @Override
    public void onDisplay()
    {
        // TODO
    }

    private void populateSpectrumChartWithLIBZPixelSpectrumIndex(int libzPixelSpectrumIndex)
    {
        List<LIBZPixelSpectrum> libzPixelSpectra = LibzSharpenManager.getInstance().getLIBZPixelSpectra();
        LIBZPixelSpectrum libzPixelSpectum = libzPixelSpectra.get(libzPixelSpectrumIndex);
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

        populateSpectrumChartWithAbstractXYDataset(dataset, "Shot Data " + libzPixelSpectrumIndex, "Wavelength", "Intensity");
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

            populateSpectrumChartWithAbstractXYDataset(dataset, csvFile.getName(), xAxisName, yAxisName);
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

    private void populateSpectrumChartWithAbstractXYDataset(XYSeriesCollection dataset, String chartName, String xAxisName, String yAxisName)
    {
        final JFreeChart jFreeChart = ChartFactory.createXYLineChart(chartName, xAxisName, yAxisName, dataset);

        XYPlot plot = jFreeChart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        // sets paint color for each series
        renderer.setSeriesPaint(0, Color.GREEN);
        renderer.setSeriesShape(0, new Line2D.Double());

        // sets thickness for series (using strokes)
        renderer.setSeriesStroke(0, new BasicStroke(1.0f));

        // sets paint color for plot outlines
        plot.setOutlinePaint(Color.LIGHT_GRAY);
        plot.setOutlineStroke(new BasicStroke(2.0f));

        // sets renderer for lines
        plot.setRenderer(renderer);

        // sets plot background
        plot.setBackgroundPaint(Color.DARK_GRAY);

        // sets paint color for the grid lines
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.BLACK);

        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.BLACK);

        remove(_chartPanel);

        final ChartPanel chartPanel = new ChartPanel(jFreeChart);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.addChartMouseListener(new ChartMouseListener()
        {
            @Override
            public void chartMouseClicked(ChartMouseEvent event)
            {
                Point2D p = event.getTrigger().getPoint();
                Rectangle2D plotArea = chartPanel.getScreenDataArea();
                XYPlot plot = (XYPlot) jFreeChart.getPlot();
                double chartX = plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
                double chartY = plot.getRangeAxis().java2DToValue(p.getY(), plotArea, plot.getRangeAxisEdge());

                System.out.println("Mouse click at Screen coordinates (" + event.getTrigger().getXOnScreen() + ", " + event.getTrigger().getYOnScreen() + ") are (" + chartX + ", " + chartY + ") in the chart");
                
                // TODO, set markers on the domain axis
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent event)
            {
                // Empty
            }
        });

        add(chartPanel, BorderLayout.CENTER);

        _chartPanel = chartPanel;

        _isChartLoaded = true;

        _mainFrame.refreshUI();
    }
}