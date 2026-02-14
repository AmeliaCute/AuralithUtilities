package gg.amecute.auralithutilities.Multiblock.Data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import gg.amecute.auralithutilities.AuralithUtilities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MultiblockStructureManager extends SimpleJsonResourceReloadListener
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String DIRECTORY = "auralith_multiblock";

	private final Map<ResourceLocation, MultiblockStructure> structures = new ConcurrentHashMap<>();
	private final Map<String, List<MultiblockStructure>> structuresByType = new ConcurrentHashMap<>();

	public MultiblockStructureManager()
	{
		super(GSON, DIRECTORY);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiller)
	{
		structures.clear();
		structuresByType.clear();

		for(Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet())
		{
			ResourceLocation fileId = entry.getKey();
			JsonElement json = entry.getValue();

			try
			{
				var result = MultiblockStructure.CODEC.parse(JsonOps.INSTANCE, json);

				result.resultOrPartial(error -> {
					AuralithUtilities.LOGGER.error("Failed to parse multiblock structure {}: {}", fileId, error);
				}).ifPresent(loadedStructure -> {
					MultiblockStructure structure = loadedStructure;

					ResourceLocation structureId = structure.id();
					if (structureId.getNamespace().equals("unknown"))
					{
						structureId = fileId;
						structure = new MultiblockStructure(
								structureId,
								structure.name().equals("Unknown") ? fileId.getPath() : structure.name(),
								structure.casing(),
								structure.type(),
								structure.size(),
								structure.palette(),
								structure.layers(),
								structure.modifierConfig(),
								structure.animationConfig(),
								structure.recipeConfig()
						);
					}

					Vec3i size = structure.size();
					if (size.x() == 3 && size.y() == 3 && size.z() == 3)
					{
						size = MultiblockStructure.calculateSize(structure.layers());
						structure = new MultiblockStructure(
								structure.id(),
								structure.name(),
								structure.casing(),
								structure.type(),
								size,
								structure.palette(),
								structure.layers(),
								structure.modifierConfig(),
								structure.animationConfig(),
								structure.recipeConfig()
						);
					}

					if (structure.casing().equals("modern_industrialization:steel_machine_casing"))
					{
						String derivedCasing = MultiblockStructure.deriveCasing(structure.palette());
						structure = new MultiblockStructure(
								structure.id(),
								structure.name(),
								derivedCasing,
								structure.type(),
								structure.size(),
								structure.palette(),
								structure.layers(),
								structure.modifierConfig(),
								structure.animationConfig(),
								structure.recipeConfig()
						);
					}

					structures.put(structure.id(), structure);
					structuresByType.computeIfAbsent(structure.type().name().toLowerCase(),k -> new ArrayList<>()).add(structure);

					AuralithUtilities.LOGGER.info("Loaded multiblock structure: {} (type: {}, size: {}x{}x{})",
							structure.name(), structure.type(), structure.size().x(), structure.size().y(), structure.size().z());
				});

				if (result.error().isPresent()) AuralithUtilities.LOGGER.error("Exception loading multiblock structure {}", fileId);

			} catch (Exception e)
			{
				AuralithUtilities.LOGGER.error("Exception loading multiblock structure {}", fileId, e);
			}
		}

		AuralithUtilities.LOGGER.info("Loaded {} multiblock structures from datapacks", structures.size());

		try
		{
			Class.forName("gg.amecute.auralithutilities.Multiblock.AuralithMultiblock")
				.getMethod("reloadAllShapes", MultiblockStructureManager.class)
				.invoke(null, this);
		}
		catch (Exception e)
		{
			AuralithUtilities.LOGGER.warn("Could not reload multiblock shapes: {}", e.getMessage());
		}
	}

	public Optional<MultiblockStructure> getStructure(ResourceLocation id)
	{
		return Optional.ofNullable(structures.get(id));
	}

	public List<MultiblockStructure> getStructuresByType(MultiblockType type)
	{
		return structuresByType.getOrDefault(type.name().toLowerCase(), List.of());
	}

	public List<MultiblockStructure> getMachines()
	{
		return getStructuresByType(MultiblockType.MACHINE);
	}

	public List<MultiblockStructure> getModifiers()
	{
		return getStructuresByType(MultiblockType.MODIFIER);
	}

	public Collection<MultiblockStructure> getAllStructures()
	{
		return structures.values();
	}

	public List<MultiblockStructure> getCompatibleModifiers(String targetType)
	{
		return getModifiers().stream()
				.filter(structure -> structure.modifierConfig().isPresent())
				.filter(structure -> {
					String configTarget = structure.modifierConfig().get().targetType();
					return configTarget.equals("*") || configTarget.equals(targetType);
				})
				.toList();
	}
}