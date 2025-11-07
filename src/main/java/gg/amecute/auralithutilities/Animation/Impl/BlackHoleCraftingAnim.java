package gg.amecute.auralithutilities.Animation.Impl;

import gg.amecute.auralithutilities.Animation.AnimationSystem;
import gg.amecute.auralithutilities.Entity.BlackHoleEntity;
import gg.amecute.auralithutilities.Registries.AuralithEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BlackHoleCraftingAnim extends AnimationSystem {

    private BlackHoleEntity blackHoleEntity;
    private final float craftingSize;

    private static final float GROW_END = 0.3f;
    private static final float STABLE_START = 0.3f;
    private static final float STABLE_END = 0.7f;
    private static final float SHRINK_START = 0.7f;
    private static final float SHRINK_END = 1.0f;

    public BlackHoleCraftingAnim(Level level, Vec3 position, float craftingSize) {
        super(level, position);
        this.craftingSize = craftingSize;
    }

    @Override
    public void startAnimation()
    {

        blackHoleEntity = new BlackHoleEntity(AuralithEntities.BLACK_HOLE.get(), this.level);
        blackHoleEntity.setPos(this.origin);
        blackHoleEntity.setSize(0.0f);
        blackHoleEntity.setControlled(true);

        level.addFreshEntity(blackHoleEntity);
        System.out.println("[BlackHole] Entity spawned successfully");
    }

    @Override
    public void updateAnimation(float progress) {
        if (blackHoleEntity == null || !blackHoleEntity.isAlive()) {
            System.out.println("[BlackHole] Warning: Entity is null or dead!");
            return;
        }

        float size = calculateSize(progress) * craftingSize;
        blackHoleEntity.setSize(size);

        blackHoleEntity.setPos(this.origin);

    }

    @Override
    public void stopAnimation()
    {
        blackHoleEntity.remove(Entity.RemovalReason.DISCARDED);
    }

    private float calculateSize(float progress) {
        progress = Math.max(0.0f, Math.min(1.0f, progress));

        if (progress <= GROW_END) {
            float phaseProgress = progress / GROW_END;
            return easeInOut(phaseProgress) * craftingSize;

        } else if (progress <= STABLE_END) {
            float phaseProgress = (progress - STABLE_START) / (STABLE_END - STABLE_START);
            float pulse = (float) Math.sin(phaseProgress * Math.PI * 10) * 0.05f;
            return craftingSize + pulse * craftingSize;

        } else {
            float phaseProgress = (progress - SHRINK_START) / (SHRINK_END - SHRINK_START);
            return craftingSize * (1.0f - easeInOut(phaseProgress));
        }
    }

    private float easeInOut(float t) {
        return t < 0.5f
                ? 2.0f * t * t
                : 1.0f - (float) Math.pow(-2.0f * t + 2.0f, 2.0f) / 2.0f;
    }

}