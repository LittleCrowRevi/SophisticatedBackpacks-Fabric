package net.p3pp3rf1y.sophisticatedbackpacks.core;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.BlockView;

import java.util.Optional;

public class WorldHelper {
    private WorldHelper() {}

    public static Optional<BlockEntity> getBlockEntity(@Nullable BlockView level, BlockPos pos) {
        return getBlockEntity(level, pos, BlockEntity.class);
    }

    public static Optional<BlockEntity> getLoadedBlockEntity(@Nullable World level, BlockPos pos) {
        if (level != null && level.isChunkLoaded(pos)) {
            return Optional.ofNullable(level.getBlockEntity(pos));
        }
        return Optional.empty();
    }

    public static <T> Optional<T> getLoadedBlockEntity(@Nullable World level, BlockPos pos, Class<T> teClass) {
        if (level != null && level.isChunkLoaded(pos)) {
            return getBlockEntity(level, pos, teClass);
        }
        return Optional.empty();
    }

    public static <T> Optional<T> getBlockEntity(@Nullable BlockView level, BlockPos pos, Class<T> teClass) {
        if (level == null) {
            return Optional.empty();
        }

        BlockEntity te = level.getBlockEntity(pos);

        if (teClass.isInstance(te)) {
            return Optional.of(teClass.cast(te));
        }

        return Optional.empty();
    }

    public static void notifyBlockUpdate(BlockEntity tile) {
        World world = tile.getWorld();
        if (world == null) {
            return;
        }
        world.updateListeners(tile.getPos(), tile.getCachedState(), tile.getCachedState(), 3);
    }
}
