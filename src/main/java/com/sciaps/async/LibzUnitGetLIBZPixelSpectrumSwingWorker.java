package com.sciaps.async;

import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.swing.libzunitapi.LibzUnitApiHandler;

/**
 *
 * @author sgowen
 */
public final class LibzUnitGetLIBZPixelSpectrumSwingWorker extends BaseLibzUnitApiSwingWorker<LIBZPixelSpectrum>
{
    private final String _shotId;

    public LibzUnitGetLIBZPixelSpectrumSwingWorker(String shotId, Class<? extends LibzUnitApiHandler> clazz, BaseLibzUnitApiSwingWorker.BaseLibzUnitApiSwingWorkerCallback callback)
    {
        super(clazz, callback);

        _shotId = shotId;
    }

    @Override
    protected LIBZPixelSpectrum doInBackground() throws Exception
    {
        LIBZPixelSpectrum spectrum = _libzUnitApiHandler.getLIBZPixelSpectrum(_shotId);

        return spectrum;
    }
}