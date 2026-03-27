package mod.journeycreative.items;

import mod.journeycreative.ResearchConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class ResearchVesselBlockItem extends BlockItem {
    public ResearchVesselBlockItem(Block block, Properties settings) {
        super(block, settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        ItemContainerContents containerComponent = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        if (containerComponent.copyOne().isEmpty()) {
            tooltip.accept(Component.translatable("item.journeycreative.research_vessel.tooltip").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        } else {
            ModComponents.ResearchTarget record = stack.getOrDefault(ModComponents.RESEARCH_VESSEL_TARGET_COMPONENT.get(), ModComponents.ResearchTarget.EMPTY);
            ItemStack target = record.stack();
            int capacity = ResearchConfig.RESEARCH_AMOUNT_REQUIREMENTS.getOrDefault(BuiltInRegistries.ITEM.getKey(target.getItem()),27 * target.getMaxStackSize());
            capacity = Math.min(capacity, 27 * target.getMaxStackSize());
            Stream<ItemStack> containerStacks = containerComponent.nonEmptyItemCopyStream();
            int quantity = containerStacks
                    .mapToInt(ItemStack::getCount)
                    .sum();

            if (quantity < capacity) {
                tooltip.accept(Component.translatable("item.journeycreative.research_vessel.tooltip.status", quantity, capacity, target.getItem().components().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY)).withStyle(ChatFormatting.YELLOW));
            } else {
                tooltip.accept(Component.translatable("item.journeycreative.research_vessel.tooltip.status", quantity, capacity, target.getItem().components().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY)).withStyle(ChatFormatting.GREEN));
            }
        }
    }
}
