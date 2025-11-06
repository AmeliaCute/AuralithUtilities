package gg.amecute.auralithutilities.Client.Renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import gg.amecute.auralithutilities.Client.Shaders.BlackHoleRenderType;
import gg.amecute.auralithutilities.Entity.BlackHoleEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import javax.swing.text.html.parser.Entity;

public class BlackHoleEntityRenderer extends EntityRenderer<BlackHoleEntity>
{

    public BlackHoleEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BlackHoleEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        float scale = entity.getSize();
        poseStack.scale(scale,scale,scale);

        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));

        VertexConsumer vertexConsumer = bufferSource.getBuffer(BlackHoleRenderType.getBlackHole());
        renderUVSphere(poseStack, vertexConsumer, packedLight, entity.isStable());

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderUVSphere(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, boolean isStable)
    {
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        int segments = 16;
        int rings = 16;

        for(int ring = 0; ring < rings; ++ring)
        {
            float phi1 = (float) Math.PI * ring / rings;
            float phi2 = (float) Math.PI * (ring + 1) / rings;

            for(int seg = 0; seg < segments; ++seg)
            {
                float theta1 = 2.0f * (float) Math.PI * (seg + 0.5f) / segments;
                float theta2 = 2.0f * (float) Math.PI * (seg + 1.5f) / segments;

                float x1 = (float) (Math.sin(phi1) * Math.cos(theta1));
                float y1 = (float) Math.cos(phi1);
                float z1 = (float) (Math.sin(phi1) * Math.sin(theta1));

                float x2 = (float) (Math.sin(phi1) * Math.cos(theta2));
                float y2 = (float) Math.cos(phi1);
                float z2 = (float) (Math.sin(phi1) * Math.sin(theta2));

                float x3 = (float) (Math.sin(phi2) * Math.cos(theta2));
                float y3 = (float) Math.cos(phi2);
                float z3 = (float) (Math.sin(phi2) * Math.sin(theta2));

                float x4 = (float) (Math.sin(phi2) * Math.cos(theta1));
                float y4 = (float) Math.cos(phi2);
                float z4 = (float) (Math.sin(phi2) * Math.sin(theta1));

                float u1 = (float) seg / segments;
                float u2 = (float) (seg + 1) / segments;
                float v1 = (float) ring / rings;
                float v2 = (float) (ring + 1) / rings;

                addVertex(vertexConsumer, pose, normal, x1, y1, z1, u1, v1, packedLight);
                addVertex(vertexConsumer, pose, normal, x2, y2, z2, u2, v1, packedLight);
                addVertex(vertexConsumer, pose, normal, x3, y3, z3, u2, v2, packedLight);
                addVertex(vertexConsumer, pose, normal, x4, y4, z4, u1, v2, packedLight);
            }
        }
    }

    private void addVertex(VertexConsumer vertexConsumer, Matrix4f pose, Matrix3f normal, float x, float y, float z, float u, float v, int packedLight)
    {
        Vector3f normalC = new Vector3f(x,y,z).mul(normal).normalize();

        vertexConsumer.addVertex(pose, x, y, z)
                .setColor(0xFFFFFFFF)
                .setUv(u,v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(normalC.x, normalC.y, normalC.z);
    }

    @Override
    public ResourceLocation getTextureLocation(BlackHoleEntity blackHoleEntity) {
        return ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    }
}
