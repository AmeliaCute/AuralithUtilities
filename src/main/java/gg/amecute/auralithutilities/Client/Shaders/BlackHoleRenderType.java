package gg.amecute.auralithutilities.Client.Shaders;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class BlackHoleRenderType extends RenderType
{
    public BlackHoleRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    private static final RenderType BLACK_HOLE = RenderType.create(
            "black_hole",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(BlackHoleShaders::getBlackHoleShader))
                    .setTextureState(new RenderStateShard.EmptyTextureStateShard( () -> {}, () -> {} ))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
            .createCompositeState(false)
    );

    public static RenderType getBlackHole()
    {
        return BLACK_HOLE;
    }
}
