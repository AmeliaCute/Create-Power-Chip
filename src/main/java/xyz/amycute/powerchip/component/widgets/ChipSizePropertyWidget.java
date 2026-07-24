package xyz.amycute.powerchip.component.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.circuits.components.properties.PropertyEntry;
import org.patryk3211.powergrid.circuits.gui.ComponentPropertiesWidget;
import org.patryk3211.powergrid.circuits.gui.PropertyWidget;
import xyz.amycute.powerchip.component.ChipComponent;

public final class ChipSizePropertyWidget extends PropertyWidget<Integer, PropertyEntry<Integer>> implements WidePropertyWidget
{
    private static final int[] SIZES = ChipComponent.SIZES;
    private static final int COUNT = SIZES.length;

    private static final int CELL_H = 8;
    private static final int GAP = 1;

    private static final int CELL_W = Math.min(13, ((6 * 13 + 5) - (COUNT - 1) * GAP) / COUNT);

    private static final int GRID_W = COUNT * CELL_W + (COUNT - 1) * GAP;
    private static final int BG_W = GRID_W + 8;
    private static final int BG_H = CELL_H + 8;

    private static final int GRID_X_OFFSET = (BG_W - GRID_W) / 2;
    private static final int GRID_Y_OFFSET = (BG_H - CELL_H) / 2;

    private static final String[] LABELS = new String[COUNT];

    private final Runnable changeMadeCallback;
    private final int[] cachedCellX = new int[COUNT];
    private final int[] cachedTextX = new int[COUNT];
    private final int textYOffset;

    private final int contentYOffset;

    static
    {
        for (int i = 0; i < COUNT; i++) LABELS[i] = Integer.toString(SIZES[i]);
    }

    public ChipSizePropertyWidget(Font textRenderer, int x, int y, PropertyEntry<Integer> property, Runnable changeMadeCallback)
    {
        super(textRenderer, x, y, property);
        this.changeMadeCallback = changeMadeCallback;

        this.textYOffset = (CELL_H - textRenderer.lineHeight) / 2 + 1;
        this.contentYOffset = 6 + textRenderer.lineHeight;
        for (int i = 0; i < COUNT; i++)
        {
            this.cachedCellX[i] = GRID_X_OFFSET + i * (CELL_W + GAP);
            this.cachedTextX[i] = this.cachedCellX[i] + (CELL_W - textRenderer.width(LABELS[i])) / 2 + 1;
        }
    }

    private int indexOfSelected()
    {
        int selected = property.get();
        for (int i = 0; i < COUNT; i++) if (SIZES[i] == selected) return i;
        return -1;
    }

    @Override
    public int powerchip$renderedWidth()
    {
        return BG_W;
    }

    @Override
    public int powerchip$renderedHeight()
    {
        return contentYOffset + BG_H + 1;
    }

    private int cellAt(double mouseX, double mouseY)
    {
        double localX = mouseX - getX() - GRID_X_OFFSET;
        double localY = mouseY - getY() - contentYOffset - GRID_Y_OFFSET;
        if (localX < 0 || localY < 0 || localX >= GRID_W || localY >= CELL_H) return -1;
        if (localX % (CELL_W + GAP) >= CELL_W) return -1;

        int cell = (int) (localX / (CELL_W + GAP));
        return cell < COUNT ? cell : -1;
    }

    public int getBackgroundWidth()
    {
        return BG_W;
    }

    @Override
    protected void doRender(@NotNull GuiGraphics ctx, int mouseX, int mouseY, float partialTicks)
    {
        int x = getX();
        int y = getY() + contentYOffset;
        ctx.blit(ComponentPropertiesWidget.PROPERTIES, x, y, 0, 99, BG_W, BG_H);

        int selectedIndex = indexOfSelected();
        int hovered = cellAt(mouseX, mouseY);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        for (int i = 0; i < COUNT; i++)
        {
            int cx = x + cachedCellX[i];
            int cy = y + GRID_Y_OFFSET;

            AllGuiTextures tex = i == selectedIndex ? AllGuiTextures.BUTTON_HOVER : AllGuiTextures.BUTTON;
            ctx.blit(tex.location, cx, cy, tex.getStartX(), tex.getStartY(), CELL_W, CELL_H);
            if (i == hovered && i != selectedIndex) ctx.fill(cx, cy, cx + CELL_W, cy + CELL_H, 0x40FFFFFF);

            ctx.drawString(textRenderer, LABELS[i], x + cachedTextX[i], cy + textYOffset, i == selectedIndex ? 0xFFFFFFFF : 0xFF404040, false);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return cellAt(mouseX, mouseY) >= 0;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        int cell = cellAt(mouseX, mouseY);
        if (cell < 0) return false;

        int size = SIZES[cell];
        if (property.get() != size)
        {
            property.set(size);
            changeMadeCallback.run();
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
