package xyz.amycute.powerchip.component.properties;

import org.patryk3211.powergrid.circuits.components.properties.IntProperty;
import xyz.amycute.powerchip.component.ChipComponent;

public class ChipSizeProperty extends IntProperty
{
    public ChipSizeProperty(String namespace, String name, int defaultSize)
    {
        super(namespace, name, defaultSize, ChipComponent.SIZES[0], ChipComponent.SIZES[ChipComponent.SIZES.length - 1]);
    }

    public static int nearestValidSize(int raw)
    {
        int best = ChipComponent.SIZES[0];
        int bestDist = Math.abs(raw - best);

        for (int size : ChipComponent.SIZES)
        {
            int dist = Math.abs(raw - size);
            if (dist < bestDist)
            {
                best = size;
                bestDist = dist;
            }
        }
        return best;
    }
}
