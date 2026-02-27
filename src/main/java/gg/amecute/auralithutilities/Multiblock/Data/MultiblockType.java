package gg.amecute.auralithutilities.Multiblock.Data;

import com.mojang.serialization.Codec;

public enum MultiblockType
{
	MACHINE,
	MODIFIER,
	DECORATION;

	public static final Codec<MultiblockType> CODEC = Codec.STRING.xmap(
			s -> MultiblockType.valueOf(s.toUpperCase()),
			Enum::name
	);
}
