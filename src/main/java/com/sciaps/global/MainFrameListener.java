package com.sciaps.global;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sgowen
 */
public final class MainFrameListener implements ComponentListener
{
    private static final Object LOCK = new Object();

    private static MainFrameListener instance;

    public static MainFrameListener getInstance()
    {
        synchronized (LOCK)
        {
            if (instance == null)
            {
                instance = new MainFrameListener();
            }

            return instance;
        }
    }

    public interface MainFrameListenerCallback
    {
        void onMainFrameResized(int width, int height);
    }

    private final List<MainFrameListenerCallback> _callbacks;

    @Override
    public void componentResized(ComponentEvent e)
    {
        Rectangle b = (e.getComponent() != null ? ((Component) e.getComponent()).getBounds() : null);
        if (b != null)
        {
            for (MainFrameListenerCallback c : _callbacks)
            {
                c.onMainFrameResized(b.width, b.height);
            }
        }
    }

    @Override
    public void componentMoved(ComponentEvent e)
    {
        // Unused
    }

    @Override
    public void componentShown(ComponentEvent e)
    {
        // Unused
    }

    @Override
    public void componentHidden(ComponentEvent e)
    {
        // Unused
    }

    public void addMainFrameListenerCallback(MainFrameListenerCallback callback)
    {
        if (!_callbacks.contains(callback))
        {
            _callbacks.add(callback);
        }
    }

    private MainFrameListener()
    {
        _callbacks = new ArrayList<MainFrameListenerCallback>();
    }
}