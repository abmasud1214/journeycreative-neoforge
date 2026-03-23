package mod.journeycreative.blocks;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ResearchVesselEntityRenderState extends EntityRenderState {
    public float openProgress;
    public Direction facing;

    public ResearchVesselEntityRenderState() {
        this.facing = Direction.DOWN;
    }
}
