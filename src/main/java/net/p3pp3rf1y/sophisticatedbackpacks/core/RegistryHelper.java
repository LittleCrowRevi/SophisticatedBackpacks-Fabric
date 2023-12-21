package net.p3pp3rf1y.sophisticatedbackpacks.core;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.Validate;

import java.util.Optional;

public class RegistryHelper {
    private RegistryHelper() {}

    public static Identifier getItemKey(Item item) {
        Identifier itemKey = Registries.ITEM.getId(item);
        Validate.notNull(itemKey, "itemKey");
        return itemKey;
    }

    public static Optional<Identifier> getRegistryName(RegistryEntry<?> registryEntry) {
        return Optional.of(new Identifier(registryEntry.getKey().get().toString()));
    }

    public static Optional<Item> getItemFromName(String itemName) {
        Identifier key = new Identifier(itemName);
        if (Registries.ITEM.containsId(key)) {
            //noinspection ConstantConditions - checked above with containsKey
            return Optional.of(Registries.ITEM.get(key));
        }
        return Optional.empty();
    }
}
