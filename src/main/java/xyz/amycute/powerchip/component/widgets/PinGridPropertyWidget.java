package xyz.amycute.powerchip.component.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.circuits.components.properties.PropertyEntry;
import org.patryk3211.powergrid.circuits.gui.ComponentPropertiesWidget;
import org.patryk3211.powergrid.circuits.gui.PropertyWidget;
import xyz.amycute.powerchip.component.IOPinComponent;

import java.util.function.IntSupplier;

public final class PinGridPropertyWidget extends PropertyWidget<Integer, PropertyEntry<Integer>> implements WidePropertyWidget
{
    private static final int MAX_PIN_COUNT = IOPinComponent.MAX_PINS;

    private static final int MAX_COLS = 6;
    private static final int CELL_W = 13;
    private static final int CELL_H = 8;
    private static final int GAP = 1;

    private static final String[] LABELS = new String[MAX_PIN_COUNT];
    static
    {
        for (int i = 0; i < MAX_PIN_COUNT; i++) LABELS[i] = Integer.toString(i);
    }

    private final Runnable changeMadeCallback;
    private final IntSupplier activePinCountSupplier;
    private final int textYOffset;

    private final int contentYOffset;

    private int cachedCount = -1;
    private int cols;
    private int rows;
    private int gridW, gridH, bgW, bgH, gridXOffset, gridYOffset;
    private final int[] cellX = new int[MAX_PIN_COUNT];
    private final int[] cellY = new int[MAX_PIN_COUNT];
    private final int[] textX = new int[MAX_PIN_COUNT];

    public PinGridPropertyWidget(Font textRenderer, int x, int y, PropertyEntry<Integer> property, Runnable changeMadeCallback)
    {
        this(textRenderer, x, y, property, changeMadeCallback, null);
    }

    public PinGridPropertyWidget(Font textRenderer, int x, int y, PropertyEntry<Integer> property, Runnable changeMadeCallback, IntSupplier activePinCountSupplier)
    {
        super(textRenderer, x, y, property);
        this.changeMadeCallback = changeMadeCallback;
        this.activePinCountSupplier = activePinCountSupplier;
        this.textYOffset = (CELL_H - textRenderer.lineHeight) / 2 + 1;
        this.contentYOffset = 6 + textRenderer.lineHeight;
        relayout();
    }

    private int activeCount()
    {
        int count = activePinCountSupplier != null ? activePinCountSupplier.getAsInt() : MAX_PIN_COUNT;
        if (count < 1) count = 1;
        if (count > MAX_PIN_COUNT) count = MAX_PIN_COUNT;
        return count;
    }

    private void relayout()
    {
        int count = activeCount();
        if (count == cachedCount) return;
        cachedCount = count;

        rows = Math.max(2, (count + MAX_COLS - 1) / MAX_COLS);
        cols = Math.min(MAX_COLS, (count + rows - 1) / rows);
        rows = (count + cols - 1) / cols;

        gridW = cols * CELL_W + (cols - 1) * GAP;
        gridH = rows * CELL_H + (rows - 1) * GAP;
        bgW = gridW + 8;
        bgH = rows * CELL_H + (rows - 1) * GAP + 8;
        gridXOffset = (bgW - gridW) / 2;
        gridYOffset = (bgH - gridH) / 2;

        for (int i = 0; i < count; i++)
        {
            cellX[i] = gridXOffset + (i % cols) * (CELL_W + GAP);
            cellY[i] = gridYOffset + (i / cols) * (CELL_H + GAP);
            textX[i] = cellX[i] + (CELL_W - textRenderer.width(LABELS[i])) / 2 + 1;
        }
    }

    @Override
    public int powerchip$renderedWidth()
    {
        relayout();
        return bgW;
    }

    @Override
    public int powerchip$renderedHeight()
    {
        relayout();
        return contentYOffset + bgH + 1;
    }

    private int cellAt(double mouseX, double mouseY)
    {
        relayout();
        double localX = mouseX - getX() - gridXOffset;
        double localY = mouseY - getY() - contentYOffset - gridYOffset;
        if (localX < 0 || localY < 0 || localX >= gridW || localY >= gridH) return -1;
        if (localX % (CELL_W + GAP) >= CELL_W || localY % (CELL_H + GAP) >= CELL_H) return -1;

        int cell = ((int) (localY / (CELL_H + GAP))) * cols + (int) (localX / (CELL_W + GAP));
        return cell < cachedCount ? cell : -1;
    }

    public int getBackgroundWidth()
    {
        relayout();
        return bgW;
    }

    @Override
    protected void doRender(@NotNull GuiGraphics ctx, int mouseX, int mouseY, float partialTicks)
    {
        relayout();

        int x = getX();
        int y = getY() + contentYOffset;
        ctx.blit(ComponentPropertiesWidget.PROPERTIES, x, y, 0, 99, bgW, bgH);

        int selected = property.get();
        int hovered = cellAt(mouseX, mouseY);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        for (int i = 0; i < cachedCount; i++)
        {
            int cx = x + cellX[i];
            int cy = y + cellY[i];

            AllGuiTextures tex = i == selected ? AllGuiTextures.BUTTON_HOVER : AllGuiTextures.BUTTON;
            ctx.blit(tex.location, cx, cy, tex.getStartX(), tex.getStartY(), CELL_W, CELL_H);
            if (i == hovered && i != selected) ctx.fill(cx, cy, cx + CELL_W, cy + CELL_H, 0x40FFFFFF);

            ctx.drawString(textRenderer, LABELS[i], x + textX[i], cy + textYOffset, i == selected ? 0xFFFFFFFF : 0xFF404040, false);
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

        if (property.get() != cell)
        {
            property.set(cell);
            changeMadeCallback.run();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
