package gg.amecute.auralithutilities.Multiblock.Modifier;

import gg.amecute.auralithutilities.Multiblock.Data.ModifierConfig;
import gg.amecute.auralithutilities.Multiblock.Data.MultiblockStructure;
import gg.amecute.auralithutilities.Multiblock.Data.MultiblockStructureManager;
import gg.amecute.auralithutilities.Multiblock.Data.MultiblockValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.stream.Collectors;

public class ModifierManager
{
	private record AttachedModifier(
			MultiblockStructure structure,
			BlockPos origin,
			Direction facing,
			ModifierConfig.Direction attachmentSide
	) {}

	private final Level level;
	private final BlockPos machineOrigin;
	private final Direction machineFacing;
	private final MultiblockStructure machineStructure;

	private final List<AttachedModifier> attachedModifiers = new ArrayList<>();
	private final Map<String, Double> cachedModifiers = new HashMap<>();
	private boolean dirty = true;

	public ModifierManager(Level level, BlockPos machineOrigin, Direction machineFacing, MultiblockStructure machineStructure)
	{
		this.level = level;
		this.machineOrigin = machineOrigin;
		this.machineFacing = machineFacing;
		this.machineStructure = machineStructure;
	}

	public void scanModifiers(MultiblockStructureManager structureManager)
	{
		attachedModifiers.clear();
		dirty = true;


		String machineType = machineStructure.id().toString();
		List<MultiblockStructure> possibleModifiers = structureManager.getCompatibleModifiers(machineType);

		List<MultiblockValidator.DetectedModifier> detected = MultiblockValidator.detectModifiers(
			level,
			machineOrigin,
			machineFacing,
			machineStructure,
			possibleModifiers
		);

		Map<ResourceLocation, List<MultiblockValidator.DetectedModifier>> byType = detected.stream().collect(Collectors.groupingBy(d -> d.structure().id()));
		for(Map.Entry<ResourceLocation, List<MultiblockValidator.DetectedModifier>> entry : byType.entrySet())
		{
			List<MultiblockValidator.DetectedModifier> ofType = entry.getValue();
			int maxStacks = ofType.get(0).structure().modifierConfig().map(ModifierConfig::maxStacks).orElse(1);
			int count = Math.min(ofType.size(), maxStacks);

			for(int i = 0; i < count; ++i)
			{
				MultiblockValidator.DetectedModifier mod = ofType.get(i);
				attachedModifiers.add(new AttachedModifier(
					mod.structure(),
					mod.origin(),
					mod.facing(),
					mod.attachmentSide()
				));
			}
		}
	}

	public double getModifier(String type)
	{
		return getModifier(type, 1.0);
	}
	
	public double getModifier(String type, double defaultValue)
	{
		if(dirty) recalculateModifiers();

		return cachedModifiers.getOrDefault(type, defaultValue);
	}

	public Map<String, Double> getAllModifiers()
	{
		if(dirty) recalculateModifiers();

		return Collections.unmodifiableMap(cachedModifiers);
	}

	private void recalculateModifiers()
	{
		cachedModifiers.clear();

		Map<ResourceLocation, List<AttachedModifier>> byType = attachedModifiers.stream().collect(Collectors.groupingBy(m -> m.structure().id()));

		for(List<AttachedModifier> stack : byType.values())
		{
			if(stack.isEmpty()) continue;

			AttachedModifier first = stack.get(0);
			Map<String, Double> baseModifiers = first.structure.modifierConfig().map(ModifierConfig::modifiers).orElse(Map.of());

			int stackCount = stack.size();

			for(Map.Entry<String, Double> entry : baseModifiers.entrySet())
			{
				String modType = entry.getKey();
				double baseValue = entry.getValue();

				double stackedValue = calculateStackedValue(modType, baseValue, stackCount);
				cachedModifiers.merge(modType, stackedValue, (a,b) -> a * b);
			}
		}

		dirty = false;
	}

	private double calculateStackedValue(String modifierType, double baseValue, int stackCount)
	{
		return switch (modifierType)
		{
			case "speed" ->
			{
				double result = 1.0;
				for (int i = 0; i < stackCount; i++) result *= (1.0 + baseValue * Math.pow(0.75, i));

				yield result;
			}

			case "energy_consumption", "energy_efficiency" -> 1.0 + (baseValue * stackCount);
			case "output_chance", "extra_output_chance" -> Math.min(1.0, baseValue * stackCount);
			case "capacity" -> Math.pow(baseValue, stackCount);
			default -> Math.pow(baseValue, stackCount);
		};
	}

	public long applyEnergyModifier(long baseEnergy)
	{
		double modifier = getModifier("energy_consumption", 1.0);
		return Math.round(baseEnergy * modifier);
	}

	public int applySpeedModifier(int baseTime)
	{
		double modifier = getModifier("speed", 1.0);
		return Math.max(1, (int) Math.round(baseTime / modifier));
	}

	public int applyOutputCount(int baseCount)
	{
		double chance = getModifier("extra_output_chance", 0.0);
		int extra = 0;

		while (chance > 0)
		{
			if (Math.random() < Math.min(chance, 1.0)) extra++;

			chance -= 1.0;
		}

		return baseCount + extra;
	}

	public int getModifierCount()
	{
		return attachedModifiers.size();
	}

	public int getModifierCount(ResourceLocation type)
	{
		return (int) attachedModifiers.stream().filter(m -> m.structure.id().equals(type)).count();
	}

	public boolean validateAll()
	{
		for (AttachedModifier modifier : attachedModifiers)
		{
			var result = MultiblockValidator.validate(level, modifier.origin, modifier.structure, modifier.facing);
			if (!result.valid()) return false;
		}

		return true;
	}

	public void writeNbt(CompoundTag tag)
	{
		ListTag list = new ListTag();

		for (AttachedModifier modifier : attachedModifiers)
		{
			CompoundTag modTag = new CompoundTag();
			modTag.putString("id", modifier.structure.id().toString());
			modTag.putLong("origin", modifier.origin.asLong());
			modTag.putInt("facing", modifier.facing.get3DDataValue());
			modTag.putString("side", modifier.attachmentSide.name());
			list.add(modTag);
		}

		tag.put("attached_modifiers", list);
	}

	public void readNbt(CompoundTag tag, MultiblockStructureManager structureManager)
	{
		attachedModifiers.clear();
		dirty = true;

		if (!tag.contains("attached_modifiers")) return;

		ListTag list = tag.getList("attached_modifiers", Tag.TAG_COMPOUND);

		for (int i = 0; i < list.size(); i++)
		{
			CompoundTag modTag = list.getCompound(i);

			ResourceLocation id = ResourceLocation.parse(modTag.getString("id"));
			BlockPos origin = BlockPos.of(modTag.getLong("origin"));
			Direction facing = Direction.from3DDataValue(modTag.getInt("facing"));
			ModifierConfig.Direction side = ModifierConfig.Direction.valueOf(modTag.getString("side"));

			structureManager.getStructure(id).ifPresent(structure -> { attachedModifiers.add(new AttachedModifier(structure, origin, facing, side)); });
		}
	}

	public List<String> getDebugInfo()
	{
		List<String> info = new ArrayList<>();
		info.add("Attached Modifiers: " + attachedModifiers.size());

		Map<ResourceLocation, Long> counts = attachedModifiers.stream().collect(Collectors.groupingBy(m -> m.structure.id(), Collectors.counting()));

		for (Map.Entry<ResourceLocation, Long> entry : counts.entrySet()) info.add("  - " + entry.getKey() + " x" + entry.getValue());

		info.add("Active Modifiers:");
		for (Map.Entry<String, Double> entry : getAllModifiers().entrySet()) info.add(String.format("  - %s: %.2f%%", entry.getKey(), entry.getValue() * 100));

		return info;
	}
}
