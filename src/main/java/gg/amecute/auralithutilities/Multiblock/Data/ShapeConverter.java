package gg.amecute.auralithutilities.Multiblock.Data;

import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.HatchType;
import aztech.modern_industrialization.machines.multiblocks.HatchTypes;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;
import gg.amecute.auralithutilities.AuralithUtilities;
import gg.amecute.auralithutilities.Utils.ShapeFixer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

				if(def.hatchFlags().isPresent() && !def.hatchFlags().get().isEmpty())
				{
					HatchFlags flags = parseHatchFlags(def.hatchFlags().get());
					Block block = BuiltInRegistries.BLOCK.get(def.blockId());
					SimpleMember member = SimpleMember.forBlock(() -> block);
					memberMap.put(key, member);
					hatchMap.put(key, flags);

					AuralithUtilities.LOGGER.debug("Registered hatch '{}' with block {} and {} flags", key, def.blockId(), def.hatchFlags().get().size());
				}
				else
				{
					Block block = BuiltInRegistries.BLOCK.get(def.blockId());
					SimpleMember member = SimpleMember.forBlock(() -> block);
					memberMap.put(key, member);

					AuralithUtilities.LOGGER.debug("Registered block '{}': {}", key, def.blockId());
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

	private static HatchFlags parseHatchFlags(List<String> flagList)
	{
		HatchFlags.Builder builder = new HatchFlags.Builder();

		for (String flag : flagList)
		{
			String cleanFlag = flag.contains(":") ? flag.substring(flag.indexOf(':') + 1) : flag;
			addHatchType(builder, cleanFlag);
		}

		return builder.build();
	}

	private static void addHatchType(HatchFlags.Builder builder, String typeName)
	{
		String normalized = typeName.trim().toLowerCase();

		try
		{
			HatchType hatchType;

			if (typeName.contains(":")) hatchType = HatchTypes.get(typeName);
			else
			{
				try
				{
					hatchType = HatchTypes.get("modern_industrialization:" + normalized);
				}
				catch (IllegalArgumentException e)
				{
					hatchType = HatchTypes.get(normalized);
				}
			}

			builder.with(hatchType);
			AuralithUtilities.LOGGER.debug("Added hatch type: {}", typeName);
		}
		catch (IllegalArgumentException e)
		{
			AuralithUtilities.LOGGER.warn("Unknown or unavailable hatch type: {} - {}", typeName, e.getMessage());
		}
	}
}