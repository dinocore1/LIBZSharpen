package com.sciaps.async;

import com.sciaps.global.LibzUnitManager;

/**
 *
 * @author sgowen
 */
public final class LibzUnitPullSwingWorker extends BaseLibzUnitApiSwingWorker
{
    public LibzUnitPullSwingWorker(BaseLibzUnitApiSwingWorker.BaseLibzUnitApiSwingWorkerCallback callback)
    {
        super(callback);
    }

    @Override
    protected Boolean doInBackground() throws Exception
    {
        return Boolean.valueOf(_libzUnitApiHandler.pullFromLibzUnit(LibzUnitManager.getInstance()));
    }
}