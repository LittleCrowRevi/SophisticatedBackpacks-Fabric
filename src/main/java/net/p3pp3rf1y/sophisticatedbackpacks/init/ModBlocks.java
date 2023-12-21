package net.p3pp3rf1y.sophisticatedbackpacks.init;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;

import static net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks.MOD_ID;

public class ModBlocks {
	//private static final DeferredRegister<Block> BLOCKS = Regist.create(ForgeRegistries.BLOCKS, SophisticatedBackpacks.MOD_ID);
	//private static final FabricRegistryBuilder BLOCK_ENTITY_TYPES = FabricRegistryBuilder.createSimple(Registries.BLOCK_ENTITY_TYPE);

	private ModBlocks() {}

	public static final Block BACKPACK = Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "backpack"), BackpackBlock::new);
	public static final Block IRON_BACKPACK = Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "iron_backpack", BackpackBlock::new));
	public static final Block GOLD_BACKPACK = Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "gold_backpack", BackpackBlock::new));
	public static final Block DIAMOND_BACKPACK = Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "diamond_backpack", BackpackBlock::new));
	
	public static final BlockEntityType<BackpackBlockEntity> BACKPACK_ENTITY_TYPE = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			new Identifier(MOD_ID, "backpack_entity"),
			FabricBlockEntityTypeBuilder.create(BackpackBlockEntity::new, BACKPACK, IRON_BACKPACK, GOLD_BACKPACK, DIAMOND_BACKPACK).build());
	//public static final Registry<BackpackBlock> NETHERITE_BACKPACK = Registry.register("netherite_backpack", () -> new BackpackBlock(1200));

	@SuppressWarnings("ConstantConditions") //no datafixer type needed

	public static void registerHandlers() {
		//BLOCK_ENTITY_TYPES.register(modBus);
		//MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, BackpackBlock::playerInteract);
	}
}
