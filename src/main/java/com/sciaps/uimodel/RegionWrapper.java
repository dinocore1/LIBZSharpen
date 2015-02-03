package com.sciaps.uimodel;


import com.sciaps.common.data.Region;

public class RegionWrapper {

    public final Region mRegion;

    public RegionWrapper(Region r) {
        mRegion = r;
    }

    @Override
    public String toString() {
        return mRegion.name;
    }

    @Override
    public int hashCode() {
        return mRegion.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return mRegion.equals(obj);
    }
}
