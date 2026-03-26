package mod.journeycreative.blocks;

import mod.journeycreative.JourneyCreative;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = JourneyCreative.MODID, value = Dist.CLIENT)
public class ModModelLayers {
    public static final ModelLayerLocation RESEARCH_VESSEL =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(JourneyCreative.MODID, "research_vessel"), "main");

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.RESEARCH_VESSEL, ResearchVesselEntityModel::getTexturedModelData);
    }
}
