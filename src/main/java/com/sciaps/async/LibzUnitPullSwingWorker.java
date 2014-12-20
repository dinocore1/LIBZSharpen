package com.sciaps.async;

import com.sciaps.common.swing.temp.LibzUnitApiHandler;

/**
 *
 * @author sgowen
 */
public final class LibzUnitPullSwingWorker extends BaseLibzUnitApiSwingWorker
{
    public LibzUnitPullSwingWorker(BaseLibzUnitApiSwingWorker.BaseLibzUnitApiSwingWorkerCallback callback, Class<? extends LibzUnitApiHandler> clazz)
    {
        super(callback, clazz);
    }

    @Override
    protected Boolean doInBackground() throws Exception
    {
        return Boolean.valueOf(_libzUnitApiHandler.pullFromLibzUnit());
    }
}