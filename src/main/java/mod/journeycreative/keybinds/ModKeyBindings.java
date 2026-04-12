package mod.journeycreative.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import mod.journeycreative.JourneyCreative;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = JourneyCreative.MODID, value = Dist.CLIENT)
public class ModKeyBindings {
    public static final KeyMapping.Category JOURNEY_CREATIVE = KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath(JourneyCreative.MODID, "main"));

    public static Lazy<KeyMapping> ROTATE_INVENTORY = Lazy.of(() -> new KeyMapping(
            "key.journeycreative.rotate_inventory",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            JOURNEY_CREATIVE
    ));

    public static Lazy<KeyMapping> REVERSE_ROTATE_INVENTORY = Lazy.of(() -> new KeyMapping(
            "key.journeycreative.reverse_rotate_inventory",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            JOURNEY_CREATIVE
    ));

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(ROTATE_INVENTORY.get());
        event.register(REVERSE_ROTATE_INVENTORY.get());
    }
}
