package mod.journeycreative.screen;

import mod.journeycreative.JourneyCreative;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import java.util.Optional;

public class ResearchVesselScreen extends AbstractContainerScreen<ResearchVesselScreenHandler> {

    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/shulker_box.png");
    private static final ResourceLocation INVALID_RESEARCH_TEXTURE = ResourceLocation.fromNamespaceAndPath(JourneyCreative.MODID, "textures/gui/sprites/invalid_research.png");
    private static final ResourceLocation WARNING_RESEARCH_TEXTURE = ResourceLocation.fromNamespaceAndPath(JourneyCreative.MODID, "textures/gui/sprites/prereq_research.png");
    private static final Component CANNOT_RESEARCH_TOOLTIP = Component.translatable("container.ender_archive.cannot_research_tooltip");
    private static final Component RESEARCH_BLOCKED_TOOLTIP = Component.translatable("container.ender_archive.research_blocked_tooltip");
    private Component warning;

    public ResearchVesselScreen(ResearchVesselScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics context, float deltaTicks, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        context.blit(RenderType::guiTextured, TEXTURE, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        warning = menu.getWarning();
        this.renderTooltip(context, mouseX, mouseY);
        this.renderItemProgress(context);
        this.renderInvalid(context);
        this.renderSlotTooltip(context, mouseX, mouseY);
    }

    private void renderItemProgress(GuiGraphics context) {
        Optional<Component> optional = Optional.empty();
        ResearchVesselScreenHandler handler = this.getMenu();
        EnderArchiveScreenHandler.researchInvalidReason reason = handler.getReason();
        boolean bl = (reason == EnderArchiveScreenHandler.researchInvalidReason.BLOCKED || reason == EnderArchiveScreenHandler.researchInvalidReason.PROHIBITED);
        boolean bl2 = warning != null && !warning.getString().isEmpty();
        if (!(handler.getInventoryCapacity() == 0) && !bl) {
            int quantity = handler.getInventoryQuantity();
            int capacity = handler.getInventoryCapacity();
            optional = Optional.of(Component.literal(String.format("%d/%d", quantity, capacity)));
        }
        optional.ifPresent((text) -> {
            int width = this.font.width(text);
            int x = this.leftPos + 168 - width;
            if (bl2) {
                x -= 7;
            }

            context.drawString(this.font, text, x, this.topPos + 6, -12566464, false);
        });
    }
    private void renderInvalid(GuiGraphics context) {

        ResearchVesselScreenHandler handler = this.getMenu();
        EnderArchiveScreenHandler.researchInvalidReason reason = handler.getReason();
        boolean bl = (reason == EnderArchiveScreenHandler.researchInvalidReason.BLOCKED || reason == EnderArchiveScreenHandler.researchInvalidReason.PROHIBITED);
        boolean bl2 = warning != null && !warning.getString().isEmpty();
        if (!(handler.getInventoryCapacity() == 0) && (bl || bl2)) {
            if (bl) {
                context.blit(RenderType::guiTextured, INVALID_RESEARCH_TEXTURE,
                        this.leftPos + 157, this.topPos + 5, 0, 0,
                        11, 11, 11, 11);
            } else {
                context.blit(RenderType::guiTextured, WARNING_RESEARCH_TEXTURE,
                        this.leftPos + 161, this.topPos + 5, 0, 0,
                        11, 11, 11, 11);
            }
        }
    }

    private void renderSlotTooltip(GuiGraphics context, int mouseX, int mouseY) {
        Optional<Component> optional = Optional.empty();
        ResearchVesselScreenHandler handler = this.getMenu();
        EnderArchiveScreenHandler.researchInvalidReason reason = handler.getReason();
        boolean bl = (reason == EnderArchiveScreenHandler.researchInvalidReason.BLOCKED || reason == EnderArchiveScreenHandler.researchInvalidReason.PROHIBITED);
        boolean bl2 = warning != null && !warning.getString().isEmpty();
        int x = bl ? 157 : 161;
        if (!(handler.getInventoryCapacity() == 0) && (bl || bl2) && this.isHovering(x, 5, 11, 11, (double) mouseX, (double) mouseY)) {
            if (bl) {
                if (reason == EnderArchiveScreenHandler.researchInvalidReason.PROHIBITED) {
                    optional = Optional.of(CANNOT_RESEARCH_TOOLTIP);
                } else {
                    optional = Optional.of(RESEARCH_BLOCKED_TOOLTIP);
                }
            } else {
                optional = Optional.of(handler.getWarning());
            }
        }

        optional.ifPresent((text) -> {
            context.renderTooltip(this.font, this.font.split(text, 115), mouseX, mouseY);
        });
    }

}
