package gg.amecute.auralithutilities.Multiblock.Data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record BlockDefinition
		(
				ResourceLocation blockId,
				Optional<List<String>> hatchFlags,
				boolean isController,
				Map<String, String> properties
		)
{
	public static final Codec<BlockDefinition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					ResourceLocation.CODEC.fieldOf("block").forGetter(BlockDefinition::blockId),
					Codec.STRING.listOf().optionalFieldOf("hatch_flags").forGetter(BlockDefinition::hatchFlags),
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
