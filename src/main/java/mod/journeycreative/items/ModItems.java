package mod.journeycreative.items;

import mod.journeycreative.JourneyCreative;
import mod.journeycreative.blocks.ModBlocks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(JourneyCreative.MODID);

    public static final Supplier<BlockItem> RESEARCH_VESSEL_BLOCK_ITEM = ITEMS.register(
            "research_vessel",
            () -> new ResearchVesselBlockItem(ModBlocks.RESEARCH_VESSEL_BLOCK.get(),
                    new Item.Properties()
                            .stacksTo(1)
                            .component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
                            .component(ModComponents.RESEARCH_VESSEL_TARGET_COMPONENT.get(), ModComponents.ResearchTarget.EMPTY)
            )
    );

    public static final Supplier<BlockItem> ENDER_ARCHIVE_BLOCK_ITEM = ITEMS.register(
            "ender_archive",
            () -> new EnderArchiveBlockItem(ModBlocks.ENDER_ARCHIVE_BLOCK.get(), new Item.Properties())
    );

    public static final Supplier<Item> RESEARCH_CERTIFICATE = ITEMS.registerItem(
            "research_certificate",
            ResearchCertificateItem::new,
            new Item.Properties()
                    .stacksTo(1)
    );

    public static void initialize(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
