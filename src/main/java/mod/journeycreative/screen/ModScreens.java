package mod.journeycreative.screen;

import mod.journeycreative.JourneyCreative;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModScreens {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, JourneyCreative.MODID);

    public static final Supplier<MenuType<ResearchVesselScreenHandler>> RESEARCH_VESSEL_SCREEN_HANDLER =
            MENUS.register("research_vessel", () -> IMenuTypeExtension.create(ResearchVesselScreenHandler::new));

    public static final Supplier<MenuType<EnderArchiveScreenHandler>> ENDER_ARCHIVE_SCREEN_HANDLER =
            MENUS.register("ender_archive", () -> IMenuTypeExtension.create(EnderArchiveScreenHandler::new));

    public static void initialize(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
