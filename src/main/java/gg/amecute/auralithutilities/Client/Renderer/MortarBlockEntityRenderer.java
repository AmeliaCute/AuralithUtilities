package gg.amecute.auralithutilities.Client.Renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import gg.amecute.auralithutilities.Block.Entity.MortarBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class MortarBlockEntityRenderer implements BlockEntityRenderer<MortarBlockEntity> {

  private static final float[][] OFFSETS_1 = {
      { 0.00f, 0.00f }
  };
  private static final float[][] OFFSETS_2 = {
      {-0.07f, -0.05f },
      { 0.07f,  0.05f }
  };
  private static final float[][] OFFSETS_3 = {
      {-0.08f, -0.07f },
      { 0.08f, -0.02f },
      { 0.00f,  0.09f }
  };
  private static final float[][] OFFSETS_4 = {
      {-0.09f, -0.09f },
      { 0.09f, -0.09f },
      {-0.09f,  0.09f },
      { 0.09f,  0.09f }
  };

  public MortarBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

  @Override
  public void render(MortarBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) 
  {

    ItemStack stack = be.getStoredOre();
    if (stack.isEmpty()) return;

    Level level = be.getLevel();

    int light = packedLight;
    if (level != null) {
      BlockPos above = be.getBlockPos().above();
      light = LightTexture.pack(
          level.getBrightness(LightLayer.BLOCK, above),
          level.getBrightness(LightLayer.SKY,   above)
      );
    }

    int count   = stack.getCount();
    float[][] offsets;
    if      (count <= 1)  offsets = OFFSETS_1;
    else if (count <= 4)  offsets = OFFSETS_2;
    else if (count <= 9)  offsets = OFFSETS_3;
    else                  offsets = OFFSETS_4;

    float baseY  = 0.15f;
    float stackH = Math.min(count - 1, 3) * 0.012f;

    for (int i = 0; i < offsets.length; i++) {
      float ox = offsets[i][0];
      float oz = offsets[i][1];
      float oy = baseY + stackH + i * 0.008f;

      poseStack.pushPose();

      poseStack.translate(0.5 + ox, oy, 0.5 + oz);

      poseStack.mulPose(Axis.XP.rotationDegrees(-90f));

      poseStack.scale(0.28f, 0.28f, 0.28f);

      Minecraft.getInstance().getItemRenderer().renderStatic(
          stack,
          ItemDisplayContext.FIXED,
          light,
          OverlayTexture.NO_OVERLAY,
          poseStack,
          buffers,
          level,
          (int) be.getBlockPos().asLong() + i
      );

      poseStack.popPose();
    }
  }

  @Override
  public boolean shouldRenderOffScreen(MortarBlockEntity be) {
    return false;
  }
}