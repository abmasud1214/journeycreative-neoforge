package mod.journeycreative.items;

import mod.journeycreative.JourneyCreative;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, JourneyCreative.MODID);

    public static final Supplier<DataComponentType<ResearchTarget>> RESEARCH_VESSEL_TARGET_COMPONENT =
            COMPONENTS.register("research_vessel_target_component",
                    () -> DataComponentType.<ResearchTarget>builder()
                            .persistent(ItemStack.OPTIONAL_CODEC.xmap(ResearchTarget::new, ResearchTarget::stack))
                            .networkSynchronized(ItemStack.OPTIONAL_STREAM_CODEC.map(ResearchTarget::new, ResearchTarget::stack))
                            .build());

    public static final Supplier<DataComponentType<ResearchTarget>> RESEARCH_ITEM_COMPONENT =
            COMPONENTS.register("research_item_component",
                    () -> DataComponentType.<ResearchTarget>builder()
                            .persistent(ItemStack.OPTIONAL_CODEC.xmap(ResearchTarget::new, ResearchTarget::stack))
                            .networkSynchronized(ItemStack.OPTIONAL_STREAM_CODEC.map(ResearchTarget::new, ResearchTarget::stack))
                            .build());

    public static void initialize(IEventBus modEventBus) {
        COMPONENTS.register(modEventBus);
    }

    public record ResearchTarget(ItemStack stack) {
        public static final ResearchTarget EMPTY = new ResearchTarget(ItemStack.EMPTY);

        public ResearchTarget {
            stack = stack.copy();
        }
    }
}
