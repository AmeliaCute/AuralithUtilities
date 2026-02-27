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

public final class BlackHoleEntityRenderer extends EntityRenderer<BlackHoleEntity>
{

    private static final int SEGMENTS = 24;
    private static final int VERTEX_COUNT = (SEGMENTS + 1) * SEGMENTS * 2;

    private static final SphereGeometry GEOMETRY = new SphereGeometry(SEGMENTS);

    private static RenderType cachedSolidType;
    private static RenderType cachedEmissiveType;

    public BlackHoleEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BlackHoleEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        poseStack.pushPose();

        final float size = entity.getSize();
        final float rotation = entity.getRotation() + partialTick * entity.getRotationSpeed();

        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation * 0.5f));

        final BlackHoleEntity.Color interior = new BlackHoleEntity.Color(entity.getInteriorColor());
        final BlackHoleEntity.Color outline = new BlackHoleEntity.Color(entity.getOutlineColor());

        renderSphereOptimized(poseStack, bufferSource, size,
            interior.rf(), interior.gf(), interior.bf(), interior.af(), false, 0);

        final float outlineSize = size + 0.1f;
        renderSphereOptimized(poseStack, bufferSource, outlineSize, outline.rf(), outline.gf(), outline.bf(), outline.af(), true, 15728880);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderSphereOptimized(PoseStack poseStack, MultiBufferSource bufferSource, float radius, float r, float g, float b, float a, boolean emissive, int light)
    {
        final RenderType renderType = emissive ? getCachedEmissiveType() : getCachedSolidType();
        final VertexConsumer consumer = bufferSource.getBuffer(renderType);
        final PoseStack.Pose pose = poseStack.last();

        final SphereGeometry geo = GEOMETRY;
        final int segments = geo.segments;

        int vertexIndex = 0;
        for (int lat = 0; lat < segments; lat++)
        {
            for (int lon = 0; lon <= segments; lon++)
            {
                {
                    final float x = geo.positions[vertexIndex];
                    final float y = geo.positions[vertexIndex + 1];
                    final float z = geo.positions[vertexIndex + 2];
                    final float nx = geo.normals[vertexIndex];
                    final float ny = geo.normals[vertexIndex + 1];
                    final float nz = geo.normals[vertexIndex + 2];

                    consumer.addVertex(pose.pose(), x * radius, y * radius, z * radius)
                        .setColor(r, g, b, a)
                        .setUv(0, 0)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(light)
                        .setNormal(pose, nx, ny, nz);

                    vertexIndex += 3;
                }

                {
                    final float x = geo.positions[vertexIndex];
                    final float y = geo.positions[vertexIndex + 1];
                    final float z = geo.positions[vertexIndex + 2];
                    final float nx = geo.normals[vertexIndex];
                    final float ny = geo.normals[vertexIndex + 1];
                    final float nz = geo.normals[vertexIndex + 2];

                    consumer.addVertex(pose.pose(), x * radius, y * radius, z * radius)
                        .setColor(r, g, b, a)
                        .setUv(0, 0)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(light)
                        .setNormal(pose, nx, ny, nz);

                    vertexIndex += 3;
                }
            }
        }
    }

    private RenderType getCachedSolidType()
    {
        if (cachedSolidType == null) cachedSolidType = RenderType.entitySolid(getTextureLocation(null));
        return cachedSolidType;
    }

    private RenderType getCachedEmissiveType() {
        if (cachedEmissiveType == null) cachedEmissiveType = RenderType.entityTranslucentEmissive(getTextureLocation(null));
        return cachedEmissiveType;
    }

    @Override
    public ResourceLocation getTextureLocation(BlackHoleEntity entity)
    {
        return ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
    }

    private static final class SphereGeometry {
        final int segments;
        final float[] positions;
        final float[] normals;

        SphereGeometry(int segments)
        {
            this.segments = segments;

            final int vertexCount = (segments + 1) * segments * 2;
            this.positions = new float[vertexCount * 3];
            this.normals = new float[vertexCount * 3];

            int idx = 0;
            for (int lat = 0; lat < segments; lat++)
            {
                final float theta1 = lat * (float) Math.PI / segments;
                final float theta2 = (lat + 1) * (float) Math.PI / segments;

                final float sinTheta1 = (float) Math.sin(theta1);
                final float cosTheta1 = (float) Math.cos(theta1);
                final float sinTheta2 = (float) Math.sin(theta2);
                final float cosTheta2 = (float) Math.cos(theta2);

                for (int lon = 0; lon <= segments; lon++)
                {
                    final float phi = lon * 2.0f * (float) Math.PI / segments;
                    final float sinPhi = (float) Math.sin(phi);
                    final float cosPhi = (float) Math.cos(phi);

                    {
                        final float nx = sinTheta1 * cosPhi;
                        final float ny = cosTheta1;
                        final float nz = sinTheta1 * sinPhi;

                        positions[idx] = nx;
                        positions[idx + 1] = ny;
                        positions[idx + 2] = nz;
                        normals[idx] = nx;
                        normals[idx + 1] = ny;
                        normals[idx + 2] = nz;
                        idx += 3;
                    }

                    {
                        final float nx = sinTheta2 * cosPhi;
                        final float ny = cosTheta2;
                        final float nz = sinTheta2 * sinPhi;

                        positions[idx] = nx;
                        positions[idx + 1] = ny;
                        positions[idx + 2] = nz;
                        normals[idx] = nx;
                        normals[idx + 1] = ny;
                        normals[idx + 2] = nz;
                        idx += 3;
                    }
                }
            }
        }
    }
}