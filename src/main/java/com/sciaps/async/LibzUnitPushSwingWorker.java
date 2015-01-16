package com.sciaps.async;

import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;

/**
 *
 * @author sgowen
 */
public final class LibzUnitPushSwingWorker extends BaseLibzUnitApiSwingWorker<Boolean>
{
    public LibzUnitPushSwingWorker(Class<? extends LibzUnitApiHandler> clazz, BaseLibzUnitApiSwingWorker.BaseLibzUnitApiSwingWorkerCallback callback)
    {
        super(clazz, callback);
    }

    @Override
    protected Boolean doInBackground() throws Exception
    {
        return Boolean.valueOf(_libzUnitApiHandler.pushToLibzUnit());
    }
}