package com.sciaps.view.tabs.calibrationcurves;

import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.IRCurve;
import com.sciaps.common.data.Model;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.utils.LibzTableUtils;
import com.sciaps.view.tabs.common.CalibrationModelsTablePanel;
import java.awt.Font;
import java.util.Map;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.JXCollapsiblePane;

/**
 *
 * @author sgowen
 */
public final class CalibrationModelsAndElementsJXCollapsiblePane extends JXCollapsiblePane
{
    public interface ModelElementSelectedCallback
    {
        void onModelElementSelected(Model model, AtomicElement element);
    }

    private final CalibrationModelsTablePanel _calibrationModelsTablePanel;
    private final ModelElementSelectedCallback _callback;
    private Model _currentlySelectedModel;
    private AtomicElement _currentlySelectedElement;

    public CalibrationModelsAndElementsJXCollapsiblePane(JXCollapsiblePane.Direction direction, ModelElementSelectedCallback callback)
    {
        super(direction);

        _callback = callback;

        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.X_AXIS));

        // Create a new listbox control
        final JList elementsListbox = new JList();
        elementsListbox.setFont(new Font("Serif", Font.BOLD, 18));
        elementsListbox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
                        _callback.onModelElementSelected(_currentlySelectedModel, _currentlySelectedElement);
                    }
                }
            }
        });

        _calibrationModelsTablePanel = new CalibrationModelsTablePanel(new CalibrationModelsTablePanel.CalibrationModelsPanelCallback()
        {
            @Override
            public void onCalibrationModelSelected(String calibrationModelId)
            {
                Model model = LibzUnitManager.getInstance().getCalibrationModels().get(calibrationModelId);
                if (model != null)
                {
                    _currentlySelectedModel = model;

                    final String listData[] = new String[model.irs.size()];
                    int i = 0;
                    for (Map.Entry<AtomicElement, IRCurve> entry : model.irs.entrySet())
                    {
                        listData[i] = entry.getKey().symbol;
                        i++;
                    }
                    elementsListbox.setModel(new AbstractListModel<String>()
                    {
                        @Override
                        public int getSize()
                        {
                            return listData.length;
                        }

                        @Override
                        public String getElementAt(int i)
                        {
                            return listData[i];
                        }
                    });

                    elementsListbox.invalidate();
                }
            }
        });
        _calibrationModelsTablePanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        add(_calibrationModelsTablePanel);
        add(elementsListbox);
    }

    public void refresh()
    {
        _calibrationModelsTablePanel.refresh();
    }
}