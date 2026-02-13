package gg.amecute.auralithutilities.Animation.Impl;

import gg.amecute.auralithutilities.Animation.AnimationSystem;
import gg.amecute.auralithutilities.Entity.BlackHoleEntity;
import gg.amecute.auralithutilities.Registries.AuralithEntities;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.core.particles.DustParticleOptions;
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

	private static final int NUM_LASERS = 4;
	private static final float LASER_CIRCLE_RADIUS = 6.0f;
	private static final int PARTICLES_PER_LASER = 50; // Increased for smoother lasers
	private static final int CENTER_PARTICLE_MAX = 15; // Max particles at center glow

	private static final float[] LASER_ANGLES = new float[NUM_LASERS];
	private static final float[] LASER_COS = new float[NUM_LASERS];
	private static final float[] LASER_SIN = new float[NUM_LASERS];

	static {
		for (int i = 0; i < NUM_LASERS; i++) {
			LASER_ANGLES[i] = (float) (i * 2 * Math.PI / NUM_LASERS);
			LASER_COS[i] = (float) Math.cos(LASER_ANGLES[i]);
			LASER_SIN[i] = (float) Math.sin(LASER_ANGLES[i]);
		}
	}

	private Vector3f outlineRGB;

	public LaserConvergenceAnimation(Level level, Vec3 position, float maxSize) {
		this(level, position, maxSize, 0xFF000000, 0xFFFFFFFF);
	}

	public LaserConvergenceAnimation(Level level, Vec3 position, float maxSize,
																	 int interiorColor, int outlineColor) {
		super(level, position);
		this.maxSize = maxSize;
		this.interiorColor = interiorColor;
		this.outlineColor = outlineColor;

		BlackHoleEntity.Color color = new BlackHoleEntity.Color(outlineColor);
		this.outlineRGB = new Vector3f(color.rf(), color.gf(), color.bf());
	}

	@Override
	public void startAnimation() {
		blackHole = null;
	}

	@Override
	public void updateAnimation(float progress) {
		progress = Math.max(0.0f, Math.min(1.0f, progress));

		if (progress < LASER_END) {
			updateLasers(progress);

			if (blackHole != null && blackHole.isAlive()) {
				blackHole.remove(Entity.RemovalReason.DISCARDED);
				blackHole = null;
			}

		} else {
			if (blackHole == null || !blackHole.isAlive()) {
				spawnBlackHole();
			}

			if (blackHole != null && blackHole.isAlive()) {
				float sphereProgress = (progress - SPHERE_START) / (SPHERE_END - SPHERE_START);
				float currentSize = easeOutElastic(sphereProgress) * maxSize;
				blackHole.setSize(currentSize);
				blackHole.setPos(origin);
			}
		}
	}

	@Override
	public void stopAnimation() {
		if (blackHole != null && blackHole.isAlive()) {
			blackHole.remove(Entity.RemovalReason.DISCARDED);
		}
	}

	@Override
	public void rebindEntity(UUID entityUuid) {
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

		float laserProgress = progress / LASER_END;
		float intensity = laserProgress;

		for (int i = 0; i < NUM_LASERS; i++) {
			renderLaserBeam(i, laserProgress, intensity);
		}

		if (laserProgress > 0.5f) {
			renderCenterGlow(laserProgress);
		}
	}

	private void renderLaserBeam(int laserIndex, float progress, float intensity) {
		float cosAngle = LASER_COS[laserIndex];
		float sinAngle = LASER_SIN[laserIndex];

		double startX = origin.x + cosAngle * LASER_CIRCLE_RADIUS;
		double startZ = origin.z + sinAngle * LASER_CIRCLE_RADIUS;

		double currentX = startX + (origin.x - startX) * progress;
		double currentZ = startZ + (origin.z - startZ) * progress;

		double dx = (currentX - startX) / PARTICLES_PER_LASER;
		double dz = (currentZ - startZ) / PARTICLES_PER_LASER;

		for (int p = 0; p < PARTICLES_PER_LASER; p++) {
			double px = startX + dx * p;
			double pz = startZ + dz * p;

			level.addParticle(
					ParticleTypes.FIREWORK,
					px, origin.y, pz,
					0, 0, 0
			);
		}
	}

	private void renderCenterGlow(float progress) {
		float centerGlow = (progress - 0.5f) * 2.0f; // 0.0 to 1.0
		int particleCount = (int)(centerGlow * CENTER_PARTICLE_MAX);

		for (int i = 0; i < particleCount; i++) {
			double offsetX = (Math.random() - 0.5) * 0.5;
			double offsetY = (Math.random() - 0.5) * 0.5;
			double offsetZ = (Math.random() - 0.5) * 0.5;

			level.addParticle(
					ParticleTypes.FIREWORK,
					origin.x + offsetX, origin.y + offsetY, origin.z + offsetZ,
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

	private float easeOutElastic(float t) {
		if (t == 0 || t == 1) return t;

		float p = 0.3f;
		return (float) (Math.pow(2, -10 * t) * Math.sin((t - p / 4) * (2 * Math.PI) / p) + 1);
	}
}