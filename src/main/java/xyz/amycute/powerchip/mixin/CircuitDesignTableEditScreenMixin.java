package xyz.amycute.powerchip.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import xyz.amycute.powerchip.component.ChipComponent;
import xyz.amycute.powerchip.component.ChipSizeSync;
import xyz.amycute.powerchip.component.IOPinComponent;
import org.patryk3211.powergrid.circuits.editor.CircuitDesignTableEditScreen;
import org.patryk3211.powergrid.circuits.schematic.CircuitSchematic;
import org.patryk3211.powergrid.circuits.schematic.ComponentFootprint;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.patryk3211.powergrid.circuits.schematic.Point;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(CircuitDesignTableEditScreen.class)
public abstract class CircuitDesignTableEditScreenMixin
{
    @Shadow
    private CircuitSchematic schematic;

    private Supplier<java.util.List<PlacedComponent>> powerchip$syncSource;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void powerchip$registerSyncSource(CallbackInfo ci)
    {
        powerchip$syncSource = schematic::components;
        ChipSizeSync.setSource(powerchip$syncSource);
    }

    @Inject(method = "placeComponent(II)V", at = @At("TAIL"))
    private void powerchip$inheritSharedPinCount(int x, int y, CallbackInfo ci)
    {
        PlacedComponent placed = schematic.getComponent(x, y);
        if (placed != null && placed.component instanceof IOPinComponent) placed.set(IOPinComponent.PIN_COUNT, ChipSizeSync.currentSharedSize());
    }

    @ModifyVariable(method = "toolSelect(Lnet/minecraft/world/inventory/Slot;)V", at = @At(value = "STORE"), ordinal = 0)
    private PlacedComponent powerchip$transferChipSchematic(PlacedComponent placed, Slot slot)
    {
        if (!(placed.component instanceof ChipComponent)) return placed;

        ItemStack stack = slot.getItem();
        if (!stack.has(DataComponents.CUSTOM_DATA)) return placed;

        CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
        if (!tag.contains("Schematic")) return placed;

        CompoundTag schematicTag = tag.getCompound("Schematic");
        placed.set(ChipComponent.SCHEMATIC, schematicTag);
        return placed;
    }

    @WrapOperation(method = "renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V", at = @At(value = "INVOKE", target = "Lorg/patryk3211/powergrid/circuits/schematic/ComponentFootprint;getTooltip(II)Lnet/minecraft/network/chat/Component;"))
    private net.minecraft.network.chat.Component powerchip$useIOPinLabelForTooltip(ComponentFootprint footprint, int localX, int localY, Operation<net.minecraft.network.chat.Component> original, @Local PlacedComponent placed)
    {
        net.minecraft.network.chat.Component result = original.call(footprint, localX, localY);
        if (!(placed.component instanceof ChipComponent)) return result;

        ComponentFootprint.PadData pad = footprint.getPads().get(new Point(localX, localY));
        if (pad == null || pad.nodeIndex() < 0) return result;

        String customLabel = ChipComponent.getPinLabel(placed, pad.nodeIndex());
        return customLabel != null ? net.minecraft.network.chat.Component.literal(customLabel) : result;
    }
}