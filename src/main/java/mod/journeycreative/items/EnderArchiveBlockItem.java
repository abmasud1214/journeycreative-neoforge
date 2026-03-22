package mod.journeycreative.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class EnderArchiveBlockItem extends BlockItem {
    public EnderArchiveBlockItem(Block block, Properties settings) {
        super(block, settings);
    }

    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.translatable("item.journeycreative.ender_archive.tooptip",
                BuiltInRegistries.ITEM.get(ResourceLocation.parse("journeycreative:research_vessel")).getDescription(),
                BuiltInRegistries.ITEM.get(ResourceLocation.parse("journeycreative:research_certificate")).getDescription()
        ).withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
    }
}
