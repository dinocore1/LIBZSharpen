package com.sciaps.view.tabs.defineregions;

import com.sciaps.common.data.Region;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.view.tabs.defineregions.RegionsPanel.RegionsPanelCallback;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import org.apache.commons.lang.math.DoubleRange;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

/**
 *
 * @author sgowen
 */
public final class RegionsJXCollapsiblePane extends JXCollapsiblePane
{
    public interface RegionsJXCollapsiblePaneCallback
    {
        void addChartMarkers(Marker[] regionMarkers);

        void removeChartMarkers(Marker[] regionMarkers);
    }

    private final Map<Object, Marker[]> regionAndAssociatedMarkersMap = new HashMap();
    private final RegionsJXCollapsiblePaneCallback _callback;
    private final RegionsPanel _regionsPanel;

    public RegionsJXCollapsiblePane(Direction direction, RegionsJXCollapsiblePaneCallback callback)
    {
        super(direction);

        _callback = callback;

        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        _regionsPanel = new RegionsPanel(new RegionsPanelCallback()
        {
            @Override
            public void onRegionDeleted(Object regionId)
            {
                Marker[] markersAssociatedWithRegion = regionAndAssociatedMarkersMap.get(regionId);

                if (markersAssociatedWithRegion != null)
                {
                    _callback.removeChartMarkers(markersAssociatedWithRegion);
                }
            }

            @Override
            public void onRegionSelected(Object regionId)
            {
                Marker[] markersAssociatedWithRegion = regionAndAssociatedMarkersMap.get(regionId);

                if (markersAssociatedWithRegion != null)
                {
                    _callback.addChartMarkers(markersAssociatedWithRegion);
                }
            }

            @Override
            public void onRegionUnselected(Object regionId)
            {
                Marker[] markersAssociatedWithRegion = regionAndAssociatedMarkersMap.get(regionId);

                if (markersAssociatedWithRegion != null)
                {
                    _callback.removeChartMarkers(markersAssociatedWithRegion);
                }
            }

            @Override
            public void onRegionEdited(Object regionId, double wavelengthMin, double wavelengthMax)
            {
                ValueMarker valueMarkerMin = (ValueMarker) regionAndAssociatedMarkersMap.get(regionId)[0];
                valueMarkerMin.setValue(wavelengthMin);

                IntervalMarker regionShadeMarker = (IntervalMarker) regionAndAssociatedMarkersMap.get(regionId)[2];
                regionShadeMarker.setStartValue(wavelengthMin);
                regionShadeMarker.setEndValue(wavelengthMax);

                ValueMarker valueMarkerMax = (ValueMarker) regionAndAssociatedMarkersMap.get(regionId)[1];
                valueMarkerMax.setValue(wavelengthMax);
            }
        });
        _regionsPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        add(_regionsPanel);

        for (Map.Entry<String, Region> entry : LibzUnitManager.getInstance().getRegionsManager().getObjects().entrySet())
        {
            Region region = entry.getValue();
            ValueMarker leftMarker = new ValueMarker(region.wavelengthRange.getMinimumDouble());
            leftMarker.setPaint(Color.RED);

            ValueMarker rightMarker = new ValueMarker(region.wavelengthRange.getMaximumDouble());
            rightMarker.setPaint(Color.RED);

            double firstValue = leftMarker.getValue();
            double secondValue = rightMarker.getValue();

            String regionName = region.name;

            final Color c = new Color(255, 60, 24, 63);
            final Marker bst = new IntervalMarker(firstValue, secondValue, c, new BasicStroke(2.0f), null, null, 1.0f);

            bst.setLabel(regionName);
            bst.setLabelAnchor(RectangleAnchor.CENTER);
            bst.setLabelFont(new Font("SansSerif", Font.ITALIC + Font.BOLD, 10));
            bst.setLabelTextAnchor(TextAnchor.BASELINE_CENTER);
            bst.setLabelPaint(new Color(255, 255, 255, 100));

            Marker[] markers = new Marker[3];
            markers[0] = leftMarker;
            markers[1] = rightMarker;
            markers[2] = bst;

            regionAndAssociatedMarkersMap.put(entry.getKey(), markers);
        }
    }

    public void refresh()
    {
        _regionsPanel.refreshData();
        
        if (!isCollapsed())
        {
            _regionsPanel.refreshUI();
        }
    }

    public void addRegion(String regionName, double wavelengthMin, double wavelengthMax, Marker... associatedMarkers)
    {
        try
        {
            Region region = new Region();
            region.wavelengthRange = new DoubleRange(wavelengthMin, wavelengthMax);
            region.name = regionName;

            String newRegionId = LibzUnitManager.getInstance().getRegionsManager().addObject(region);

            Marker[] markers = new Marker[associatedMarkers.length];
            System.arraycopy(associatedMarkers, 0, markers, 0, associatedMarkers.length);

            regionAndAssociatedMarkersMap.put(newRegionId, markers);

            refresh();
        }
        catch (Exception ex)
        {
            Logger.getLogger(RegionsJXCollapsiblePane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}