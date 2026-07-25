package xyz.amycute.powerchip.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.Font;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.circuits.components.properties.ComponentProperty;
import org.patryk3211.powergrid.circuits.components.properties.PropertyEntry;
import org.patryk3211.powergrid.circuits.gui.ComponentPropertiesWidget;
import org.patryk3211.powergrid.circuits.gui.PropertyWidget;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.amycute.powerchip.component.IOPinComponent;
import xyz.amycute.powerchip.component.ChipSizeSync;
import xyz.amycute.powerchip.component.properties.ChipSizeProperty;
import xyz.amycute.powerchip.component.properties.PinGridProperty;
import xyz.amycute.powerchip.component.widgets.ChipSizePropertyWidget;
import xyz.amycute.powerchip.component.widgets.PinGridPropertyWidget;

import java.util.List;

@Mixin(ComponentPropertiesWidget.class)
public abstract class ComponentPropertiesWidgetMixin
{
    @Shadow
    private Font textRenderer;

    @Shadow
    @Nullable
    private PlacedComponent component;

    private int powerchip$nextRowHeight = 20;
    private int powerchip$totalContentHeight = 0;

    @Inject(method = "setComponent(Lorg/patryk3211/powergrid/circuits/schematic/PlacedComponent;Ljava/lang/Runnable;)V", at = @At("HEAD"))
    private void powerchip$resetTotalHeight(PlacedComponent component, Runnable changeMadeCallback, CallbackInfo ci)
    {
        powerchip$totalContentHeight = 0;
    }

    @WrapOperation(method = "setComponent(Lorg/patryk3211/powergrid/circuits/schematic/PlacedComponent;Ljava/lang/Runnable;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private boolean powerchip$useGridForPinProperty(List<PropertyWidget<?, ?>> list, Object widget, Operation<Boolean> original, @Local ComponentProperty<?> property, @Local(argsOnly = true) Runnable changeMadeCallback)
    {
        PropertyWidget<?, ?> existing = (PropertyWidget<?, ?>) widget;
        powerchip$nextRowHeight = 20;

        if (property instanceof PinGridProperty)
        {
            PropertyEntry<Integer> pinEntry = (PropertyEntry<Integer>) component.getEntry(property);
            PlacedComponent capturedComponent = component;
            PinGridPropertyWidget grid = new PinGridPropertyWidget(textRenderer, existing.getX(), existing.getY(), pinEntry, changeMadeCallback, () -> capturedComponent.get(IOPinComponent.PIN_COUNT));
            powerchip$nextRowHeight = Math.max(20, grid.powerchip$renderedHeight());
            powerchip$totalContentHeight += powerchip$nextRowHeight;
            return original.call(list, grid);
        }
        else if (property instanceof ChipSizeProperty)
        {
            PropertyEntry<Integer> sizeEntry = (PropertyEntry<Integer>) component.getEntry(property);
            Runnable broadcastingCallback = () -> {
                ChipSizeSync.broadcast(sizeEntry.get());
                changeMadeCallback.run();
            };
            ChipSizePropertyWidget row = new ChipSizePropertyWidget(textRenderer, existing.getX(), existing.getY(), sizeEntry, broadcastingCallback);
            powerchip$nextRowHeight = Math.max(20, row.powerchip$renderedHeight());
            powerchip$totalContentHeight += powerchip$nextRowHeight;
            return original.call(list, row);
        }
        else
        {
            powerchip$totalContentHeight += 20;
            return original.call(list, widget);
        }
    }

    @ModifyVariable(method = "setComponent(Lorg/patryk3211/powergrid/circuits/schematic/PlacedComponent;Ljava/lang/Runnable;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.AFTER), name = "y")
    private int powerchip$adjustRowStep(int y)
    {
        return y + (powerchip$nextRowHeight - 20);
    }

    @ModifyArg(method = "doRender", at = @At(value = "INVOKE", target = "Lorg/patryk3211/powergrid/circuits/gui/ComponentPropertiesWidget;blitRepeating(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V", ordinal = 0), index = 5)
    private int powerchip$backgroundHeightMain(int height)
    {
        return powerchip$totalContentHeight > 0 ? powerchip$totalContentHeight : height;
    }

    @ModifyArg(method = "doRender", at = @At(value = "INVOKE", target = "Lorg/patryk3211/powergrid/circuits/gui/ComponentPropertiesWidget;blitRepeating(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V", ordinal = 1), index = 5)
    private int powerchip$backgroundHeightSide(int height)
    {
        return powerchip$totalContentHeight > 0 ? powerchip$totalContentHeight : height;
    }
}
