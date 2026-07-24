package xyz.amycute.powerchip.mixin;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import org.patryk3211.powergrid.circuits.schematic.CircuitSchematic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.amycute.powerchip.component.ChipComponent;
import xyz.amycute.powerchip.component.ChipNameComponent;
import xyz.amycute.powerchip.registry.ModItems;
import xyz.amycute.powerchip.registry.ModNbt;

import java.util.List;

@Mixin(SequencedAssemblyRecipe.class)
public abstract class SequencedAssemblyRecipeMixin
{
    @Inject(method = "advance(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/util/RandomSource;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), cancellable = true)
    private void powerchip$carrySchematicThroughSequence(ResourceLocation id, ItemStack input, RandomSource random, CallbackInfoReturnable<ItemStack> cir)
    {
        if (!input.has(DataComponents.CUSTOM_DATA)) return;

        CompoundTag inputTag = input.get(DataComponents.CUSTOM_DATA).copyTag();
        if (!inputTag.contains(ModNbt.NBT_SCHEMATIC)) return;

        ItemStack result = cir.getReturnValue();
        if (result.isEmpty()) return;

        boolean isIncomplete = result.is(ModItems.INCOMPLETE_CHIP.get());
        ItemStack finalResult = result;
        boolean isFinalChip = ModItems.CHIPS.values().stream().anyMatch(holder -> finalResult.is(holder.get()));
        if (!isIncomplete && !isFinalChip) return;

        CompoundTag schematicTag = inputTag.getCompound(ModNbt.NBT_SCHEMATIC).copy();
        if (isFinalChip && ChipComponent.exceedsMaxDepth(schematicTag)) return;

        int size = -1;
        if (isFinalChip)
        {
            size = ChipComponent.designatedSize(schematicTag);
            if (size < 0) return;

            result = new ItemStack(ModItems.chip(size));
        }

        CompoundTag outTag = result.has(DataComponents.CUSTOM_DATA) ? result.get(DataComponents.CUSTOM_DATA).copyTag() : new CompoundTag();
        outTag.put(ModNbt.NBT_SCHEMATIC, schematicTag.copy());
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(outTag));
        if (isFinalChip)
        {
            String chipName = findChipName(schematicTag);
            result.set(DataComponents.CUSTOM_NAME, Component.literal(chipName != null ? chipName : "CHIP"));

            List<Component> loreLines = new java.util.ArrayList<>();
            loreLines.add(Component.literal(size + " Pins").withStyle(ChatFormatting.DARK_GRAY));
            result.set(DataComponents.LORE, new ItemLore(loreLines));
        }

        cir.setReturnValue(result);
    }

    private static String findChipName(CompoundTag schematicTag)
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