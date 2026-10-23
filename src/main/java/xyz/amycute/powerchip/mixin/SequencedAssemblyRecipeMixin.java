package xyz.amycute.powerchip.mixin;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.patryk3211.powergrid.circuits.schematic.CircuitSchematic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.amycute.powerchip.component.ChipComponent;
import xyz.amycute.powerchip.component.ChipNameComponent;
import xyz.amycute.powerchip.registry.ModItems;
import xyz.amycute.powerchip.registry.ModNbt;
import xyz.amycute.powerchip.util.Registries;

import java.util.List;

@Mixin(value = SequencedAssemblyRecipe.class, remap = false)
public abstract class SequencedAssemblyRecipeMixin
{
    @Inject(method = "advance(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), cancellable = true)
    private void powerchip$carrySchematicThroughSequence(ItemStack stack, CallbackInfoReturnable<ItemStack> cir)
    {
        CompoundTag inputTag = stack.getTag();
        if (inputTag == null) return;

        if (!inputTag.contains(ModNbt.NBT_SCHEMATIC)) return;

        ItemStack result = cir.getReturnValue();
        if (result.isEmpty()) return;

        boolean isIncomplete = result.is(ModItems.INCOMPLETE_CHIP.get());
        ItemStack finalResult = result;
        boolean isFinalChip = ModItems.CHIPS.values().stream().anyMatch(holder -> finalResult.is(holder.get()));
        if (!isIncomplete && !isFinalChip) return;

        CompoundTag schematicTag = inputTag.getCompound(ModNbt.NBT_SCHEMATIC).copy();
        HolderLookup.Provider registries = Registries.get();

        if (isFinalChip && ChipComponent.exceedsMaxDepth(registries, schematicTag))
        {
            forceIncomplete(schematicTag, cir);
            return;
        }

        int size = -1;
        if (isFinalChip)
        {
            size = ChipComponent.designatedSize(registries, schematicTag);
            if (size < 0)
            {
                forceIncomplete(schematicTag, cir);
                return;
            }

            if (ChipComponent.exceedsMaxPower(registries, schematicTag, size))
            {
                forceIncomplete(schematicTag, cir);
                return;
            }

            result = new ItemStack(ModItems.chip(size));
        }

        CompoundTag outTag = result.getOrCreateTag();
        outTag.put(ModNbt.NBT_SCHEMATIC, schematicTag.copy());
        if (isFinalChip)
        {
            String chipName = findChipName(registries, schematicTag);
            result.setHoverName(Component.literal(chipName != null ? chipName : "CHIP"));

            ListTag loreLines = new ListTag();
            loreLines.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(size + " Pins").withStyle(ChatFormatting.DARK_GRAY))));
            outTag.put(ItemStack.TAG_LORE, loreLines);
        }

        cir.setReturnValue(result);
    }

    private static void forceIncomplete(CompoundTag schematicTag, CallbackInfoReturnable<ItemStack> cir)
    {
        ItemStack incomplete = new ItemStack(ModItems.INCOMPLETE_CHIP.get());
        CompoundTag outTag = incomplete.getOrCreateTag();

        outTag.put(ModNbt.NBT_SCHEMATIC, schematicTag.copy());

        cir.setReturnValue(incomplete);
    }

    private static String findChipName(HolderLookup.Provider registries, CompoundTag schematicTag)
    {
        var schematic = CircuitSchematic.fromNbt(schematicTag);
        if (schematic == null) return null;

        for (var placed : schematic.components())
        {
            if (placed.component instanceof ChipNameComponent)
            {
                String name = ChipNameComponent.nameof(placed);
                if (!name.isEmpty()) return name;
            }
        }
        return null;
    }
}
