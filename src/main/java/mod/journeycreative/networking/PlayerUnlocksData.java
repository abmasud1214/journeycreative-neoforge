package mod.journeycreative.networking;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import mod.journeycreative.JourneyCreative;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerUnlocksData {
    private ImmutableSet<ItemStack> unlockedItemKeys;

    public PlayerUnlocksData() {
        unlockedItemKeys = ImmutableSet.of();
    }

    public PlayerUnlocksData(ImmutableSet<ItemStack> unlockedItemKeys) {
        this.unlockedItemKeys = unlockedItemKeys;
    }

    public ImmutableSet<ItemStack> getUnlockedItemKeys() {
        return unlockedItemKeys;
    }

    public void setUnlockedItemKeys(ImmutableSet<ItemStack> itemKeys) {
        unlockedItemKeys = itemKeys;
    }

    public boolean unlockItem(ItemStack item) {
        if (isUnlocked(item, false)) { // We don't check the gamerule so that the item can be unlocked regardless
            return false;
        } else {
            ItemStack normalized = normalizeForUnlocks(item);
            this.unlockedItemKeys = ImmutableSet.<ItemStack>builder().addAll(this.unlockedItemKeys).add(normalized).build();
            return true;
        }
    }

    public boolean isUnlocked(ItemStack item, boolean researchItems) {
        ItemStack normalized = normalizeForUnlocks(item);
        AtomicBoolean equal = new AtomicBoolean(false);

        unlockedItemKeys.stream().iterator().forEachRemaining(stack -> {
            if (ItemStack.isSameItemSameComponents(stack, normalized)) equal.set(true);
        });

        if (researchItems) {
            ItemStack researchVessel = normalizeForUnlocks(new ItemStack(BuiltInRegistries.ITEM.getValue(ResourceLocation.fromNamespaceAndPath(JourneyCreative.MODID, "research_vessel")), 1));
            ItemStack enderArchive = normalizeForUnlocks(new ItemStack(BuiltInRegistries.ITEM.getValue(ResourceLocation.fromNamespaceAndPath(JourneyCreative.MODID, "ender_archive")), 1));
            if (ItemStack.isSameItemSameComponents(researchVessel, normalized)) equal.set(true);
            if (ItemStack.isSameItemSameComponents(enderArchive, normalized)) equal.set(true);
        }

        return equal.get();
    }

    private static ItemStack normalizeForUnlocks(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;

        ItemStack copy = stack.copy();
        copy.setCount(1);

        Set<DataComponentType<?>> keepComponents = Set.of(
                DataComponents.POTION_CONTENTS,
                DataComponents.STORED_ENCHANTMENTS,
                DataComponents.INSTRUMENT,
                DataComponents.FIREWORKS,
                DataComponents.SUSPICIOUS_STEW_EFFECTS,
                DataComponents.OMINOUS_BOTTLE_AMPLIFIER
        );

        copy.getComponents().forEach(component -> {
            DataComponentType<?> componentType = component.type();
            if (!keepComponents.contains(componentType)) {
                copy.remove(componentType);
            }
        });

        return copy;
    }

    public static final Codec<PlayerUnlocksData> PLAYER_UNLOCKS_CODEC = ItemStack.CODEC
            .listOf()
            .xmap(
                    ImmutableSet::copyOf,
                    ImmutableList::copyOf
            )
            .fieldOf("unlockedItems")
            .codec()
            .xmap(
                    PlayerUnlocksData::new,
                    PlayerUnlocksData::getUnlockedItemKeys
            );

    public CompoundTag toNbt(HolderLookup.Provider registries) {
        CompoundTag nbt = new CompoundTag();
        ListTag list = new ListTag();
        for (ItemStack stack : unlockedItemKeys) {
            list.add(stack.save(registries));
        }

        nbt.put("unlockedItems", list);
        return nbt;
    }

    public static PlayerUnlocksData fromNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        ImmutableSet.Builder<ItemStack> builder = ImmutableSet.builder();
        ListTag list = nbt.getList("unlockedItems", 10);

        for (int i = 0; i < list.size(); i++) {
            ItemStack.parse(registries, list.getCompound(i)).ifPresent(builder::add);
        }

        return new PlayerUnlocksData(builder.build());
    }
}
