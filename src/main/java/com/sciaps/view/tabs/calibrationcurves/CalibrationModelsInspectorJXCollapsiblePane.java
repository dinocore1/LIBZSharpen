package com.sciaps.view.tabs.calibrationcurves;

import com.devsmart.swing.BackgroundTask;
import com.google.common.base.*;
import com.google.common.collect.ComparisonChain;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.IRCurve;
import com.sciaps.common.data.Model;
import com.sciaps.common.data.Standard;
import com.sciaps.common.swing.global.InstanceManager;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.libzunitapi.HttpLibzUnitApiHandler;
import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;
import com.sciaps.common.swing.view.ModelCellRenderer;
import com.sciaps.utils.SpectraUtils;
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
public final class CalibrationModelsInspectorJXCollapsiblePane extends JPanel
{

    static Logger logger = LoggerFactory.getLogger(CalibrationModelsInspectorJXCollapsiblePane.class);

    public interface CalibrationModelsInspectorCallback
    {
        void onModelElementSelected(IRCurve curve, List<Standard> enabledStandards, List<Standard> disabledStandards);
    }


    private class StandardsTableModel extends AbstractTableModel {

        final String[] columnNames = new String[]{ "Enabled", "Standard" };
        ArrayList<Standard> standards = new ArrayList<Standard>();
        ArrayList<Boolean> enabled = new ArrayList<Boolean>();
        ArrayList<Boolean> hasData = new ArrayList<Boolean>();

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
                if(hasData.get(row)) {
                    return standards.get(row).name;
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
            hasData.clear();

            standards.addAll(standardList);
            Collections.sort(standards, new Comparator<Standard>() {
                @Override
                public int compare(Standard standard, Standard standard2) {
                    return standard.name.compareTo(standard2.name);
                }
            });

            for(int i=0;i<standards.size();i++){
                enabled.add(false);
                hasData.add(SpectraUtils.hasCalibrationShotData(standards.get(i)));
            }

            fireTableDataChanged();
        }
    }

    private final JList<AtomicElement> elementsListbox;
    private final JTable standardsListbox;

    private Model _selectedModel;
    private IRCurve _selectedCurve;
    private JComboBox<Model> _modelComboBox;
    private DefaultListModel<AtomicElement> _elementListModel = new DefaultListModel<AtomicElement>();
    private SpinnerNumberModel _polyDegreeModel = new SpinnerNumberModel(0, 0, 10, 1);
    private JSpinner _polyDegreeSpinner;
    private final JCheckBox _forceZeroCheckbox;
    private DefaultComboBoxModel<Model> _modelModel = new DefaultComboBoxModel<Model>();
    private StandardsTableModel _standardsTable = new StandardsTableModel();

    private final CalibrationModelsInspectorCallback _callback;

    private AtomicElement _currentlySelectedElement;



    public CalibrationModelsInspectorJXCollapsiblePane(CalibrationModelsInspectorCallback callback)
    {
        super();

        _callback = callback;


        setLayout(new MigLayout("fillx"));


        _modelComboBox = new JComboBox<Model>(_modelModel);
        _modelComboBox.setRenderer(new ModelCellRenderer());
        _modelComboBox.addActionListener(mOnModelSelected);
        //_modelComboBox.setMaximumSize(new Dimension(100, 100));
        add(_modelComboBox, "w 50mm::, growx, wrap");

        elementsListbox = new JList<AtomicElement>();
        JScrollPane elementScollePane = new JScrollPane(elementsListbox);
        elementsListbox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        elementsListbox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true), "Curve"));
        elementsListbox.setModel(_elementListModel);
        elementsListbox.addListSelectionListener(mOnElementSelection);
        add(elementScollePane, "h 200::, grow, gapy 3mm, wrap");

        JPanel p = new JPanel(new MigLayout("fillx"));
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true), "Settings"));

        JLabel degreeLabel = new JLabel("Degree");
        p.add(degreeLabel, "split");
        _polyDegreeSpinner = new JSpinner(_polyDegreeModel);
        p.add(_polyDegreeSpinner, "align left");

        _forceZeroCheckbox = new JCheckBox("Force Zero");
        p.add(_forceZeroCheckbox, "wrap");


        standardsListbox = new JTable(_standardsTable);
        JScrollPane standardsScrollPane = new JScrollPane(standardsListbox);
        p.add(standardsScrollPane, "span, h 200::, w :100:, gapy 3mm, grow");

        add(p, "gapy 3mm, grow, wrap");

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

            JDialog progressDialog;
            public JProgressBar progressBar;

            @Override
            public void onBefore() {
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(CalibrationModelsInspectorJXCollapsiblePane.this);

                progressDialog = new JDialog(topFrame, "Loading Data...");
                progressBar = new JProgressBar(0, 100);
                progressBar.setIndeterminate(false);
                progressDialog.add(BorderLayout.CENTER, progressBar);
                progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                progressDialog.setSize(600, 160);

                progressDialog.pack();
                progressDialog.setLocationRelativeTo(topFrame);
                progressDialog.setVisible(true);
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
                            progressBar.setValue(dialogCount);
                            progressBar.setMinimum(0);
                            progressBar.setMaximum(total);
                        }
                    });


                }
            }

            @Override
            public void onAfter() {
                progressDialog.setVisible(false);
                progressDialog.dispose();
                if(onFinished != null) {
                    onFinished.run();
                }
            }
        });
    }

    private void displayCalibrationGraph() {
        final ArrayList<Standard> enabledStandards = new ArrayList<Standard>(_standardsTable.standards.size());
        final ArrayList<Standard> disabledStandards = new ArrayList<Standard>(_standardsTable.standards.size());

        for(int i=0;i<_standardsTable.standards.size();i++){
            final Standard standard = _standardsTable.standards.get(i);
            if(_standardsTable.hasData.get(i)) {
                if (_standardsTable.enabled.get(i)) {
                    enabledStandards.add(standard);
                } else {
                    disabledStandards.add(standard);
                }
            }
        }

        Runnable onAllShotsLoaded = new Runnable() {

            @Override
            public void run() {
                if(_callback != null) {
                    _callback.onModelElementSelected(_selectedCurve, enabledStandards, disabledStandards);
                }
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

        _currentlySelectedElement = element;
        if(element == null) {
            return;
        }
        _selectedCurve = _selectedModel.irs.get(element);


        //setup standards
        _standardsTable.setStandards(_selectedModel.standardList);
        for(int i=0;i<_standardsTable.standards.size();i++){
            _standardsTable.enabled.set(i, !_selectedCurve.excludedStandards.contains(_standardsTable.standards.get(i)));
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

    private final class ModelUIContainer
    {
        public Map<AtomicElement, List<Standard>> modelElementStandardsMap = new HashMap();
    }
}