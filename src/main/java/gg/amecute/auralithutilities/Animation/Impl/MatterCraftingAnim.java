package gg.amecute.auralithutilities.Animation.Impl;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MatterCraftingAnim extends LaserConvergenceAnimation
{
	public MatterCraftingAnim(Level level, Vec3 position, float maxSize)
	{
		super(level, position, maxSize, 0xFF000000, 0xFFFFFFFF);
	}
}
