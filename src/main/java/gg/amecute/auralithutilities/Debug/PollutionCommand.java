package gg.amecute.auralithutilities.Debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import gg.amecute.auralithutilities.Config;
import gg.amecute.auralithutilities.Polution.PollutionSystem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public class PollutionCommand
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(
                Commands.literal("aurp")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("check").executes(PollutionCommand::checkCurrentChunk))
        );
    }

    private static int checkCurrentChunk(CommandContext<CommandSourceStack> ctx)
    {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level =  source.getLevel();
        ChunkPos chunkPos = new ChunkPos((int) source.getPosition().x, (int) source.getPosition().z);

        PollutionSystem data = PollutionSystem.get(level);
        float pollution = data.getPollution(chunkPos);
        float percentage = (pollution / Config.POLL_MAX_PER_CHUNK.get()) * 100f;

        source.sendSuccess(() -> Component.literal("ChunksPos: " + chunkPos.getRegionX() + ", " + chunkPos.getRegionZ() + " Pollution: " + percentage+"%"), false);

        return 1;
    }
}
