package mod.journeycreative.blocks;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class ResearchVesselEntityRenderState extends BlockEntityRenderState {
    public float openProgress;
    public Direction facing;
    public boolean showPortal;

    public ResearchVesselEntityRenderState() {
        this.facing = Direction.DOWN;
    }
}
