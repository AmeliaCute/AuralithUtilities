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

public final class ShapeConverter
{
	private static final Map<String, HatchType> HATCH_TYPE_CACHE = new HashMap<>(32);

	public static ShapeTemplate convert(MultiblockStructure structure)
	{
		try
		{
			final String[][] miShape = buildMIShapeOptimized(structure);

			String[][] transformed = ShapeFixer.transposeLayers(miShape);
			transformed = ShapeFixer.reverseEachRowInAll(transformed);

			final Map<Character, BlockDefinition> palette = structure.palette();
			final Map<Character, SimpleMember> memberMap = new HashMap<>(palette.size());
			final Map<Character, HatchFlags> hatchMap = new HashMap<>(palette.size() / 4);

			for (Map.Entry<Character, BlockDefinition> entry : palette.entrySet())
			{
				final char key = entry.getKey();
				final BlockDefinition def = entry.getValue();

				if (key == '#' || key == ' ') continue;

				final Block block = BuiltInRegistries.BLOCK.get(def.blockId());
				final SimpleMember member = SimpleMember.forBlock(() -> block);
				memberMap.put(key, member);

				if (def.hatchFlags().isPresent())
				{
					final List<String> flags = def.hatchFlags().get();
					if (!flags.isEmpty())
					{
						final HatchFlags hatchFlags = parseHatchFlagsOptimized(flags);
						hatchMap.put(key, hatchFlags);

						AuralithUtilities.LOGGER.debug("Registered hatch '{}' with block {} and {} flags",
								key, def.blockId(), flags.size());
					}
				} else
				{
					AuralithUtilities.LOGGER.debug("Registered block '{}': {}", key, def.blockId());
				}
			}

			final ShapeTemplate.LayeredBuilder builder = new ShapeTemplate.LayeredBuilder(MachineCasings.get(structure.casing()), transformed);

			for (Map.Entry<Character, SimpleMember> entry : memberMap.entrySet())
			{
				final char key = entry.getKey();
				final SimpleMember member = entry.getValue();
				final HatchFlags hatches = hatchMap.get(key);

				builder.key(key, member, hatches);
			}

			return builder.build();

		} catch (Exception e)
		{
			AuralithUtilities.LOGGER.error("Failed to convert structure: {}", structure.id(), e);
			throw new RuntimeException("Failed to convert structure: " + structure.id(), e);
		}
	}

	private static String[][] buildMIShapeOptimized(MultiblockStructure structure)
	{
		final List<List<String>> layers = structure.layers();
		final int layerCount = layers.size();
		final String[][] miShape = new String[layerCount][];

		for (int y = 0; y < layerCount; y++)
		{
			final List<String> layer = layers.get(y);
			miShape[y] = layer.toArray(new String[layer.size()]);
		}

		return miShape;
	}

	private static HatchFlags parseHatchFlagsOptimized(List<String> flagList)
	{
		final HatchFlags.Builder builder = new HatchFlags.Builder();

		for (int i = 0, size = flagList.size(); i < size; i++)
		{
			final String flag = flagList.get(i);

			final String cleanFlag;
			final int colonIdx = flag.indexOf(':');
			if (colonIdx >= 0) cleanFlag = flag.substring(colonIdx + 1);
			else cleanFlag = flag;

			addHatchTypeOptimized(builder, cleanFlag);
		}

		return builder.build();
	}

	private static void addHatchTypeOptimized(HatchFlags.Builder builder, String typeName)
	{
		HatchType cached = HATCH_TYPE_CACHE.get(typeName);
		if (cached != null)
		{
			builder.with(cached);
			return;
		}

		final String normalized = typeName.trim().toLowerCase();

		try
		{
			final HatchType hatchType;
			HatchType hatchTypeTemp;

			if (typeName.indexOf(':') >= 0)  hatchTypeTemp = HatchTypes.get(typeName);
			else
			{
				try
				{
					hatchTypeTemp = HatchTypes.get("modern_industrialization:" + normalized);
				} catch (IllegalArgumentException e)
				{
					hatchTypeTemp = HatchTypes.get(normalized);
				}
			}

			hatchType = hatchTypeTemp;
			HATCH_TYPE_CACHE.put(typeName, hatchType);

			builder.with(hatchType);
			AuralithUtilities.LOGGER.debug("Added hatch type: {}", typeName);

		} catch (IllegalArgumentException e)
		{
			AuralithUtilities.LOGGER.warn("Unknown or unavailable hatch type: {} - {}", typeName, e.getMessage());
		}
	}
}