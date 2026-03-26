package mod.journeycreative;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import mod.journeycreative.networking.JourneyNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterGameRuleCategoryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModGameRules {
    public static final DeferredRegister<GameRule<?>> GAME_RULES = DeferredRegister.create(
            BuiltInRegistries.GAME_RULE,
            JourneyCreative.MODID
    );

    public static final GameRuleCategory GAME_RULE_CATEGORY = new GameRuleCategory(Identifier.fromNamespaceAndPath(JourneyCreative.MODID, "category"));

    @SubscribeEvent
    public static void registerGameRuleCategory(RegisterGameRuleCategoryEvent event) {
        event.register(GAME_RULE_CATEGORY);
    }

    public static final Supplier<GameRule<Boolean>> RESEARCH_ITEMS_UNLOCKED = GAME_RULES.register(
            "research_items_unlocked",
            () -> new GameRule<>(
                    GAME_RULE_CATEGORY,
                    GameRuleType.BOOL,
                    BoolArgumentType.bool(),
                    GameRuleTypeVisitor::visitBoolean,
                    Codec.BOOL,
                    p -> p ? 1 : 0,
                    false,
                    FeatureFlagSet.of()
            )
    );

    public static void initialize(IEventBus modEventBus) {
        GAME_RULES.register(modEventBus);
    }
}
