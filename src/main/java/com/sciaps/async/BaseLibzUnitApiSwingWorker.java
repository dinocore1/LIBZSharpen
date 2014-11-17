package com.sciaps.async;

import com.sciaps.global.InstanceManager;
import com.sciaps.libzunitapi.LibzUnitApiHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author sgowen
 */
public abstract class BaseLibzUnitApiSwingWorker extends SwingWorker<Boolean, Void>
{
    public interface BaseLibzUnitApiSwingWorkerCallback
    {
        void onComplete(boolean isSuccessful);

        void onFail();
    }

    private final BaseLibzUnitApiSwingWorkerCallback _callback;
    protected final LibzUnitApiHandler _libzUnitApiHandler;

    public BaseLibzUnitApiSwingWorker(BaseLibzUnitApiSwingWorkerCallback callback)
    {
        _callback = callback;
        _libzUnitApiHandler = InstanceManager.getInstance().retrieveInstance(LibzUnitApiHandler.class);
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