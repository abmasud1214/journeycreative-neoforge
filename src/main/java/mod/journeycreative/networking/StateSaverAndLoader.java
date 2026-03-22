package mod.journeycreative.networking;

import mod.journeycreative.JourneyCreative;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import java.util.HashMap;
import java.util.UUID;

public class StateSaverAndLoader extends SavedData {
    public HashMap<UUID, PlayerUnlocksData> players = new HashMap<>();

    private StateSaverAndLoader() {
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries) {
        CompoundTag playersNbt = new CompoundTag();

        players.forEach(((uuid, playerUnlocksData) -> {
            playersNbt.put(uuid.toString(), playerUnlocksData.toNbt(registries));
        }));

        nbt.put("players", playersNbt);
        return nbt;
    }

    public static StateSaverAndLoader createFromNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        CompoundTag playersNbt = nbt.getCompound("players");

        for (String key : playersNbt.getAllKeys()) {
            UUID uuid = UUID.fromString(key);
            PlayerUnlocksData data = PlayerUnlocksData.fromNbt(playersNbt.getCompound(key), registries);
            state.players.put(uuid, data);
        }
        return state;
    }

    public static PlayerUnlocksData getPlayerState(LivingEntity player) {
        StateSaverAndLoader serverState = getServerState(player.level().getServer());

        PlayerUnlocksData playerState = serverState.players.computeIfAbsent(player.getUUID(), uuid -> new PlayerUnlocksData());

        return playerState;
    }

    private static Factory<StateSaverAndLoader> type = new Factory<>(
            StateSaverAndLoader::new,
            StateSaverAndLoader::createFromNbt,
            null
    );

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        DimensionDataStorage persistentStateManager = server.getLevel(Level.OVERWORLD).getDataStorage();

        StateSaverAndLoader state = persistentStateManager.computeIfAbsent(type, JourneyCreative.MODID);

        state.setDirty();

        return state;
    }
}
