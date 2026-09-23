package xyz.amycute.powerchip.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;

public final class Registries
{
    public static HolderLookup.Provider get()
    {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) return server.registryAccess();

        if (FMLEnvironment.dist == Dist.CLIENT)
        {
            HolderLookup.Provider client = ClientRegistries.get();
            if (client != null) return client;
        }
        return RegistryAccess.EMPTY;
    }

    public static HolderLookup.Provider of(@Nullable Level level)
    {
        return level != null ? level.registryAccess() : get();
    }

    public static HolderLookup.Provider of(@Nullable PlacedComponent placed)
    {
        if (placed == null) return get();

        try
        {
            return of(placed.getWorld());
        }
        catch (Exception ignored)
        {
            return get();
        }
    }
}
