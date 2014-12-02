package com.sciaps.view.tabs;

import com.sciaps.MainFrame;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.swing.async.DownloadFileSwingWorker;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.model.CSV;
import com.sciaps.utils.CSVFileFilter;
import com.sciaps.utils.CSVReader;
import com.sciaps.utils.CSVUtils;
import com.sciaps.utils.IOUtils;
import com.sciaps.utils.RegexUtil;
import com.sciaps.view.LIBZUnitConnectedPanel;
import com.sciaps.view.tabs.defineregions.RegionsJXCollapsiblePane;
import com.sciaps.view.tabs.defineregions.RegionsJXCollapsiblePane.RegionsJXCollapsiblePaneCallback;
import com.sciaps.view.tabs.defineregions.ShotDataJXCollapsiblePane;
import com.sciaps.view.tabs.defineregions.ShotDataJXCollapsiblePane.ShotDataJXCollapsiblePaneCallback;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
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
import java.util.ArrayList;
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
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

/**
 *
 * @author sgowen
 */
public final class DefineRegionsPanel extends AbstractTabPanel
{
    private static final String CSV_FILE_URL_REGEX = "(^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]\\.csv)";
    private static final String TAB_NAME = "Define Regions";

    private final List<ValueMarker> _valueMarkersAddedToChart;
    private final ShotDataJXCollapsiblePane _shotDataJXCollapsiblePane;
    private final RegionsJXCollapsiblePane _regionsJXCollapsiblePane;

    private JPanel _chartPanel;
    private JFreeChart _jFreeChart;
    private boolean _isChartLoaded;

    public DefineRegionsPanel(MainFrame mainFrame)
    {
        super(mainFrame);

        _chartPanel = new JPanel();
        _isChartLoaded = false;
        _valueMarkersAddedToChart = new ArrayList<ValueMarker>();

        _shotDataJXCollapsiblePane = new ShotDataJXCollapsiblePane(JXCollapsiblePane.Direction.RIGHT, new ShotDataJXCollapsiblePaneCallback()
        {
            @Override
            public void shotDataSelected(int shotDataIndex)
            {
                populateSpectrumChartWithLIBZPixelSpectrumIndex(shotDataIndex);
            }
        });
        _shotDataJXCollapsiblePane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl S"), JXCollapsiblePane.TOGGLE_ACTION);
        _shotDataJXCollapsiblePane.setCollapsed(false);

        _regionsJXCollapsiblePane = new RegionsJXCollapsiblePane(JXCollapsiblePane.Direction.LEFT, new RegionsJXCollapsiblePaneCallback()
        {
            @Override
            public void removeChartMarkers(Marker[] regionMarkers)
            {
                XYPlot plot = (XYPlot) _jFreeChart.getPlot();
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

        add(_chartPanel, BorderLayout.CENTER);
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

        menuBar.add(fileMenu);
        menuBar.add(chartMenu);
    }

    @Override
    public void onDisplay()
    {
        if (_jFreeChart != null)
        {
            XYPlot plot = (XYPlot) _jFreeChart.getPlot();
            plot.clearDomainMarkers();
        }

        _shotDataJXCollapsiblePane.refresh();
        _regionsJXCollapsiblePane.refresh();
    }

    private void populateSpectrumChartWithLIBZPixelSpectrumIndex(int libzPixelSpectrumIndex)
    {
        List<LIBZPixelSpectrum> libzPixelSpectra = LibzUnitManager.getInstance().getLIBZPixelSpectra();
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
        _jFreeChart = ChartFactory.createXYLineChart(chartName, xAxisName, yAxisName, dataset);

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

        _valueMarkersAddedToChart.clear();

        remove(_chartPanel);

        final ChartPanel chartPanel = new ChartPanel(_jFreeChart);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.addChartMouseListener(new ChartMouseListener()
        {
            @Override
            public void chartMouseClicked(ChartMouseEvent event)
            {
                Point2D p = event.getTrigger().getPoint();
                Rectangle2D plotArea = chartPanel.getScreenDataArea();
                XYPlot plot = (XYPlot) _jFreeChart.getPlot();
                double chartX = plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
                double chartY = plot.getRangeAxis().java2DToValue(p.getY(), plotArea, plot.getRangeAxisEdge());

                System.out.println("Mouse click at Screen coordinates (" + event.getTrigger().getXOnScreen() + ", " + event.getTrigger().getYOnScreen() + ") are (" + chartX + ", " + chartY + ") in the chart");

                ValueMarker marker = new ValueMarker(chartX);
                marker.setPaint(Color.RED);

                if (_valueMarkersAddedToChart.size() % 2 == 0)
                {
                    _valueMarkersAddedToChart.add(marker);
                    plot.addDomainMarker(marker);
                }
                else
                {
                    String[] elements = getArrayOfElements();
                    String element = (String) JOptionPane.showInputDialog(_mainFrame, "Please specify an element for this region:", "Elements", JOptionPane.INFORMATION_MESSAGE, null, elements, null);

                    if (element != null)
                    {
                        int firstValue = (int) Math.min(marker.getValue(), _valueMarkersAddedToChart.get(0).getValue());
                        int secondValue = (int) Math.max(marker.getValue(), _valueMarkersAddedToChart.get(0).getValue());

                        String regionName = element + "_" + firstValue + "-" + secondValue;

                        final Color c = new Color(255, 60, 24, 63);
                        final Marker bst = new IntervalMarker(firstValue, secondValue, c, new BasicStroke(2.0f), null, null, 1.0f);

                        bst.setLabel(regionName);
                        bst.setLabelAnchor(RectangleAnchor.CENTER);
                        bst.setLabelFont(new Font("SansSerif", Font.ITALIC + Font.BOLD, 10));
                        bst.setLabelTextAnchor(TextAnchor.BASELINE_CENTER);
                        bst.setLabelPaint(new Color(255, 255, 255, 100));

                        plot.addDomainMarker(marker);
                        plot.addDomainMarker(bst, Layer.BACKGROUND);

                        _regionsJXCollapsiblePane.addRegion(regionName, firstValue, secondValue, _valueMarkersAddedToChart.get(0), marker, bst);

                        _valueMarkersAddedToChart.clear();
                    }
                }
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

    private String[] getArrayOfElements()
    {
        List<String> elements = new ArrayList<String>();
        for (int i = 1; i <= LibzUnitManager.NUM_ATOMIC_ELEMENTS; i++)
        {
            AtomicElement ae = AtomicElement.getElementByAtomicNum(i);
            elements.add(ae.symbol);
        }

        String[] elementsArray = new String[elements.size()];
        elementsArray = elements.toArray(elementsArray);

        return elementsArray;
    }
}