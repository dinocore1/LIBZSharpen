package com.sciaps.async;

import com.sciaps.global.LibzUnitManager;

/**
 *
 * @author sgowen
 */
public final class LibzUnitPushSwingWorker extends BaseLibzUnitApiSwingWorker
{
    public LibzUnitPushSwingWorker(BaseLibzUnitApiSwingWorker.BaseLibzUnitApiSwingWorkerCallback callback)
    {
        super(callback);
    }

    @Override
    protected Boolean doInBackground() throws Exception
    {
        return Boolean.valueOf(_libzUnitApiHandler.pushToLibzUnit(LibzUnitManager.getInstance()));
    }
}