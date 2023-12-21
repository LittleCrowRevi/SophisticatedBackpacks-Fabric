package net.p3pp3rf1y.sophisticatedbackpacks.registry.tool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;
import net.p3pp3rf1y.sophisticatedbackpacks.core.WorldHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Matchers {
	private Matchers() {}

	private static final List<ItemMatcherFactory> ITEM_MATCHER_FACTORIES = new ArrayList<>();
	private static final List<IMatcherFactory<BlockContext>> BLOCK_MATCHER_FACTORIES = new ArrayList<>();
	private static final List<IMatcherFactory<Entity>> ENTITY_MATCHER_FACTORIES = new ArrayList<>();

	static {
		addItemMatcherFactory(new ItemMatcherFactory("tag") {
			@Override
			protected Optional<Predicate<ItemStack>> getPredicateFromObject(JsonObject jsonObject) {
				String tagName = JsonHelper.asString(jsonObject, "tag");
				TagKey<Item> tag = TagKey.of(Registries.ITEM.getKey(), new Identifier(tagName));
				return Optional.of(new ItemTagMatcher(tag));
			}
		});

		addItemMatcherFactory(new ItemMatcherFactory("emptynbt") {
			@Override
			protected Optional<Predicate<ItemStack>> getPredicateFromObject(JsonObject jsonObject) {
				Identifier itemName = new Identifier(JsonHelper.asString(jsonObject, "item"));
				if (!Registries.ITEM.contains(RegistryKey.of(Registries.ITEM.getKey(), itemName))) {
					SophisticatedBackpacks.LOGGER.debug("{} isn't loaded in item registry, skipping ...", itemName);
				}
				Item item = Registries.ITEM.get(itemName);
				return Optional.of(st -> st.getItem() == item && (st.streamTags() == null || st.streamTags().findAny().isEmpty()));
			}
		});

		BLOCK_MATCHER_FACTORIES.add(new IMatcherFactory<>() {
			@Override
			public boolean appliesTo(JsonElement jsonElement) {
				return jsonElement.isJsonPrimitive();
			}

			@Override
			public Optional<Predicate<BlockContext>> getPredicate(JsonElement jsonElement) {
				String modId = jsonElement.getAsString();
				if (!FabricLoader.getInstance().isModLoaded(modId)) {
					SophisticatedBackpacks.LOGGER.debug("{} mod isn't loaded, skipping ...", modId);
					return Optional.empty();
				}

				return Optional.of(new ModMatcher<>(Registries.BLOCK, modId, BlockContext::getBlock));
			}
		});
		BLOCK_MATCHER_FACTORIES.add(new TypedMatcherFactory<>("all") {
			@Override
			protected Optional<Predicate<BlockContext>> getPredicateFromObject(JsonObject jsonObject) {
				return Optional.of(block -> true);
			}
		});
		BLOCK_MATCHER_FACTORIES.add(new TypedMatcherFactory<>("rail") {
			@Override
			protected Optional<Predicate<BlockContext>> getPredicateFromObject(JsonObject jsonObject) {
				return Optional.of(blockContext -> blockContext.getBlock() != null);
			}
		});
		BLOCK_MATCHER_FACTORIES.add(new TypedMatcherFactory<>("item_handler") {
			@Override
			protected Optional<Predicate<BlockContext>> getPredicateFromObject(JsonObject jsonObject) {
				return Optional.of(blockContext -> WorldHelper.getBlockEntity(blockContext.getWorld(),
						blockContext.getPos()).map(te -> te.getCachedState() instanceof Inventory).orElse(false));
			}
		});
		ENTITY_MATCHER_FACTORIES.add(new TypedMatcherFactory<>("animal") {
			@Override
			protected Optional<Predicate<Entity>> getPredicateFromObject(JsonObject jsonObject) {
				return Optional.of(AnimalEntity.class::isInstance);
			}
		});
		ENTITY_MATCHER_FACTORIES.add(new TypedMatcherFactory<>("living") {
			@Override
			protected Optional<Predicate<Entity>> getPredicateFromObject(JsonObject jsonObject) {
				return Optional.of(LivingEntity.class::isInstance);
			}
		});
		ENTITY_MATCHER_FACTORIES.add(new TypedMatcherFactory<>("bee") {
			@Override
			protected Optional<Predicate<Entity>> getPredicateFromObject(JsonObject jsonObject) {
				return Optional.of(BeeEntity.class::isInstance);
			}
		});
		ENTITY_MATCHER_FACTORIES.add(new TypedMatcherFactory<>("tameable") {
			@Override
			protected Optional<Predicate<Entity>> getPredicateFromObject(JsonObject jsonObject) {
				return Optional.of(Tameable.class::isInstance);
			}
		});
	}

	static void addItemMatcherFactory(ItemMatcherFactory matcherFactory) {
		ITEM_MATCHER_FACTORIES.add(matcherFactory);
	}

	public static Optional<Predicate<ItemStack>> getItemMatcher(JsonElement jsonElement) {
		for (ItemMatcherFactory itemMatcherFactory : Matchers.ITEM_MATCHER_FACTORIES) {
			if (itemMatcherFactory.appliesTo(jsonElement)) {
				return itemMatcherFactory.getPredicate(jsonElement);
			}
		}
		return Optional.empty();
	}

	public static List<IMatcherFactory<BlockContext>> getBlockMatcherFactories() {
		return BLOCK_MATCHER_FACTORIES;
	}

	public static List<IMatcherFactory<Entity>> getEntityMatcherFactories() {
		return ENTITY_MATCHER_FACTORIES;
	}
}
