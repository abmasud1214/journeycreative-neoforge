package mod.journeycreative.networking;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientGameRule {
    private static boolean researchItemsUnlocked = false;

    public static void setResearchItemsUnlocked(boolean value) {
        researchItemsUnlocked = value;
    }

    public static boolean isResearchItemsUnlocked() {
        return researchItemsUnlocked;
    }
}
