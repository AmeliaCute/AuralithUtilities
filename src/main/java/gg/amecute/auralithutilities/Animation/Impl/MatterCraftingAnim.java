package gg.amecute.auralithutilities.Animation.Impl;

import gg.amecute.auralithutilities.Animation.AnimationSystem;
import gg.amecute.auralithutilities.Entity.BlackHoleEntity;
import gg.amecute.auralithutilities.Registries.AuralithEntities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class MatterCraftingAnim extends AnimationSystem
{
    private BlackHoleEntity matterEntity;

    private final float craftingSize;

    private static final float GROW_END = 0.5f;
    private static final float STABLE_END = 0.8f;
    private static final float SHRINK_START = 0.8f;
    private static final float SHRINK_END = 1.0f;

    public MatterCraftingAnim(Level level, Vec3 position, float craftingSize)
    {
        super(level, position);
        this.craftingSize = craftingSize;
    }

    @Override
    public void startAnimation()
    {
        matterEntity = new BlackHoleEntity(AuralithEntities.BLACK_HOLE.get(), this.level);
        matterEntity.setPos(this.origin);
        matterEntity.setSize(0.0f);

        level.addFreshEntity(matterEntity);
    }

    @Override
    public void updateAnimation(float progress)
    {
        if (matterEntity == null || !matterEntity.isAlive()) return;

        float size = calculateSize(progress);
        matterEntity.setSize(size);

        matterEntity.setPos(this.origin);
    }

    @Override
    public void stopAnimation()
    {
        matterEntity.remove(Entity.RemovalReason.DISCARDED);
    }

    private float calculateSize(float progress)
    {
        progress = Math.max(0.0f, Math.min(1.0f, progress));

        if (progress <= GROW_END)
        {
            float phaseProgress = progress / GROW_END;
            return easeInOut(phaseProgress) * craftingSize;

        } else if (progress <= STABLE_END)
        {
            return craftingSize;

        } else
        {
            float phaseProgress = (progress - SHRINK_START) / (SHRINK_END - SHRINK_START);
            return craftingSize * (1.0f - easeInOut(phaseProgress));
        }
    }

    private float easeInOut(float t)
    {
        return t < 0.5f ?
                  2.0f * t * t
                : 1.0f - (float) Math.pow(-2.0f * t + 2.0f, 2.0f) / 2.0f;
    }

    @Override
    public void rebindEntity(UUID entityUuid)
    {
        if(level == null || entityUuid == null) return;
        this.uuid = entityUuid;

        level.getEntitiesOfClass(BlackHoleEntity.class, new AABB(origin, origin).inflate(20))
                .stream().filter(e -> e.getUUID().equals(entityUuid))
                .findFirst().ifPresent(e -> matterEntity = e);
    }
}