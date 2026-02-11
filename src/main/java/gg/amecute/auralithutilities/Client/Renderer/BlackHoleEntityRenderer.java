package gg.amecute.auralithutilities.Client.Renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import gg.amecute.auralithutilities.Entity.BlackHoleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class BlackHoleEntityRenderer extends EntityRenderer<BlackHoleEntity> {

    private static final int SEGMENTS = 24; // Quality

    public BlackHoleEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BlackHoleEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
    {
        poseStack.pushPose();

        float size = entity.getSize();
        float rotation = entity.getRotation() + partialTick * entity.getRotationSpeed();

        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation * 0.5f));

        BlackHoleEntity.Color interior = new BlackHoleEntity.Color(entity.getInteriorColor());
        BlackHoleEntity.Color outline = new BlackHoleEntity.Color(entity.getOutlineColor());

        renderSphere(poseStack, bufferSource, size, interior.rf(), interior.gf(), interior.bf(), interior.af(),false, 0);

        float outlineSize = size + 0.1f;
        renderSphere(poseStack, bufferSource, outlineSize,outline.rf(), outline.gf(), outline.bf(), outline.af(),true, 15728880);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderSphere(PoseStack poseStack, MultiBufferSource bufferSource, float radius, float r, float g, float b, float a, boolean emissive, int light)
    {
        RenderType renderType = emissive ? createEmissiveRenderType() : createSolidRenderType();

        VertexConsumer consumer = bufferSource.getBuffer(renderType);
        PoseStack.Pose pose = poseStack.last();

        for (int lat = 0; lat < SEGMENTS; lat++)
        {
            float theta1 = lat * (float) Math.PI / SEGMENTS;
            float theta2 = (lat + 1) * (float) Math.PI / SEGMENTS;

            float sinTheta1 = (float) Math.sin(theta1);
            float cosTheta1 = (float) Math.cos(theta1);
            float sinTheta2 = (float) Math.sin(theta2);
            float cosTheta2 = (float) Math.cos(theta2);

            for (int lon = 0; lon <= SEGMENTS; lon++)
            {
                float phi = lon * 2 * (float) Math.PI / SEGMENTS;
                float sinPhi = (float) Math.sin(phi);
                float cosPhi = (float) Math.cos(phi);

                float x1 = radius * sinTheta1 * cosPhi;
                float y1 = radius * cosTheta1;
                float z1 = radius * sinTheta1 * sinPhi;

                float x2 = radius * sinTheta2 * cosPhi;
                float y2 = radius * cosTheta2;
                float z2 = radius * sinTheta2 * sinPhi;

                float nx1 = sinTheta1 * cosPhi;
                float ny1 = cosTheta1;
                float nz1 = sinTheta1 * sinPhi;

                float nx2 = sinTheta2 * cosPhi;
                float ny2 = cosTheta2;
                float nz2 = sinTheta2 * sinPhi;

                vertex(consumer, pose, x1, y1, z1, nx1, ny1, nz1, r, g, b, a, light);
                vertex(consumer, pose, x2, y2, z2, nx2, ny2, nz2, r, g, b, a, light);
            }
        }
    }

    private void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float nx, float ny, float nz, float r, float g, float b, float a, int light)
    {
        consumer.addVertex(pose.pose(), x, y, z)
            .setColor(r, g, b, a)
            .setUv(0, 0)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(pose, nx, ny, nz);
    }

    private RenderType createSolidRenderType()
    {
        return RenderType.entitySolid(getTextureLocation(null));
    }

    private RenderType createEmissiveRenderType() {
        return RenderType.entityTranslucentEmissive(getTextureLocation(null));
    }

    @Override
    public ResourceLocation getTextureLocation(BlackHoleEntity entity)
    {
        return ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
    }
}