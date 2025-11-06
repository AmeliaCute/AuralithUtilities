package gg.amecute.auralithutilities.Item;

import gg.amecute.auralithutilities.Entity.BlackHoleEntity;
import gg.amecute.auralithutilities.Registries.AuralithEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class BlackHoleSpawnItem extends Item
{
    private final float size;
    private final boolean stable;
    private final float mass;

    public BlackHoleSpawnItem(Properties properties, float size, boolean stable, float mass)
    {
        super(properties);
        this.size = size;
        this.stable = stable;
        this.mass = mass;
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Level level = context.getLevel();

        if(!level.isClientSide)
        {
            BlockPos pos = context.getClickedPos().above();

            BlackHoleEntity blackHoleEntity = new BlackHoleEntity(AuralithEntities.BLACK_HOLE.get(), level);
            blackHoleEntity.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);

            blackHoleEntity.setSize(this.size);
            blackHoleEntity.setStable(this.stable);

            level.addFreshEntity(blackHoleEntity);
        }

        return InteractionResult.SUCCESS;
    }
}
