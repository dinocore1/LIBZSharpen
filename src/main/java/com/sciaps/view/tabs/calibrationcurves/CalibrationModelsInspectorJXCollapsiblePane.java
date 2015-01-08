package com.sciaps.view.tabs.calibrationcurves;

import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.IRCurve;
import com.sciaps.common.data.Model;
import com.sciaps.common.data.Standard;
import com.sciaps.common.spectrum.Spectrum;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.utils.SpectraUtil;
import com.sciaps.view.tabs.common.CalibrationModelsTablePanel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.JXCollapsiblePane;

/**
 *
 * @author sgowen
 */
public final class CalibrationModelsInspectorJXCollapsiblePane extends JXCollapsiblePane
{
    public interface CalibrationModelsInspectorCallback
    {
        void onModelElementSelected(Model model, AtomicElement element, List<Standard> standards);
    }

    private final CalibrationModelsTablePanel _calibrationModelsTablePanel;
    private final CalibrationModelsInspectorCallback _callback;
    private Model _currentlySelectedModel;
    private List<Standard> _currentlySelectedModelValidStandards;
    private AtomicElement _currentlySelectedElement;
    private List<Standard> _currentlySelectedStandards;

    public CalibrationModelsInspectorJXCollapsiblePane(JXCollapsiblePane.Direction direction, CalibrationModelsInspectorCallback callback)
    {
        super(direction);

        _callback = callback;
        _currentlySelectedStandards = new ArrayList();
        _currentlySelectedModelValidStandards = new ArrayList();

        getContentPane().setLayout(new BoxLayout(getContentPane(), javax.swing.BoxLayout.X_AXIS));

        final JList elementsListbox = new JList();
        elementsListbox.setFont(new Font("Serif", Font.BOLD, 18));
        elementsListbox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        elementsListbox.setBorder(BorderFactory.createEmptyBorder(15, 0, 60, 0));
        elementsListbox.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting() && elementsListbox.getModel().getSize() > 0 && elementsListbox.getSelectedIndex() != -1)
                {
                    _currentlySelectedElement = AtomicElement.getElementBySymbol((String) elementsListbox.getSelectedValue());

                    if (_callback != null)
                    {
                        _callback.onModelElementSelected(_currentlySelectedModel, _currentlySelectedElement, _currentlySelectedStandards);
                    }
                }
            }
        });

        final JList standardsListbox = new JList();
        standardsListbox.setFont(new Font("Serif", Font.BOLD, 18));
        standardsListbox.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        standardsListbox.setBorder(BorderFactory.createEmptyBorder(15, 0, 60, 0));
        standardsListbox.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting() && elementsListbox.getModel().getSize() > 0 && elementsListbox.getSelectedIndex() != -1)
                {
                    if (_callback != null && _currentlySelectedElement != null)
                    {
                        _currentlySelectedStandards.clear();

                        int[] selectedIndices = standardsListbox.getSelectedIndices();
                        for (int i = 0; i < selectedIndices.length; i++)
                        {
                            _currentlySelectedStandards.add(_currentlySelectedModelValidStandards.get(selectedIndices[i]));
                        }

                        _callback.onModelElementSelected(_currentlySelectedModel, _currentlySelectedElement, _currentlySelectedStandards);
                    }
                }
            }
        });

        _calibrationModelsTablePanel = new CalibrationModelsTablePanel(new CalibrationModelsTablePanel.CalibrationModelsPanelCallback()
        {
            @Override
            public void onCalibrationModelSelected(String calibrationModelId)
            {
                Model model = LibzUnitManager.getInstance().getModelsManager().getObjects().get(calibrationModelId);
                if (model != null)
                {
                    _currentlySelectedModel = model;

                    final String[] elementsListData = new String[model.irs.size()];
                    int i = 0;
                    for (Map.Entry<AtomicElement, IRCurve> entry : model.irs.entrySet())
                    {
                        elementsListData[i] = entry.getKey().symbol;
                        i++;
                    }
                    elementsListbox.setModel(new AbstractListModel<String>()
                    {
                        @Override
                        public int getSize()
                        {
                            return elementsListData.length;
                        }

                        @Override
                        public String getElementAt(int i)
                        {
                            return elementsListData[i];
                        }
                    });

                    elementsListbox.invalidate();
                    
                    _currentlySelectedModelValidStandards.clear();

                    final List<String> standardsListData = new ArrayList();
                    for (Standard standard : model.standardList)
                    {
                        final List<Spectrum> spectra = SpectraUtil.getSpectraForStandard(standard);
                        if (spectra.size() > 0)
                        {
                            standardsListData.add(standard.name);
                            _currentlySelectedModelValidStandards.add(standard);
                        }
                    }
                    standardsListbox.setModel(new AbstractListModel<String>()
                    {
                        @Override
                        public int getSize()
                        {
                            return standardsListData.size();
                        }

                        @Override
                        public String getElementAt(int i)
                        {
                            return standardsListData.get(i);
                        }
                    });

                    standardsListbox.invalidate();

                    Timer timer = new Timer(1000, new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent ae)
                        {
                            standardsListbox.setSelectionInterval(0, standardsListData.size() - 1);
                            elementsListbox.setSelectedIndex(0);
                        }
                    });
                    timer.setRepeats(false);
                    timer.setCoalesce(true);
                    timer.start();
                }
            }
        });
        _calibrationModelsTablePanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JPanel standardsAndElementsContainer = new JPanel();
        standardsAndElementsContainer.setLayout(new BoxLayout(standardsAndElementsContainer, javax.swing.BoxLayout.Y_AXIS));
        standardsAndElementsContainer.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        standardsAndElementsContainer.add(createJLabelWithText("Elements"));
        JScrollPane elementsListboxScrollPane = new JScrollPane();
        elementsListboxScrollPane.setViewportView(elementsListbox);
        standardsAndElementsContainer.add(elementsListboxScrollPane);

        standardsAndElementsContainer.add(createJLabelWithText("Standards"));
        JScrollPane standardsListboxScrollPane = new JScrollPane();
        standardsListboxScrollPane.setViewportView(standardsListbox);
        standardsAndElementsContainer.add(standardsListboxScrollPane);

        standardsAndElementsContainer.setSize(standardsAndElementsContainer.getPreferredSize().width, standardsAndElementsContainer.getPreferredSize().height * 2);

        add(_calibrationModelsTablePanel);
        add(standardsAndElementsContainer);
    }

    public void refresh()
    {
        _calibrationModelsTablePanel.refresh();
    }

    private JLabel createJLabelWithText(String text)
    {
        JLabel label = new JLabel(text);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        label.setFont(new Font("Serif", Font.BOLD, 24));
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getPreferredSize().height));
        label.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        return label;
    }
}