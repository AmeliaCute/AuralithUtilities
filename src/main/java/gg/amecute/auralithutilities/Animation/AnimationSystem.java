package gg.amecute.auralithutilities.Animation;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class AnimationSystem
{
    protected final Level level;
    protected final Vec3 origin;

    public AnimationSystem(Level level, Vec3 position)
    {
        this.level = level;
        this.origin = position;
    }

    public void startAnimation() {}
    public void stopAnimation() {}
    public void updateAnimation(float tick) {}
}
