package net.p3pp3rf1y.sophisticatedbackpacks.registry.tool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonHelper;

import java.util.Optional;
import java.util.function.Predicate;

abstract class ItemMatcherFactory {
	private final String typeName;

	public ItemMatcherFactory(String typeName) {
		this.typeName = typeName;
	}

	public boolean appliesTo(JsonElement jsonElement) {
		return jsonElement.isJsonObject() && JsonHelper.asString(jsonElement.getAsJsonObject(), "type").equals(typeName);
	}

	public Optional<Predicate<ItemStack>> getPredicate(JsonElement jsonElement) {
		return getPredicateFromObject(jsonElement.getAsJsonObject());
	}

	protected abstract Optional<Predicate<ItemStack>> getPredicateFromObject(JsonObject jsonObject);
}
