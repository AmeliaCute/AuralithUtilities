package gg.amecute.auralithutilities.Client.Shaders;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import gg.amecute.auralithutilities.AuralithUtilities;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

@EventBusSubscriber(modid = AuralithUtilities.MODID, value = Dist.CLIENT)
public class BlackHoleShaders
{
    private static ShaderInstance blackHoleShader;

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException
    {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(AuralithUtilities.MODID, "rendertype_black_hole"),
                        DefaultVertexFormat.NEW_ENTITY
                ),
                shader -> blackHoleShader = shader
        );
    }

    public static ShaderInstance getBlackHoleShader()
    {
        return blackHoleShader;
    }
}
