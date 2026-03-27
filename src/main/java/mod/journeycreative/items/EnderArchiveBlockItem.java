package mod.journeycreative.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.Consumer;

public class EnderArchiveBlockItem extends BlockItem {
    public EnderArchiveBlockItem(Block block, Properties settings) {
        super(block, settings);
    }

    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag type) {
        tooltip.accept(Component.translatable("item.journeycreative.ender_archive.tooptip",
                BuiltInRegistries.ITEM.getValue(Identifier.parse("journeycreative:research_vessel")).components().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY),
                BuiltInRegistries.ITEM.getValue(Identifier.parse("journeycreative:research_certificate")).components().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY)
        ).withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
    }
}
