package net.p3pp3rf1y.sophisticatedbackpacks.registry.tool;

import net.minecraft.registry.Registerable;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.p3pp3rf1y.sophisticatedbackpacks.core.RegistryHelper;

import java.util.function.Function;
import java.util.function.Predicate;

public class ModMatcher<V, R extends Registry<V>, C> implements Predicate<C> {
	private final R registry;
	private final String modId;
	private final Function<C, V> getObjectFromContext;

	public ModMatcher(R registry, String modId, Function<C, V> getObjectFromContext) {
		this.registry = registry;
		this.modId = modId;
		this.getObjectFromContext = getObjectFromContext;
		ToolRegistry.addModWithMapping(modId);
	}

	@Override
	public boolean test(C context) {
		return RegistryHelper.getRegistryName((RegistryEntry<?>) getObjectFromContext.apply(context)).map(rn -> rn.getNamespace().equals(modId)).orElse(false);
	}
}
