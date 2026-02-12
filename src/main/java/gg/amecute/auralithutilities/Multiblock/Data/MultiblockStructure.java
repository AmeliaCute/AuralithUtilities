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
				String casing,
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
					Codec.STRING.fieldOf("casing").forGetter(MultiblockStructure::casing),
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

