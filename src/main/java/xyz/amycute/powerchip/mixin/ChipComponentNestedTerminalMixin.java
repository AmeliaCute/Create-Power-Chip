package xyz.amycute.powerchip.mixin;

import org.patryk3211.powergrid.circuits.components.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.amycute.powerchip.component.ChipComponent;

@Mixin(ChipComponent.class)
public abstract class ChipComponentNestedTerminalMixin
{
    @Redirect(method = "bake(Lorg/patryk3211/powergrid/circuits/schematic/PlacedComponent;Lorg/patryk3211/powergrid/circuits/circuitboard/ComponentCircuitBuilder;Lorg/patryk3211/powergrid/circuits/thermal/ThermalBuilder$IEmitter;)V", at = @At(value = "INVOKE", target = "Lorg/patryk3211/powergrid/circuits/components/Component;emitExternalTerminals()Z"))
    private boolean powerchip$dontTreatInnerComponentsAsExternal(Component innerComponent)
    {
        return false;
    }
}