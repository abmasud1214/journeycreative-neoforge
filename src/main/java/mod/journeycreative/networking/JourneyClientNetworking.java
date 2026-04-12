package mod.journeycreative.networking;

import ca.weblite.objc.Client;
import mod.journeycreative.JourneyCreative;
import mod.journeycreative.screen.JourneyInventoryScreen;
import mod.journeycreative.screen.ResearchVesselScreenHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class JourneyClientNetworking {
    public static void sendGiveItem(int slot, ItemStack stack) {
        PacketDistributor.sendToServer(new JourneyNetworking.GiveItemPayload(slot, stack.copy()));
    }

    public static void clickJourneyStack(ItemStack stack, int slot) {
        sendGiveItem(slot, stack);
    }

    public static void dropJourneyStack(ItemStack stack, LocalPlayer player) {
        if (!stack.isEmpty()) {
            sendGiveItem(-1, stack);
        }
    }

    public static void sendTrashcanUpdate(ItemStack stack) {
        PacketDistributor.sendToServer(new JourneyNetworking.TrashCanPayload(stack));
    }

    static void ReceiveUnlockedItems(JourneyNetworking.SyncUnlockedItemsPayload payload, IPayloadContext context) {
        PlayerUnlocksData playerUnlocksData = payload.playerUnlocksData();
        context.enqueueWork(() -> {
            PlayerClientUnlocksData.playerUnlocksData = playerUnlocksData;
        });
    }

    static void ReceiveResearchItemRule(JourneyNetworking.SyncResearchItemsUnlockRulePayload payload, IPayloadContext context) {
        boolean value = payload.value();
        context.enqueueWork(() -> {
            ClientGameRule.setResearchItemsUnlocked(value);
        });
    }

    static void ReceiveTrashcanSync(JourneyNetworking.SyncTrashCanPayload payload, IPayloadContext context) {
        var minecraft = Minecraft.getInstance();
        var currentScreen = minecraft.screen;
        if (currentScreen instanceof JourneyInventoryScreen screen) {
            screen.getDeleteItemSlot().set(payload.stack());
        }
    }

    static void ReceiveWarning(JourneyNetworking.ItemWarningMessage payload, IPayloadContext context) {
        AbstractContainerMenu handler = context.player().containerMenu;

        if (!(handler instanceof ResearchVesselScreenHandler rvh)) return;

        rvh.setWarning(payload.warningMessage());
    }

}
