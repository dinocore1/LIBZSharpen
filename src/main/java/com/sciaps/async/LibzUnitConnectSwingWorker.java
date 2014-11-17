package com.sciaps.async;

import com.sciaps.global.LibzUnitManager;

/**
 *
 * @author sgowen
 */
public final class LibzUnitConnectSwingWorker extends BaseLibzUnitApiSwingWorker
{
    public LibzUnitConnectSwingWorker(BaseLibzUnitApiSwingWorkerCallback callback)
    {
        super(callback);
    }

    @Override
    protected Boolean doInBackground() throws Exception
    {
        return _libzUnitApiHandler.connectToLibzUnit(LibzUnitManager.getInstance());
    }
}