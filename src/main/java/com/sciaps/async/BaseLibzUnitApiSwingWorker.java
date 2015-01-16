package com.sciaps.async;

import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;
import com.sciaps.global.InstanceManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author sgowen
 * @param <T>
 */
public abstract class BaseLibzUnitApiSwingWorker<T> extends SwingWorker<T, Void>
{
    public interface BaseLibzUnitApiSwingWorkerCallback<T>
    {
        void onComplete(T isSuccessful);

        void onFail();
    }

    private final BaseLibzUnitApiSwingWorkerCallback _callback;
    protected final LibzUnitApiHandler _libzUnitApiHandler;

    public BaseLibzUnitApiSwingWorker(Class<? extends LibzUnitApiHandler> clazz, BaseLibzUnitApiSwingWorkerCallback callback)
    {
        _libzUnitApiHandler = InstanceManager.getInstance().retrieveInstance(clazz);
        _callback = callback;
    }

    public final void start()
    {
        execute();
    }

    @Override
    public void done()
    {
        try
        {
            _callback.onComplete(get());
        }
        catch (Exception e)
        {
            Logger.getLogger(DownloadFileSwingWorker.class.getName()).log(Level.SEVERE, null, e);

            _callback.onFail();
        }
    }
}