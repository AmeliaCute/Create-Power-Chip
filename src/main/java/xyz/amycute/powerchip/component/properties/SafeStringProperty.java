package xyz.amycute.powerchip.component.properties;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.circuits.components.properties.StringProperty;

public class SafeStringProperty extends StringProperty
{
    private final short limit;

    public SafeStringProperty(String namespace, String name, short limit)
    {
        super(namespace, name);
        this.limit = limit;
    }

    private String limit(String value)
    {
        return value.length() > limit ? value.substring(0, limit) : value;
    }

    @Override
    public String parse(String str)
    {
        return limit(super.parse(str));
    }

    @Override
    public String read(HolderLookup.Provider registries, @Nullable Tag element)
    {
        return limit(super.read(registries, element));
    }
}
