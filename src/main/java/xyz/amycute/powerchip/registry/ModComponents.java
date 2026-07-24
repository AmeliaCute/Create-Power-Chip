package xyz.amycute.powerchip.registry;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.patryk3211.powergrid.circuits.components.ComponentRegistry;
import org.patryk3211.powergrid.circuits.schematic.ComponentFootprint;
import xyz.amycute.powerchip.PowerChips;
import xyz.amycute.powerchip.component.ChipComponent;
import xyz.amycute.powerchip.component.ChipNameComponent;
import xyz.amycute.powerchip.component.IOPinComponent;

import java.util.LinkedHashMap;
import java.util.Map;

@EventBusSubscriber(modid = PowerChips.MOD_ID)
public class ModComponents
{
    public static final Map<Integer, ChipComponent> CHIP_COMPONENTS = buildChipComponents();
    public static final IOPinComponent IO_PIN_COMPONENT = buildIOPinComponent();
    public static final ChipNameComponent CHIP_NAME_COMPONENT = buildChipNameComponent();

    public static String chipId(int pinCount)
    {
        // Compatibility with older version
        return pinCount == 8 ? "chip" : "chip_" + pinCount;
    }

    private static Map<Integer, ChipComponent> buildChipComponents()
    {
        Map<Integer, ChipComponent> map = new LinkedHashMap<>();
        for (int pinCount : ChipComponent.SIZES) map.put(pinCount, buildChipComponent(pinCount));
        return map;
    }

    private static ChipComponent buildChipComponent(int pinCount)
    {
        int cols = pinCount / 2;
        ComponentFootprint.Builder builder = new ComponentFootprint.Builder(cols, 2, "component." + PowerChips.MOD_ID + ".chip", null);
        for (int pin = 0; pin < pinCount; pin++)
        {
            int col = pin % cols;
            int row = pin / cols;
            String pinNumber = Integer.toString(pin + 1);
            builder.addPad(col, row, pin, "PIN " + pinNumber, pinNumber);
        }
        ComponentFootprint footprint = builder.withOutline().build();
        return new ChipComponent(footprint, pinCount);
    }

    private static IOPinComponent buildIOPinComponent()
    {
        ComponentFootprint footprint = new ComponentFootprint.Builder(1, 1, "component." + PowerChips.MOD_ID + ".io_pin", null)
            .addPad(0, 0, 0, "Pin", null)
            .build();
        return new IOPinComponent(footprint);
    }

    private static ChipNameComponent buildChipNameComponent()
    {
        ComponentFootprint footprint = new ComponentFootprint.Builder(1, 1, "component." + PowerChips.MOD_ID + ".chip_name", null)
            .withItem()
            .withOutline()
            .build();
        return new ChipNameComponent(footprint);
    }

    @SubscribeEvent
    public static void onRegister(RegisterEvent event)
    {
        if (event.getRegistryKey().equals(ComponentRegistry.REGISTRY_KEY))
        {
            for (var entry : CHIP_COMPONENTS.entrySet())
            {
                ChipComponent component = entry.getValue();
                event.register(ComponentRegistry.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(PowerChips.MOD_ID, chipId(entry.getKey())), () -> component);
            }
            event.register(ComponentRegistry.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(PowerChips.MOD_ID, "io_pin"), () -> IO_PIN_COMPONENT);
            event.register(ComponentRegistry.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(PowerChips.MOD_ID, "chip_name"), () -> CHIP_NAME_COMPONENT);
        }
    }
}