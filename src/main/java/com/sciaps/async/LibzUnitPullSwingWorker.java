package com.sciaps.async;

import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;

/**
 *
 * @author sgowen
 */
public final class LibzUnitPullSwingWorker extends BaseLibzUnitApiSwingWorker<Boolean>
{
    public LibzUnitPullSwingWorker(Class<? extends LibzUnitApiHandler> clazz, BaseLibzUnitApiSwingWorker.BaseLibzUnitApiSwingWorkerCallback callback)
    {
        super(clazz, callback);
    }

    @Override
    protected Boolean doInBackground() throws Exception
    {
        return Boolean.valueOf(_libzUnitApiHandler.pullFromLibzUnit());
    }
}