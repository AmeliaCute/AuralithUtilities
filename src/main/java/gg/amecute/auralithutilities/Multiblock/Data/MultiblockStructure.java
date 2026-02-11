package gg.amecute.auralithutilities.Multiblock.Data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record MultiblockStructure
		(
				ResourceLocation id,
				String name,
				MultiblockType type,
				Vec3i size,
				Map<Character, BlockDefinition> palette,
				List<List<String>> layers,
				Optional<ModifierConfig> modifierConfig,
				Optional<AnimationConfig> animationConfig,
				RecipeConfig recipeConfig
		)
{
	public static final Codec<MultiblockStructure> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					ResourceLocation.CODEC.fieldOf("id").forGetter(MultiblockStructure::id),
					Codec.STRING.fieldOf("name").forGetter(MultiblockStructure::name),
					MultiblockType.CODEC.fieldOf("type").forGetter(MultiblockStructure::type),
					Vec3i.CODEC.fieldOf("size").forGetter(MultiblockStructure::size),
					Codec.unboundedMap(Codec.STRING.xmap(s -> s.charAt(0), c -> String.valueOf(c)),
							BlockDefinition.CODEC).fieldOf("palette").forGetter(MultiblockStructure::palette),
					Codec.list(Codec.STRING.listOf()).fieldOf("layers").forGetter(MultiblockStructure::layers),
					ModifierConfig.CODEC.optionalFieldOf("modifier_config").forGetter(MultiblockStructure::modifierConfig),
					AnimationConfig.CODEC.optionalFieldOf("animation_config").forGetter(MultiblockStructure::animationConfig),
					RecipeConfig.CODEC.fieldOf("recipe_config").forGetter(MultiblockStructure::recipeConfig)
			).apply(instance, MultiblockStructure::new)
	);

	public BlockDefinition getBlockAt(BlockPos localPos)
	{
		if (localPos.getY() < 0 || localPos.getY() >= layers().size()) return null;

		List<String> layer = layers().get(localPos.getY());
		if (localPos.getZ() < 0 || localPos.getZ() >= layer.size()) return null;

		String row = layer.get(localPos.getZ());
		if (localPos.getX() < 0 || localPos.getX() >= row.length()) return null;

		char key = row.charAt(localPos.getX());
		return palette.get(key);
	}

	public Optional<BlockPos> findController()
	{
		for (int y = 0; y < layers.size(); ++y)
		{
			List<String> layer = layers.get(y);

			for (int z = 0; z < layer.size(); ++z)
			{
				String row = layer.get(z);

				for (int x = 0; x < row.length(); ++x)
				{
					char key = row.charAt(x);
					BlockDefinition def = palette.get(key);

					if (def != null && def.isController())
						return Optional.of(new BlockPos(x, y, z));
				}
			}
		}
		return Optional.empty();
	}
}

record Vec3i(int x, int y, int z)
{
	public static final Codec<Vec3i> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.INT.fieldOf("x").forGetter(Vec3i::x),
					Codec.INT.fieldOf("y").forGetter(Vec3i::y),
					Codec.INT.fieldOf("z").forGetter(Vec3i::z)
			).apply(instance, Vec3i::new)
	);
}

record BlockDefinition
		(
				ResourceLocation blockId,
				Optional<String> hatchType,
				boolean isController,
				Map<String, String> properties
		)
{
	public static final Codec<BlockDefinition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					ResourceLocation.CODEC.fieldOf("block").forGetter(BlockDefinition::blockId),
					Codec.STRING.optionalFieldOf("hatch_type").forGetter(BlockDefinition::hatchType),
					Codec.BOOL.optionalFieldOf("is_controller", false).forGetter(BlockDefinition::isController),
					Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("properties", Map.of())
							.forGetter(BlockDefinition::properties)
			).apply(instance, BlockDefinition::new)
	);

	public Block getBlock()
	{
		return BuiltInRegistries.BLOCK.get(blockId);
	}
}

enum MultiblockType
{
	MACHINE,
	MODIFIER,
	DECORATION;

	public static final Codec<MultiblockType> CODEC = Codec.STRING.xmap(
			s -> MultiblockType.valueOf(s.toUpperCase()),
			Enum::name
	);
}

public record ModifierConfig
		(
				String targetType,
				Map<String, Double> modifiers,
				int maxStacks,
				List<Direction> allowedDirections
		)
{
	public static final Codec<ModifierConfig> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.STRING.fieldOf("target_type").forGetter(ModifierConfig::targetType),
					Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).fieldOf("modifiers").forGetter(ModifierConfig::modifiers),
					Codec.INT.optionalFieldOf("max_stacks", 1).forGetter(ModifierConfig::maxStacks),
					Codec.list(Direction.CODEC).optionalFieldOf("allowed_directions", List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST))
							.forGetter(ModifierConfig::allowedDirections)
			).apply(instance, ModifierConfig::new)
	);

	public enum Direction
	{
		NORTH, SOUTH, EAST, WEST, UP, DOWN;

		public static final Codec<Direction> CODEC = Codec.STRING.xmap(
				s -> Direction.valueOf(s.toUpperCase()),
				Enum::name
		);
	}
}

record Vec3d(double x, double y, double z)
{
	public static final Codec<Vec3d> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.DOUBLE.fieldOf("x").forGetter(Vec3d::x),
					Codec.DOUBLE.fieldOf("y").forGetter(Vec3d::y),
					Codec.DOUBLE.fieldOf("z").forGetter(Vec3d::z)
			).apply(instance, Vec3d::new)
	);
}

record AnimationConfig
		(
				String animationType,
				Vec3d offset,
				float scale,
				Map<String, Object> parameters
		)
{
	public static final Codec<AnimationConfig> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.STRING.fieldOf("animation_type").forGetter(AnimationConfig::animationType),
					Vec3d.CODEC.fieldOf("offset").forGetter(AnimationConfig::offset),
					Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(AnimationConfig::scale),
					Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("parameters", Map.of())
							.xmap(m -> (Map<String, Object>)(Map<?, ?>)m, m -> (Map<String, String>)(Map<?, ?>)m)
							.forGetter(AnimationConfig::parameters)
			).apply(instance, AnimationConfig::new)
	);
}

record RecipeConfig(
		ResourceLocation recipeType,
		long baseEnergyUsage,
		long maxEnergyUsage,
		int baseProcessingTime
)
{
	public static final Codec<RecipeConfig> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					ResourceLocation.CODEC.fieldOf("recipe_type").forGetter(RecipeConfig::recipeType),
					Codec.LONG.fieldOf("base_energy_usage").forGetter(RecipeConfig::baseEnergyUsage),
					Codec.LONG.fieldOf("max_energy_usage").forGetter(RecipeConfig::maxEnergyUsage),
					Codec.INT.optionalFieldOf("base_processing_time", 200).forGetter(RecipeConfig::baseProcessingTime)
			).apply(instance, RecipeConfig::new)
	);
}