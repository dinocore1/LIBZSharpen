package com.sciaps.async;

import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;

/**
 *
 * @author sgowen
 */
public final class LibzUnitConnectSwingWorker extends BaseLibzUnitApiSwingWorker<Boolean>
{
    public LibzUnitConnectSwingWorker(Class<? extends LibzUnitApiHandler> clazz, BaseLibzUnitApiSwingWorker.BaseLibzUnitApiSwingWorkerCallback callback)
    {
        super(clazz, callback);
    }

    @Override
    protected Boolean doInBackground() throws Exception
    {
        return _libzUnitApiHandler.connectToLibzUnit();
    }
}