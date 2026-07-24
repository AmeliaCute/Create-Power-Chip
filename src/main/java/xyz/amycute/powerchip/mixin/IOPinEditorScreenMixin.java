package xyz.amycute.powerchip.mixin;

import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.circuits.editor.CircuitDesignTableEditScreen;
import org.patryk3211.powergrid.circuits.gui.ComponentPropertiesWidget;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.amycute.powerchip.component.IOPinComponent;
import xyz.amycute.powerchip.component.widgets.IOPinEditorWidget;

@Mixin(CircuitDesignTableEditScreen.class)
public abstract class IOPinEditorScreenMixin extends Screen
{
    private IOPinEditorScreenMixin(net.minecraft.network.chat.Component title)
    {
        super(title);
    }

    @Shadow
    private boolean changed;

    @Unique
    private IOPinEditorWidget powerchip$ioPinEditor;

    @Inject(method = "init", at = @At("TAIL"))
    private void powerchip$addIOPinEditor(CallbackInfo ci)
    {
        var accessor = (AbstractContainerScreenAccessor) this;
        int right = accessor.powerchip$getLeftPos() - 15;
        int y = accessor.powerchip$getTopPos() + 12;
        powerchip$ioPinEditor = new IOPinEditorWidget(font, right - 150, y, this::powerchip$recordChange);
        addRenderableWidget(powerchip$ioPinEditor);
    }

    @Unique
    private void powerchip$recordChange()
    {
        this.changed = true;
    }

    @Redirect(method = "selectComponent(IIIIII)Lorg/patryk3211/powergrid/circuits/gui/CircuitEditWidget$SelectionResult;", at = @At(value = "INVOKE", target = "Lorg/patryk3211/powergrid/circuits/gui/ComponentPropertiesWidget;setComponent(Lorg/patryk3211/powergrid/circuits/schematic/PlacedComponent;Ljava/lang/Runnable;)V"))
    private void powerchip$routeOnSelectComponent(ComponentPropertiesWidget widget, @Nullable PlacedComponent component, Runnable changeMadeCallback)
    {
        powerchip$route(widget, component, changeMadeCallback);
    }

    @Redirect(method = "mouseClicked(DDI)Z", at = @At(value = "INVOKE", target = "Lorg/patryk3211/powergrid/circuits/gui/ComponentPropertiesWidget;setComponent(Lorg/patryk3211/powergrid/circuits/schematic/PlacedComponent;)V"))
    private void powerchip$routeOnMouseClicked(ComponentPropertiesWidget widget, @Nullable PlacedComponent component)
    {
        powerchip$route(widget, component, () -> {});
    }

    @Redirect(method = "toolSelect(Lnet/minecraft/world/inventory/Slot;)V", at = @At(value = "INVOKE", target = "Lorg/patryk3211/powergrid/circuits/gui/ComponentPropertiesWidget;setComponent(Lorg/patryk3211/powergrid/circuits/schematic/PlacedComponent;)V"))
    private void powerchip$routeOnToolSelectSlot(ComponentPropertiesWidget widget, @Nullable PlacedComponent component)
    {
        powerchip$route(widget, component, () -> {});
    }

    @Redirect(method = "toolSelect(Lnet/minecraft/world/item/Item;)V", at = @At(value = "INVOKE", target = "Lorg/patryk3211/powergrid/circuits/gui/ComponentPropertiesWidget;setComponent(Lorg/patryk3211/powergrid/circuits/schematic/PlacedComponent;)V"))
    private void powerchip$routeOnToolSelectItem(ComponentPropertiesWidget widget, @Nullable PlacedComponent component)
    {
        powerchip$route(widget, component, () -> {});
    }

    @Unique
    private void powerchip$route(ComponentPropertiesWidget widget, @Nullable PlacedComponent component, Runnable changeMadeCallback)
    {
        if (component != null && component.component instanceof IOPinComponent)
        {
            widget.setComponent(null);
            powerchip$ioPinEditor.setComponent(component);
        }
        else
        {
            powerchip$ioPinEditor.setComponent(null);
            widget.setComponent(component, changeMadeCallback);
        }
    }
}
