package com.sciaps.async;

import com.sciaps.common.swing.temp.LibzUnitApiHandler;

/**
 *
 * @author sgowen
 */
public final class LibzUnitConnectSwingWorker extends BaseLibzUnitApiSwingWorker
{
    public LibzUnitConnectSwingWorker(BaseLibzUnitApiSwingWorkerCallback callback, Class<? extends LibzUnitApiHandler> clazz)
    {
        super(callback, clazz);
    }

    @Override
    protected Boolean doInBackground() throws Exception
    {
        return _libzUnitApiHandler.connectToLibzUnit();
    }
}