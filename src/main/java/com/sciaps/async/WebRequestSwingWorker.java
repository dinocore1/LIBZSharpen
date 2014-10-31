package com.sciaps.async;

import com.sciaps.listener.DownloadListener;
import com.sciaps.model.IsAlive;
import com.sciaps.utils.DownloadUtils;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

/**
 *
 * @author sgowen
 *
 * TODO, parameterize this class to work with types other than IsAlive
 */
public final class WebRequestSwingWorker extends SwingWorker<IsAlive, Void>
{
    public interface WebRequestSwingWorkerCallback
    {
        void onComplete(IsAlive isAlive);

        void onFail();
    }

    private final String _ipAddress;
    private final WebRequestSwingWorkerCallback _callback;

    public WebRequestSwingWorker(String ipAddress, WebRequestSwingWorkerCallback callback)
    {
        _ipAddress = ipAddress;
        _callback = callback;
    }

    public void start()
    {
        JPanel panel = new JPanel();
        panel.setOpaque(true);

        execute();
    }

    @Override
    public IsAlive doInBackground()
    {
        IsAlive isAlive = DownloadUtils.connectToLibzUnit(_ipAddress);

        return isAlive;
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