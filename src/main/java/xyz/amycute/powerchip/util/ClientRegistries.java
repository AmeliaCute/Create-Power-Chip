package xyz.amycute.powerchip.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

final class ClientRegistries
{
    @Nullable
    static HolderLookup.Provider get()
    {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) return minecraft.level.registryAccess();

        ClientPacketListener connection = minecraft.getConnection();
        return connection != null ? connection.registryAccess() : null;
    }
}
