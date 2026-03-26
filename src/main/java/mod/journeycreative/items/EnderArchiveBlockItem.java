package mod.journeycreative.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
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
                BuiltInRegistries.ITEM.getValue(Identifier.parse("journeycreative:research_vessel")).getName(),
                BuiltInRegistries.ITEM.getValue(Identifier.parse("journeycreative:research_certificate")).getName()
        ).withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
    }
}
