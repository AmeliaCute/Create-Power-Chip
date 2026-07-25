package xyz.amycute.powerchip.registry;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import xyz.amycute.powerchip.PowerChips;
import xyz.amycute.powerchip.component.ChipComponent;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModItems
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PowerChips.MOD_ID);
    public static final DeferredHolder<Item, Item> CHIP_CASING = ITEMS.registerSimpleItem("chip_casing", new Item.Properties());
    public static final DeferredHolder<Item, Item> INCOMPLETE_CHIP = ITEMS.registerSimpleItem("incomplete_chip", new Item.Properties());

    public static final Map<Integer, DeferredHolder<Item, Item>> CHIPS = buildChips();

    private static Map<Integer, DeferredHolder<Item, Item>> buildChips()
    {
        Map<Integer, DeferredHolder<Item, Item>> map = new LinkedHashMap<>();
        for (int pinCount : ChipComponent.SIZES) map.put(pinCount, ITEMS.registerSimpleItem(ModComponents.chipId(pinCount), new Item.Properties()));
        return map;
    }

    public static Item chip(int pinCount)
    {
        return CHIPS.get(pinCount).get();
    }
}
