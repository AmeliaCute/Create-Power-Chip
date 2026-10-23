package xyz.amycute.powerchip.component.renderings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import xyz.amycute.powerchip.PowerChips;

// Gaymelia moment trying to optimize this shit for runtime and probably making worse, feel free to PR this crap
public final class ChipLabelRenderer
{
    private static final ResourceLocation CHIP_LABEL_SPRITE_LOCATION = ResourceLocation.fromNamespaceAndPath(PowerChips.MOD_ID, "block/component/chip_label");

    private static final float TEXT_SCALE = 0.005125f;

    private static final float PADDING_X = 1f / 16f;
    private static final float PADDING_Z = .5f / 16f;

    private static final float CHIP_TOP_Y = 3.5f / 16f - (2f / 16f);
    private static final float PLATE_Y_OFFSET = CHIP_TOP_Y + (2f / 512f);
    private static final float TEXT_Y_OFFSET = CHIP_TOP_Y + (3f / 512f);

    private static final float LABEL_BORDER_WORLD = 0.01f;
    private static final float BORDER_MAX_FRACTION = 0.15f;
    private static final float LABEL_BORDER_PX = 2f;

    private static TextureAtlasSprite cachedSprite;
    private static float[] cachedUs;
    private static float[] cachedVs;

    private static void ensureCached()
    {
        if (cachedSprite != null) return;

        cachedSprite = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(CHIP_LABEL_SPRITE_LOCATION);
        float texW = cachedSprite.contents().width();
        float texH = cachedSprite.contents().height();
        float borderNominalU = LABEL_BORDER_PX / texW;
        float borderNominalV = LABEL_BORDER_PX / texH;
        float halfU = 0.5f / texW;
        float halfV = 0.5f / texH;

        cachedUs = new float[]
        {
            cachedSprite.getU(0f + halfU),
            cachedSprite.getU(borderNominalU),
            cachedSprite.getU(1f - borderNominalU),
            cachedSprite.getU(1f - halfU)
        };

        cachedVs = new float[]
        {
            cachedSprite.getV(0f + halfV),
            cachedSprite.getV(borderNominalV),
            cachedSprite.getV(1f - borderNominalV),
            cachedSprite.getV(1f - halfV)
        };
    }

    public static void render(PoseStack ms, MultiBufferSource bufferSource, String name, int color, float centerX, float centerZ, int light, int overlay)
    {
        if (name.isEmpty()) return;

        Font font = Minecraft.getInstance().font;
        float textWidthPx = font.width(name);
        float plateWidth = textWidthPx * TEXT_SCALE + PADDING_X;
        float plateHeight = font.lineHeight * TEXT_SCALE + PADDING_Z;

        var buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        renderNineSlicePlate(ms, buffer, centerX - plateWidth / 2f, PLATE_Y_OFFSET, centerZ - plateHeight / 2f, plateWidth, plateHeight, light, overlay);

        ms.pushPose();
        ms.translate(centerX, TEXT_Y_OFFSET, centerZ);
        ms.mulPose(Axis.XP.rotationDegrees(90));
        ms.scale(TEXT_SCALE, TEXT_SCALE, TEXT_SCALE);
        font.drawInBatch(name, -textWidthPx / 2f, -font.lineHeight / 2f, color, false, ms.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, light);
        ms.popPose();
    }

    private static void renderNineSlicePlate(PoseStack ms, VertexConsumer buffer, float originX, float y, float originZ, float width, float height, int light, int overlay)
    {
        ensureCached();

        float borderX = Math.min(LABEL_BORDER_WORLD, width * BORDER_MAX_FRACTION);
        float borderZ = Math.min(LABEL_BORDER_WORLD, height * BORDER_MAX_FRACTION);

        float[] xs =
        {
            originX,
            originX + borderX,
            originX + width - borderX,
            originX + width
        };

        float[] zs =
        {
            originZ,
            originZ + borderZ,
            originZ + height - borderZ,
            originZ + height
        };

        var pose = ms.last();
        for (int cx = 0; cx < 3; cx++)
            for (int cz = 0; cz < 3; cz++)
                plateQuad(pose, buffer, xs[cx], xs[cx + 1], zs[cz], zs[cz + 1], cachedUs[cx], cachedUs[cx + 1], cachedVs[cz], cachedVs[cz + 1], y, light, overlay);
    }

    private static void plateQuad(PoseStack.Pose pose, VertexConsumer buffer, float x0, float x1, float z0, float z1, float u0, float u1, float v0, float v1, float y, int light, int overlay)
    {
        vertex(pose, buffer, x0, y, z0, u0, v0, light, overlay);
        vertex(pose, buffer, x0, y, z1, u0, v1, light, overlay);
        vertex(pose, buffer, x1, y, z1, u1, v1, light, overlay);
        vertex(pose, buffer, x1, y, z0, u1, v0, light, overlay);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float u, float v, int light, int overlay)
    {
        buffer.vertex(pose.pose(), x, y, z).color(255, 255, 255, 255).uv(u, v).overlayCoords(overlay).uv2(light & 65535, light >> 16 & 65535).normal(pose.normal(), 0, 1, 0).endVertex();
    }
}