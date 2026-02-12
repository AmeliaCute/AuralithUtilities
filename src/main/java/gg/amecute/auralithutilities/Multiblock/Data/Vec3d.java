package gg.amecute.auralithutilities.Multiblock.Data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Vec3d(double x, double y, double z)
{
	public static final Codec<Vec3d> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.DOUBLE.fieldOf("x").forGetter(Vec3d::x),
					Codec.DOUBLE.fieldOf("y").forGetter(Vec3d::y),
					Codec.DOUBLE.fieldOf("z").forGetter(Vec3d::z)
			).apply(instance, Vec3d::new)
	);
}
