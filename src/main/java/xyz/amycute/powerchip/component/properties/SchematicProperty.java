package xyz.amycute.powerchip.component.properties;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.circuits.components.properties.ComponentProperty;

public class SchematicProperty extends ComponentProperty<CompoundTag>
{
    public SchematicProperty(String namespace, String name)
    {
        super(namespace, name);
        hidden();
    }

    @Override
    public CompoundTag parse(String value)
    {
        throw new UnsupportedOperationException("SchematicProperty cannot be parsed from a string");
    }

    @Override
    public String toString(CompoundTag value)
    {
        return "<schematic>";
    }

    @Override
    public CompoundTag read(HolderLookup.Provider registries, @Nullable Tag element)
    {
        if (element == null || element.getId() != Tag.TAG_COMPOUND) return new CompoundTag();
        return ((CompoundTag) element).copy();
    }

    @Override
    public Tag write(HolderLookup.Provider registries, CompoundTag value)
    {
        return value == null ? new CompoundTag() : value.copy();
    }

    @Override
    public CompoundTag defaultValue()
    {
        return new CompoundTag();
    }
}
