package mod.journeycreative.networking;

import com.google.common.eventbus.Subscribe;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.logging.LogUtils;
import mod.journeycreative.JourneyCreative;
import mod.journeycreative.screen.TrashcanInventory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TickThrottler;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import java.util.*;

@EventBusSubscriber(modid = JourneyCreative.MODID)
public class JourneyNetworking {
    public static final ResourceLocation GIVE_ITEM = ResourceLocation.fromNamespaceAndPath(JourneyCreative.MODID, "give_item");
    public static final ResourceLocation UNLOCK_ITEM = ResourceLocation.fromNamespaceAndPath(JourneyCreative.MODID, "unlock_item");
    public static final ResourceLocation TRASH_CAN = ResourceLocation.fromNamespaceAndPath(JourneyCreative.MODID, "trash_can");
    public static final ResourceLocation SYNC_UNLOCKED_ITEMS = ResourceLocation.fromNamespaceAndPath(JourneyCreative.MODID, "sync_unlock_item");
    public static final ResourceLocation SYNC_TRASH_CAN = ResourceLocation.fromNamespaceAndPath(JourneyCreative.MODID, "sync_trash_can");
    public static final ResourceLocation SYNC_RESEARCH_ITEMS_UNLOCKED_RULE = ResourceLocation.fromNamespaceAndPath(JourneyCreative.MODID, "sync_research_rule");
    public static final ResourceLocation ROTATE_ITEMS = ResourceLocation.fromNamespaceAndPath(JourneyCreative.MODID, "rotate_items");
    public static final ResourceLocation SEND_ITEM_WARNING_MESSAGE = ResourceLocation.fromNamespaceAndPath(JourneyCreative.MODID, "send_item_warning_message");

    static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, TickThrottler> playerCreativeItemDropCooldowns = new HashMap<>();

    @SubscribeEvent
    public static void registerServerPackets(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(RotateItemsPayload.ID, RotateItemsPayload.CODEC, JourneyNetworking::rotateItemsPacket);
        registrar.playToServer(GiveItemPayload.ID, GiveItemPayload.CODEC, JourneyNetworking::giveItemPacket);
        registrar.playToServer(UnlockItemPayload.ID, UnlockItemPayload.CODEC, JourneyNetworking::unlockItemPacket);
        registrar.playToServer(TrashCanPayload.ID, TrashCanPayload.CODEC, JourneyNetworking::trashCanPacket);

        registrar.playToClient(JourneyNetworking.SyncUnlockedItemsPayload.ID, JourneyNetworking.SyncUnlockedItemsPayload.CODEC, JourneyClientNetworking::ReceiveUnlockedItems);
        registrar.playToClient(JourneyNetworking.SyncResearchItemsUnlockRulePayload.ID, JourneyNetworking.SyncResearchItemsUnlockRulePayload.CODEC, JourneyClientNetworking::ReceiveResearchItemRule);
        registrar.playToClient(JourneyNetworking.SyncTrashCanPayload.ID, JourneyNetworking.SyncTrashCanPayload.CODEC, JourneyClientNetworking::ReceiveTrashcanSync);
        registrar.playToClient(JourneyNetworking.ItemWarningMessage.ID, JourneyNetworking.ItemWarningMessage.CODEC, JourneyClientNetworking::ReceiveWarning);

    }

    public static void rotateItemsPacket(RotateItemsPayload payload, IPayloadContext context) {
        Player player = context.player();
        Inventory inv = player.getInventory();

        boolean reversed = payload.reversed();

        List<ItemStack> hotbar = new ArrayList<>();
        List<ItemStack> row1 = new ArrayList<>();
        List<ItemStack> row2 = new ArrayList<>();
        List<ItemStack> row3 = new ArrayList<>();

        for (int i = 0; i < 9; i++) hotbar.add(inv.getItem(i));
        for (int i = 9; i < 18; i++) row1.add(inv.getItem(i));
        for (int i = 18; i < 27; i++) row2.add(inv.getItem(i));
        for (int i = 27; i < 36; i++) row3.add(inv.getItem(i));

        // Rotate
        if (!reversed) {
            for (int i = 0; i < 9; i++) {
                inv.setItem(i, row3.get(i));       // hotbar <- row3
                inv.setItem(i + 9, hotbar.get(i)); // row1 <- hotbar
                inv.setItem(i + 18, row1.get(i));  // row2 <- row1
                inv.setItem(i + 27, row2.get(i));  // row3 <- row2
            }
        } else {
            for (int i = 0; i < 9; i++) {
                inv.setItem(i, row1.get(i)); // hotbar <- row1
                inv.setItem(i + 9, row2.get(i)); // row1 <- row2
                inv.setItem(i + 18, row3.get(i)); // row2 <- row3
                inv.setItem(i + 27, hotbar.get(i)); // row3 <- hotbar
            }
        }

        player.containerMenu.broadcastChanges();
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        playerCreativeItemDropCooldowns.values().forEach(TickThrottler::tick);
    }

    private static void giveItemPacket(GiveItemPayload payload, IPayloadContext context){
        var player = context.player();
        int slot = payload.slot();
        var stack = payload.stack();
        boolean bl = slot < 0;
        //TODO: return here if item is not unlocked

        boolean bl2 = slot >= 1 && slot <= 45;
        boolean bl3 = stack.isEmpty() || stack.getCount() <= stack.getMaxStackSize();

        context.enqueueWork(() -> {
            UUID uuid = player.getUUID();
            playerCreativeItemDropCooldowns.putIfAbsent(uuid, new TickThrottler(20, 1480));
            TickThrottler cooldown = playerCreativeItemDropCooldowns.get(uuid);

            if (bl2 && bl3) {
                player.inventoryMenu.getSlot(slot).setByPlayer(stack);
                player.inventoryMenu.setRemoteSlot(slot, stack);
                player.inventoryMenu.broadcastChanges();
            } else if (bl && bl3) {
                if (cooldown.isUnderThreshold()) {
                    cooldown.increment();
                    player.drop(stack, true);
                } else {
                    LOGGER.warn("Player {} was dropping items too fast in journey mode, ignoring.", player.getName().getString());
                }
            }
        });
    }

    private static void unlockItemPacket(UnlockItemPayload payload, IPayloadContext context) {
        var player = context.player();
        var server = player.getServer();
        var item = payload.stack();

        context.enqueueWork(() -> {
            PlayerUnlocksData playerState = StateSaverAndLoader.getPlayerState(player);
            boolean r = playerState.unlockItem(item);

            ServerPlayer playerEntity = server.getPlayerList().getPlayer(player.getUUID());
            server.execute(() -> {
                PacketDistributor.sendToPlayer(playerEntity, new SyncUnlockedItemsPayload(playerState));
            });
        });
    }

    private static void trashCanPacket(TrashCanPayload payload, IPayloadContext context) {
        Player player = context.player();
        MinecraftServer server = player.getServer();
        context.enqueueWork(() -> {
            TrashcanInventory inv = TrashcanServerStorage.get(player);
            ItemStack stack = payload.stack();
            inv.setItem(0, stack);
            ServerPlayer playerEntity = server.getPlayerList().getPlayer(player.getUUID());
            assert playerEntity != null;
            PacketDistributor.sendToPlayer(playerEntity, new SyncTrashCanPayload(stack));
        });
    }

    @SubscribeEvent
    private static void unlockItemCommandEvent(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        var registryAccess = event.getBuildContext();
        dispatcher
                .register(Commands.literal("unlockitem")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("item", ItemArgument.item(registryAccess))
                        .executes(JourneyNetworking::unlockItemCommand)));
    }

    private static int unlockItemCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = ctx.getSource().getPlayer();

        ItemStack unlockStack = ItemArgument.getItem(ctx, "item").createItemStack(1, false);

        StateSaverAndLoader state = StateSaverAndLoader.getServerState(source.getServer());
        PlayerUnlocksData playerState = StateSaverAndLoader.getPlayerState(player);

        if (playerState.unlockItem(unlockStack)) {
            player.displayClientMessage(Component.translatable("item.journeycreative.research_certificate.unlocked", unlockStack.getItem().getName()), true);
        } else {
            player.displayClientMessage(Component.translatable("item.journeycreative.research_certificate.already_unlocked", unlockStack.getItem().getName()), true);
        }

        source.getServer().execute(() -> {
            PacketDistributor.sendToPlayer(player, new SyncUnlockedItemsPayload(playerState));
        });

        return 1;
    }

    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerUnlocksData playerState = StateSaverAndLoader.getPlayerState(player);
            PacketDistributor.sendToPlayer(player, new SyncUnlockedItemsPayload(playerState));
            syncResearchItemsUnlocked(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TrashcanServerStorage.remove(player);
        }
    }

    public static void syncResearchItemsUnlocked(ServerPlayer player) {
        if (player.level() instanceof ServerLevel serverWorld) {
            boolean value = serverWorld.getGameRules().getBoolean(JourneyCreative.RESEARCH_ITEMS_UNLOCKED);
            player.level().getServer().execute(() -> {
                PacketDistributor.sendToPlayer(player, new SyncResearchItemsUnlockRulePayload(value));
            });
        }
    }

    public record GiveItemPayload(int slot, ItemStack stack) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<GiveItemPayload> ID =
                new CustomPacketPayload.Type<>(GIVE_ITEM);

        public static final StreamCodec<RegistryFriendlyByteBuf, GiveItemPayload> CODEC =
                StreamCodec.composite(ByteBufCodecs.INT, GiveItemPayload::slot, ItemStack.OPTIONAL_STREAM_CODEC, GiveItemPayload::stack, GiveItemPayload::new);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record UnlockItemPayload(ItemStack stack) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<UnlockItemPayload> ID =
                new CustomPacketPayload.Type(UNLOCK_ITEM);
        public static final StreamCodec<RegistryFriendlyByteBuf, UnlockItemPayload> CODEC =
                StreamCodec.composite(ItemStack.STREAM_CODEC, UnlockItemPayload::stack, UnlockItemPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record SyncUnlockedItemsPayload(PlayerUnlocksData playerUnlocksData) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncUnlockedItemsPayload> ID =
                new CustomPacketPayload.Type(SYNC_UNLOCKED_ITEMS);
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncUnlockedItemsPayload> CODEC =
                StreamCodec.composite(ByteBufCodecs.fromCodecWithRegistries(PlayerUnlocksData.PLAYER_UNLOCKS_CODEC), SyncUnlockedItemsPayload::playerUnlocksData, SyncUnlockedItemsPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record SyncResearchItemsUnlockRulePayload(boolean value) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncResearchItemsUnlockRulePayload> ID =
                new CustomPacketPayload.Type(SYNC_RESEARCH_ITEMS_UNLOCKED_RULE);
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncResearchItemsUnlockRulePayload> CODEC =
                StreamCodec.composite(ByteBufCodecs.BOOL, SyncResearchItemsUnlockRulePayload::value, SyncResearchItemsUnlockRulePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record RotateItemsPayload(boolean reversed) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RotateItemsPayload> ID =
                new CustomPacketPayload.Type(ROTATE_ITEMS);
        public static final StreamCodec<RegistryFriendlyByteBuf, RotateItemsPayload> CODEC =
                StreamCodec.composite(ByteBufCodecs.BOOL, RotateItemsPayload::reversed, RotateItemsPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record TrashCanPayload(ItemStack stack) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<TrashCanPayload> ID =
                new CustomPacketPayload.Type(TRASH_CAN);
        public static final StreamCodec<RegistryFriendlyByteBuf, TrashCanPayload> CODEC =
                StreamCodec.composite(ItemStack.OPTIONAL_STREAM_CODEC, TrashCanPayload::stack, TrashCanPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record SyncTrashCanPayload(ItemStack stack) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncTrashCanPayload> ID =
                new CustomPacketPayload.Type(SYNC_TRASH_CAN);
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncTrashCanPayload> CODEC =
                StreamCodec.composite(ItemStack.OPTIONAL_STREAM_CODEC, SyncTrashCanPayload::stack, SyncTrashCanPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record ItemWarningMessage(Component warningMessage) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ItemWarningMessage> ID =
                new CustomPacketPayload.Type(SEND_ITEM_WARNING_MESSAGE);

        public static final StreamCodec<RegistryFriendlyByteBuf, ItemWarningMessage> CODEC =
                StreamCodec.composite(ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC, ItemWarningMessage::warningMessage, ItemWarningMessage::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }
}

