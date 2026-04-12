package mod.journeycreative.keybinds;

import mod.journeycreative.JourneyCreative;
import mod.journeycreative.networking.JourneyNetworking;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = JourneyCreative.MODID, value = Dist.CLIENT)
public class KeyInputHandler {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (ModKeyBindings.ROTATE_INVENTORY.get().consumeClick()) {
            ClientPacketDistributor.sendToServer(new JourneyNetworking.RotateItemsPayload(false));
        }
        while (ModKeyBindings.REVERSE_ROTATE_INVENTORY.get().consumeClick()) {
            ClientPacketDistributor.sendToServer(new JourneyNetworking.RotateItemsPayload(true));
        }
    }
}
