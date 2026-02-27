package gg.amecute.auralithutilities.Multiblock.Data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

public record AnimationConfig
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
