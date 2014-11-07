package com.sciaps.async;

import com.sciaps.global.LibzSharpenManager;
import com.sciaps.utils.DownloadUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

/**
 *
 * @author sgowen
 */
public final class LibzUnitPullSwingWorker extends SwingWorker<Boolean, Void>
{
    public interface LibzUnitPullSwingWorkerCallback
    {
        void onComplete(boolean isSuccessful);

        void onFail();
    }

    private final LibzUnitPullSwingWorkerCallback _callback;

    public LibzUnitPullSwingWorker(LibzUnitPullSwingWorkerCallback callback)
    {
        _callback = callback;
    }

    public void start()
    {
        JPanel panel = new JPanel();
        panel.setOpaque(true);

        execute();
    }

    @Override
    public Boolean doInBackground()
    {
        return Boolean.valueOf(DownloadUtils.pullFromLibzUnit(LibzSharpenManager.getInstance()));
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