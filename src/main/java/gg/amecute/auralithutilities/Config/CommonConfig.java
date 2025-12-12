package gg.amecute.auralithutilities.Config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Objects;

public class CommonConfig
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<List<? extends String>> LOG_BREAK_ALLOWED_ITEM;

    static {
        BUILDER.push("Common settings");

        LOG_BREAK_ALLOWED_ITEM = BUILDER.comment("List of additional item registry keys allowed to break logs")
                .defineList("allowed_items",
                        List.of("minecraft:flint"),
                        obj -> {
                            if (obj instanceof String str) {
                                try {
                                    ResourceLocation.parse(str);
                                    return true;
                                } catch (Exception e) {
                                    return false;
                                }
                            }
                            return false;
                        }
                );

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static List<ResourceLocation> getAllowedItem()
    {
        return LOG_BREAK_ALLOWED_ITEM.get().stream()
                .map(str -> {
                    try {
                        return ResourceLocation.parse(str);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }
}