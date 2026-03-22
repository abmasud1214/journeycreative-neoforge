package mod.journeycreative.items;

import mod.journeycreative.JourneyCreative;
import mod.journeycreative.ResearchConfig;
import mod.journeycreative.networking.JourneyNetworking;
import mod.journeycreative.networking.PlayerUnlocksData;
import mod.journeycreative.networking.StateSaverAndLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ResearchCertificateItem extends Item {
    private static final ItemStack barrier = new ItemStack(Items.BARRIER);

    public ResearchCertificateItem(Properties settings) {
        super(settings);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.set(ModComponents.RESEARCH_ITEM_COMPONENT.get(),
                new ModComponents.ResearchTarget(new ItemStack(Items.BARRIER)));
        return stack;
    }

    private static Component getItemName(ItemStack stack) {
        Item item = stack.getItem();

        if (item == null) {
            return Component.literal("Unknown Item");
        }

        return item.getDescription();
    }

    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        boolean exists = stack.has(ModComponents.RESEARCH_ITEM_COMPONENT);
        if (exists) {
            ItemStack research_item = stack.get(ModComponents.RESEARCH_ITEM_COMPONENT).stack();
            tooltip.add(Component.translatable("item.journeycreative.research_certificate.research_item", getItemName(research_item)).withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);

        if (user.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(itemStack);
        }

        user.startUsingItem(hand);
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 10;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClientSide && user instanceof Player player) {
            int heldTicks = this.getUseDuration(stack, user) - remainingUseTicks;
            player.displayClientMessage(Component.literal("[" + "+".repeat(heldTicks) + "-".repeat(remainingUseTicks) + "]"), true);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        boolean exists = stack.has(ModComponents.RESEARCH_ITEM_COMPONENT);

        if (world instanceof ServerLevel serverWorld && user instanceof Player player && exists) {
            PlayerUnlocksData playerState = StateSaverAndLoader.getPlayerState(player);
            ItemStack research_target = stack.get(ModComponents.RESEARCH_ITEM_COMPONENT).stack();

            Set<ResourceLocation> prohibited = ResearchConfig.RESEARCH_PROHIBITED;
            if (prohibited.contains(BuiltInRegistries.ITEM.getKey(research_target.getItem()))) {
                player.displayClientMessage(Component.translatable("item.journeycreative.research_certificate.cannot_unlock", getItemName(research_target)), true);
                return stack;
            }

            List<ResourceLocation> prerequisites = ResearchConfig.RESEARCH_PREREQUISITES.getOrDefault(
                    BuiltInRegistries.ITEM.getKey(research_target.getItem()), new ArrayList<ResourceLocation>()
            );
            ArrayList<Component> prereqs = new ArrayList<>();
            if (!prerequisites.isEmpty()) {
                for (ResourceLocation id : prerequisites) {
                    ItemStack prereqStack = new ItemStack(BuiltInRegistries.ITEM.get(id), 1);
                    if (!playerState.isUnlocked(prereqStack, serverWorld.getGameRules().getBoolean(JourneyCreative.RESEARCH_ITEMS_UNLOCKED))) {
                        prereqs.add(getItemName(prereqStack));
                    }
                }
            }
            if (!prereqs.isEmpty()) {
                MutableComponent prereqText = Component.empty();
                prereqText.append(Component.literal("["));
                prereqText.append(ComponentUtils.formatList(prereqs, Component.literal(", ")));
                prereqText.append(Component.literal("]"));
                player.displayClientMessage(Component.translatable("item.journeycreative.research_certificate.need_prerequisite", prereqText, getItemName(research_target)), false);
                return stack;
            }

            boolean unlocked = playerState.unlockItem(research_target);
            ServerPlayer playerEntity = world.getServer().getPlayerList().getPlayer(player.getUUID());
            PacketDistributor.sendToPlayer(playerEntity, new JourneyNetworking.SyncUnlockedItemsPayload(playerState));

            if (unlocked) {
                player.displayClientMessage(Component.translatable("item.journeycreative.research_certificate.unlocked", getItemName(research_target)), true);
                stack.shrink(1);
                player.getCooldowns().addCooldown(this, 10);
            } else {
                player.displayClientMessage(Component.translatable("item.journeycreative.research_certificate.already_unlocked", getItemName(research_target)), true);
                player.getCooldowns().addCooldown(this, 10);
            }
        }
        return stack;
    }

}
