package com.sciaps;

import com.sciaps.utils.CSVFileFilter;
import com.sciaps.model.CSV;
import com.sciaps.async.DownloadFileSwingWorker;
import com.sciaps.async.DownloadFileSwingWorker.DownloadFileSwingWorkerCallback;
import com.sciaps.utils.CSVReader;
import com.sciaps.utils.CSVUtils;
import com.sciaps.utils.IOUtils;
import com.sciaps.utils.RegexUtil;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public final class Main extends JFrame
{
    private static final String CSV_FILE_URL_REGEX = "(^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]\\.csv)";

    private JFreeChart _jFreeChart;
    private JPanel _chartPanel;
    private DownloadFileSwingWorker _downloadFileSwingWorker;
    private boolean _isChartLoaded = false;

    public Main()
    {
        super("LIBZ Desktop");

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
                int result = chooser.showDialog(Main.this, "Load");
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
                final String url = JOptionPane.showInputDialog(Main.this, "Enter the URL to download the CSV file:");
                if (url != null)
                {
                    if (RegexUtil.findValue(url, CSV_FILE_URL_REGEX, 1) != null)
                    {
                        _downloadFileSwingWorker = new DownloadFileSwingWorker(url, new DownloadFileSwingWorkerCallback()
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

                        _downloadFileSwingWorker.start();
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(Main.this, "Please enter a valid URL with a \".csv\" extension!");
                    }
                }
            }
        });

        fileMenu.add(loadCSVMenuItem);
        fileMenu.add(downloadCSVMenuItem);

        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
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

        viewMenu.add(zoomWavelengthMenuItem);
        viewMenu.add(zoomIntensityMenuItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);

        setJMenuBar(menuBar);

        _chartPanel = new JPanel();
        add(_chartPanel, BorderLayout.CENTER);

        setSize(640, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
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

            String xAxis = CSVUtils.readValueFromCSV(csv, 0, 0);
            String yAxis = CSVUtils.readValueFromCSV(csv, 0, 1);
            _jFreeChart = ChartFactory.createXYLineChart(csvFile.getName(), xAxis, yAxis, dataset);

            XYPlot plot = _jFreeChart.getXYPlot();
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

            ChartPanel chartPanel = new ChartPanel(_jFreeChart);
            chartPanel.setMouseWheelEnabled(true);

            add(chartPanel, BorderLayout.CENTER);

            _chartPanel = chartPanel;

            _isChartLoaded = true;

            setVisible(true);
        }
        catch (IOException e)
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
        }
        finally
        {
            IOUtils.safeClose(is);
        }
    }

    public static void main(String[] args)
    {
        JFrame.setDefaultLookAndFeelDecorated(true);

        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel");
                }
                catch (Exception e)
                {
                    System.out.println("Substance Graphite failed to initialize");
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
                }

                Main main = new Main();
                main.setVisible(true);
            }
        });
    }
}