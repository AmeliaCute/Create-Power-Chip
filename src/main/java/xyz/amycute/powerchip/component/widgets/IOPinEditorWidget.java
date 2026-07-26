package xyz.amycute.powerchip.component.widgets;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import xyz.amycute.powerchip.PowerChips;
import xyz.amycute.powerchip.component.ChipComponent;
import xyz.amycute.powerchip.component.ChipSizeSync;
import xyz.amycute.powerchip.component.IOPinComponent;

public final class IOPinEditorWidget extends AbstractSimiWidget
{
    private static final int PANEL_W = 150;
    private static final int LABEL_BAR_H = 16;
    private static final int SLIDER_BAR_H = 24;
    private static final int CHIP_AREA_PAD = 6;
    private static final int PIN_BTN_W = 20;
    private static final int PIN_BTN_H = 10;
    private static final int PIN_BTN_GAP = 1;
    private static final int CHIP_MARGIN = 1;
    private static final int LABEL_BOX_H = 9;
    private static final int SOCKET_PAD = 4;
    private static final int CONTAINER_PAD = 4;
    private static final int CHIP_PAD = 8;

    private static final ResourceLocation PANEL_PIN_BG_SPRITE = ResourceLocation.fromNamespaceAndPath(PowerChips.MOD_ID, "pin_grid");
    private static final ResourceLocation PANEL_BG_SPRITE = ResourceLocation.fromNamespaceAndPath(PowerChips.MOD_ID, "pin_editor_panel");
    private static final int SLIDER_HITBOX_PAD = 5;

    private final Font textRenderer;
    private final Runnable changeMadeCallback;
    private final EditBox labelBox;

    private PlacedComponent placed;

    private int pinCount;
    private int chipAreaY;
    private int chipAreaH;
    private int chipX, chipY, chipW, chipH;
    private final int[] pinX = new int[IOPinComponent.MAX_PINS];
    private final int[] pinY = new int[IOPinComponent.MAX_PINS];
    private final boolean[] pinOnLeft = new boolean[IOPinComponent.MAX_PINS];
    private int socketX0, socketY0, socketX1, socketY1;
    private boolean socketLayoutDone = false;
    private int cachedLayoutPinCount = -1;

    private static final int SLIDER_KNOB_W = 12;
    private static final int SLIDER_KNOB_H = 10;
    private static final float SLIDER_SMOOTHING = 0.35f;

    private int sliderBarY0, sliderBarY1;
    private int sliderTrackX, sliderTrackW;
    private boolean draggingSlider = false;
    private float visualT = -1f;

    private boolean editingLabel = false;

    public IOPinEditorWidget(Font textRenderer, int x, int y, Runnable changeMadeCallback)
    {
        super(x, y, PANEL_W, computeHeight());
        this.textRenderer = textRenderer;
        this.changeMadeCallback = changeMadeCallback;

        this.labelBox = new EditBox(textRenderer, x + 6, y + 4, PANEL_W - 12, LABEL_BOX_H, Component.empty());
        this.labelBox.setTextColor(0xFFFFFFFF);
        this.labelBox.setBordered(false);
        this.labelBox.setMaxLength(32);
        this.labelBox.setEditable(true);
        this.labelBox.setFocused(false);
        this.labelBox.setResponder(this::onLabelEdited);
    }

    private static final int BOTTOM_PAD = 6;

    private static int computeHeight()
    {
        int maxRows = (IOPinComponent.MAX_PINS + 1) / 2;
        int chipAreaH = CHIP_AREA_PAD * 2 + Math.max(60, maxRows * (PIN_BTN_H + PIN_BTN_GAP));
        return LABEL_BAR_H + chipAreaH + SLIDER_BAR_H + BOTTOM_PAD;
    }

    public void setComponent(PlacedComponent placed)
    {
        this.placed = placed;
        cachedLayoutPinCount = -1;
        editingLabel = false;
        labelBox.setFocused(false);
        if (placed != null)
        {
            String label = placed.getString(IOPinComponent.PIN_LABEL);
            labelBox.setValue(label == null ? "" : label);
            relayout();

            visualT = targetT();
        }
    }

    private void onLabelEdited(String value)
    {
        if (placed == null) return;
        placed.setString(IOPinComponent.PIN_LABEL, value);
        changeMadeCallback.run();
    }

    private int activePinCount()
    {
        int count = placed != null ? placed.get(IOPinComponent.PIN_COUNT) : ChipComponent.SIZES[ChipComponent.SIZES.length - 1];
        if (count < 1) count = 1;
        if (count > IOPinComponent.MAX_PINS) count = IOPinComponent.MAX_PINS;
        return count;
    }

    private void relayout()
    {
        int count = activePinCount();
        if (count == cachedLayoutPinCount) return;
        cachedLayoutPinCount = count;
        pinCount = count;

        chipAreaY = getY() + LABEL_BAR_H;
        chipAreaH = getHeight() - LABEL_BAR_H - SLIDER_BAR_H - BOTTOM_PAD;

        int rightCount = count / 2;
        int leftCount = count - rightCount;
        int maxSideCount = Math.max(leftCount, rightCount);

        int usableH = chipAreaH - CHIP_AREA_PAD * 2;
        int rowH = PIN_BTN_H + PIN_BTN_GAP;
        int totalSideH = maxSideCount * rowH - PIN_BTN_GAP + 4;
        int sideStartY = chipAreaY + CHIP_AREA_PAD + Math.max(0, (usableH - totalSideH) / 2);

        int leftX = getX() + CONTAINER_PAD + SOCKET_PAD;
        int rightX = getX() + PANEL_W - CONTAINER_PAD - SOCKET_PAD - PIN_BTN_W;

        for (int i = 0; i < count; i++)
        {
            boolean left = i >= rightCount;
            int rowIndex = left ? (i - rightCount) : i;
            pinOnLeft[i] = left;
            pinX[i] = left ? leftX : rightX;
            pinY[i] = sideStartY + rowIndex * rowH;
        }

        if (!socketLayoutDone)
        {
            socketLayoutDone = true;
            socketX0 = Math.max(leftX - SOCKET_PAD, getX() + CONTAINER_PAD);
            socketX1 = Math.min(rightX + PIN_BTN_W + SOCKET_PAD, getX() + PANEL_W - CONTAINER_PAD);
            socketY0 = Math.max(chipAreaY + CHIP_AREA_PAD - SOCKET_PAD, chipAreaY + CONTAINER_PAD);
            socketY1 = Math.min(chipAreaY + chipAreaH - CHIP_AREA_PAD + SOCKET_PAD, chipAreaY + chipAreaH - CONTAINER_PAD);
        }

        int centerX = getX() + PANEL_W / 2;
        int centerY = socketY0 + (socketY1 - socketY0) / 2;

        int chipAreaLeft = leftX + PIN_BTN_W + CHIP_MARGIN;
        int chipAreaRight = rightX - CHIP_MARGIN;
        int maxChipW = (chipAreaRight - chipAreaLeft) - 2 * CHIP_PAD;

        int[] sizes = ChipComponent.SIZES;
        int minPins = sizes[0];
        int maxPins = sizes[sizes.length - 1];
        float widthT = maxPins <= minPins ? 1f : (float) (count - minPins) / (maxPins - minPins);

        int minChipW = 55;
        chipW = Math.round(minChipW + widthT * (maxChipW - minChipW));
        chipW = Math.clamp(chipW, 24, maxChipW);

        chipH = Math.max(24, totalSideH);

        chipX = centerX - chipW / 2;
        chipY = centerY - chipH / 2;
    }

    @Override
    protected void doRender(@NotNull GuiGraphics ctx, int mouseX, int mouseY, float partialTicks)
    {
        if (placed == null) return;

        if (draggingSlider)
        {
            long window = Minecraft.getInstance().getWindow().getWindow();
            boolean stillDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            if (stillDown) setSizeFromMouseX(mouseX);
            else draggingSlider = false;
        }

        relayout();

        int x = getX(), y = getY();
        int selected = placed.get(IOPinComponent.PIN);

        ctx.blitSprite(PANEL_BG_SPRITE, x, y, PANEL_W, getHeight());

        labelBox.setX(x + 6);
        labelBox.setY(y + 4);
        labelBox.render(ctx, mouseX, mouseY, partialTicks);
        if (!editingLabel && labelBox.getValue().isEmpty())
        {
            String hint = Component.translatable(PowerChips.MOD_ID+".component.gui.pin_hint", selected).getString();
            ctx.drawString(textRenderer, hint, x + 7, y + 5, 0xFF8A8A8A, false);
        }

        ctx.fill(x + 4, y + LABEL_BAR_H - 1, x + PANEL_W - 4, y + LABEL_BAR_H, 0xFF8B8B8B);
        ctx.blitSprite(PANEL_PIN_BG_SPRITE, socketX0, socketY0, socketX1 - socketX0, socketY1 - socketY0);

        ctx.fill(chipX, chipY, chipX + chipW, chipY + chipH, 0xFF2B2B2B);
        ctx.fill(chipX, chipY, chipX + chipW, chipY + 2, 0xFF454545);
        ctx.fill(chipX, chipY, chipX + 2, chipY + chipH, 0xFF454545);
        ctx.fill(chipX + chipW - 2, chipY, chipX + chipW, chipY + chipH, 0xFF141414);
        ctx.fill(chipX, chipY + chipH - 2, chipX + chipW, chipY + chipH, 0xFF141414);

        int notchSize = 6;
        ctx.fill(chipX, chipY, chipX + notchSize, chipY + notchSize, 0xFF585858);

        String sizeLabel = Component.translatable(PowerChips.MOD_ID+".component.gui.pin_count_label", pinCount).getString();
        int labelWidth = textRenderer.width(sizeLabel);
        ctx.drawString(textRenderer, sizeLabel, chipX + (chipW - labelWidth) / 2, chipY + chipH / 2 - 4, 0xFFB0B0B0, false);

        int hovered = pinAt(mouseX, mouseY);
        for (int i = 0; i < pinCount; i++)
        {
            int bx = pinX[i], by = pinY[i];
            AllGuiTextures tex = i == selected ? AllGuiTextures.BUTTON_HOVER : AllGuiTextures.BUTTON;
            ctx.blit(tex.location, bx, by, tex.getStartX(), tex.getStartY(), PIN_BTN_W, PIN_BTN_H);
            if (i == hovered && i != selected) ctx.fill(bx, by, bx + PIN_BTN_W, by + PIN_BTN_H, 0x40FFFFFF);

            String label = Integer.toString(i);
            int tw = textRenderer.width(label);
            int ty = by + (PIN_BTN_H - textRenderer.lineHeight) / 2 + 1;
            ctx.drawString(textRenderer, label, bx + (PIN_BTN_W - tw) / 2, ty, i == selected ? 0xFFFFFFFF : 0xFF404040, false);

            boolean left = pinOnLeft[i];
            int leadStartX = left ? bx + PIN_BTN_W : bx;
            int leadEndX = left ? chipX : chipX + chipW;
            int leadY = by + PIN_BTN_H / 2;
            ctx.fill(Math.min(leadStartX, leadEndX), leadY, Math.max(leadStartX, leadEndX), leadY + 1, 0xFF8B8B8B);
        }

        sliderBarY0 = y + getHeight() - BOTTOM_PAD - SLIDER_BAR_H;
        sliderBarY1 = y + getHeight() - BOTTOM_PAD;
        sliderTrackX = x + 6;
        sliderTrackW = PANEL_W - 12;
        drawSlider(ctx, mouseX, mouseY);
    }

    private float targetT()
    {
        int[] sizes = ChipComponent.SIZES;
        int index = indexOfSize(placed.get(IOPinComponent.PIN_COUNT));
        int count = sizes.length;
        return count <= 1 ? 0f : (float) index / (count - 1);
    }

    private void drawSlider(GuiGraphics ctx, int mouseX, int mouseY)
    {
        int currentSize = placed.get(IOPinComponent.PIN_COUNT);

        if (visualT < 0f)
            visualT = targetT();

        if (!draggingSlider)
        {
            float target = targetT();
            visualT += (target - visualT) * SLIDER_SMOOTHING;
            if (Math.abs(target - visualT) < 0.001f) visualT = target;
        }

        int textY = sliderBarY0 + 3;
        int trackY = sliderBarY0 + 15;
        int knobY = trackY - (SLIDER_KNOB_H / 2 - 1);

        ctx.fill(sliderTrackX, trackY, sliderTrackX + sliderTrackW, trackY + 2, 0xFF373737);
        ctx.fill(sliderTrackX, trackY + 2, sliderTrackX + sliderTrackW, trackY + 3, 0xFFFFFFFF);

        int knobW = SLIDER_KNOB_W;
        int knobX = sliderTrackX + Math.round(visualT * (sliderTrackW - knobW));

        boolean hovering = mouseX >= sliderTrackX - 4 && mouseX <= sliderTrackX + sliderTrackW + 4 && mouseY >= sliderBarY0 && mouseY <= sliderBarY1;
        AllGuiTextures knobTex = (hovering || draggingSlider) ? AllGuiTextures.BUTTON_HOVER : AllGuiTextures.BUTTON;
        ctx.blit(knobTex.location, knobX, knobY, knobTex.getStartX(), knobTex.getStartY(), knobW, SLIDER_KNOB_H);

        String sizeText = Component.translatable(PowerChips.MOD_ID+".component.gui.size_label", currentSize).getString();
        int tw = textRenderer.width(sizeText);
        ctx.drawString(textRenderer, sizeText, sliderTrackX + (sliderTrackW - tw) / 2, textY, 0xFF404040, false);
    }

    private static int indexOfSize(int size)
    {
        int[] sizes = ChipComponent.SIZES;
        for (int i = 0; i < sizes.length; i++) if (sizes[i] == size) return i;
        return sizes.length - 1;
    }

    private int pinAt(double mouseX, double mouseY)
    {
        for (int i = 0; i < pinCount; i++)
            if (mouseX >= pinX[i] && mouseX < pinX[i] + PIN_BTN_W && mouseY >= pinY[i] && mouseY < pinY[i] + PIN_BTN_H) return i;

        return -1;
    }

    private boolean isOverLabelBox(double mouseX, double mouseY)
    {
        int x = getX(), y = getY();
        return mouseX >= x + 6 && mouseX < x + 6 + (PANEL_W - 12) && mouseY >= y + 4 && mouseY < y + 4 + LABEL_BOX_H;
    }

    private void setSizeFromMouseX(double mouseX)
    {
        int[] sizes = ChipComponent.SIZES;
        int count = sizes.length;
        float t = (float) (mouseX - sliderTrackX) / Math.max(1, (sliderTrackW - SLIDER_KNOB_W));
        t = Math.clamp(t, 0f, 1f);

        if (draggingSlider)
        {
            visualT = t;
        }

        int index = Math.round(t * (count - 1));
        index = Math.clamp(index, 0, count - 1);
        int newSize = sizes[index];

        if (placed.get(IOPinComponent.PIN_COUNT) != newSize)
        {
            ChipSizeSync.broadcast(newSize);
            changeMadeCallback.run();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (placed == null) return false;

        if (isOverLabelBox(mouseX, mouseY))
        {
            editingLabel = true;
            labelBox.setFocused(true);
            labelBox.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        else if (editingLabel)
        {
            editingLabel = false;
            labelBox.setFocused(false);
        }

        int pin = pinAt(mouseX, mouseY);
        if (pin >= 0)
        {
            if (placed.get(IOPinComponent.PIN) != pin)
            {
                placed.set(IOPinComponent.PIN, pin);
                changeMadeCallback.run();
            }
            return true;
        }

        if (mouseY >= sliderBarY0 - SLIDER_HITBOX_PAD && mouseY <= sliderBarY1 + SLIDER_HITBOX_PAD
                && mouseX >= sliderTrackX - (double) SLIDER_KNOB_W / 2 - SLIDER_HITBOX_PAD && mouseX <= sliderTrackX + sliderTrackW + (double) SLIDER_KNOB_W / 2 + SLIDER_HITBOX_PAD)
        {
            draggingSlider = true;
            setSizeFromMouseX(mouseX);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if (draggingSlider)
        {
            setSizeFromMouseX(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        boolean was = draggingSlider;
        draggingSlider = false;
        return was || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return placed != null && mouseX >= getX() && mouseX < getX() + PANEL_W  && mouseY >= getY() && mouseY < getY() + getHeight();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (placed == null || !editingLabel) return false;

        if (keyCode == 256 || keyCode == 257)
        {
            editingLabel = false;
            labelBox.setFocused(false);
            return true;
        }
        return labelBox.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers)
    {
        return placed != null && editingLabel && labelBox.charTyped(chr, modifiers);
    }
}
