package xyz.amycute.powerchip.component;

import com.google.common.collect.ImmutableCollection;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.circuits.circuitboard.ComponentCircuitBuilder;
import org.patryk3211.powergrid.circuits.components.Component;
import org.patryk3211.powergrid.circuits.components.properties.ComponentProperty;
import org.patryk3211.powergrid.circuits.components.properties.EnumProperty;
import org.patryk3211.powergrid.circuits.schematic.ComponentFootprint;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.patryk3211.powergrid.circuits.thermal.ThermalBuilder;
import xyz.amycute.powerchip.PowerChips;
import xyz.amycute.powerchip.component.properties.SafeStringProperty;

public class ChipNameComponent extends Component
{
    public static final short MAX_LENGTH = 5;
    public static final SafeStringProperty NAME = new SafeStringProperty(PowerChips.MOD_ID, "chip_name", MAX_LENGTH);
    public static final EnumProperty<DyeColor> COLOR = new EnumProperty<>(PowerChips.MOD_ID, "chip_color", DyeColor.class);

    public ChipNameComponent(ComponentFootprint footprint)
    {
        super(footprint);
    }

    @Override
    protected void addProperties(ImmutableCollection.Builder<ComponentProperty<?>> properties)
    {
        super.addProperties(properties);
        properties.add(NAME);
        properties.add(COLOR);
    }

    @Override
    public void bake(@NotNull PlacedComponent placedComponent, @NotNull ComponentCircuitBuilder componentCircuitBuilder, ThermalBuilder.@NotNull IEmitter iEmitter)
    {

    }

    public static String nameof(PlacedComponent placed)
    {
        String value = placed.get(NAME);
        if (value == null) return "Chip";

        return value.length() > MAX_LENGTH ? value.substring(0, MAX_LENGTH) : value;
    }

    public static int colorof(PlacedComponent placed)
    {
        try
        {
            DyeColor color = placed.get(COLOR);
            if (color == null) return 0xFFFFFFFF;
            float[] textureDiffuseColors = color.getTextureDiffuseColors();
            //mostly stolen from 1.21.1 FastColor.ARGB32.colorFromFloat
            return FastColor.ARGB32.color(0xFF,
                    Mth.floor(textureDiffuseColors[0] * 255),
                    Mth.floor(textureDiffuseColors[1] * 255),
                    Mth.floor(textureDiffuseColors[2] * 255));
        }
        catch (IllegalArgumentException e)
        {
            return 0xFFFFFFFF;
        }
    }
}
