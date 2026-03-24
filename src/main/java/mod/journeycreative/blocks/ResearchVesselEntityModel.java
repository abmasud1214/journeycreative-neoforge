package mod.journeycreative.blocks;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;



public class ResearchVesselEntityModel extends EntityModel<ResearchVesselEntityRenderState> {
    private final ModelPart Bottom;
    private final ModelPart Top;

    public ResearchVesselEntityModel(ModelPart root) {
        super(root);
        this.Bottom = root.getChild("Bottom");
        this.Top = root.getChild("Top");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition Bottom = modelPartData.addOrReplaceChild("Bottom", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -1.0F, -6.0F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-4.0F, -2.0F, 4.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(20, 32).addBox(-4.0F, -2.0F, -6.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition cube_r1 = Bottom.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(28, 26).addBox(-6.0F, -1.0F, -1.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -1.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cube_r2 = Bottom.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 26).addBox(-6.0F, -1.0F, -1.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -1.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition Top = modelPartData.addOrReplaceChild("Top", CubeListBuilder.create().texOffs(0, 13).addBox(-6.0F, 4.0F, -6.0F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 35).addBox(-4.0F, 3.0F, 4.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(20, 35).addBox(-4.0F, 3.0F, -6.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 19.0F, 0.0F, 3.1416F, 0.0F, 0.0F));

        PartDefinition cube_r3 = Top.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(28, 29).addBox(-6.0F, 4.0F, -1.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -1.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cube_r4 = Top.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 29).addBox(-6.0F, 4.0F, -1.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -1.0F, 0.0F, 0.0F, 1.5708F, 0.0F));
        return LayerDefinition.create(modelData, 64, 64);
    }

    public void setOpenProgress(float progress) {
        float pivotY = 24.0F - progress * 11.0F;
        this.Top.y = pivotY;
    }

    @Override
    public void setupAnim(ResearchVesselEntityRenderState researchVesselEntityRenderState) {
        super.setupAnim(researchVesselEntityRenderState);

        float f = (researchVesselEntityRenderState.openProgress * 11.0F) + 5.0F;
        this.Top.setPos(0, f, 0);
    }
}
