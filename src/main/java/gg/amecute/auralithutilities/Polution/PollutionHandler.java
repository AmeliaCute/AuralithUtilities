package gg.amecute.auralithutilities.Polution;

import gg.amecute.auralithutilities.AuralithUtilities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = AuralithUtilities.MODID)
public class PollutionHandler
{

    @SubscribeEvent
    public static void onMachineEvent(LevelTickEvent.Post event)
    {
        if(!(event.getLevel() instanceof ServerLevel serverLevel) || !PollutionSystem.isDimEligible(serverLevel)) return;

        PollutionSystem data = PollutionSystem.get(serverLevel);
        serverLevel.getChunkSource().chunkMap.getChunks().forEach(chunkHolder -> {
            ChunkPos chunkPos = chunkHolder.getPos();
            var chunk = chunkHolder.getTickingChunk();
            if(chunk == null) return;

            for(BlockEntity blockEntity : chunk.getBlockEntities().values())
            {
                ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockEntity.getBlockState().getBlock());

                boolean isActive = false;
                switch (blockId.getNamespace())
                {
                    case "modern_industrialization":
                        isActive = PollutionDeterminator.isActiveMIMachine(blockEntity);
                        break;


                    default:
                        break;
                }

                if(!isActive) continue;

                Float pollRateData = PollutionSystem.shouldPollute(blockId);
                if(pollRateData == -1f) continue;

                data.addPollution(chunkPos, pollRateData);
            }
        });
    }
}
