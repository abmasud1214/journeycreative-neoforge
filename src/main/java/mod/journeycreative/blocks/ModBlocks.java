package mod.journeycreative.blocks;

import mod.journeycreative.JourneyCreative;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(JourneyCreative.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, JourneyCreative.MODID);

    public static final DeferredBlock<ResearchVesselBlock> RESEARCH_VESSEL_BLOCK = BLOCKS.register(
            "research_vessel",
            registryName -> new ResearchVesselBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST)
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .noOcclusion()
                    .strength(1.0F, 1200.0F)
                    .lightLevel(state -> state.getValue(ResearchVesselBlock.OPENED) ? 10 : 0))
    );

    public static final Supplier<BlockEntityType<ResearchVesselBlockEntity>> RESEARCH_VESSEL_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "research_vessel",
            () -> new BlockEntityType<>(
                    ResearchVesselBlockEntity::new,
                    RESEARCH_VESSEL_BLOCK.get()
                    )
    );

    public static final DeferredBlock<EnderArchiveBlock> ENDER_ARCHIVE_BLOCK = BLOCKS.register(
            "ender_archive",
            registryName -> new EnderArchiveBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHISELED_BOOKSHELF)
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .strength(5.0F, 1200.0F)
                    .lightLevel(state -> 5))
    );

    public static final Supplier<BlockEntityType<EnderArchiveBlockEntity>> ENDER_ARCHIVE_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "ender_archive",
            () -> new BlockEntityType<>(
                    EnderArchiveBlockEntity::new,
                    ENDER_ARCHIVE_BLOCK.get()
            )
    );

    public static void initialize(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
    }

}
