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
			ResourceLocation id = entry.getKey();
			JsonElement json = entry.getValue();

			try
			{
				var result = MultiblockStructure.CODEC.parse(JsonOps.INSTANCE, json);

				result.resultOrPartial(error -> {
					AuralithUtilities.LOGGER.error("Failed to parse multiblock structure {}: {}", id, error);
				}).ifPresent(structure -> {
					structures.put(structure.id(), structure);
					structuresByType.computeIfAbsent(structure.type().name().toLowerCase(),k -> new ArrayList<>()).add(structure);

					AuralithUtilities.LOGGER.info("Loaded multiblock structure: {} (type: {})",
							structure.name(), structure.type());
				});

				if (result.error().isPresent()) AuralithUtilities.LOGGER.error("Exception loading multiblock structure {}", id);

			} catch (Exception e) {
				AuralithUtilities.LOGGER.error("Exception loading multiblock structure {}", id, e);
			}
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
