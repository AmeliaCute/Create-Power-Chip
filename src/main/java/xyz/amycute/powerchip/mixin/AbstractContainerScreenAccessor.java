package xyz.amycute.powerchip.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor
{
    @Accessor("leftPos")
    int powerchip$getLeftPos();

    @Accessor("topPos")
    int powerchip$getTopPos();
}
