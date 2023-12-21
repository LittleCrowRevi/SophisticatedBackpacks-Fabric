package net.p3pp3rf1y.sophisticatedbackpacks.registry.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;
import net.p3pp3rf1y.sophisticatedbackpacks.registry.IRegistryDataLoader;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class SwordRegistry {
	private SwordRegistry() {}

	private static final Set<Item> SWORD_ITEMS = new HashSet<>();
	private static final Map<String, Set<Predicate<ItemStack>>> MOD_SWORD_MATCHERS = new HashMap<>();
	private static final Set<Predicate<ItemStack>> SWORD_MATCHERS = new HashSet<>();

	public static boolean isSword(ItemStack stack) {
		if (SWORD_ITEMS.contains(stack.getItem())) {
			return true;
		}

		if (stack.getItem() instanceof SwordItem) {
			SWORD_ITEMS.add(stack.getItem());
			return true;
		}

		Optional<RegistryKey<Item>> registryName = Registries.ITEM.getKey(stack.getItem());
		if (registryName.isEmpty()) {
			return false;
		}

		for (Predicate<ItemStack> swordMatcher : SWORD_MATCHERS) {
			if (swordMatcher.test(stack)) {
				return true;
			}
		}

		String modId = registryName.get().getRegistry().getNamespace();			
		if (!MOD_SWORD_MATCHERS.containsKey(modId)) {
			return false;
		}
				
		Set<Predicate<ItemStack>> matchers = MOD_SWORD_MATCHERS.get(modId);
		for (Predicate<ItemStack> matcher : matchers) {
			if (matcher.test(stack)) {
				return true;
			}
		}

		return false;
	}

	public static class SwordsLoader implements IRegistryDataLoader {
		@Override
		public String getName() {
			return "swords";
		}

		@Override
		public void parse(JsonObject json, @Nullable String modId) {
			JsonArray swords = json.getAsJsonArray("swords");
			for (JsonElement jsonElement : swords) {
				if (jsonElement.isJsonPrimitive()) {
					parseSword(jsonElement.getAsString());
				} else {
					parseSwordMatcher(modId, jsonElement);
				}
			}
		}

		private void parseSwordMatcher(@Nullable String modId, JsonElement jsonElement) {
			Matchers.getItemMatcher(jsonElement)
					.ifPresent(swordMatcher -> {
								if (modId != null) {
									MOD_SWORD_MATCHERS.computeIfAbsent(modId, m -> new HashSet<>()).add(swordMatcher);
								} else {
									SWORD_MATCHERS.add(swordMatcher);
								}
							}
					);
		}

		private void parseSword(String swordName) {
			Optional<Item> sword = Registries.ITEM.getOrEmpty(new Identifier(swordName));
			if (sword.isPresent()) {
				SWORD_ITEMS.add(sword.get());
			} else {
				String modId = swordName.split(":")[0];
				if (!FabricLoader.getInstance().isModLoaded(modId)) {
					SophisticatedBackpacks.LOGGER.debug("Mod {} isn't loaded skipping load of sword {}", modId, swordName);
				} else {
					SophisticatedBackpacks.LOGGER.warn("Mod {} is loaded and yet sword {} doesn't exist in registry, skipping its load", modId, swordName);
				}
			}
		}

		@Override
		public void clear() {
			SWORD_ITEMS.clear();
			SWORD_MATCHERS.clear();
			MOD_SWORD_MATCHERS.clear();
		}
	}
}
