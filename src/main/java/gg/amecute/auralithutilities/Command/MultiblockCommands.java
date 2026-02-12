package gg.amecute.auralithutilities.Command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import gg.amecute.auralithutilities.AuralithUtilities;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class MultiblockCommands
{
	private static final Map<UUID, BlockPos> POS1 = new HashMap<>();
	private static final Map<UUID, BlockPos> POS2 = new HashMap<>();

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(
			Commands.literal("au")
			.requires(source -> source.hasPermission(3))
			.then(Commands.literal("shape")
				.then(Commands.literal("pos1").executes(MultiblockCommands::setPos1))
				.then(Commands.literal("pos2").executes(MultiblockCommands::setPos2))
				.then(Commands.literal("save")
					.then(Commands.argument("name", StringArgumentType.string())
						.executes(ctx -> saveStructure(ctx, StringArgumentType.getString(ctx, "name")))
					)
				)
			)
		);
	}

	private static int setPos1(CommandContext<CommandSourceStack> ctx)
	{
		if(!(ctx.getSource().getEntity() instanceof ServerPlayer player)) return 0;

		BlockPos pos = player.blockPosition();
		POS1.put(player.getUUID(), pos);

		player.sendSystemMessage(Component.literal("§a@ Pos1 set: §f" + pos.toShortString()));
		return 1;
	}

	private static int setPos2(CommandContext<CommandSourceStack> ctx)
	{
		if(!(ctx.getSource().getEntity() instanceof ServerPlayer player)) return 0;

		BlockPos pos = player.blockPosition();
		POS2.put(player.getUUID(), pos);

		player.sendSystemMessage(Component.literal("§a@ Pos2 set: §f" + pos.toShortString()));
		return 1;
	}

	private static int saveStructure(CommandContext<CommandSourceStack> ctx, String name)
	{
		if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) return 0;

		UUID playerId = player.getUUID();
		BlockPos pos1 = POS1.get(playerId);
		BlockPos pos2 = POS2.get(playerId);

		if (pos1 == null || pos2 == null)
		{
			player.sendSystemMessage(Component.literal("§c✗ Set pos1 and pos2 first!"));
			return 0;
		}

		try
		{
			Level level = player.level();
			JsonObject json = captureStructure(level, pos1, pos2, name);
			File file = saveToFile(json, name);

			player.sendSystemMessage(Component.literal("§a✓ Structure saved: §f" + file.getName()));
			player.sendSystemMessage(Component.literal("§7→ " + file.getAbsolutePath()));
			return 1;
		}
		catch (Exception e)
		{
			player.sendSystemMessage(Component.literal("§c✗ Error: " + e.getMessage()));
			AuralithUtilities.LOGGER.error("Failed to save structure", e);
			return 0;
		}
	}

	private static JsonObject captureStructure(Level level, BlockPos p1, BlockPos p2, String name)
	{
		int minX = Math.min(p1.getX(), p2.getX());
		int maxX = Math.max(p1.getX(), p2.getX());
		int minY = Math.min(p1.getY(), p2.getY());
		int maxY = Math.max(p1.getY(), p2.getY());
		int minZ = Math.min(p1.getZ(), p2.getZ());
		int maxZ = Math.max(p1.getZ(), p2.getZ());

		List<List<String>> rawLayers = new ArrayList<>();
		Map<Block, Character> blockToKey = new LinkedHashMap<>();
		Map<Character, JsonObject> palette = new LinkedHashMap<>();
		char nextKey = 'a';

		blockToKey.put(Blocks.AIR, ' ');
		JsonObject airDef = new JsonObject();
		airDef.addProperty("block", "minecraft:air");
		palette.put(' ', airDef);

		for (int y = minY; y <= maxY; y++)
		{
			List<String> layer = new ArrayList<>();

			for (int z = minZ; z <= maxZ; z++)
			{
				StringBuilder row = new StringBuilder();

				for (int x = minX; x <= maxX; x++)
				{
					BlockPos pos = new BlockPos(x, y, z);
					BlockState state = level.getBlockState(pos);
					Block block = state.isAir() ? Blocks.AIR : state.getBlock();

					if (!blockToKey.containsKey(block))
					{
						blockToKey.put(block, nextKey);

						JsonObject blockDef = new JsonObject();
						ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
						blockDef.addProperty("block", blockId.toString());
						palette.put(nextKey, blockDef);

						nextKey++;
						if (nextKey > 'z') nextKey = 'A';
						if (nextKey > 'Z') nextKey = '0';
					}

					row.append(blockToKey.get(block));
				}

				layer.add(row.toString());
			}

			rawLayers.add(layer);
		}

		TrimmedStructure trimmed = trimStructure(rawLayers);

		JsonObject json = new JsonObject();
		json.addProperty("id", AuralithUtilities.MODID + ":" + name);
		json.addProperty("name", name);
		json.addProperty("type", "machine");

		JsonObject size = new JsonObject();
		size.addProperty("x", trimmed.sizeX);
		size.addProperty("y", trimmed.sizeY);
		size.addProperty("z", trimmed.sizeZ);
		json.add("size", size);

		JsonObject paletteJson = new JsonObject();
		for (Map.Entry<Character, JsonObject> entry : palette.entrySet())
		{
			paletteJson.add(String.valueOf(entry.getKey()), entry.getValue());
		}
		json.add("palette", paletteJson);

		Gson gson = new Gson();
		json.add("layers", gson.toJsonTree(trimmed.layers));

		JsonObject recipeConfig = new JsonObject();
		recipeConfig.addProperty("recipe_type", AuralithUtilities.MODID + ":example_recipe");
		recipeConfig.addProperty("base_energy_usage", 1024);
		recipeConfig.addProperty("max_energy_usage", 100000);
		recipeConfig.addProperty("base_processing_time", 200);
		json.add("recipe_config", recipeConfig);
		return json;
	}

	private static class TrimmedStructure
	{
		List<List<String>> layers;
		int sizeX, sizeY, sizeZ;

		TrimmedStructure(List<List<String>> layers, int x, int y, int z)
		{
			this.layers = layers;
			this.sizeX = x;
			this.sizeY = y;
			this.sizeZ = z;
		}
	}

	private static TrimmedStructure trimStructure(List<List<String>> layers)
	{
		if (layers.isEmpty()) return new TrimmedStructure(layers, 0, 0, 0);

		int originalX = layers.get(0).isEmpty() ? 0 : layers.get(0).get(0).length();
		int originalY = layers.size();
		int originalZ = layers.get(0).size();

		int minX = originalX, maxX = -1;
		int minY = originalY, maxY = -1;
		int minZ = originalZ, maxZ = -1;

		for (int y = 0; y < layers.size(); y++)
		{
			List<String> layer = layers.get(y);
			boolean layerHasBlocks = false;

			for (int z = 0; z < layer.size(); z++)
			{
				String row = layer.get(z);

				for (int x = 0; x < row.length(); x++)
				{
					if (row.charAt(x) != ' ')
					{
						layerHasBlocks = true;
						if (x < minX) minX = x;
						if (x > maxX) maxX = x;
						if (z < minZ) minZ = z;
						if (z > maxZ) maxZ = z;
					}
				}
			}

			if (layerHasBlocks)
			{
				if (y < minY) minY = y;
				if (y > maxY) maxY = y;
			}
		}

		if (minX > maxX || minY > maxY || minZ > maxZ) return new TrimmedStructure(layers, originalX, originalY, originalZ);

		List<List<String>> trimmed = new ArrayList<>();
		for (int y = minY; y <= maxY; y++)
		{
			List<String> layer = layers.get(y);
			List<String> trimmedLayer = new ArrayList<>();

			for (int z = minZ; z <= maxZ; z++)
			{
				String row = layer.get(z);
				String trimmedRow = row.substring(minX, maxX + 1);
				trimmedLayer.add(trimmedRow);
			}

			trimmed.add(trimmedLayer);
		}

		int sizeX = maxX - minX + 1;
		int sizeY = maxY - minY + 1;
		int sizeZ = maxZ - minZ + 1;

		return new TrimmedStructure(trimmed, sizeX, sizeY, sizeZ);
	}

	private static File saveToFile(JsonObject json, String name) throws Exception
	{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String jsonStr = gson.toJson(json);

		File structuresDir = new File("structures");
		if (!structuresDir.exists()) structuresDir.mkdirs();

		File outputFile = new File(structuresDir, name + ".json");
		try (FileWriter writer = new FileWriter(outputFile))
		{
			writer.write(jsonStr);
		}

		AuralithUtilities.LOGGER.info("Saved structure to: {}", outputFile.getAbsolutePath());
		return outputFile;
	}
}
