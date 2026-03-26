package mod.journeycreative;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import mod.journeycreative.blocks.ModBlocks;
import mod.journeycreative.items.ModComponents;
import mod.journeycreative.items.ModItems;
import mod.journeycreative.networking.JourneyNetworking;
import mod.journeycreative.screen.ModScreens;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.*;
import net.neoforged.neoforge.event.level.GameRuleChangedEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(JourneyCreative.MODID)
public class JourneyCreative {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "journeycreative";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

//    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "journeycreative" namespace
//    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
//
//    // Creates a creative tab with the id "journeycreative:example_tab" for the example item, that is placed after the combat tab
//    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
//            .title(Component.translatable("itemGroup.journeycreative")) //The language key for the title of your CreativeModeTab
//            .withTabsBefore(CreativeModeTabs.COMBAT)
//            .icon(() -> ModItems.RESEARCH_CERTIFICATE.get().getDefaultInstance())
//            .displayItems((parameters, output) -> {}).build());


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public JourneyCreative(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        ModBlocks.initialize(modEventBus);
        ModItems.initialize(modEventBus);
        ModScreens.initialize(modEventBus);
        ModComponents.initialize(modEventBus);
        ModGameRules.initialize(modEventBus);

        // Register the Deferred Register to the mod event bus so tabs get registered
//        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (JourneyCreative) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModItems.RESEARCH_VESSEL_BLOCK_ITEM.get());
            event.accept(ModItems.ENDER_ARCHIVE_BLOCK_ITEM.get());
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onGameRuleChange(GameRuleChangedEvent event) {
        MinecraftServer server = event.getServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            JourneyNetworking.syncResearchItemsUnlocked(player);
        }
    }
}
