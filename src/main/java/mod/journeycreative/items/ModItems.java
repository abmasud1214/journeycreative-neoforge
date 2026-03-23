package mod.journeycreative.items;

import mod.journeycreative.JourneyCreative;
import mod.journeycreative.blocks.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(JourneyCreative.MODID);

    public static final DeferredItem<BlockItem> RESEARCH_VESSEL_BLOCK_ITEM = ITEMS.register(
            "research_vessel",
            (id) -> new ResearchVesselBlockItem(ModBlocks.RESEARCH_VESSEL_BLOCK.get(),
                    new Item.Properties()
                            .setId(ResourceKey.create(Registries.ITEM, id))
                            .stacksTo(1))
    );

    public static final DeferredItem<BlockItem> ENDER_ARCHIVE_BLOCK_ITEM = ITEMS.register(
            "ender_archive",
            (id) -> new EnderArchiveBlockItem(ModBlocks.ENDER_ARCHIVE_BLOCK.get(),
                    new Item.Properties()
                            .setId(ResourceKey.create(Registries.ITEM, id)))
    );

    public static final DeferredItem<Item> RESEARCH_CERTIFICATE = ITEMS.registerItem(
            "research_certificate",
            ResearchCertificateItem::new,
            new Item.Properties()
                    .stacksTo(1)
    );

    public static void initialize(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
