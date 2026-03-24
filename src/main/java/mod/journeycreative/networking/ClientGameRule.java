package mod.journeycreative.networking;




public class ClientGameRule {
    private static boolean researchItemsUnlocked = false;

    public static void setResearchItemsUnlocked(boolean value) {
        researchItemsUnlocked = value;
    }

    public static boolean isResearchItemsUnlocked() {
        return researchItemsUnlocked;
    }
}
