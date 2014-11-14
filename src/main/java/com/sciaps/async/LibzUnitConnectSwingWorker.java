package com.sciaps.async;

import com.sciaps.model.IsAlive;
import com.sciaps.utils.LibzUnitApiUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

/**
 *
 * @author sgowen
 */
public final class LibzUnitConnectSwingWorker extends SwingWorker<IsAlive, Void>
{
    public interface LibzUnitConnectSwingWorkerCallback
    {
        void onComplete(IsAlive isAlive);

        void onFail();
    }

    private final String _ipAddress;
    private final LibzUnitConnectSwingWorkerCallback _callback;

    public LibzUnitConnectSwingWorker(String ipAddress, LibzUnitConnectSwingWorkerCallback callback)
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
        return LibzUnitApiUtils.connectToLibzUnit(_ipAddress);
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