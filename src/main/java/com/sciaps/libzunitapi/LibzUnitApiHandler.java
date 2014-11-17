package com.sciaps.libzunitapi;

import com.sciaps.global.LibzUnitManager;

/**
 *
 * @author sgowen
 */
public interface LibzUnitApiHandler
{
    boolean connectToLibzUnit(LibzUnitManager libzUnitManager);

    boolean pullFromLibzUnit(LibzUnitManager libzUnitManager);

    boolean pushToLibzUnit(LibzUnitManager libzUnitManager);
}