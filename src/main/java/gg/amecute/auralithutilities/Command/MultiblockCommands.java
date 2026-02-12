package gg.amecute.auralithutilities.Command;

import aztech.modern_industrialization.machines.blockentities.multiblocks.AbstractElectricCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.machines.models.MachineCasings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import gg.amecute.auralithutilities.AuralithUtilities;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class MultiblockCommands
{
	private static final Map<UUID, BlockPos> POS1 = new HashMap<>();
	private static final Map<UUID, BlockPos> POS2 = new HashMap<>();

	private static final char CONTROLLER_CHAR = '#';
	private static final char HATCH_CHAR      = 'H';
	private static final char AIR_CHAR        = ' ';

	private static final List<String> ALL_HATCH_FLAGS = List.of(
			"modern_industrialization:item_input",
			"modern_industrialization:item_output",
			"modern_industrialization:fluid_input",
			"modern_industrialization:fluid_output",
			"modern_industrialization:energy_input",
			"modern_industrialization:energy_output",
			"modern_industrialization:nuclear_item",
			"modern_industrialization:nuclear_fluid",
			"modern_industrialization:large_tank"
	);

	private enum BlockRole { AIR, CONTROLLER, HATCH, STRUCTURE }

	private static BlockRole classify(Level level, BlockPos pos, Block block, ResourceLocation blockId)
	{
		if (block == Blocks.AIR)      return BlockRole.AIR;
		if (isController(level, pos)) return BlockRole.CONTROLLER;
		if (isHatch(blockId))         return BlockRole.HATCH;
		return BlockRole.STRUCTURE;
	}

	private static boolean isController(Level level, BlockPos pos)
	{
		BlockEntity be = level.getBlockEntity(pos);
		return be instanceof AbstractElectricCraftingMultiblockBlockEntity;
	}

	private static boolean isHatch(ResourceLocation blockId)
	{
		String path = blockId.getPath();
		return path.contains("_hatch") || path.contains("_bus");
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("au")
						.requires(source -> source.hasPermission(3))
						.then(Commands.literal("shape")
								.then(Commands.literal("pos1").executes(MultiblockCommands::setPos1))
								.then(Commands.literal("pos2").executes(MultiblockCommands::setPos2))
								.then(Commands.literal("save")
										.then(Commands.argument("name", StringArgumentType.string())
												.then(Commands.argument("casing", ResourceLocationArgument.id())
														.suggests((ctx, builder) -> {
															MachineCasings.registeredCasings.keySet()
																	.forEach(rl -> builder.suggest(rl.toString()));
															return builder.buildFuture();
														})
														.executes(ctx -> saveStructure(
																ctx,
																StringArgumentType.getString(ctx, "name"),
																ResourceLocationArgument.getId(ctx, "casing").toString()
														))
												)
										)
								)
						)
		);
	}

	private static int setPos1(CommandContext<CommandSourceStack> ctx)
	{
		if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) return 0;
		BlockPos pos = player.blockPosition();
		POS1.put(player.getUUID(), pos);
		player.sendSystemMessage(Component.literal("§a@ Pos1 set: §f" + pos.toShortString()));
		return 1;
	}

	private static int setPos2(CommandContext<CommandSourceStack> ctx)
	{
		if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) return 0;
		BlockPos pos = player.blockPosition();
		POS2.put(player.getUUID(), pos);
		player.sendSystemMessage(Component.literal("§a@ Pos2 set: §f" + pos.toShortString()));
		return 1;
	}

	private static int saveStructure(CommandContext<CommandSourceStack> ctx, String name, String casingArg)
	{
		if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) return 0;

		BlockPos pos1 = POS1.get(player.getUUID());
		BlockPos pos2 = POS2.get(player.getUUID());

		if (pos1 == null || pos2 == null)
		{
			player.sendSystemMessage(Component.literal("§c@ Set pos1 and pos2 first!"));
			return 0;
		}

		ResourceLocation casingRl = ResourceLocation.tryParse(casingArg);
		if (casingRl == null || !MachineCasings.registeredCasings.containsKey(casingRl))
		{
			player.sendSystemMessage(Component.literal("§c@ Unknown casing: §f" + casingArg));
			return 0;
		}

		try
		{
			JsonObject json = captureStructure(player.level(), pos1, pos2, name, casingRl.toString());
			File file = saveToFile(json, name);

			player.sendSystemMessage(Component.literal("§a@ Structure saved: §f" + file.getName()));
			player.sendSystemMessage(Component.literal("§7@ " + file.getAbsolutePath()));
			return 1;
		}
		catch (Exception e)
		{
			player.sendSystemMessage(Component.literal("§c@ Error: " + e.getMessage()));
			AuralithUtilities.LOGGER.error("Failed to save structure", e);
			return 0;
		}
	}

	private static JsonObject captureStructure(Level level, BlockPos p1, BlockPos p2, String name, String casingId)
	{
		int minX = Math.min(p1.getX(), p2.getX()), maxX = Math.max(p1.getX(), p2.getX());
		int minY = Math.min(p1.getY(), p2.getY()), maxY = Math.max(p1.getY(), p2.getY());
		int minZ = Math.min(p1.getZ(), p2.getZ()), maxZ = Math.max(p1.getZ(), p2.getZ());

		Map<Character, JsonObject> palette    = new LinkedHashMap<>();
		Map<Block, Character>      blockToKey = new LinkedHashMap<>();

		blockToKey.put(Blocks.AIR, AIR_CHAR);
		JsonObject airEntry = new JsonObject();
		airEntry.addProperty("block", "minecraft:air");
		palette.put(AIR_CHAR, airEntry);

		boolean controllerFound   = false;
		boolean hatchEntryCreated = false;
		char    nextKey           = 'a';

		List<List<String>> rawLayers = new ArrayList<>();

		for (int y = minY; y <= maxY; y++)
		{
			List<String> layer = new ArrayList<>();

			for (int z = minZ; z <= maxZ; z++)
			{
				StringBuilder row = new StringBuilder();

				for (int x = minX; x <= maxX; x++)
				{
					BlockPos         pos     = new BlockPos(x, y, z);
					BlockState       state   = level.getBlockState(pos);
					Block            block   = state.isAir() ? Blocks.AIR : state.getBlock();
					ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);

					char key = switch (classify(level, pos, block, blockId))
					{
						case AIR -> AIR_CHAR;

						case CONTROLLER ->
						{
							if (!controllerFound)
							{
								controllerFound = true;
								JsonObject entry = new JsonObject();
								entry.addProperty("block", blockId.toString());
								entry.addProperty("controller", true);
								palette.put(CONTROLLER_CHAR, entry);
							}
							yield CONTROLLER_CHAR;
						}

						case HATCH ->
						{
							if (!hatchEntryCreated)
							{
								hatchEntryCreated = true;
								JsonObject entry = new JsonObject();
								entry.addProperty("block", blockId.toString());
								JsonArray flags = new JsonArray();
								ALL_HATCH_FLAGS.forEach(flags::add);
								entry.add("hatch_flags", flags);
								palette.put(HATCH_CHAR, entry);
							}
							yield HATCH_CHAR;
						}

						case STRUCTURE ->
						{
							if (blockToKey.containsKey(block)) yield blockToKey.get(block);

							while (nextKey == CONTROLLER_CHAR || nextKey == HATCH_CHAR || nextKey == AIR_CHAR)
								nextKey = advanceKey(nextKey);

							char assigned = nextKey;
							blockToKey.put(block, assigned);
							JsonObject entry = new JsonObject();
							entry.addProperty("block", blockId.toString());
							palette.put(assigned, entry);
							nextKey = advanceKey(nextKey);
							yield assigned;
						}
					};

					row.append(key);
				}
				layer.add(row.toString());
			}
			rawLayers.add(layer);
		}

		if (!controllerFound)
			AuralithUtilities.LOGGER.warn("No multiblock controller found in selection for '{}'", name);

		TrimmedStructure trimmed = trimStructure(rawLayers);

		JsonObject paletteJson = new JsonObject();
		List<Character> order = new ArrayList<>(List.of(AIR_CHAR));
		if (controllerFound)   order.add(CONTROLLER_CHAR);
		if (hatchEntryCreated) order.add(HATCH_CHAR);
		palette.keySet().stream()
				.filter(c -> c != AIR_CHAR && c != CONTROLLER_CHAR && c != HATCH_CHAR)
				.forEach(order::add);
		order.stream().filter(palette::containsKey)
				.forEach(c -> paletteJson.add(String.valueOf(c), palette.get(c)));

		JsonObject json = new JsonObject();
		json.addProperty("id",     AuralithUtilities.MODID + ":" + name);
		json.addProperty("name",   name);
		json.addProperty("casing", casingId);
		json.addProperty("type",   "machine");

		JsonObject size = new JsonObject();
		size.addProperty("x", trimmed.sizeX);
		size.addProperty("y", trimmed.sizeY);
		size.addProperty("z", trimmed.sizeZ);
		json.add("size", size);
		json.add("palette", paletteJson);
		json.add("layers", new Gson().toJsonTree(trimmed.layers));

		JsonObject recipeConfig = new JsonObject();
		recipeConfig.addProperty("recipe_type",          AuralithUtilities.MODID + ":example_recipe");
		recipeConfig.addProperty("base_energy_usage",    1024);
		recipeConfig.addProperty("max_energy_usage",     100000);
		recipeConfig.addProperty("base_processing_time", 200);
		json.add("recipe_config", recipeConfig);

		return json;
	}

	private static char advanceKey(char c)
	{
		c++;
		if (c == CONTROLLER_CHAR || c == HATCH_CHAR || c == AIR_CHAR) c++;
		if (c > 'z') c = 'A';
		if (c > 'Z' && c < 'a') c = '0';
		if (c > '9' && c < 'A') c = 'a';
		return c;
	}

	private record TrimmedStructure(List<List<String>> layers, int sizeX, int sizeY, int sizeZ) {}

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
			List<String> layer     = layers.get(y);
			boolean layerHasBlocks = false;

			for (int z = 0; z < layer.size(); z++)
			{
				String row = layer.get(z);
				for (int x = 0; x < row.length(); x++)
				{
					if (row.charAt(x) != AIR_CHAR)
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

		if (minX > maxX || minY > maxY || minZ > maxZ)
			return new TrimmedStructure(layers, originalX, originalY, originalZ);

		List<List<String>> trimmed = new ArrayList<>();
		for (int y = minY; y <= maxY; y++)
		{
			List<String> trimmedLayer = new ArrayList<>();
			for (int z = minZ; z <= maxZ; z++)
				trimmedLayer.add(layers.get(y).get(z).substring(minX, maxX + 1));
			trimmed.add(trimmedLayer);
		}

		return new TrimmedStructure(trimmed, maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
	}

	private static File saveToFile(JsonObject json, String name) throws Exception
	{
		File dir = new File("structures");
		if (!dir.exists()) dir.mkdirs();

		File out = new File(dir, name + ".json");
		try (FileWriter writer = new FileWriter(out))
		{
			writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(json));
		}

		AuralithUtilities.LOGGER.info("Saved structure to: {}", out.getAbsolutePath());
		return out;
	}
}