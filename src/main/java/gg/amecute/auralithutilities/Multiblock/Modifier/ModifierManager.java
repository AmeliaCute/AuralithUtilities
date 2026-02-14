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

public final class ModifierManager
{
	private static final class AttachedModifier
	{
		final MultiblockStructure structure;
		final BlockPos origin;
		final Direction facing;
		final ModifierConfig.Direction attachmentSide;

		AttachedModifier(MultiblockStructure structure, BlockPos origin, Direction facing, ModifierConfig.Direction attachmentSide)
		{
			this.structure = structure;
			this.origin = origin;
			this.facing = facing;
			this.attachmentSide = attachmentSide;
		}
	}

	private final Level level;
	private final BlockPos machineOrigin;
	private final Direction machineFacing;
	private final MultiblockStructure machineStructure;

	private final List<AttachedModifier> attachedModifiers = new ArrayList<>(8);
	private final Map<String, Double> cachedModifiers = new HashMap<>(8);
	private final Map<ResourceLocation, Integer> typeCountCache = new HashMap<>(4);

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
		typeCountCache.clear();
		dirty = true;

		final String machineType = machineStructure.id().toString();
		final List<MultiblockStructure> possibleModifiers =
				structureManager.getCompatibleModifiers(machineType);

		final List<MultiblockValidator.DetectedModifier> detected =
				MultiblockValidator.detectModifiers(
						level,
						machineOrigin,
						machineFacing,
						machineStructure,
						possibleModifiers
				);

		final Map<ResourceLocation, List<MultiblockValidator.DetectedModifier>> byType = new HashMap<>(possibleModifiers.size());

		for (int i = 0, size = detected.size(); i < size; i++)
		{
			final MultiblockValidator.DetectedModifier mod = detected.get(i);
			final ResourceLocation id = mod.structure().id();

			List<MultiblockValidator.DetectedModifier> list = byType.get(id);
			if (list == null)
			{
				list = new ArrayList<>(4);
				byType.put(id, list);
			}
			list.add(mod);
		}

		for (Map.Entry<ResourceLocation, List<MultiblockValidator.DetectedModifier>> entry : byType.entrySet())
		{
			final List<MultiblockValidator.DetectedModifier> ofType = entry.getValue();
			if (ofType.isEmpty()) continue;

			final MultiblockValidator.DetectedModifier first = ofType.get(0);
			final int maxStacks = first.structure().modifierConfig()
					.map(ModifierConfig::maxStacks)
					.orElse(1);

			final int count = Math.min(ofType.size(), maxStacks);

			for (int i = 0; i < count; i++)
			{
				final MultiblockValidator.DetectedModifier mod = ofType.get(i);
				attachedModifiers.add(new AttachedModifier(
						mod.structure(),
						mod.origin(),
						mod.facing(),
						mod.attachmentSide()
				));
			}

			typeCountCache.put(entry.getKey(), count);
		}
	}

	public double getModifier(String type)
	{
		return getModifier(type, 1.0);
	}

	public double getModifier(String type, double defaultValue)
	{
		if (dirty) recalculateModifiers();
		return cachedModifiers.getOrDefault(type, defaultValue);
	}

	public Map<String, Double> getAllModifiers()
	{
		if (dirty) recalculateModifiers();
		return Collections.unmodifiableMap(cachedModifiers);
	}

	private void recalculateModifiers()
	{
		cachedModifiers.clear();

		final Map<ResourceLocation, List<AttachedModifier>> byType = new HashMap<>(attachedModifiers.size());

		for (int i = 0, size = attachedModifiers.size(); i < size; i++)
		{
			final AttachedModifier mod = attachedModifiers.get(i);
			final ResourceLocation id = mod.structure.id();

			List<AttachedModifier> list = byType.get(id);
			if (list == null)
			{
				list = new ArrayList<>(4);
				byType.put(id, list);
			}

			list.add(mod);
		}

		for (Map.Entry<ResourceLocation, List<AttachedModifier>> entry : byType.entrySet())
		{
			final List<AttachedModifier> stack = entry.getValue();
			if (stack.isEmpty()) continue;

			final AttachedModifier first = stack.get(0);
			final Map<String, Double> baseModifiers = first.structure.modifierConfig()
					.map(ModifierConfig::modifiers)
					.orElse(Collections.emptyMap());

			final int stackCount = stack.size();

			for (Map.Entry<String, Double> modEntry : baseModifiers.entrySet())
			{
				final String modType = modEntry.getKey();
				final double baseValue = modEntry.getValue();

				final double stackedValue = calculateStackedValue(modType, baseValue, stackCount);

				final Double existing = cachedModifiers.get(modType);
				if (existing != null) cachedModifiers.put(modType, existing * stackedValue);
				else cachedModifiers.put(modType, stackedValue);
			}
		}

		dirty = false;
	}

	private static double calculateStackedValue(String modifierType, double baseValue, int stackCount)
	{
		if (stackCount == 1)
		{
			return switch (modifierType)
			{
				case "speed" -> 1.0 + baseValue;
				case "energy_consumption", "energy_efficiency" -> 1.0 + baseValue;
				case "output_chance", "extra_output_chance" -> baseValue;
				case "capacity" -> baseValue;
				default -> baseValue;
			};
		}

		return switch (modifierType)
		{
			case "speed" ->
			{
				double result = 1.0;
				double factor = baseValue;
				final double decay = 0.75;

				for (int i = 0; i < stackCount; i++)
				{
					result *= (1.0 + factor);
					factor *= decay;
				}
				yield result;
			}

			case "energy_consumption", "energy_efficiency" -> 1.0 + (baseValue * stackCount);

			case "output_chance", "extra_output_chance" -> Math.min(1.0, baseValue * stackCount);

			case "capacity" -> fastPow(baseValue, stackCount);

			default -> fastPow(baseValue, stackCount);
		};
	}

	private static double fastPow(double base, int exp)
	{
		if (exp == 0) return 1.0;
		if (exp == 1) return base;
		if (exp == 2) return base * base;

		double result = 1.0;
		double current = base;

		while (exp > 0)
		{
			if ((exp & 1) == 1) result *= current;

			current *= current;
			exp >>= 1;
		}

		return result;
	}

	public long applyEnergyModifier(long baseEnergy)
	{
		final double modifier = getModifier("energy_consumption", 1.0);
		return Math.round(baseEnergy * modifier);
	}

	public int applySpeedModifier(int baseTime)
	{
		final double modifier = getModifier("speed", 1.0);
		return Math.max(1, (int) Math.round(baseTime / modifier));
	}

	public int applyOutputCount(int baseCount)
	{
		double chance = getModifier("extra_output_chance", 0.0);
		int extra = 0;

		if (chance <= 0.0) return baseCount;

		while (chance > 0.0)
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
		return typeCountCache.getOrDefault(type, 0);
	}

	public boolean validateAll()
	{
		for (int i = 0, size = attachedModifiers.size(); i < size; i++)
		{
			final AttachedModifier modifier = attachedModifiers.get(i);
			final MultiblockValidator.ValidationResult result =
					MultiblockValidator.validate(level, modifier.origin, modifier.structure, modifier.facing);

			if (!result.valid()) return false;
		}
		return true;
	}

	public void writeNbt(CompoundTag tag)
	{
		final ListTag list = new ListTag();

		for (int i = 0, size = attachedModifiers.size(); i < size; i++)
		{
			final AttachedModifier modifier = attachedModifiers.get(i);
			final CompoundTag modTag = new CompoundTag();

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
		typeCountCache.clear();
		dirty = true;

		if (!tag.contains("attached_modifiers")) return;
		final ListTag list = tag.getList("attached_modifiers", Tag.TAG_COMPOUND);
		final int size = list.size();

		if (attachedModifiers instanceof ArrayList) ((ArrayList<?>) attachedModifiers).ensureCapacity(size);

		for (int i = 0; i < size; i++)
		{
			final CompoundTag modTag = list.getCompound(i);

			final ResourceLocation id = ResourceLocation.parse(modTag.getString("id"));
			final BlockPos origin = BlockPos.of(modTag.getLong("origin"));
			final Direction facing = Direction.from3DDataValue(modTag.getInt("facing"));
			final ModifierConfig.Direction side = ModifierConfig.Direction.valueOf(modTag.getString("side"));

			structureManager.getStructure(id).ifPresent(structure ->
			{
				attachedModifiers.add(new AttachedModifier(structure, origin, facing, side));
				typeCountCache.merge(id, 1, Integer::sum);
			});
		}
	}
}