package gg.amecute.auralithutilities.Multiblock.Data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MultiblockValidator
{

	public record ValidationResult(
			boolean valid,
			@Nullable BlockPos controllerPos,
			@Nullable Direction facing,
			List<String> errors,
			Map<BlockPos, BlockDefinition> matchedBlocks
	)
	{
		public static ValidationResult success(BlockPos controller, Direction facing, Map<BlockPos, BlockDefinition> blocks) { return new ValidationResult(true, controller, facing, List.of(), blocks); }

		public static ValidationResult failure(String... errors) { return new ValidationResult(false, null, null, Arrays.asList(errors), Map.of()); }
	}

	public static ValidationResult validate(Level level, BlockPos origin, MultiblockStructure structure, Direction facing)
	{
		List<String> errors = new ArrayList<>();
		Map<BlockPos, BlockDefinition> matchedBlocks = new HashMap<>();

		Rotation rotation = Rotation.fromDirection(facing);

		for (int y = 0; y < structure.layers().size(); ++y)
		{
			List<String> layer = structure.layers().get(y);

			for (int z = 0; z < layer.size(); ++z)
			{
				String row = layer.get(z);

				for (int x = 0; x < row.length(); ++x)
				{
					char key = row.charAt(x);
					if (key == ' ') continue;

					BlockDefinition expected = structure.palette().get(key);
					if (expected == null)
					{
						errors.add("Unknown palette key: " + key);
						continue;
					}

					BlockPos localPos = new BlockPos(x, y, z);
					BlockPos worldPos = rotation.rotate(localPos, structure.size()).offset(origin);

					BlockState actualState = level.getBlockState(worldPos);

					if (!actualState.is(expected.getBlock()))
					{
						errors.add(String.format(
								"Block mismatch at %s: expected %s, found %s",
								worldPos,
								expected.blockId(),
								actualState.getBlock()
						));
					}
					else
					{
						matchedBlocks.put(worldPos, expected);
					}
				}
			}
		}

		if (!errors.isEmpty()) return ValidationResult.failure(errors.toArray(new String[0]));

		Optional<BlockPos> controllerLocal = structure.findController();
		if (controllerLocal.isEmpty()) return ValidationResult.failure("No controller found in structure");

		BlockPos controllerWorld =
				rotation.rotate(controllerLocal.get(), structure.size()).offset(origin);

		return ValidationResult.success(controllerWorld, facing, matchedBlocks);
	}



	public static ValidationResult detectStructure(Level level, BlockPos pos, MultiblockStructure structure)
	{
		for (Direction facing : new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST})
		{
			ValidationResult result = validate(level, pos, structure, facing);
			if (result.valid()) return result;
		}

		return ValidationResult.failure("Structure not found in any rotation");
	}

	public static List<DetectedModifier> detectModifiers(Level level, BlockPos machineOrigin, Direction machineFacing, MultiblockStructure machineStructure, List<MultiblockStructure> possibleModifiers)
	{
		List<DetectedModifier> detected = new ArrayList<>();

		for (MultiblockStructure modifier : possibleModifiers)
		{
			if (modifier.modifierConfig().isEmpty()) continue;

			ModifierConfig config = modifier.modifierConfig().get();

			for (ModifierConfig.Direction dir : config.allowedDirections())
			{
				Direction mcDir = toMinecraftDirection(dir);

				BlockPos searchOrigin = calculateModifierOffset(
						machineOrigin,
						machineFacing,
						machineStructure.size(),
						modifier.size(),
						mcDir
				);

				ValidationResult result = validate(level, searchOrigin, modifier, mcDir);

				if (result.valid()) detected.add(new DetectedModifier(modifier, searchOrigin, result.facing(), dir));
			}
		}

		return detected;
	}

	private static BlockPos calculateModifierOffset
	(
			BlockPos machineOrigin,
			Direction machineFacing,
			Vec3i machineSize,
			Vec3i modifierSize,
			Direction modifierDirection
	)
	{
		int offsetX = 0, offsetZ = 0;

		switch (modifierDirection)
		{
			case NORTH -> offsetZ = -modifierSize.z();
			case SOUTH -> offsetZ = machineSize.z();
			case WEST -> offsetX = -modifierSize.x();
			case EAST -> offsetX = machineSize.x();
		}

		return machineOrigin.offset(offsetX, 0, offsetZ);
	}

	private static Direction toMinecraftDirection(ModifierConfig.Direction dir)
	{
		return switch (dir)
		{
			case NORTH -> Direction.NORTH;
			case SOUTH -> Direction.SOUTH;
			case EAST -> Direction.EAST;
			case WEST -> Direction.WEST;
			case UP -> Direction.UP;
			case DOWN -> Direction.DOWN;
		};
	}

	public record DetectedModifier
	(
			MultiblockStructure structure,
			BlockPos origin,
			Direction facing,
			ModifierConfig.Direction attachmentSide
	)
	{
		public Map<String, Double> calculateModifiers()
		{
			return structure.modifierConfig()
					.map(ModifierConfig::modifiers)
					.orElse(Map.of());
		}
	}

	private static class Rotation
	{
		private final int[][] matrix;

		private Rotation(int[][] matrix)
		{
			this.matrix = matrix;
		}

		public static Rotation fromDirection(Direction facing)
		{
			return switch (facing)
			{
				case NORTH -> new Rotation(new int[][]{{1, 0}, {0, 1}});
				case EAST -> new Rotation(new int[][]{{0, -1}, {1, 0}});
				case SOUTH -> new Rotation(new int[][]{{-1, 0}, {0, -1}});
				case WEST -> new Rotation(new int[][]{{0, 1}, {-1, 0}});
				default -> new Rotation(new int[][]{{1, 0}, {0, 1}});
			};
		}

		public BlockPos rotate(BlockPos pos, Vec3i size)
		{
			int x = pos.getX();
			int z = pos.getZ();

			int newX = matrix[0][0] * x + matrix[0][1] * z;
			int newZ = matrix[1][0] * x + matrix[1][1] * z;

			if (newX < 0) newX += size.x() - 1;
			if (newZ < 0) newZ += size.z() - 1;

			return new BlockPos(newX, pos.getY(), newZ);
		}
	}
}