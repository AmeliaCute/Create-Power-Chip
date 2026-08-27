package xyz.amycute.powerchip.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;
import org.patryk3211.powergrid.circuits.schematic.CircuitSchematic;

public final class BoardSchematic
{
    private static final String ROOT = "";
    private static String cachedKey = null;

    private BoardSchematic() {}

    public static CompoundTag read(CircuitBoardBlockEntity be, HolderLookup.Provider registries)
    {
        CompoundTag root = be.saveWithoutMetadata(registries);

        String key = cachedKey;
        if (key != null)
        {
            CompoundTag hit = ROOT.equals(key) ? root : root.getCompound(key);
            if (!hit.isEmpty() && CircuitSchematic.fromNbt(hit) != null) return hit;
            cachedKey = null;
        }

        if (CircuitSchematic.fromNbt(root) != null)
        {
            cachedKey = ROOT;
            return root;
        }

        for (String candidate : root.getAllKeys())
        {
            if (root.getTagType(candidate) != Tag.TAG_COMPOUND) continue;

            CompoundTag child = root.getCompound(candidate);
            if (child.isEmpty() || CircuitSchematic.fromNbt(child) == null) continue;

            cachedKey = candidate;
            return child;
        }

        return null;
    }
}
