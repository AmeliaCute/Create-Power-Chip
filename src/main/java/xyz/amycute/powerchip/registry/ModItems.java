package xyz.amycute.powerchip.registry;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.amycute.powerchip.PowerChips;
import xyz.amycute.powerchip.component.ChipComponent;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModItems
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PowerChips.MOD_ID);
    public static final RegistryObject<Item> CHIP_CASING = ITEMS.register("chip_casing", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> INCOMPLETE_CHIP = ITEMS.register("incomplete_chip", () -> new Item(new Item.Properties()));

    public static final Map<Integer, RegistryObject<Item>> CHIPS = buildChips();

    private static Map<Integer, RegistryObject<Item>> buildChips()
    {
        Map<Integer, RegistryObject<Item>> map = new LinkedHashMap<>();
        for (int pinCount : ChipComponent.SIZES) map.put(pinCount, ITEMS.register(ModComponents.chipId(pinCount), () -> new Item(new Item.Properties())));
        return map;
    }

    public static Item chip(int pinCount)
    {
        return CHIPS.get(pinCount).get();
    }
}
