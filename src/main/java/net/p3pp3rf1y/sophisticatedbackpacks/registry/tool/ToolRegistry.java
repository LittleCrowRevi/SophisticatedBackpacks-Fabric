package net.p3pp3rf1y.sophisticatedbackpacks.registry.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;
import net.p3pp3rf1y.sophisticatedbackpacks.core.RegistryHelper;
import net.p3pp3rf1y.sophisticatedbackpacks.registry.IRegistryDataLoader;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ToolRegistry {
	private ToolRegistry() {}

	private static final String TOOLS_PROPERTY = "tools";

	private static final Set<String> modsWithMapping = new HashSet<>();

	private static final ToolMapping<Block, BlockContext> BLOCK_TOOL_MAPPING = new ToolMapping<>(Registries.BLOCK, BlockContext::getBlock);
	private static final ToolMapping<EntityType<?>, Entity> ENTITY_TOOL_MAPPING = new ToolMapping<>(Registries.ENTITY_TYPE, Entity::getType);

	public static boolean isToolForBlock(ItemStack stack, Block block, World world, BlockState blockState, BlockPos pos) {
		return BLOCK_TOOL_MAPPING.isToolFor(stack, block, () -> new BlockContext(world, blockState, block, pos));
	}

	public static boolean isToolForEntity(ItemStack stack, Entity entity) {
		return ENTITY_TOOL_MAPPING.isToolFor(stack, entity.getType(), () -> entity);
	}

	private abstract static class ToolsLoaderBase<V, C> implements IRegistryDataLoader {
		private final List<IMatcherFactory<C>> objectMatcherFactories;
		private final ToolMapping<V, C> toolMapping;
		private final Registry<V> registry;
		private final Function<Identifier, Optional<V>> getObjectFromRegistry;
		private final String name;
		private final String objectJsonArrayName;

		public ToolsLoaderBase(List<IMatcherFactory<C>> objectMatcherFactories, ToolMapping<V, C> toolMapping, Registry<V> registry, Function<Identifier, Optional<V>> getObjectFromRegistry, String name, String objectJsonArrayName) {
			this.objectMatcherFactories = objectMatcherFactories;
			this.toolMapping = toolMapping;
			this.registry = registry;
			this.getObjectFromRegistry = getObjectFromRegistry;
			this.name = name;
			this.objectJsonArrayName = objectJsonArrayName;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void parse(JsonObject json, @Nullable String modId) {
			JsonArray toolsMap = JsonHelper.asArray(json, name);

			for (JsonElement jsonElement : toolsMap) {
				if (!jsonElement.isJsonObject()) {
					continue;
				}
				JsonObject entry = jsonElement.getAsJsonObject();
				parseEntry(entry);
			}
			toolMapping.getObjectTools().keySet().forEach(object -> RegistryHelper.getRegistryName((RegistryEntry<?>) object).ifPresent(rn -> modsWithMapping.add(rn.getNamespace())));
		}

		@Override
		public void clear() {
			toolMapping.clear();
			modsWithMapping.clear();
		}

		private void parseEntry(JsonObject entry) {
			if (entry.size() == 1) {
				parseFromProperty(entry);
			} else {
				if (entry.size() == 2 && entry.has(objectJsonArrayName) && entry.has(TOOLS_PROPERTY)) {
					parseFromArrays(JsonHelper.asArray(entry, objectJsonArrayName), JsonHelper.asArray(entry, TOOLS_PROPERTY));
				} else {
					SophisticatedBackpacks.LOGGER.error("Invalid block tools entry - needs to have either 1 array property with mod/entity name or \"{}\" and \"tools\" array properties {}", objectJsonArrayName, entry);
				}
			}
		}

		private void parseFromArrays(JsonArray blocksArray, JsonArray toolsArray) {
			Pair<Set<Item>, Set<Predicate<ItemStack>>> tools = getItemsAndItemPredicates(toolsArray);
			if (tools.getLeft().isEmpty() && tools.getRight().isEmpty()) {
				return;
			}
			for (JsonElement jsonElement : blocksArray) {
				if (jsonElement.isJsonPrimitive() && jsonElement.getAsString().contains(":")) {
					parseObjectEntry(tools, jsonElement.getAsString());
				} else {
					parseObjectPredicateEntry(tools, jsonElement);
				}
			}
		}

		private void parseObjectPredicateEntry(Pair<Set<Item>, Set<Predicate<ItemStack>>> tools, JsonElement jsonElement) {
			for (IMatcherFactory<C> blockMatcherFactory : objectMatcherFactories) {
				if (blockMatcherFactory.appliesTo(jsonElement)) {
					blockMatcherFactory.getPredicate(jsonElement).ifPresent(predicate -> toolMapping.addObjectPredicateTools(tools, predicate));
					break;
				}
			}
		}

		private void parseObjectEntry(Pair<Set<Item>, Set<Predicate<ItemStack>>> tools, String objectName) {
			Identifier registryName = new Identifier(objectName);
			Optional<V> objectOptional = getObjectFromRegistry.apply(registryName);
			if (objectOptional.isPresent()) {
				toolMapping.addObjectTools(tools, objectOptional.get());
			} else {
				SophisticatedBackpacks.LOGGER.debug("{} doesn't exist in registry, skipping ...", objectName);
			}
		}

		private void parseFromProperty(JsonObject entry) {
			for (Map.Entry<String, JsonElement> property : entry.entrySet()) {
				if (property.getKey().contains(":")) {
					parseObjectTools(property);
				} else {
					parseModTools(property);
				}
			}
		}

		private void parseModTools(Map.Entry<String, JsonElement> property) {
			String modId = property.getKey();
			if (!FabricLoader.getInstance().isModLoaded(modId)) {
				SophisticatedBackpacks.LOGGER.debug("{} mod isn't loaded, skipping ... {} ", modId, property);
				return;
			}
			Pair<Set<Item>, Set<Predicate<ItemStack>>> tools = getItemsAndItemPredicates(property);
			if (tools.getLeft().isEmpty() && tools.getRight().isEmpty()) {
				return;
			}
			toolMapping.addModPredicateTools(modId, tools);
		}

		private void parseObjectTools(Map.Entry<String, JsonElement> property) {
			Pair<Set<Item>, Set<Predicate<ItemStack>>> tools = getItemsAndItemPredicates(property);
			if (tools.getLeft().isEmpty() && tools.getRight().isEmpty()) {
				return;
			}
			parseObjectEntry(tools, property.getKey());
		}

	}

	protected static Pair<Set<Item>, Set<Predicate<ItemStack>>> getItemsAndItemPredicates(Map.Entry<String, JsonElement> property) {
		if (property.getValue().isJsonArray()) {
			JsonArray toolArray = JsonHelper.asArray(property.getValue(), "");
			return getItemsAndItemPredicates(toolArray);
		} else {
			SophisticatedBackpacks.LOGGER.error("Invalid tools list - needs to be an array {}", property.getValue());
			return new Pair<>(Collections.emptySet(), Collections.emptySet());
		}
	}

	protected static Pair<Set<Item>, Set<Predicate<ItemStack>>> getItemsAndItemPredicates(JsonArray toolArray) {
		Set<Item> items = new HashSet<>();
		Set<Predicate<ItemStack>> itemPredicates = new HashSet<>();
		for (JsonElement jsonElement : toolArray) {
			if (jsonElement.isJsonPrimitive()) {
				Identifier itemName = new Identifier(jsonElement.getAsString());
				if (!Registries.ITEM.containsId(itemName)) {
					SophisticatedBackpacks.LOGGER.debug("{itemName} isn't loaded in item registry, skipping ...");
				}
				Item item = Registries.ITEM.get(itemName);
				items.add(item);
			} else if (jsonElement.isJsonObject()) {
				Matchers.getItemMatcher(jsonElement).ifPresent(itemPredicates::add);
			}
		}
		return new Pair<>(items, itemPredicates);
	}

	public static class BlockToolsLoader extends ToolsLoaderBase<Block, BlockContext> {
		public BlockToolsLoader() {
			super(Matchers.getBlockMatcherFactories(), BLOCK_TOOL_MAPPING, Registries.BLOCK, rn -> Optional.ofNullable(Registries.BLOCK.get(rn)), "block_tools", "blocks");
		}
	}

	public static class EntityToolsLoader extends ToolsLoaderBase<EntityType<?>, Entity> {
		public EntityToolsLoader() {
			super(Matchers.getEntityMatcherFactories(), ENTITY_TOOL_MAPPING, Registries.ENTITY_TYPE, rn -> Optional.ofNullable(Registries.ENTITY_TYPE.get(rn)), "entity_tools", "entities");
		}
	}

	public static void addModWithMapping(String modId) {
		modsWithMapping.add(modId);
	}

	private static class ToolMapping<V, C> {
		private final Registry<V> registry;
		private final Function<C, V> getObjectFromContext;
		private final Map<V, Set<Item>> notToolCache = new HashMap<>();

		private final Map<V, Set<Item>> objectTools = new HashMap<>();
		private final Map<V, Set<Predicate<ItemStack>>> objectToolPredicates = new HashMap<>();
		private final Map<Predicate<C>, Set<Item>> objectPredicateTools = new HashMap<>();
		private final Map<Predicate<C>, Set<Predicate<ItemStack>>> objectPredicateToolPredicates = new HashMap<>();

		public ToolMapping(Registry<V> registry, Function<C, V> getObjectFromContext) {
			this.registry = registry;
			this.getObjectFromContext = getObjectFromContext;
		}

		private void addObjectPredicateTools(Pair<Set<Item>, Set<Predicate<ItemStack>>> tools, Predicate<C> predicate) {
			tools.getLeft().forEach(t -> objectPredicateTools.computeIfAbsent(predicate, p -> new HashSet<>()).add(t));
			tools.getRight().forEach(tp -> objectPredicateToolPredicates.computeIfAbsent(predicate, p -> new HashSet<>()).add(tp));
		}

		private void addObjectTools(Pair<Set<Item>, Set<Predicate<ItemStack>>> tools, V object) {
			tools.getLeft().forEach(t -> objectTools.computeIfAbsent(object, b -> new HashSet<>()).add(t));
			tools.getRight().forEach(tp -> objectToolPredicates.computeIfAbsent(object, b -> new HashSet<>()).add(tp));
		}

		public void clear() {
			notToolCache.clear();
			objectTools.clear();
			objectToolPredicates.clear();
			objectPredicateTools.clear();
			objectPredicateToolPredicates.clear();
		}

		public boolean isToolFor(ItemStack stack, V object, Supplier<C> getContext) {
			Item item = stack.getItem();
			if (objectTools.containsKey(object) && objectTools.get(object).contains(item)) {
				return true;
			}
			if (notToolCache.containsKey(object) && notToolCache.get(object).contains(item)) {
				return false;
			}

			if (tryToMatchAgainstObjectToolPredicates(stack, object)) {
				return true;
			}

			C context = getContext.get();
			if (tryToMatchAgainstObjectPredicateTools(item, context)) {
				return true;
			}

			if (tryToMatchAgainstObjectPredicateToolPredicates(stack, context)) {
				return true;
			}

			if (tryToMatchNoMappingMod(stack, object)) {
				return true;
			}

			notToolCache.computeIfAbsent(object, b -> new HashSet<>()).add(item);

			return false;
		}

		private boolean tryToMatchNoMappingMod(ItemStack stack, V object) {
			if (isNoMappingModAndNonStackableItemFromSameMod(stack, object)) {
				addObjectToolMapping(object, stack.getItem());
				return true;
			}
			return false;
		}

		private boolean isNoMappingModAndNonStackableItemFromSameMod(ItemStack stack, V object) {
			return RegistryHelper.getRegistryName((RegistryEntry<?>) object).map(rn ->
					!rn.getNamespace().equals("minecraft")
							&& !modsWithMapping.contains(rn.getNamespace()) && RegistryHelper.getRegistryName((RegistryEntry<?>) stack.getItem()).map(itemRegistryName -> itemRegistryName.getNamespace().equals(rn.getNamespace())).orElse(false)
			).orElse(false) && stack.getMaxCount() == 1;
		}

		private boolean tryToMatchAgainstObjectPredicateToolPredicates(ItemStack stack, C context) {
			for (Map.Entry<Predicate<C>, Set<Predicate<ItemStack>>> entry : objectPredicateToolPredicates.entrySet()) {
				if (entry.getKey().test(context)) {
					Set<Predicate<ItemStack>> toolPredicates = entry.getValue();
					if (tryToMatchTools(stack, getObjectFromContext.apply(context), toolPredicates)) {
						return true;
					}
				}
			}
			return false;
		}

		private boolean tryToMatchAgainstObjectToolPredicates(ItemStack stack, V object) {
			if (objectToolPredicates.containsKey(object)) {
				Set<Predicate<ItemStack>> toolPredicates = objectToolPredicates.get(object);
				return tryToMatchTools(stack, object, toolPredicates);
			}
			return false;
		}

		private boolean tryToMatchAgainstObjectPredicateTools(Item item, C context) {
			for (Map.Entry<Predicate<C>, Set<Item>> entry : objectPredicateTools.entrySet()) {
				if (entry.getKey().test(context) && entry.getValue().contains(item)) {
					addObjectToolMapping(getObjectFromContext.apply(context), item);
					return true;
				}
			}
			return false;
		}

		private boolean tryToMatchTools(ItemStack stack, V object, Set<Predicate<ItemStack>> toolPredicates) {
			for (Predicate<ItemStack> itemPredicate : toolPredicates) {
				if (itemPredicate.test(stack)) {
					objectTools.computeIfAbsent(object, b -> new HashSet<>()).add(stack.getItem());
					return true;
				}
			}
			return false;
		}

		private void addObjectToolMapping(V block, Item item) {
			objectTools.computeIfAbsent(block, b -> new HashSet<>()).add(item);
		}

		public Map<V, Set<Item>> getObjectTools() {
			return objectTools;
		}

		public void addModPredicateTools(String modId, Pair<Set<Item>, Set<Predicate<ItemStack>>> tools) {
			addObjectPredicateTools(tools, new ModMatcher<>(registry, modId, getObjectFromContext));
		}
	}
}
