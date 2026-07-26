package xyz.amycute.powerchip.component;

import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;

import java.util.List;
import java.util.function.Supplier;

public final class ChipSizeSync
{
    private static Supplier<List<PlacedComponent>> currentSchematic = null;

    private ChipSizeSync() {}

    public static void setSource(Supplier<List<PlacedComponent>> source)
    {
        currentSchematic = source;
    }

    public static void clearSource(Supplier<List<PlacedComponent>> source)
    {
        if (currentSchematic == source) currentSchematic = null;
    }

    public static int currentSharedSize()
    {
        if (currentSchematic != null)
        {
            for (PlacedComponent placed : currentSchematic.get())
            {
                if (placed.component instanceof IOPinComponent && placed.has(IOPinComponent.PIN_COUNT)) return placed.get(IOPinComponent.PIN_COUNT);
            }
        }

        return IOPinComponent.PIN_COUNT.defaultValue();
    }

    public static void broadcast(int size)
    {
        if (currentSchematic == null) return;

        for (PlacedComponent placed : currentSchematic.get())
            if (placed.component instanceof IOPinComponent && placed.get(IOPinComponent.PIN_COUNT) != size) placed.set(IOPinComponent.PIN_COUNT, size);
    }
}
