package mod.journeycreative.items;

import mod.journeycreative.ResearchConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.Consumer;

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
            int quantity = 0;
            Iterable<ItemStack> containerStacks = containerComponent.nonEmptyItems();
            for (ItemStack s : containerStacks) {
                quantity += s.getCount();
            }

            if (quantity < capacity) {
                tooltip.accept(Component.translatable("item.journeycreative.research_vessel.tooltip.status", quantity, capacity, target.getItem().getName()).withStyle(ChatFormatting.YELLOW));
            } else {
                tooltip.accept(Component.translatable("item.journeycreative.research_vessel.tooltip.status", quantity, capacity, target.getItem().getName()).withStyle(ChatFormatting.GREEN));
            }
        }
    }
}
