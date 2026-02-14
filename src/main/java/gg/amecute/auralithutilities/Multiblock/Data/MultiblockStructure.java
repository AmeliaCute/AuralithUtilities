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
					ResourceLocation.CODEC.optionalFieldOf("id", ResourceLocation.fromNamespaceAndPath("unknown", "unknown")).forGetter(MultiblockStructure::id),
					Codec.STRING.optionalFieldOf("name", "Unknown").forGetter(MultiblockStructure::name),
					Codec.STRING.optionalFieldOf("casing", "modern_industrialization:steel_machine_casing").forGetter(MultiblockStructure::casing),
					MultiblockType.CODEC.optionalFieldOf("type", MultiblockType.MACHINE).forGetter(MultiblockStructure::type),
					Vec3i.CODEC.optionalFieldOf("size", new Vec3i(3, 3, 3)).forGetter(MultiblockStructure::size),
					Codec.unboundedMap(Codec.STRING.xmap(s -> s.charAt(0), c -> String.valueOf(c)),
							BlockDefinition.CODEC).fieldOf("palette").forGetter(MultiblockStructure::palette),
					Codec.list(Codec.STRING.listOf()).fieldOf("layers").forGetter(MultiblockStructure::layers),
					ModifierConfig.CODEC.optionalFieldOf("modifier_config").forGetter(MultiblockStructure::modifierConfig),
					AnimationConfig.CODEC.optionalFieldOf("animation_config").forGetter(MultiblockStructure::animationConfig),
					RecipeConfig.CODEC.optionalFieldOf("recipe_config", new RecipeConfig(
							ResourceLocation.fromNamespaceAndPath("unknown", "unknown"),
							1024L,
							100000L,
							200
					)).forGetter(MultiblockStructure::recipeConfig)
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

	public static Vec3i calculateSize(List<List<String>> layers)
	{
		if (layers.isEmpty()) return new Vec3i(0, 0, 0);

		int y = layers.size();
		int z = layers.get(0).size();
		int x = layers.get(0).isEmpty() ? 0 : layers.get(0).get(0).length();

		return new Vec3i(x, y, z);
	}

	public static String deriveCasing(Map<Character, BlockDefinition> palette)
	{
		String fallback = "modern_industrialization:steel_machine_casing";

		for (Map.Entry<Character, BlockDefinition> entry : palette.entrySet())
		{
			BlockDefinition def = entry.getValue();
			if (def == null || def.blockId() == null) continue;

			ResourceLocation blockId = def.blockId();
			String path = blockId.getPath();

			if (path.endsWith("_machine_casing") && !path.contains("pipe"))
				return blockId.toString();
		}

		return fallback;
	}

	public static ResourceLocation deriveId(String filename, String modid)
	{
		return ResourceLocation.fromNamespaceAndPath(modid, filename);
	}
}