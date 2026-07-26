package xyz.amycute.powerchip.mixin;

import org.patryk3211.powergrid.circuits.thermal.ThermalBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ThermalBuilder.class)
public interface ThermalBuilderAccessor
{
    @Accessor("dissipationFactor")
    float powerchip$getDissipationFactor();

    @Accessor("overheatTemperature")
    float powerchip$getOverheatTemperature();
}