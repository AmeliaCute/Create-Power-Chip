package xyz.amycute.powerchip.component;

import com.google.common.collect.ImmutableCollection;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.circuits.circuitboard.ComponentCircuitBuilder;
import org.patryk3211.powergrid.circuits.components.Component;
import org.patryk3211.powergrid.circuits.components.properties.ComponentProperty;
import org.patryk3211.powergrid.circuits.components.properties.StringProperty;
import xyz.amycute.powerchip.component.properties.ChipSizeProperty;
import xyz.amycute.powerchip.component.properties.PinGridProperty;
import org.patryk3211.powergrid.circuits.schematic.ComponentFootprint;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.patryk3211.powergrid.circuits.thermal.ThermalBuilder;
import org.patryk3211.powergrid.electricity.base.TerminalBoundingBox;
import xyz.amycute.powerchip.PowerChips;

import java.util.List;

public class IOPinComponent extends Component
{
    public static final int MAX_PINS = 24; //TODO: If someone care
    public static final PinGridProperty PIN = new PinGridProperty(PowerChips.MOD_ID, "pin", 0, 0, MAX_PINS - 1);
    public static final StringProperty PIN_LABEL = new StringProperty(PowerChips.MOD_ID, "pin_label");
    public static final ChipSizeProperty PIN_COUNT = new ChipSizeProperty(PowerChips.MOD_ID, "pin_count", ChipComponent.SIZES[ChipComponent.SIZES.length - 1]);

    static
    {
        PIN.hidden();
        PIN_LABEL.hidden();
        PIN_COUNT.hidden();
    }

    public IOPinComponent(ComponentFootprint footprint)
    {
        super(footprint);
    }

    @Override
    protected void addProperties(ImmutableCollection.Builder<ComponentProperty<?>> properties)
    {
        super.addProperties(properties);
        properties.add(PIN_COUNT);
        properties.add(PIN);
        properties.add(PIN_LABEL);
    }

    @Override
    public boolean emitExternalTerminals()
    {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TerminalBoundingBox> terminals(@NotNull PlacedComponent placed)
    {
        if (placed.customData instanceof List) return (List<TerminalBoundingBox>) placed.customData;

        String label = placed.getString(PIN_LABEL);
        int pin = placed.get(PIN);

        String text = (label == null || label.isEmpty()) ? ("IO " + pin) : label;
        List<TerminalBoundingBox> list = List.of(new TerminalBoundingBox(net.minecraft.network.chat.Component.literal(text), 1, 1, 1, 2, 2, 2));
        placed.customData = list;
        return list;
    }

    @Override
    public void bake(@NotNull PlacedComponent placed, @NotNull ComponentCircuitBuilder builder, @NotNull ThermalBuilder.IEmitter thermals)
    {

    }
}