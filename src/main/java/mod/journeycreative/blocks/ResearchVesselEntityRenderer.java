package mod.journeycreative.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.journeycreative.JourneyCreative;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Matrix4f;

import java.util.Objects;

public class ResearchVesselEntityRenderer implements BlockEntityRenderer<ResearchVesselBlockEntity> {
    private final ResearchVesselEntityModel model;
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(JourneyCreative.MODID, "textures/block/research_vessel.png");

    public ResearchVesselEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this(ctx.getModelSet());
    }

    public ResearchVesselEntityRenderer(EntityModelSet loader) {
        this.model = new ResearchVesselEntityModel(loader.bakeLayer(ModModelLayers.RESEARCH_VESSEL));
    }

    @Override
    public void render(ResearchVesselBlockEntity researchVesselBlockEntity, float f, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, int j, Vec3 vec3) {
        Direction direction = Direction.UP;

        float g = researchVesselBlockEntity.getAnimationProgress(f);
        boolean portal = researchVesselBlockEntity.getAnimationStage() == ResearchVesselBlockEntity.AnimationStage.OPENED;
        this.render(matrixStack, vertexConsumerProvider, i, j, direction, g, portal);
    }

    public void render(PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, Direction facing, float openness, boolean show_portal) {
        matrices.pushPose();
        this.setTransforms(matrices, facing, openness);
        ResearchVesselEntityModel blockModel = this.model;
        Objects.requireNonNull(blockModel);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.model.renderToBuffer(matrices, vertexConsumer, light, overlay);
        matrices.popPose();
        Matrix4f matrix4f = matrices.last().pose();
        this.renderSides(show_portal, matrix4f, vertexConsumers.getBuffer(RenderType.endPortal()));
    }

    private void renderSides(boolean show_portal, Matrix4f matrix, VertexConsumer vertexConsumer) {
        this.renderSide(show_portal, matrix, vertexConsumer, No16(4), No16(12), No16(2), No16(14), No16(12), No16(12), Direction.SOUTH);
        this.renderSide(show_portal, matrix, vertexConsumer, No16(4), No16(12), No16(2), No16(14), No16(4), No16(4), Direction.NORTH);
        this.renderSide(show_portal, matrix, vertexConsumer, No16(12), No16(12), No16(2), No16(14), No16(4), No16(12), Direction.EAST);
        this.renderSide(show_portal, matrix, vertexConsumer, No16(4), No16(4), No16(2), No16(14), No16(4), No16(12), Direction.WEST);
    }

    private float No16(float f) {
        return f / 16.0F;
    }

    private void renderSide(boolean show_portal, Matrix4f model, VertexConsumer vertices, float x1, float x2, float y1, float y2, float z1, float z2, Direction side) {
        if (show_portal) {
            vertices.addVertex(model, x1, y1, z1);
            vertices.addVertex(model, x2, y1, z2);
            vertices.addVertex(model, x2, y2, z2);
            vertices.addVertex(model, x1, y2, z1);

            vertices.addVertex(model, x1, y2, z1);
            vertices.addVertex(model, x2, y2, z2);
            vertices.addVertex(model, x2, y1, z2);
            vertices.addVertex(model, x1, y1, z1);
        }
    }

    private void setTransforms(PoseStack matrices, Direction facing, float openness) {
        matrices.translate(0.5F, 0.5F, 0.5F);
        float f = 0.9995F;
        matrices.scale(0.9995F, 0.9995F, 0.9995F);
        matrices.scale(1.0F, -1.0F, -1.0F);
        matrices.translate(0.0F, -1.0F, 0.0F);
        this.model.setOpenProgress(openness);
    }
}
