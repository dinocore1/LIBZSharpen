package com.sciaps.async;

import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;

/**
 *
 * @author sgowen
 */
public final class LibzUnitConnectSwingWorker extends BaseLibzUnitApiSwingWorker<Boolean>
{
    public LibzUnitConnectSwingWorker(BaseLibzUnitApiSwingWorker.BaseLibzUnitApiSwingWorkerCallback callback)
    {
        super(callback);
    }

    @Override
    protected Boolean doInBackground() throws Exception
    {
        return _libzUnitApiHandler.connectToLibzUnit();
    }
}