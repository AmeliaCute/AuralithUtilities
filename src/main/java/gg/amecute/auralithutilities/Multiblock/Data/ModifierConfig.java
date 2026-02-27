package gg.amecute.auralithutilities.Multiblock.Data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Map;

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
