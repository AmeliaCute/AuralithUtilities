package gg.amecute.auralithutilities;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue AURALITH_MAIN_MENU;

    static {
        BUILDER.push("Client settings");

        AURALITH_MAIN_MENU = BUILDER.comment("Show Auralith special mod menu").define("showAuralith", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
