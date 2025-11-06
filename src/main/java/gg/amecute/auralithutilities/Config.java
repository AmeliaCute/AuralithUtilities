package gg.amecute.auralithutilities;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;

public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue AURALITH_MAIN_MENU;

    // Pollution basic config
    public static final ModConfigSpec.BooleanValue POLL_ENABLED;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> POLL_ENABLED_DIM;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> POLL_ENABLED_MACHINE;

    public static final ModConfigSpec.ConfigValue<Integer> POLL_TICK_RATE;
    public static final ModConfigSpec.ConfigValue<Float> POLL_MAX_PER_CHUNK;
    public static final ModConfigSpec.ConfigValue<Float> POLL_NATURAL_DECAY_RATE;

    // Starting to get foggy
    public static final ModConfigSpec.ConfigValue<Float> POLL_STAGE_1;
    // Always foggy and starting to make natures decay
    public static final ModConfigSpec.ConfigValue<Float> POLL_STAGE_2;

    // Inhabitable, make the player die after a certain time without mask
    public static final ModConfigSpec.ConfigValue<Float> POLL_STAGE_3;



    static {
        BUILDER.push("Client settings");

        AURALITH_MAIN_MENU = BUILDER.comment("Show Auralith special mod menu").define("showAuralith", false);

        BUILDER.pop();
        BUILDER.push("Server settings");

        POLL_ENABLED = BUILDER.define("pollution.enabled", false);
        POLL_ENABLED_DIM = BUILDER.defineListAllowEmpty("pollution.enabledDimensions", Arrays.asList("minecraft:overworld"), () -> "", obj -> obj instanceof String);
        POLL_ENABLED_MACHINE = BUILDER.defineListAllowEmpty("pollution.enabledMachine",  Arrays.asList("modern_industrialization:*=1f"), () -> "", obj -> obj instanceof String);

        POLL_TICK_RATE = BUILDER.define("pollution.tickRate", 20);
        POLL_MAX_PER_CHUNK = BUILDER.define("pollution.maxPerChunk", 50000f);
        POLL_NATURAL_DECAY_RATE = BUILDER.define("pollution.naturalDecayRate", 0.1f);

        POLL_STAGE_1 = BUILDER.define("pollution.stageFirst", 0.25f);
        POLL_STAGE_2 = BUILDER.define("pollution.stageSecond", 0.5f);
        POLL_STAGE_3 = BUILDER.define("pollution.stageFinal", 0.75f);

        BUILDER.pop();
        SPEC = BUILDER.build();

    }
}
