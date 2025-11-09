package gg.amecute.auralithutilities.Client.Geo;

import gg.amecute.auralithutilities.AuralithUtilities;
import gg.amecute.auralithutilities.Entity.BlackHoleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

public class BlackHoleModel extends GeoModel<BlackHoleEntity> {

    @Override
    public ResourceLocation getModelResource(BlackHoleEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(AuralithUtilities.MODID, "geo/black_hole.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BlackHoleEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(AuralithUtilities.MODID, "textures/geo/black_hole_entity.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BlackHoleEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(AuralithUtilities.MODID, "animations/black_hole.animation.json");
    }
}
