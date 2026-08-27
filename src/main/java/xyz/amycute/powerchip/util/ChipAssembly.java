package xyz.amycute.powerchip.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.patryk3211.powergrid.circuits.schematic.CircuitSchematic;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import xyz.amycute.powerchip.component.ChipComponent;
import xyz.amycute.powerchip.component.ChipNameComponent;
import xyz.amycute.powerchip.component.IOPinComponent;
import xyz.amycute.powerchip.registry.ModItems;
import xyz.amycute.powerchip.registry.ModNbt;

import java.util.List;
import java.util.Locale;

public final class ChipAssembly
{
    public enum Failure
    {
        NONE,
        NOT_A_BOARD,
        EMPTY_BOARD,
        NO_SCHEMATIC,
        INVALID_SCHEMATIC,
        NO_PINS,
        UNKNOWN_SIZE,
        TOO_DEEP,
        TOO_MUCH_POWER,
        ALREADY_COMPILED;

        private final String key = name().toLowerCase(Locale.ROOT);

        public String key()
        {
            return key;
        }
    }

    public record Result(ItemStack stack, Failure failure)
    {
        public boolean ok()
        {
            return failure == Failure.NONE;
        }
    }

    public static final Result NOT_A_BOARD = new Result(ItemStack.EMPTY, Failure.NOT_A_BOARD);
    public static final Result EMPTY_BOARD = new Result(ItemStack.EMPTY, Failure.EMPTY_BOARD);

    private static final Result NO_SCHEMATIC = new Result(ItemStack.EMPTY, Failure.NO_SCHEMATIC);
    private static final Result INVALID_SCHEMATIC = new Result(ItemStack.EMPTY, Failure.INVALID_SCHEMATIC);
    private static final Result NO_PINS = new Result(ItemStack.EMPTY, Failure.NO_PINS);
    private static final Result UNKNOWN_SIZE = new Result(ItemStack.EMPTY, Failure.UNKNOWN_SIZE);
    private static final Result TOO_DEEP = new Result(ItemStack.EMPTY, Failure.TOO_DEEP);
    private static final Result TOO_MUCH_POWER = new Result(ItemStack.EMPTY, Failure.TOO_MUCH_POWER);
    private static final Result ALREADY_COMPILED = new Result(ItemStack.EMPTY, Failure.ALREADY_COMPILED);

    public static CompoundTag schematicOf(ItemStack stack)
    {
        if (stack.isEmpty()) return null;

        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;

        CompoundTag tag = data.getUnsafe();
        if (!tag.contains(ModNbt.NBT_SCHEMATIC, Tag.TAG_COMPOUND)) return null;

        return tag.getCompound(ModNbt.NBT_SCHEMATIC).copy();
    }

    public static Result convert(ItemStack input, int count)
    {
        return fromSchematic(schematicOf(input), count);
    }

    public static Result fromSchematic(CompoundTag schematicTag, int count)
    {
        if (schematicTag == null || schematicTag.isEmpty()) return NO_SCHEMATIC;

        CircuitSchematic schematic = CircuitSchematic.fromNbt(schematicTag);
        if (schematic == null) return INVALID_SCHEMATIC;

        int size = -1;
        String name = null;

        for (PlacedComponent placed : schematic.components())
        {
            if (size < 0 && placed.component instanceof IOPinComponent && placed.has(IOPinComponent.PIN_COUNT))
            {
                size = placed.get(IOPinComponent.PIN_COUNT);
                if (name != null) break;
            }
            else if (name == null && placed.component instanceof ChipNameComponent)
            {
                String candidate = ChipNameComponent.nameof(placed);
                if (!candidate.isEmpty())
                {
                    name = candidate;
                    if (size >= 0) break;
                }
            }
        }

        if (size < 0) return NO_PINS;

        DeferredHolder<Item, Item> holder = ModItems.CHIPS.get(size);
        if (holder == null) return UNKNOWN_SIZE;

        if (ChipComponent.exceedsMaxDepth(schematicTag)) return TOO_DEEP;
        if (ChipComponent.exceedsMaxPower(schematicTag, size)) return TOO_MUCH_POWER;

        ItemStack out = new ItemStack(holder.get(), count);

        CompoundTag data = new CompoundTag();
        data.put(ModNbt.NBT_SCHEMATIC, schematicTag);
        out.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        out.set(DataComponents.CUSTOM_NAME, Component.literal(name != null ? name : "CHIP"));
        out.set(DataComponents.LORE, new ItemLore(List.of(Component.literal(size + " Pins").withStyle(ChatFormatting.DARK_GRAY))));

        return new Result(out, Failure.NONE);
    }
}
