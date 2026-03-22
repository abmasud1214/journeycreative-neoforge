package mod.journeycreative;

import mod.journeycreative.blocks.EnderArchiveEntityRenderer;
import mod.journeycreative.blocks.ModBlocks;
import mod.journeycreative.blocks.ResearchVesselEntityRenderer;
import mod.journeycreative.screen.EnderArchiveScreen;
import mod.journeycreative.screen.EnderArchiveScreenHandler;
import mod.journeycreative.screen.ModScreens;
import mod.journeycreative.screen.ResearchVesselScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = JourneyCreative.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = JourneyCreative.MODID, value = Dist.CLIENT)
public class JourneyCreativeClient {
    public JourneyCreativeClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        JourneyCreative.LOGGER.info("HELLO FROM CLIENT SETUP");
        JourneyCreative.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlocks.RESEARCH_VESSEL_BLOCK_ENTITY.get(),
                ResearchVesselEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlocks.ENDER_ARCHIVE_BLOCK_ENTITY.get(),
                EnderArchiveEntityRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModScreens.RESEARCH_VESSEL_SCREEN_HANDLER.get(), ResearchVesselScreen::new);
        event.register(ModScreens.ENDER_ARCHIVE_SCREEN_HANDLER.get(), EnderArchiveScreen::new);
    }
}
