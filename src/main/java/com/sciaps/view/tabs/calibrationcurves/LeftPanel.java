package com.sciaps.view.tabs.calibrationcurves;

import com.devsmart.*;
import com.devsmart.swing.BackgroundTask;
import com.google.common.collect.ComparisonChain;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.IRCurve;
import com.sciaps.common.data.Model;
import com.sciaps.common.data.Standard;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.libzunitapi.HttpLibzUnitApiHandler;
import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;
import com.sciaps.common.swing.utils.TableColumnAdjuster;
import com.sciaps.common.swing.view.ModelCellRenderer;
import com.sciaps.components.IRBox;
import com.sciaps.utils.SpectraUtils;
import com.sciaps.view.tabs.CalibrationCurvesPanel;
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
            enabled.set(row, (Boolean)value);

            if(enabled.get(row)) {
                while(_selectedCurve.excludedStandards.contains(standards.get(row))) {
                    _selectedCurve.excludedStandards.remove(standards.get(row));
                }
            } else {
                _selectedCurve.excludedStandards.add(standards.get(row));
            }

            markModelModified();

            displayCalibrationGraph();

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
                numShots.add(SpectraUtils.getShotsForStandard(standards.get(i)).size());
            }

            fireTableDataChanged();
        }

        public boolean hasData(int row) {
            return numShots.get(row) > 0;
        }
    }

    private final CalibrationCurvesPanel mCalCurvesPanel;

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

    public LeftPanel(CalibrationCurvesPanel calCurvesPanel) {
        super();
        mCalCurvesPanel = calCurvesPanel;

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
            markModelModified();
            displayCalibrationGraph();
        }
    };

    private final ItemListener mOnForceZeroChange = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent itemEvent) {
            _selectedCurve.forceZero = _forceZeroCheckbox.isSelected();
            markModelModified();
            displayCalibrationGraph();
        }
    };

    public void refresh() {
        _modelModel.removeAllElements();
        ArrayList<Model> modelList = new ArrayList<Model>(LibzUnitManager.getInstance().getModelsManager().getObjects().values());
        Collections.sort(modelList, new Comparator<Model>() {
            @Override
            public int compare(Model o1, Model o2) {
                return ComparisonChain.start()
                        .compare(o1.name, o2.name)
                        .result();
            }
        });
        for(Model m : modelList) {
            _modelModel.addElement(m);
        }
    }

    private void loadCalibrationData(final List<String> calibrationShotIds, final Runnable onFinished) {
        BackgroundTask.runBackgroundTask(new BackgroundTask() {

            LoadingPane loadingPane;

            @Override
            public void onBefore() {
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(LeftPanel.this);

                loadingPane = new LoadingPane();
                mCalCurvesPanel.mLayeredPane.add(loadingPane, new Integer(1));
                mCalCurvesPanel.revalidate();
            }

            @Override
            public void onBackground() {
                int count = 0;
                final int total = calibrationShotIds.size();
                LibzUnitApiHandler apiHandler = InstanceManager.getInstance().retrieveInstance(HttpLibzUnitApiHandler.class);
                for(String calShotId : calibrationShotIds) {
                    apiHandler.getLIBZPixelSpectrum(calShotId);

                    count++;
                    final int dialogCount = count;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            loadingPane.mProgressBar.setMinimum(0);
                            loadingPane.mProgressBar.setMaximum(total);
                            loadingPane.mProgressBar.setValue(dialogCount);
                        }
                    });

                }
            }

            @Override
            public void onAfter() {
                mCalCurvesPanel.mLayeredPane.remove(loadingPane);

                if(onFinished != null) {
                    onFinished.run();
                }
            }
        }, mLoadQueue);
    }

    private void displayCalibrationGraph() {
        final ArrayList<Standard> enabledStandards = new ArrayList<Standard>(_standardsTableModel.standards.size());
        final ArrayList<Standard> disabledStandards = new ArrayList<Standard>(_standardsTableModel.standards.size());

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
        List<String> missingCalShots = SpectraUtils.getCalibrationShotIdsForMissingStandardsShotData(enabledStandards);
        if(missingCalShots.size() > 0){
            loadCalibrationData(missingCalShots, onAllShotsLoaded);
        } else {
            onAllShotsLoaded.run();
        }
    }

    private void selectElementCurve(AtomicElement element) {

        _polyDegreeSpinner.removeChangeListener(mOnPolyDegreeChange);
        _forceZeroCheckbox.removeItemListener(mOnForceZeroChange);

        if(element == null) {
            return;
        }
        _selectedCurve = _selectedModel.irs.get(element);
        mIRBox.setIRRatio(_selectedCurve);


        //setup standards
        _standardsTableModel.setStandards(_selectedModel.standardList);
        for(int i=0;i< _standardsTableModel.standards.size();i++){
            _standardsTableModel.enabled.set(i, !_selectedCurve.excludedStandards.contains(_standardsTableModel.standards.get(i)));
        }

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

    private void markModelModified() {
        LibzUnitManager.getInstance().getModelsManager().markObjectAsModified(_selectedModel.mId);
    }

}