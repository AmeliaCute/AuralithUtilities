package gg.amecute.auralithutilities.Client.Renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import gg.amecute.auralithutilities.Client.Geo.BlackHoleModel;
import gg.amecute.auralithutilities.Entity.BlackHoleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class BlackHoleEntityRenderer extends GeoEntityRenderer<BlackHoleEntity>
{
    public BlackHoleEntityRenderer(EntityRendererProvider.Context context)
    {
        super(context, new BlackHoleModel());

        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void render(BlackHoleEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float size = entity.getSize();


        poseStack.scale(size, size, size);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
