package gg.amecute.auralithutilities.Polution;

import gg.amecute.auralithutilities.Config;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PollutionSystem extends SavedData
{
    private static final Factory<PollutionSystem> FACTORY = new Factory<>(PollutionSystem::new, PollutionSystem::new);
    private static final String DATA_NAME = "polution_data";
    private final Map<ChunkPos, Float> chunkPollution = new HashMap<>();

    public PollutionSystem(CompoundTag tag, HolderLookup.Provider registries)
    {
        CompoundTag pollutionTag = tag.getCompound("pollution");

        for(String key : pollutionTag.getAllKeys())
        {
            String[] coords = key.split(",");
            int x = Integer.parseInt(coords[0]);
            int z = Integer.parseInt(coords[1]);

            ChunkPos pos = new ChunkPos(x,z);
            this.chunkPollution.put(pos, pollutionTag.getFloat(key));
        }
    }

    public PollutionSystem() {}

    public static boolean isDimEligible(ServerLevel level)
    {
        return Config.POLL_ENABLED_DIM.get().contains(level.dimension().location().toString());
    }

    public static Float shouldPollute(ResourceLocation blockID) {
        String id = blockID.toString();

        for (String patternRaw : Config.POLL_ENABLED_MACHINE.get())
        {
            String[] pattern = patternRaw.split("=");
            String key = pattern[0];
            Float  value  = Float.valueOf(pattern[1]);

            if(key.endsWith("*"))
            {
                String prefix = key.substring(0, key.length() - 1);
                if(id.startsWith(prefix)) return value;
            } else if (key.equals(id)) return value;
        }

        return -1f;
    }

    public void addPollution(ChunkPos pos, float amount)
    {
        chunkPollution.merge(pos, amount, (oldVal, add) -> {
            float sum = oldVal + add;
            return Math.min(sum, Config.POLL_MAX_PER_CHUNK.get());
        });
        setDirty();

        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAA, "+amount);
    }

    public float getPollution(ChunkPos pos)
    {
        return chunkPollution.get(pos);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag, HolderLookup.@NotNull Provider provider)
    {
        CompoundTag pollutionTag = new CompoundTag();
        for (Map.Entry<ChunkPos, Float> entry : chunkPollution.entrySet())
        {
            ChunkPos pos = entry.getKey();
            String key = pos.x + "," + pos.z;
            pollutionTag.putFloat(key, entry.getValue());
        }

        compoundTag.put("pollution", pollutionTag);
        return compoundTag;
    }

    public static PollutionSystem get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(
                FACTORY,
                DATA_NAME
        );
    }
}
