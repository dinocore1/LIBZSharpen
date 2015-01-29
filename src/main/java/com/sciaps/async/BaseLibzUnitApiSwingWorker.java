package com.sciaps.async;

import com.google.inject.Inject;
import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    @Inject
    LibzUnitApiHandler _libzUnitApiHandler;


    public BaseLibzUnitApiSwingWorker(BaseLibzUnitApiSwingWorkerCallback callback) {
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