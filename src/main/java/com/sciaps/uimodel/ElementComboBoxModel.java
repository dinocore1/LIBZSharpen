package com.sciaps.uimodel;

import com.sciaps.common.AtomicElement;

import javax.swing.*;
import java.util.Arrays;


public class ElementComboBoxModel extends AbstractListModel<AtomicElement> implements ComboBoxModel<AtomicElement> {

    private final AtomicElement[] mElements;
    private AtomicElement selectedObject;

    public ElementComboBoxModel() {
        mElements = new AtomicElement[AtomicElement.values().length];
        System.arraycopy(AtomicElement.values(), 0, mElements, 0, mElements.length);
        Arrays.sort(mElements, AtomicElement.Atomic_NumberComparator);
    }

    @Override
    public int getSize() {
        return mElements.length;
    }

    @Override
    public AtomicElement getElementAt(int i) {
        return mElements[i];
    }

    @Override
    public void setSelectedItem(Object anObject) {
        if(this.selectedObject != null && !this.selectedObject.equals(anObject) || this.selectedObject == null && anObject != null) {
            this.selectedObject = (AtomicElement) anObject;
            this.fireContentsChanged(this, -1, -1);
        }
    }

    @Override
    public Object getSelectedItem() {
        return selectedObject;
    }
}
