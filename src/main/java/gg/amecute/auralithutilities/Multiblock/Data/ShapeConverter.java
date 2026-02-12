package gg.amecute.auralithutilities.Multiblock.Data;

import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;
import gg.amecute.auralithutilities.AuralithUtilities;
import gg.amecute.auralithutilities.Utils.ShapeFixer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static aztech.modern_industrialization.machines.multiblocks.HatchTypes.*;

public class ShapeConverter
{
	public static ShapeTemplate convert(MultiblockStructure structure)
	{
		try
		{
			String[][] miShape = buildMIShape(structure);

			miShape = ShapeFixer.transposeLayers(miShape);
			miShape = ShapeFixer.reverseEachRowInAll(miShape);

			Map<Character, SimpleMember> memberMap = new HashMap<>();
			Map<Character, HatchFlags> hatchMap = new HashMap<>();

			for(Map.Entry<Character, BlockDefinition> entry : structure.palette().entrySet())
			{
				char key = entry.getKey();
				BlockDefinition def = entry.getValue();

				if (key == '#' || key == ' ')
				{
					continue;
				}

				Block block = BuiltInRegistries.BLOCK.get(def.blockId());
				SimpleMember member = SimpleMember.forBlock(() -> block);
				memberMap.put(key, member);

				if(def.hatchType() != null && !def.hatchType().isEmpty())
				{
					HatchFlags flags = parseHatchFlags(def.hatchType());
					hatchMap.put(key, flags);
				}
				else if(def.hatchType().isPresent())
				{
					HatchFlags flags = parseHatchType(def.hatchType().get());
					hatchMap.put(key, flags);
				}
			}

			ShapeTemplate.LayeredBuilder builder = new ShapeTemplate.LayeredBuilder(MachineCasings.get(structure.casing()), miShape);

			for(Map.Entry<Character, SimpleMember> entry : memberMap.entrySet())
			{
				char key = entry.getKey();
				SimpleMember member = entry.getValue();
				HatchFlags hatches = hatchMap.get(key);

				builder.key(key, member, hatches);
			}

			return builder.build();
		} catch (Exception e)
		{
			AuralithUtilities.LOGGER.error("Failed to convert structure: {}", structure.id(), e);
			throw new RuntimeException("Failed to convert structure: " + structure.id(), e);
		}
	}

	private static String[][] buildMIShape(MultiblockStructure structure)
	{
		List<List<String>> layers = structure.layers();
		String[][] miShape = new String[layers.size()][];

		for (int y = 0; y < layers.size(); y++)
		{
			List<String> layer = layers.get(y);
			miShape[y] = layer.toArray(new String[0]);
		}

		return miShape;
	}

	private static HatchFlags parseHatchFlags(Optional<String> flagList)
	{
		HatchFlags.Builder builder = new HatchFlags.Builder();

		for (String flag : flagList.stream().toList())
		{
			String cleanFlag = flag.contains(":") ? flag.substring(flag.indexOf(':') + 1) : flag;
			addHatchType(builder, cleanFlag);
		}

		return builder.build();
	}

	private static HatchFlags parseHatchType(String type)
	{
		HatchFlags.Builder builder = new HatchFlags.Builder();

		String[] types = type.split(",");
		for (String t : types)
		{
			String trimmed = t.trim().toLowerCase();
			addHatchType(builder, trimmed);
		}

		return builder.build();
	}

	private static void addHatchType(HatchFlags.Builder builder, String typeName)
	{
		String normalized = typeName.trim().toLowerCase();

		switch (normalized)
		{
			case "any" -> builder
					.with(ITEM_INPUT)
					.with(ITEM_OUTPUT)
					.with(FLUID_INPUT)
					.with(FLUID_OUTPUT)
					.with(ENERGY_INPUT);

			case "item_input" -> builder.with(ITEM_INPUT);
			case "item_output" -> builder.with(ITEM_OUTPUT);
			case "fluid_input" -> builder.with(FLUID_INPUT);
			case "fluid_output" -> builder.with(FLUID_OUTPUT);
			case "energy_input" -> builder.with(ENERGY_INPUT);
			case "energy_output" -> builder.with(ENERGY_OUTPUT);

			case "nuclear_item" -> {
				try {
					builder.with(ITEM_INPUT).with(ITEM_OUTPUT);
				} catch (Exception e) {
					AuralithUtilities.LOGGER.debug("Nuclear item hatch type not available");
				}
			}
			case "nuclear_fluid" -> {
				try {
					builder.with(FLUID_INPUT).with(FLUID_OUTPUT);
				} catch (Exception e) {
					AuralithUtilities.LOGGER.debug("Nuclear fluid hatch type not available");
				}
			}
			case "large_tank" -> {
				try {
					builder.with(FLUID_INPUT).with(FLUID_OUTPUT);
				} catch (Exception e) {
					AuralithUtilities.LOGGER.debug("Large tank hatch type not available");
				}
			}

			default -> AuralithUtilities.LOGGER.warn("Unknown hatch type: {}", normalized);
		}
	}
}