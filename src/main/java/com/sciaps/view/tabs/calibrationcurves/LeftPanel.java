package com.sciaps.view.tabs.calibrationcurves;

import com.devsmart.*;
import com.devsmart.swing.BackgroundTask;
import com.google.common.collect.ComparisonChain;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.CalibrationShot;
import com.sciaps.common.data.IRCurve;
import com.sciaps.common.data.Model;
import com.sciaps.common.data.Standard;
import com.sciaps.common.objtracker.DBObjTracker;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.swing.OverlayPane;
import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;
import com.sciaps.common.swing.utils.TableColumnAdjuster;
import com.sciaps.common.swing.view.ModelCellRenderer;
import com.sciaps.components.IRBox;
import com.sciaps.events.PullEvent;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 *
 * @author sgowen
 */
public final class LeftPanel extends JPanel
{

    static Logger logger = LoggerFactory.getLogger(LeftPanel.class);

    private class StandardsTableModel extends AbstractTableModel {

        final String[] columnNames = new String[]{ "Enabled", "Standard" };
        ArrayList<Standard> standards = new ArrayList<Standard>();
        ArrayList<Boolean> enabled = new ArrayList<Boolean>();
        ArrayList<Integer> numShots = new ArrayList<Integer>();

        @Override
        public int getRowCount() {
            return standards.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }



        @Override
        public Object getValueAt(int row, int column) {
            if(column == 0){
                return enabled.get(row);
            } else if(column == 1){
                int shots = numShots.get(row);
                if(shots > 0) {
                    return String.format("%s [%d shots]", standards.get(row).name, shots);
                } else {
                    return String.format("%s *NODATA*", standards.get(row).name);
                }

            } else {
                return null;
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 0;
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            boolean bval = (Boolean)value;
            if(enabled.get(row) != bval) {
                enabled.set(row, bval);

                if (enabled.get(row)) {
                    while (_selectedCurve.excludedStandards.contains(standards.get(row))) {
                        _selectedCurve.excludedStandards.remove(standards.get(row));
                    }
                } else {
                    _selectedCurve.excludedStandards.add(standards.get(row));
                }

                mObjTracker.markModified(_selectedModel);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        displayCalibrationGraph();
                    }
                });
            }


        }

        @Override
        public Class getColumnClass(int column) {
            if(column == 0){
                return Boolean.class;
            } else {
                return String.class;
            }
        }

        public void setStandards(List<Standard> standardList) {
            standards.clear();
            enabled.clear();
            numShots.clear();

            standards.addAll(standardList);
            Collections.sort(standards, new Comparator<Standard>() {
                @Override
                public int compare(Standard standard, Standard standard2) {
                    return standard.name.compareTo(standard2.name);
                }
            });

            for(int i=0;i<standards.size();i++){
                enabled.add(false);
                numShots.add(getNumShotsForStandard(standards.get(i)));
            }

            fireTableDataChanged();
        }

        public boolean hasData(int row) {
            return numShots.get(row) > 0;
        }
    }

    private CalibrationCurvesPanel mCalCurvesPanel;

    private final JList<AtomicElement> elementsListbox;
    private final JTable _standardsTable;

    private Model _selectedModel;
    private IRCurve _selectedCurve;
    private JComboBox<Model> _modelComboBox;
    private DefaultListModel<AtomicElement> _elementListModel = new DefaultListModel<AtomicElement>();
    private SpinnerNumberModel _polyDegreeModel = new SpinnerNumberModel(0, 0, 10, 1);
    private JSpinner _polyDegreeSpinner;
    private final JCheckBox _forceZeroCheckbox;
    private DefaultComboBoxModel<Model> _modelModel = new DefaultComboBoxModel<Model>();
    private StandardsTableModel _standardsTableModel = new StandardsTableModel();
    private TaskQueue mLoadQueue = new TaskQueue(ThreadUtils.IOThreads);

    private final IRBox mIRBox;

    @Inject
    DBObjTracker mObjTracker;

    EventBus mGlobalEventBus;

    @Inject
    void setGlobalEventBus(EventBus eventBus) {
        mGlobalEventBus = eventBus;
        mGlobalEventBus.register(this);
    }

    @Inject
    LibzUnitApiHandler mUnitApiHandler;

    public LeftPanel() {

        setLayout(new MigLayout("fill"));

        _modelComboBox = new JComboBox<Model>(_modelModel);
        _modelComboBox.setRenderer(new ModelCellRenderer());
        _modelComboBox.addActionListener(mOnModelSelected);
        add(_modelComboBox, "w 50mm::, growx, wrap");

        elementsListbox = new JList<AtomicElement>();
        JScrollPane elementScollePane = new JScrollPane(elementsListbox);
        elementScollePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true), "Curve"));
        elementsListbox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        elementsListbox.setModel(_elementListModel);
        elementsListbox.addListSelectionListener(mOnElementSelection);
        add(elementScollePane, "h 120::, gapy 2mm, grow, wrap");

        JPanel p = new JPanel(new MigLayout("fill"));
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true), "Settings"));

        JLabel degreeLabel = new JLabel("Degree");
        p.add(degreeLabel, "split");
        _polyDegreeSpinner = new JSpinner(_polyDegreeModel);
        p.add(_polyDegreeSpinner, "align left");

        _forceZeroCheckbox = new JCheckBox("Force Zero");
        p.add(_forceZeroCheckbox, "wrap");


        _standardsTable = new JTable(_standardsTableModel);
        _standardsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        _standardsTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        TableColumnAdjuster tca = new TableColumnAdjuster(_standardsTable);
        tca.adjustColumns();

        JScrollPane standardsScrollPane = new JScrollPane(_standardsTable);
        p.add(standardsScrollPane, "span, w :100:, h 75::, grow, gapy 2mm");

        add(p, "gapy 3mm, growx, growy 50, wrap");

        JPanel irPanel = new JPanel(new MigLayout("fill"));
        irPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true), "Ratio"));
        mIRBox = new IRBox();
        irPanel.add(mIRBox, "grow");

        add(irPanel, "gapy 2mm, h 200::, growx, growy 50");

    }

    public void setCalCurvesPanel(CalibrationCurvesPanel calCurvesPanel) {
        mCalCurvesPanel = calCurvesPanel;
    }

    private ActionListener mOnModelSelected = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            Model model = (Model)_modelComboBox.getSelectedItem();
            loadModel(model);
        }
    };

    private ListSelectionListener mOnElementSelection = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if(!e.getValueIsAdjusting()) {
                selectElementCurve(elementsListbox.getSelectedValue());
            }
        }
    };

    private final ChangeListener mOnPolyDegreeChange = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            _selectedCurve.degree = (Integer)_polyDegreeSpinner.getValue();
            mObjTracker.markModified(_selectedModel);
            displayCalibrationGraph();
        }
    };

    private final ItemListener mOnForceZeroChange = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent itemEvent) {
            _selectedCurve.forceZero = _forceZeroCheckbox.isSelected();
            mObjTracker.markModified(_selectedModel);
            displayCalibrationGraph();
        }
    };

    @Subscribe
    public void onPullEvent(PullEvent event) {
        if(event.mSuccess) {
            _modelModel.removeAllElements();
            ArrayList<Model> modelList = new ArrayList<Model>();
            Iterator<Model> it = mObjTracker.getAllObjectsOfType(Model.class);
            while (it.hasNext()) {
                modelList.add(it.next());
            }
            Collections.sort(modelList, new Comparator<Model>() {
                @Override
                public int compare(Model o1, Model o2) {
                    return ComparisonChain.start()
                            .compare(o1.name, o2.name)
                            .result();
                }
            });
            for (Model m : modelList) {
                _modelModel.addElement(m);
            }
        }
    }

    private void loadCalibrationData(final List<String> calibrationShotIds, final Runnable onFinished) {
        BackgroundTask.runBackgroundTask(new BackgroundTask() {

            public OverlayPane mOverlayPane;
            public JProgressBar mProgressBar;

            @Override
            public void onBefore() {
                mOverlayPane = new OverlayPane();
                mOverlayPane.mContentPanel.setLayout(new MigLayout());

                mProgressBar = new JProgressBar(0, calibrationShotIds.size());
                mProgressBar.setIndeterminate(false);
                mOverlayPane.mContentPanel.add(mProgressBar, "w 200!, wrap");

                JLabel label = new JLabel("Downloading Data...");
                mOverlayPane.mContentPanel.add(label, "align center");

                mCalCurvesPanel.mFramePanel.add(mOverlayPane, new Integer(1));
                mCalCurvesPanel.revalidate();
                mCalCurvesPanel.repaint();

            }

            @Override
            public void onBackground() {
                try {
                    mUnitApiHandler.getLIBZPixelSpectrum(calibrationShotIds, new LibzUnitApiHandler.DownloadCallback() {

                        int count = 0;

                        @Override
                        public void onData(String shotid, LIBZPixelSpectrum data) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setValue(++count);
                                }
                            });
                        }
                    });
                } catch (IOException e) {
                    logger.error("", e);
                }

            }

            @Override
            public void onAfter() {
                mCalCurvesPanel.mFramePanel.remove(mOverlayPane);

                if(onFinished != null) {
                    onFinished.run();
                }

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        mCalCurvesPanel.revalidate();
                        mCalCurvesPanel.repaint();
                    }
                });

            }
        }, mLoadQueue);
    }

    private void displayCalibrationGraph() {
        final Set<Standard> enabledStandards = new HashSet<Standard>();
        final Set<Standard> disabledStandards = new HashSet<Standard>();

        for(int i=0;i< _standardsTableModel.standards.size();i++){
            final Standard standard = _standardsTableModel.standards.get(i);
            if(_standardsTableModel.hasData(i)) {
                if (_standardsTableModel.enabled.get(i)) {
                    enabledStandards.add(standard);
                } else {
                    disabledStandards.add(standard);
                }
            }
        }

        Runnable onAllShotsLoaded = new Runnable() {

            @Override
            public void run() {
                mCalCurvesPanel.populateSpectrumChartWithModelAndElement(_selectedCurve, enabledStandards, disabledStandards);
            }
        };
        List<String> missingCalShots = getShotIdsForStandards(enabledStandards);
        if(missingCalShots.size() > 0){
            loadCalibrationData(missingCalShots, onAllShotsLoaded);
        } else {
            onAllShotsLoaded.run();
        }
    }

    private List<String> getShotIdsForStandards(Set<Standard> standards) {
        LinkedList<String> retval = new LinkedList<String>();
        Iterator<CalibrationShot> it = mObjTracker.getAllObjectsOfType(CalibrationShot.class);
        while(it.hasNext()) {
            CalibrationShot shot = it.next();
            if(standards.contains(shot.standard)){
                retval.add(shot.mId);
            }
        }
        return retval;
    }

    private int getNumShotsForStandard(Standard standard) {
        int retval = 0;
        Iterator<CalibrationShot> it = mObjTracker.getAllObjectsOfType(CalibrationShot.class);
        while(it.hasNext()) {
            CalibrationShot shot = it.next();
            if(shot.standard == standard){
                retval++;
            }
        }
        return retval;
    }

    private void selectElementCurve(AtomicElement element) {

        if(element == null) {
            return;
        }

        _polyDegreeSpinner.removeChangeListener(mOnPolyDegreeChange);
        _forceZeroCheckbox.removeItemListener(mOnForceZeroChange);

        _selectedCurve = _selectedModel.irs.get(element);
        mIRBox.setIRRatio(_selectedCurve);


        //setup standards
        _standardsTableModel.setStandards(_selectedModel.standardList);
        for(int i=0;i< _standardsTableModel.standards.size();i++){
            _standardsTableModel.enabled.set(i, !_selectedCurve.excludedStandards.contains(_standardsTableModel.standards.get(i)));
        }
        _standardsTableModel.fireTableDataChanged();

        //setup poly degree
        _polyDegreeModel.setValue(_selectedCurve.degree);

        _forceZeroCheckbox.setSelected(_selectedCurve.forceZero);


        displayCalibrationGraph();

        _polyDegreeSpinner.addChangeListener(mOnPolyDegreeChange);
        _forceZeroCheckbox.addItemListener(mOnForceZeroChange);
    }


    private void loadModel(Model model) {
        _selectedModel = model;
        if(model == null){
            return;
        }

        //setup element list
        Set<AtomicElement> elementSet = _selectedModel.irs.keySet();
        final AtomicElement[] elementList = elementSet.toArray(new AtomicElement[elementSet.size()]);
        Arrays.sort(elementList, AtomicElement.Atomic_NumberComparator);

        _elementListModel.clear();
        for(AtomicElement e : elementList) {
            _elementListModel.addElement(e);
        }


        if(elementList.length > 0) {
            elementsListbox.setSelectedIndex(0);
        }
    }

}