package com.sciaps.view.tabs.calibrationcurves;

import com.sciaps.async.BaseLibzUnitApiSwingWorker;
import com.sciaps.async.LibzUnitGetLIBZPixelSpectrumSwingWorker;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.IRCurve;
import com.sciaps.common.data.Model;
import com.sciaps.common.data.Standard;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.spectrum.Spectrum;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.libzunitapi.HttpLibzUnitApiHandler;
import com.sciaps.common.swing.utils.JDialogUtils;
import com.sciaps.common.swing.utils.SwingUtils;
import com.sciaps.utils.SpectraUtils;
import com.sciaps.view.tabs.common.CalibrationModelsTablePanel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
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
        void onModelElementSelected(String modelId, AtomicElement element, List<Standard> standards);
    }

    private final CalibrationModelsTablePanel _calibrationModelsTablePanel;
    private final CalibrationModelsInspectorCallback _callback;
    private final Map<String, ModelUIContainer> _modelUIContainer;
    private String _currentlySelectedModelId;
    private ArrayList<Standard> _currentlySelectedModelValidStandards;
    private AtomicElement _currentlySelectedElement;
    private List<Standard> _currentlySelectedStandards;
    private String[] elementsListData;
    private final JList elementsListbox;
    private final JList standardsListbox;
    private int _numShotDataToDownload;
    private int _numShotDataThatFailedToDownload;

    public CalibrationModelsInspectorJXCollapsiblePane(JXCollapsiblePane.Direction direction, CalibrationModelsInspectorCallback callback)
    {
        super(direction);

        _callback = callback;
        _currentlySelectedStandards = new ArrayList();
        _currentlySelectedModelValidStandards = new ArrayList();
        _modelUIContainer = new HashMap();

        getContentPane().setLayout(new BoxLayout(getContentPane(), javax.swing.BoxLayout.X_AXIS));

        elementsListbox = new JList();
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
                    selectStandardsForElementsListboxIndex(elementsListbox.getSelectedIndex());
                    if (_callback != null)
                    {
                        _callback.onModelElementSelected(_currentlySelectedModelId, _currentlySelectedElement, _currentlySelectedStandards);
                    }
                }
            }
        });

        standardsListbox = new JList();
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

                        _modelUIContainer.get(_currentlySelectedModelId).modelElementStandardsMap.get(_currentlySelectedElement).clear();
                        int[] selectedIndices = standardsListbox.getSelectedIndices();
                        for (int i = 0; i < selectedIndices.length; i++)
                        {
                            Standard selectedStandard = _currentlySelectedModelValidStandards.get(selectedIndices[i]);
                            _currentlySelectedStandards.add(selectedStandard);
                            _modelUIContainer.get(_currentlySelectedModelId).modelElementStandardsMap.get(_currentlySelectedElement).add(selectedStandard);
                        }

                        _callback.onModelElementSelected(_currentlySelectedModelId, _currentlySelectedElement, _currentlySelectedStandards);
                    }
                }
            }
        });

        _calibrationModelsTablePanel = new CalibrationModelsTablePanel(new CalibrationModelsTablePanel.CalibrationModelsPanelCallback()
        {
            @Override
            public void onCalibrationModelSelected(String calibrationModelId)
            {
                loadModel(calibrationModelId);
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

        _modelUIContainer.clear();

        if (LibzUnitManager.getInstance().getModelsManager().getObjects() != null)
        {
            for (Map.Entry<String, Model> entry : LibzUnitManager.getInstance().getModelsManager().getObjects().entrySet())
            {
                ModelUIContainer modelUIContainer = new ModelUIContainer();
                populateModelUIContainer(modelUIContainer, entry.getValue());
                _modelUIContainer.put(entry.getKey(), modelUIContainer);
            }
        }
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

    private void selectStandardsForElementsListboxIndex(int elementsListboxIndex)
    {
        _currentlySelectedStandards.clear();

        AtomicElement ae = AtomicElement.getElementBySymbol((String) elementsListData[elementsListboxIndex]);
        List<Integer> selectedIndicesList = new ArrayList();
        for (int i = 0; i < _currentlySelectedModelValidStandards.size(); i++)
        {
            Standard standard = _currentlySelectedModelValidStandards.get(i);
            if (isStandardSelected(_currentlySelectedModelId, ae, standard))
            {
                _currentlySelectedStandards.add(standard);
                selectedIndicesList.add(i);
            }
        }

        int[] selectedIndices = new int[selectedIndicesList.size()];
        for (int i = 0; i < selectedIndices.length; i++)
        {
            selectedIndices[i] = selectedIndicesList.get(i);
        }

        standardsListbox.setSelectedIndices(selectedIndices);
    }

    private boolean isStandardSelected(String modelId, AtomicElement ae, Standard standard)
    {
        for (Standard s : _modelUIContainer.get(modelId).modelElementStandardsMap.get(ae))
        {
            if (s == standard)
            {
                return true;
            }
        }

        return false;
    }

    private void populateModelUIContainer(ModelUIContainer muc, Model model)
    {
        final List<Standard> standards = new ArrayList();
        for (Standard standard : model.standardList)
        {
            standards.add(standard);
        }

        for (final Map.Entry<AtomicElement, IRCurve> entry : model.irs.entrySet())
        {
            muc.modelElementStandardsMap.put(entry.getKey(), standards);
        }
    }

    private void loadModel(String calibrationModelId)
    {
        final Model model = LibzUnitManager.getInstance().getModelsManager().getObjects().get(calibrationModelId);
        if (model != null)
        {
            _currentlySelectedModelId = calibrationModelId;

            _currentlySelectedModelValidStandards.clear();

            final List<String> standardsListData = new ArrayList();
            int numStandardsLackingShotData = 0;
            for (Standard standard : model.standardList)
            {
                final List<Spectrum> spectra = SpectraUtils.getSpectraForStandard(standard);
                if (spectra.size() > 0)
                {
                    standardsListData.add(standard.name);
                    _currentlySelectedModelValidStandards.add(standard);
                }
                else
                {
                    numStandardsLackingShotData++;
                }
            }

            if (numStandardsLackingShotData > 0)
            {
                final int choice = JOptionPane.showOptionDialog(
                        null,
                        "Download missing shot data?",
                        "Confirm",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        new String[]
                        {
                            "No", "Yes"
                        },
                        "Yes");

                if (choice == 1)
                {
                    final JDialog progressDialog = JDialogUtils.createDialogWithMessage(new JFrame(), "Retrieving Shot Data...");
                    List<String> calibrationShotIds = SpectraUtils.getCalibrationShotIdsForMissingStandardsShotData(model.standardList);
                    downloadShotDataForCalibrationIds(calibrationShotIds, progressDialog);
                }

                return;
            }

            elementsListData = new String[model.irs.size()];
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
                    selectStandardsForElementsListboxIndex(0);

                    elementsListbox.setSelectedIndex(0);
                }
            });
            timer.setRepeats(false);
            timer.setCoalesce(true);
            timer.start();
        }
    }

    private void downloadShotDataForCalibrationIds(List<String> calibrationShotIds, final JDialog progressDialog)
    {
        _numShotDataToDownload = calibrationShotIds.size();
        _numShotDataThatFailedToDownload = 0;

        if (_numShotDataToDownload > 0)
        {
            for (String calibrationShotId : calibrationShotIds)
            {
                LibzUnitGetLIBZPixelSpectrumSwingWorker libzUnitGetLIBZPixelSpectrumSwingWorker = new LibzUnitGetLIBZPixelSpectrumSwingWorker(calibrationShotId, HttpLibzUnitApiHandler.class, new BaseLibzUnitApiSwingWorker.BaseLibzUnitApiSwingWorkerCallback<LIBZPixelSpectrum>()
                {
                    @Override
                    public void onComplete(LIBZPixelSpectrum libzPixelSpectum)
                    {
                        if (libzPixelSpectum == null)
                        {
                            onFail();
                        }

                        _numShotDataToDownload--;
                        if (_numShotDataToDownload <= 0)
                        {
                            SwingUtils.hideDialog(progressDialog);

                            if (_numShotDataThatFailedToDownload > 0)
                            {
                                JOptionPane.showConfirmDialog(new JFrame(), "Error retrieving " + _numShotDataThatFailedToDownload + " Shot Data files...", "Error", JOptionPane.DEFAULT_OPTION);
                            }

                            loadModel(_currentlySelectedModelId);
                        }
                    }

                    @Override
                    public void onFail()
                    {
                        _numShotDataThatFailedToDownload++;
                    }
                });

                libzUnitGetLIBZPixelSpectrumSwingWorker.start();
            }

            progressDialog.setVisible(true);
        }
    }

    private final class ModelUIContainer
    {
        public Map<AtomicElement, List<Standard>> modelElementStandardsMap = new HashMap();
    }
}