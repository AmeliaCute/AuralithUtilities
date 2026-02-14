package gg.amecute.auralithutilities.Multiblock.Data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record RecipeConfig(
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
