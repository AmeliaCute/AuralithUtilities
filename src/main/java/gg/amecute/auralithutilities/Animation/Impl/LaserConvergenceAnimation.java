package gg.amecute.auralithutilities.Animation.Impl;

import gg.amecute.auralithutilities.Animation.AnimationSystem;
import gg.amecute.auralithutilities.Entity.BlackHoleEntity;
import gg.amecute.auralithutilities.Registries.AuralithEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.UUID;

public class LaserConvergenceAnimation extends AnimationSystem {

	private BlackHoleEntity blackHole;
	private final float maxSize;
	private final int interiorColor;
	private final int outlineColor;

	private static final float LASER_START = 0.0f;
	private static final float LASER_END = 0.5f;
	private static final float SPHERE_START = 0.5f;
	private static final float SPHERE_END = 1.0f;
	private static final float LASER_PROGRESS_SCALE = 1.0f / LASER_END;

	private static final int NUM_LASERS = 4;
	private static final float LASER_CIRCLE_RADIUS = 6.0f;
	private static final int PARTICLES_PER_LASER = 50;
	private static final int CENTER_PARTICLE_MAX = 15;

	private static final float[] LASER_COS = new float[NUM_LASERS];
	private static final float[] LASER_SIN = new float[NUM_LASERS];
	private static final double[] LASER_START_X = new double[NUM_LASERS];
	private static final double[] LASER_START_Z = new double[NUM_LASERS];

	private static final double PARTICLE_DELTA_SCALE = 1.0 / PARTICLES_PER_LASER;

	private long randomSeed = System.nanoTime();

	private static final int EASE_CACHE_SIZE = 101;
	private static final float[] EASE_CACHE = new float[EASE_CACHE_SIZE];

	static
	{
		final double angleStep = 2.0 * Math.PI / NUM_LASERS;
		for (int i = 0; i < NUM_LASERS; i++)
		{
			final double angle = i * angleStep;
			LASER_COS[i] = (float) Math.cos(angle);
			LASER_SIN[i] = (float) Math.sin(angle);
		}

		final float p = 0.3f;
		final float pDiv4 = p * 0.25f;
		final float piDiv = (float) (2.0 * Math.PI / p);

		for (int i = 0; i < EASE_CACHE_SIZE; i++)
		{
			final float t = i / (float) (EASE_CACHE_SIZE - 1);
			if (t == 0.0f || t == 1.0f) {
				EASE_CACHE[i] = t;
			} else
			{
				EASE_CACHE[i] = (float) (Math.pow(2.0, -10.0 * t) *
						Math.sin((t - pDiv4) * piDiv) + 1.0);
			}
		}
	}

	private final Vector3f outlineRGB;

	private final double originX;
	private final double originY;
	private final double originZ;

	public LaserConvergenceAnimation(Level level, Vec3 position, float maxSize)
	{
		this(level, position, maxSize, 0xFF000000, 0xFFFFFFFF);
	}

	public LaserConvergenceAnimation(Level level, Vec3 position, float maxSize, int interiorColor, int outlineColor)
	{
		super(level, position);
		this.maxSize = maxSize;
		this.interiorColor = interiorColor;
		this.outlineColor = outlineColor;

		this.originX = position.x;
		this.originY = position.y;
		this.originZ = position.z;

		for (int i = 0; i < NUM_LASERS; i++)
		{
			LASER_START_X[i] = originX + LASER_COS[i] * LASER_CIRCLE_RADIUS;
			LASER_START_Z[i] = originZ + LASER_SIN[i] * LASER_CIRCLE_RADIUS;
		}

		final BlackHoleEntity.Color color = new BlackHoleEntity.Color(outlineColor);
		this.outlineRGB = new Vector3f(color.rf(), color.gf(), color.bf());
	}

	@Override
	public void startAnimation()
	{
		blackHole = null;
	}

	@Override
	public void updateAnimation(float progress)
	{
		progress = Math.max(0.0f, Math.min(1.0f, progress));

		if (progress < LASER_END) {
			updateLasers(progress);

			if (blackHole != null && blackHole.isAlive())
			{
				blackHole.remove(Entity.RemovalReason.DISCARDED);
				blackHole = null;
			}

		} else {
			if (blackHole == null || !blackHole.isAlive())
			{
				spawnBlackHole();
			}

			if (blackHole != null && blackHole.isAlive())
			{
				final float sphereProgress = (progress - SPHERE_START) / (SPHERE_END - SPHERE_START);
				final float currentSize = easeOutElasticCached(sphereProgress) * maxSize;
				blackHole.setSize(currentSize);
				blackHole.setPos(origin);
			}
		}
	}

	@Override
	public void stopAnimation()
	{
		if (blackHole != null && blackHole.isAlive())
		{
			blackHole.remove(Entity.RemovalReason.DISCARDED);
		}
	}

	@Override
	public void rebindEntity(UUID entityUuid)
	{
		if (level == null || entityUuid == null) return;
		this.uuid = entityUuid;

		level.getEntitiesOfClass(BlackHoleEntity.class, new AABB(origin, origin).inflate(20))
				.stream()
				.filter(e -> e.getUUID().equals(entityUuid))
				.findFirst()
				.ifPresent(e -> blackHole = e);
	}

	private void updateLasers(float progress) {
		if (!level.isClientSide) return;

		final float laserProgress = progress * LASER_PROGRESS_SCALE;
		final float intensity = laserProgress;

		// Render all laser beams
		for (int i = 0; i < NUM_LASERS; i++) {
			renderLaserBeam(i, laserProgress, intensity);
		}

		if (laserProgress > 0.5f) {
			renderCenterGlow(laserProgress);
		}
	}

	private void renderLaserBeam(int laserIndex, float progress, float intensity)
	{
		final double startX = LASER_START_X[laserIndex];
		final double startZ = LASER_START_Z[laserIndex];

		final double currentX = startX + (originX - startX) * progress;
		final double currentZ = startZ + (originZ - startZ) * progress;

		final double dx = (currentX - startX) * PARTICLE_DELTA_SCALE;
		final double dz = (currentZ - startZ) * PARTICLE_DELTA_SCALE;

		for (int p = 0; p < PARTICLES_PER_LASER; p++)
		{
			final double px = startX + dx * p;
			final double pz = startZ + dz * p;

			level.addParticle(
					ParticleTypes.FIREWORK,
					px, originY, pz,
					0, 0, 0
			);
		}
	}

	private void renderCenterGlow(float progress)
	{
		final float centerGlow = (progress - 0.5f) * 2.0f;
		final int particleCount = (int)(centerGlow * CENTER_PARTICLE_MAX);

		for (int i = 0; i < particleCount; i++)
		{
			final double offsetX = (fastRandom() - 0.5) * 0.5;
			final double offsetY = (fastRandom() - 0.5) * 0.5;
			final double offsetZ = (fastRandom() - 0.5) * 0.5;

			level.addParticle(
					ParticleTypes.FIREWORK,
					originX + offsetX,
					originY + offsetY,
					originZ + offsetZ,
					0, 0, 0
			);
		}
	}

	private void spawnBlackHole() {
		blackHole = new BlackHoleEntity(AuralithEntities.BLACK_HOLE.get(), level);
		blackHole.setPos(origin);
		blackHole.setSize(0.1f);
		blackHole.setInteriorColor(interiorColor);
		blackHole.setOutlineColor(outlineColor);
		blackHole.setControlled(true);

		level.addFreshEntity(blackHole);
		this.uuid = blackHole.getUUID();
	}

	private static float easeOutElasticCached(float t) {
		if (t <= 0.0f) return 0.0f;
		if (t >= 1.0f) return 1.0f;

		final int index = (int)(t * (EASE_CACHE_SIZE - 1) + 0.5f);
		return EASE_CACHE[index];
	}

	private double fastRandom() {
		randomSeed ^= (randomSeed << 21);
		randomSeed ^= (randomSeed >>> 35);
		randomSeed ^= (randomSeed << 4);
		return (randomSeed & 0x7FFFFFFFFFFFFFFFL) / (double) 0x7FFFFFFFFFFFFFFFL;
	}
}