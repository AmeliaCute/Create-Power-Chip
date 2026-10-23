package xyz.amycute.powerchip.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.patryk3211.powergrid.circuits.schematic.CircuitSchematic;
import org.patryk3211.powergrid.circuits.schematic.CircuitSchematicRender;
import org.patryk3211.powergrid.circuits.schematic.ComponentFootprint;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.amycute.powerchip.component.ChipComponent;

@Mixin(value = CircuitSchematicRender.class, remap = false)
public abstract class CircuitSchematicRenderMixin
{
    @Inject(method = "renderComponents(Lorg/patryk3211/powergrid/circuits/schematic/CircuitSchematic;Lnet/minecraft/client/gui/GuiGraphics;IIIII)V", at = @At("TAIL"))
    private static void powerchip$drawChipNames(CircuitSchematic schematic, GuiGraphics ctx, int x, int y, int scale, int mouseX, int mouseY, CallbackInfo ci)
    {
        Font font = Minecraft.getInstance().font;
        PoseStack ms = ctx.pose();

        for (PlacedComponent placed : schematic.components())
        {
            if (!(placed.component instanceof ChipComponent)) continue;

            String name = ChipComponent.getChipName(placed);
            if (name.isEmpty()) continue;

            int color = ChipComponent.getChipColor(placed);
            ComponentFootprint footprint = placed.footprint();
            int cellX = x + placed.x * scale;
            int cellY = y + placed.y * scale;
            int cellWidth = footprint.getWidth() * scale;
            int cellHeight = footprint.getHeight() * scale;

            ms.pushPose();
            ms.translate(cellX + cellWidth / 2f, cellY + cellHeight / 2f, 200);
            float textScale = 0.6f;
            ms.scale(textScale, textScale, 1f);
            ctx.drawCenteredString(font, name, 0, -font.lineHeight / 2, color);
            ms.popPose();
        }
    }
}