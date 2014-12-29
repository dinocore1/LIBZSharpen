package com.sciaps.temp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.sciaps.common.AtomicElement;
import com.sciaps.common.data.CalibrationShot;
import com.sciaps.common.data.IRCurve;
import com.sciaps.common.data.IRRatio;
import com.sciaps.common.data.Model;
import com.sciaps.common.data.Region;
import com.sciaps.common.data.Standard;
import com.sciaps.common.spectrum.LIBZPixelSpectrum;
import com.sciaps.common.swing.global.LibzUnitManager;
import com.sciaps.common.swing.model.IsAlive;
import com.sciaps.common.swing.temp.HttpLibzUnitApiHandler;
import com.sciaps.common.swing.temp.LibzUnitApiHandler;
import com.sciaps.common.swing.utils.IOUtils;
import com.sciaps.common.swing.utils.JsonUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.apache.commons.lang.math.DoubleRange;

/**
 *
 * @author sgowen
 */
public final class MockedLibzUnitApiHandler implements LibzUnitApiHandler
{
    @Override
    public boolean connectToLibzUnit()
    {
        IsAlive isAlive = new IsAlive();
        isAlive.libzUnitUniqueIdentifier = "UNIQUE_ID";

        LibzUnitManager.getInstance().setLibzUnitUniqueIdentifier(isAlive.libzUnitUniqueIdentifier);

        return true;
    }

    @Override
    public boolean pullFromLibzUnit()
    {
        Map<String, Standard> standards = getStandards(null);
        LibzUnitManager.getInstance().setStandards(standards);

        Map<String, CalibrationShot> calibrationShots = getCalibrationShots(null);
        LibzUnitManager.getInstance().setCalibrationShots(calibrationShots);

        if (calibrationShots != null)
        {
            List<LIBZPixelSpectrum> libzPixelSpectra = new ArrayList<LIBZPixelSpectrum>();
            for (Map.Entry entry : calibrationShots.entrySet())
            {
                LIBZPixelSpectrum libzPixelSpectum = getLIBZPixelSpectrum("res", entry.getKey());
                if (libzPixelSpectum == null)
                {
                    Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.WARNING, "LIBZPixelSpectrum retrieved via id: {0} was NULL! Continuing to download the other LIBZPixelSpectrum objects...", entry.getKey());
                }
                else
                {
                    libzPixelSpectra.add(libzPixelSpectum);
                }
            }

            LibzUnitManager.getInstance().setLIBZPixelSpectra(libzPixelSpectra);
        }

        Map<String, Region> regions = getRegions(null);
        LibzUnitManager.getInstance().setRegions(regions);

        Map<String, IRRatio> intensityRatios = getIntensityRatios(null);
        LibzUnitManager.getInstance().setIntensityRatios(intensityRatios);

        Map<String, Model> calModels = getCalibrationModels(null);
        LibzUnitManager.getInstance().setCalibrationModels(calModels);

        return LibzUnitManager.getInstance().isValidAfterPull();
    }

    @Override
    public boolean pushToLibzUnit()
    {
        if (postStandards(null, LibzUnitManager.getInstance().getStandards()))
        {
            if (postRegions(null, LibzUnitManager.getInstance().getRegions()))
            {
                if (postIntensityRatios(null, LibzUnitManager.getInstance().getIntensityRatios()))
                {
                    if (postCalibrationModels(null, LibzUnitManager.getInstance().getCalibrationModels()))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public Map<String, Standard> getStandards(final String getStandardsUrlString)
    {
        try
        {
            URL url = ClassLoader.getSystemResource("res/standards.json");
            String json = IOUtils.extractStringFromInputStream(url.openStream());
            Type type = new TypeToken<Map<String, Standard>>()
            {
            }.getType();
            Map<String, Standard> standards = JsonUtils.deserializeJsonIntoType(json, type);

            System.out.println("# of Standards pulled from LIBZ Unit: " + standards.size());

            return standards;
        }
        catch (IOException e)
        {
            throw new RuntimeException("ERROR");
        }
    }

    @Override
    public Map<String, CalibrationShot> getCalibrationShots(final String getCalibrationShotsUrlString)
    {
        Map<String, CalibrationShot> calibrationShots = new HashMap<String, CalibrationShot>();
        CalibrationShot cal1 = new CalibrationShot();
        cal1.displayName = "Shot Data 1";
        cal1.timeStamp = new Date();
        cal1.standard = LibzUnitManager.getInstance().getStandards().get("123456789");
        CalibrationShot cal2 = new CalibrationShot();
        cal2.displayName = "Shot Data 2";
        cal2.timeStamp = new Date();
        cal2.standard = LibzUnitManager.getInstance().getStandards().get("1");
        CalibrationShot cal3 = new CalibrationShot();
        cal3.displayName = "Shot Data 3";
        cal3.timeStamp = new Date();
        cal3.standard = LibzUnitManager.getInstance().getStandards().get("12");
        CalibrationShot cal4 = new CalibrationShot();
        cal4.displayName = "Shot Data 4";
        cal4.timeStamp = new Date();
        cal4.standard = LibzUnitManager.getInstance().getStandards().get("123");

        calibrationShots.put("a4653d0b-4c1f-429b-9cb2-0403817f8e16", cal1);
        calibrationShots.put("a4653d0b-4c1f-429b-9cb2-0403817f8e17", cal2);
        calibrationShots.put("a4653d0b-4c1f-429b-9cb2-0403817f8e18", cal3);
        calibrationShots.put("a4653d0b-4c1f-429b-9cb2-0403817f8e19", cal4);

        return calibrationShots;
    }

    @Override
    public LIBZPixelSpectrum getLIBZPixelSpectrum(final String getLIBZPixelSpectrumUrlString, final Object shotId)
    {
        JsonReader jsonReader = null;

        try
        {
            URL url = ClassLoader.getSystemResource(getLIBZPixelSpectrumUrlString + "/" + shotId + ".json.gz");

            BufferedInputStream bis = new BufferedInputStream(url.openStream());
            GZIPInputStream gzis = new GZIPInputStream(bis);
            jsonReader = new JsonReader(new InputStreamReader(gzis));

            Gson gson = new GsonBuilder().create();
            final LIBZPixelSpectrum.SerializationObj obj = gson.fromJson(jsonReader, LIBZPixelSpectrum.SerializationObj.class);
            if (obj == null)
            {
                return null;
            }

            LIBZPixelSpectrum libzPixelSpectrum = new LIBZPixelSpectrum(obj);

            return libzPixelSpectrum;
        }
        catch (IOException e)
        {
            Logger.getLogger(HttpLibzUnitApiHandler.class.getName()).log(Level.SEVERE, null, e);

            return null;
        }
        finally
        {
            IOUtils.safeClose(jsonReader);
        }
    }

    @Override
    public Map<String, Region> getRegions(final String getRegionsUrlString)
    {
        Map<String, Region> regions = new HashMap();
        Region region1 = new Region();
        region1.wavelengthRange = new DoubleRange(640.0, 670.0);
        region1.name = "Cu_640-670";

        Region region2 = new Region();
        region2.wavelengthRange = new DoubleRange(380.0, 410.0);
        region2.name = "Al_380-410";

        regions.put("7af7ec16-b1ea-46a4-bf6b-1dfab8318d33", region1);
        regions.put("24d7c7d1-3abc-48b9-b07a-292ea44f0738", region2);

        System.out.println("# of Regions pulled from LIBZ Unit: " + regions.size());

        return regions;
    }

    @Override
    public Map<String, IRRatio> getIntensityRatios(final String getIntensityRatiosUrlString)
    {
        Map<String, IRRatio> intensityRatios = new HashMap<String, IRRatio>();
        IRRatio intensityRatio = new IRRatio();
        intensityRatio.name = "Copper Finder 12/10/14";
        intensityRatio.element = AtomicElement.Copper;
        intensityRatio.numerator = new ArrayList<Region>();
        intensityRatio.numerator.add(LibzUnitManager.getInstance().getRegions().get("7af7ec16-b1ea-46a4-bf6b-1dfab8318d33"));
        intensityRatio.denominator = new ArrayList<Region>();
        intensityRatio.denominator.add(LibzUnitManager.getInstance().getRegions().get("24d7c7d1-3abc-48b9-b07a-292ea44f0738"));

        IRRatio intensityRatio2 = new IRRatio();
        intensityRatio2.name = "Aluminum Finder 12/10/14";
        intensityRatio2.element = AtomicElement.Aluminum;
        intensityRatio2.numerator = new ArrayList<Region>();
        intensityRatio2.numerator.add(LibzUnitManager.getInstance().getRegions().get("24d7c7d1-3abc-48b9-b07a-292ea44f0738"));
        intensityRatio2.denominator = new ArrayList<Region>();
        intensityRatio2.denominator.add(LibzUnitManager.getInstance().getRegions().get("7af7ec16-b1ea-46a4-bf6b-1dfab8318d33"));

        intensityRatios.put("UNIQUE_ID_IR_1", intensityRatio);
        intensityRatios.put("UNIQUE_ID_IR_2", intensityRatio2);

        return intensityRatios;
    }

    @Override
    public Map<String, Model> getCalibrationModels(final String getCalibrationModelsUrlString)
    {
        Map<String, Model> calModels = new HashMap();
        Model calModel = new Model();
        calModel.name = "Copper Cal Model";
        calModel.standardList.add(LibzUnitManager.getInstance().getStandards().get("12345678891234567891234567890"));
        IRRatio irRatio = LibzUnitManager.getInstance().getIntensityRatios().get("UNIQUE_ID_IR_1");
        IRCurve irCurve = new IRCurve();
        irCurve.name = irRatio.name;
        irCurve.element = irRatio.element;
        irCurve.numerator = irRatio.numerator;
        irCurve.denominator = irRatio.denominator;
        calModel.irs.put(AtomicElement.Copper, irCurve);

        calModels.put(java.util.UUID.randomUUID().toString(), calModel);

        return calModels;
    }

    @Override
    public boolean postStandards(final String postStandardsUrlString, Map<String, Standard> standards)
    {
        return true;
    }

    @Override
    public boolean postRegions(final String postRegionsUrlString, Map<String, Region> regions)
    {
        return true;
    }

    @Override
    public boolean postIntensityRatios(final String postIntensityRatiosUrlString, Map<String, IRRatio> intensityRatios)
    {
        return true;
    }

    @Override
    public boolean postCalibrationModels(final String postCalibrationModels, Map<String, Model> calModels)
    {
        return true;
    }
}