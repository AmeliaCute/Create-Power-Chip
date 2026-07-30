package xyz.amycute.powerchip.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import xyz.amycute.powerchip.PowerChips;

import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = PowerChips.MOD_ID, value = Dist.CLIENT)
public final class EasterEggNameFormatter
{
    private static final Set<UUID> GOONING_UUIDS = Set.of(
        UUID.fromString("e33c9383-1949-4848-9456-e16c88a67b77"), // AMC
        UUID.fromString("30c8af5c-7965-45ff-8da4-f36896c36bb0"), // EMQ
        UUID.fromString("e90240dd-92d2-4a08-b241-55896ae04c94") // AMQ
    );

    @SubscribeEvent
    public static void onNameFormat(PlayerEvent.NameFormat event)
    {
        UUID uuid = event.getEntity().getUUID();
        if (!GOONING_UUIDS.contains(uuid)) return;

        String name = event.getUsername().getString();
        event.setDisplayname(gradient(name));
    }

    private static MutableComponent gradient(String text)
    {
        text = "❤ " + text;
        MutableComponent result = Component.empty();
        int length = Math.max(1, text.length() - 1);

        for (int i = 0; i < text.length(); i++)
        {
            float t = text.length() == 1 ? 0f : (float) i / length;
            int color = lerpColor(0xFF9DC9, 0xE0339E, t);
            Style style = Style.EMPTY.withColor(TextColor.fromRgb(color)).withBold(true);
            result.append(Component.literal(String.valueOf(text.charAt(i))).setStyle(style));
        }
        return result;
    }

    private static int lerpColor(int from, int to, float t)
    {
        int r1 = (from >> 16) & 0xFF, g1 = (from >> 8) & 0xFF, b1 = from & 0xFF;
        int r2 = (to >> 16) & 0xFF, g2 = (to >> 8) & 0xFF, b2 = to & 0xFF;

        int r = Math.round(r1 + (r2 - r1) * t);
        int g = Math.round(g1 + (g2 - g1) * t);
        int b = Math.round(b1 + (b2 - b1) * t);

        return (r << 16) | (g << 8) | b;
    }
}