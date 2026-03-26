package mod.journeycreative.items;

import mod.journeycreative.JourneyCreative;
import mod.journeycreative.blocks.ModBlocks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;


public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(JourneyCreative.MODID);

    public static final DeferredItem<BlockItem> RESEARCH_VESSEL_BLOCK_ITEM = ITEMS.register(
            "research_vessel",
            (id) -> new ResearchVesselBlockItem(ModBlocks.RESEARCH_VESSEL_BLOCK.get(),
                    new Item.Properties()
                            .setId(ResourceKey.create(Registries.ITEM, id))
                            .stacksTo(1)
                            .component(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT
                                    .withHidden(DataComponents.CONTAINER, true)))
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
            props -> props.stacksTo(1)
    );

    public static void initialize(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
