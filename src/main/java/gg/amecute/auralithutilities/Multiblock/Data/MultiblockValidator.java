package gg.amecute.auralithutilities.Multiblock.Data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class MultiblockValidator
{
	private static final ThreadLocal<MutableBlockPos> MUTABLE_POS = ThreadLocal.withInitial(MutableBlockPos::new);
	private static final int TYPICAL_ERROR_COUNT = 4;

	private static final Rotation[] ROTATION_CACHE = new Rotation[4];
	static
	{
		ROTATION_CACHE[0] = new Rotation(new int[][]{{1, 0}, {0, 1}});    // NORTH
		ROTATION_CACHE[1] = new Rotation(new int[][]{{0, -1}, {1, 0}});   // EAST
		ROTATION_CACHE[2] = new Rotation(new int[][]{{-1, 0}, {0, -1}});  // SOUTH
		ROTATION_CACHE[3] = new Rotation(new int[][]{{0, 1}, {-1, 0}});   // WEST
	}

	public record ValidationResult(
			boolean valid,
			@Nullable BlockPos controllerPos,
			@Nullable Direction facing,
			List<String> errors,
			Map<BlockPos, BlockDefinition> matchedBlocks
	)
	{
		public static ValidationResult success(BlockPos controller, Direction facing, Map<BlockPos, BlockDefinition> blocks)
		{
			return new ValidationResult(true, controller, facing, Collections.emptyList(), blocks);
		}

		public static ValidationResult failure(String... errors)
		{
			return new ValidationResult(false, null, null, Arrays.asList(errors), Collections.emptyMap());
		}
	}

	public static ValidationResult validate(Level level, BlockPos origin,	MultiblockStructure structure, Direction facing)
	{
		final List<List<String>> layers = structure.layers();
		final Map<Character, BlockDefinition> palette = structure.palette();
		final Vec3i size = structure.size();

		final List<String> errors = new ArrayList<>(TYPICAL_ERROR_COUNT);
		final Map<BlockPos, BlockDefinition> matchedBlocks = new HashMap<>(size.x() * size.y() * size.z());

		final Rotation rotation = getRotation(facing);
		final MutableBlockPos worldPos = MUTABLE_POS.get();
		final StringBuilder errorBuilder = new StringBuilder(64);

		BlockPos controllerWorldPos = null;

		for (int y = 0, yMax = layers.size(); y < yMax; y++)
		{
			final List<String> layer = layers.get(y);

			for (int z = 0, zMax = layer.size(); z < zMax; z++)
			{
				final String row = layer.get(z);

				for (int x = 0, xMax = row.length(); x < xMax; x++)
				{
					final char key = row.charAt(x);

					if (key == ' ') continue;
					final BlockDefinition expected = palette.get(key);

					if (expected == null)
					{
						errors.add("Unknown palette key: " + key);
						continue;
					}

					rotation.rotate(x, y, z, size, worldPos);
					worldPos.move(origin.getX(), origin.getY(), origin.getZ());

					final BlockState actualState = level.getBlockState(worldPos.immutable());

					if (!actualState.is(expected.getBlock()))
					{
						errorBuilder.setLength(0);
						errorBuilder.append("Block mismatch at ")
								.append(worldPos.getX()).append(',')
								.append(worldPos.getY()).append(',')
								.append(worldPos.getZ())
								.append(": expected ").append(expected.blockId())
								.append(", found ").append(actualState.getBlock());
						errors.add(errorBuilder.toString());
					} else
					{
						matchedBlocks.put(worldPos.immutable(), expected);

						if (expected.isController()) controllerWorldPos = worldPos.immutable();
					}
				}
			}
		}

		if (!errors.isEmpty()) return ValidationResult.failure(errors.toArray(new String[0]));
		if (controllerWorldPos == null) return ValidationResult.failure("No controller found in structure");

		return ValidationResult.success(controllerWorldPos, facing, matchedBlocks);
	}

	public static ValidationResult detectStructure(Level level, BlockPos pos, MultiblockStructure structure)
	{
		for (Direction facing : COMMON_FACINGS)
		{
			final ValidationResult result = validate(level, pos, structure, facing);
			if (result.valid()) return result;
		}

		return ValidationResult.failure("Structure not found in any rotation");
	}

	private static final Direction[] COMMON_FACINGS =
	{
			Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST
	};

	public static List<DetectedModifier> detectModifiers(Level level, BlockPos machineOrigin, Direction machineFacing, MultiblockStructure machineStructure, List<MultiblockStructure> possibleModifiers)
	{

		final List<DetectedModifier> detected = new ArrayList<>(possibleModifiers.size());
		final MutableBlockPos searchOrigin = new MutableBlockPos();

		for (int i = 0, size = possibleModifiers.size(); i < size; i++)
		{
			final MultiblockStructure modifier = possibleModifiers.get(i);

			if (modifier.modifierConfig().isEmpty()) continue;

			final ModifierConfig config = modifier.modifierConfig().get();
			final List<ModifierConfig.Direction> allowedDirs = config.allowedDirections();

			for (int j = 0, dirCount = allowedDirs.size(); j < dirCount; j++)
			{
				final ModifierConfig.Direction dir = allowedDirs.get(j);
				final Direction mcDir = toMinecraftDirection(dir);

				calculateModifierOffset(
						machineOrigin,
						machineFacing,
						machineStructure.size(),
						modifier.size(),
						mcDir,
						searchOrigin
				);

				final ValidationResult result = validate(level, searchOrigin.immutable(), modifier, mcDir);
				if (result.valid()) detected.add(new DetectedModifier(modifier, searchOrigin.immutable(), result.facing(), dir));
			}
		}

		return detected;
	}

	private static void calculateModifierOffset(
			BlockPos machineOrigin,
			Direction machineFacing,
			Vec3i machineSize,
			Vec3i modifierSize,
			Direction modifierDirection,
			MutableBlockPos output
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

		output.set(
				machineOrigin.getX() + offsetX,
				machineOrigin.getY(),
				machineOrigin.getZ() + offsetZ
		);
	}

	private static Direction toMinecraftDirection(ModifierConfig.Direction dir) {
		return DIRECTION_MAPPING[dir.ordinal()];
	}

	private static final Direction[] DIRECTION_MAPPING =
	{
			Direction.NORTH, Direction.SOUTH, Direction.EAST,
			Direction.WEST, Direction.UP, Direction.DOWN
	};

	public record DetectedModifier(
			MultiblockStructure structure,
			BlockPos origin,
			Direction facing,
			ModifierConfig.Direction attachmentSide
	) {
		public Map<String, Double> calculateModifiers()
		{
			return structure.modifierConfig()
					.map(ModifierConfig::modifiers)
					.orElse(Collections.emptyMap());
		}
	}

	private static final class Rotation
	{
		private final int[][] matrix;

		Rotation(int[][] matrix)
		{
			this.matrix = matrix;
		}

		void rotate(int x, int y, int z, Vec3i size, MutableBlockPos output)
		{
			int newX = matrix[0][0] * x + matrix[0][1] * z;
			int newZ = matrix[1][0] * x + matrix[1][1] * z;

			if (newX < 0) newX += size.x() - 1;
			if (newZ < 0) newZ += size.z() - 1;

			output.set(newX, y, newZ);
		}
	}

	private static Rotation getRotation(Direction facing)
	{
		return switch (facing) {
			case NORTH -> ROTATION_CACHE[0];
			case EAST -> ROTATION_CACHE[1];
			case SOUTH -> ROTATION_CACHE[2];
			case WEST -> ROTATION_CACHE[3];
			default -> ROTATION_CACHE[0];
		};
	}

	private static final class MutableBlockPos
	{
		private int x, y, z;

		void set(int x, int y, int z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}

		void move(int dx, int dy, int dz)
		{
			this.x += dx;
			this.y += dy;
			this.z += dz;
		}

		int getX() { return x; }
		int getY() { return y; }
		int getZ() { return z; }

		BlockPos immutable()
		{
			return new BlockPos(x, y, z);
		}
	}
}