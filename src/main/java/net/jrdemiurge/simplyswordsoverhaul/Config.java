package net.jrdemiurge.simplyswordsoverhaul;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLE_EMBERLASH_CHANGES = BUILDER
            .comment("Emberlash: Toggle changes made by the mod (true = changes enabled, false = disabled)")
            .define("enableEmberlashChanges", true);

    private static final ModConfigSpec.BooleanValue ENABLE_MOLTEN_EDGE_CHANGES = BUILDER
            .comment("Molten Edge: Toggle changes made by the mod (true = changes enabled, false = disabled)")
            .define("enableMoltenEdgeChanges", true);

    private static final ModConfigSpec.BooleanValue ENABLE_SHADOWSTING_CHANGES = BUILDER
            .comment("Shadowsting: Toggle changes made by the mod (true = changes enabled, false = disabled)")
            .define("enableShadowstingChanges", true);

    private static final ModConfigSpec.BooleanValue ENABLE_SOUL_PYRE_CHANGES = BUILDER
            .comment("Soul Pyre: Toggle changes made by the mod (true = changes enabled, false = disabled)")
            .define("enableSoulPyreChanges", true);

    private static final ModConfigSpec.BooleanValue ENABLE_SOULRENDER_CHANGES = BUILDER
            .comment("Soulrender: Toggle changes made by the mod (true = changes enabled, false = disabled)")
            .define("enableSoulrenderChanges", true);

    private static final ModConfigSpec.BooleanValue ENABLE_STARS_EDGE_CHANGES = BUILDER
            .comment("Star's Edge: Toggle changes made by the mod (true = changes enabled, false = disabled)")
            .define("enableStarsEdgeChanges", true);

    private static final ModConfigSpec.BooleanValue ENABLE_STORMS_EDGE_CHANGES = BUILDER
            .comment("Storm's Edge: Toggle changes made by the mod (true = changes enabled, false = disabled)")
            .define("enableStormsEdgeChanges", true);

    private static final ModConfigSpec.BooleanValue ENABLE_WATCHER_CHANGES = BUILDER
            .comment("Watcher: Toggle changes made by the mod (true = changes enabled, false = disabled)")
            .define("enableWatcherChanges", true);

    private static final ModConfigSpec.BooleanValue ENABLE_WHISPERWIND_CHANGES = BUILDER
            .comment("Whisperwind: Toggle changes made by the mod (true = changes enabled, false = disabled)")
            .define("enableWhisperwindChanges", true);

    private static final ModConfigSpec.DoubleValue EMBERLASH_SMOULDER_DAMAGE_MULTIPLIER = BUILDER
            .comment("Emberlash: Bonus damage dealt per stack of Smouldering (default: 0.75)")
            .defineInRange("emberlashSmoulderDamageMultiplier", 0.75, 0.0, 10.0);

    private static final ModConfigSpec.IntValue EMBERLASH_DASH_DISTANCE = BUILDER
            .comment("Emberlash: Distance in blocks for the dash ability (default: 12)")
            .defineInRange("emberlashDashDistance", 12, 1, 12);

    private static final ModConfigSpec.IntValue EMBERLASH_HEAL_PERCENTAGE = BUILDER
            .comment("Emberlash: Percentage of Max HP restored after dashing (default: 10%)")
            .defineInRange("emberlashHealPercentage", 10, 0, 100);

    private static final ModConfigSpec.IntValue EMBERLASH_MAX_SMOULDER_LEVEL = BUILDER
            .comment("Emberlash: Maximum level of Smouldering that can be applied to a target (default: 254)")
            .defineInRange("emberlashMaxSmoulderLevel", 254, 1, 254);

    private static final ModConfigSpec.IntValue EMBERLASH_COOLDOWN_TICKS = BUILDER
            .comment("Emberlash: Cooldown of the dash ability in ticks (default: 80 ticks = 4 seconds)")
            .defineInRange("emberlashCooldownTicks", 80, 1, 50000);

    private static final ModConfigSpec.BooleanValue EMBERLASH_IGNORE_DOWNWARD_ANGLE_ON_GROUND = BUILDER
            .comment("If true, dashing on the ground will ignore the downward angle and retain full dash length.",
                    "If false, dashing on the ground with a downward viewing angle will reduce the dash distance, ",
                    "as part of the dash impulse will be directed downward.")
            .define("emberlashIgnoreDownwardAngleOnGround", true);

    private static final ModConfigSpec.IntValue SOULRENDER_MAX_SLOWNESS_LEVEL = BUILDER
            .comment("Soulrender: Maximum level of Slowness that can be applied (default: 3)")
            .defineInRange("soulrenderMaxSlownessLevel", 3, 1, 254);

    private static final ModConfigSpec.IntValue SOULRENDER_MAX_WEAKNESS_LEVEL = BUILDER
            .comment("Soulrender: Maximum level of Weakness that can be applied (default: 2)")
            .defineInRange("soulrenderMaxWeaknessLevel", 2, 1, 254);

    private static final ModConfigSpec.IntValue SOULRENDER_MAX_UNLUCK_LEVEL = BUILDER
            .comment("Soulrender: Maximum level of Unluck that can be applied (default: 254)")
            .defineInRange("soulrenderMaxUnluckLevel", 254, 1, 254);

    private static final ModConfigSpec.IntValue SOULRENDER_EFFECT_RADIUS = BUILDER
            .comment("Soulrender: Horizontal radius of the active ability area (default: 10 blocks)")
            .defineInRange("soulrenderEffectRadius", 10, 1, 30);

    private static final ModConfigSpec.DoubleValue SOULRENDER_HEAL_MULTIPLIER = BUILDER
            .comment("Soulrender: Healing per stack (default: 0.5)")
            .defineInRange("soulrenderHealMultiplier", 0.5, 0.0, 10.0);

    private static final ModConfigSpec.IntValue SOULRENDER_BASE_DAMAGE = BUILDER
            .comment("Soulrender: Base damage multiplier for the initial stacks (default: 2)")
            .defineInRange("soulrenderBaseDamage", 2, 0, 100);

    private static final ModConfigSpec.IntValue SOULRENDER_STACKS_PER_STAGE = BUILDER
            .comment("Soulrender: Number of stacks required to increase damage to the next stage (default: 5)")
            .defineInRange("soulrenderStacksPerStage", 5, 1, 1000);

    private static final ModConfigSpec.DoubleValue SOULRENDER_MAX_HEAL_PERCENT = BUILDER
            .comment("Soulrender: Maximum healing percentage of the player's max health (default: 0.5)")
            .defineInRange("soulrenderMaxHealPercent", 0.5, 0.0, 1.0);

    private static final ModConfigSpec.DoubleValue SHADOWSTING_ARMOR_DAMAGE_MULTIPLIER = BUILDER
            .comment("Shadowsting: Bonus damage multiplier per unit of target's armor (default: 0.025)")
            .defineInRange("shadowstingArmorDamageMultiplier", 0.025, 0.0, 1.0);

    private static final ModConfigSpec.DoubleValue SHADOWSTING_TELEPORT_DISTANCE = BUILDER
            .comment("Shadowsting: Distance in blocks for teleportation (default: 5.0)")
            .defineInRange("shadowstingTeleportDistance", 5.0, 0.0, 20.0);

    private static final ModConfigSpec.IntValue SHADOWSTING_BLINDNESS_DURATION = BUILDER
            .comment("Shadowsting: Duration of blindness effect on target in seconds (default: 3)")
            .defineInRange("shadowstingBlindnessDuration", 3, 0, 10000);

    private static final ModConfigSpec.IntValue SHADOWSTING_COOLDOWN_TICKS = BUILDER
            .comment("Shadowsting: Cooldown of Shadowsting ability in ticks (default: 200 ticks = 10 seconds)")
            .defineInRange("shadowstingCooldownTicks", 200, 1, 10000);

    private static final ModConfigSpec.DoubleValue SOUL_PYRE_MAX_ABILITY_DISTANCE = BUILDER
            .comment("Soul Pyre: Maximum range of the ability in blocks (default: 32)")
            .defineInRange("soulPyreMaxAbilityDistance", 32.0, 1.0, 100.0);

    private static final ModConfigSpec.DoubleValue SOUL_PYRE_TELEPORT_DISTANCE = BUILDER
            .comment("Soul Pyre: Distance in blocks at which the target teleports relative to the player (default: 1.5)")
            .defineInRange("soulPyreTeleportDistance", 1.5, 0.5, 10.0);

    private static final ModConfigSpec.IntValue SOUL_PYRE_COOLDOWN_TICKS = BUILDER
            .comment("Soul Pyre: Cooldown of the ability in ticks (default: 400 ticks = 20 seconds)")
            .defineInRange("soulPyreCooldownTicks", 400, 1, 10000);

    private static final ModConfigSpec.IntValue SOUL_PYRE_WITHER_DURATION = BUILDER
            .comment("Soul Pyre: Duration of the Wither effect in seconds (default: 10 seconds)")
            .defineInRange("soulPyreWitherDuration", 10, 0, 10000);

    private static final ModConfigSpec.IntValue SOUL_PYRE_WITHER_LEVEL = BUILDER
            .comment("Soul Pyre: Level of the Wither effect applied to the target (default: 2)")
            .defineInRange("soulPyreWitherLevel", 2, 1, 100);

    private static final ModConfigSpec.DoubleValue STARS_EDGE_MAGIC_DAMAGE = BUILDER
            .comment("Star's Edge: Bonus magic damage dealt on hit (default: 3.0)")
            .defineInRange("starsEdgeMagicDamage", 3.0, 0.0, 100.0);

    private static final ModConfigSpec.IntValue STARS_EDGE_TELEPORT_DELAY = BUILDER
            .comment("Star's Edge: Delay before teleporting player back in seconds (default: 15)")
            .defineInRange("starsEdgeTeleportDelay", 15, 1, 1000);

    private static final ModConfigSpec.IntValue STARS_EDGE_COOLDOWN_TICKS = BUILDER
            .comment("Star's Edge: Cooldown of the ability in ticks (default: 300 ticks = 15 seconds)")
            .defineInRange("starsEdgeCooldownTicks", 300, 1, 10000);

    private static final ModConfigSpec.IntValue STARS_EDGE_SPEED_DURATION = BUILDER
            .comment("Star's Edge: Duration of Speed effect in seconds (default: 15)")
            .defineInRange("starsEdgeSpeedDuration", 15, 0, 1000);

    private static final ModConfigSpec.IntValue STARS_EDGE_SPEED_LEVEL = BUILDER
            .comment("Star's Edge: Level of Speed effect (default: 2)")
            .defineInRange("starsEdgeSpeedLevel", 2, 1, 10);

    private static final ModConfigSpec.IntValue STARS_EDGE_HASTE_DURATION = BUILDER
            .comment("Star's Edge: Duration of Haste effect in seconds (default: 15)")
            .defineInRange("starsEdgeHasteDuration", 15, 0, 1000);

    private static final ModConfigSpec.IntValue STARS_EDGE_HASTE_LEVEL = BUILDER
            .comment("Star's Edge: Level of Haste effect (default: 2)")
            .defineInRange("starsEdgeHasteLevel", 2, 1, 10);

    private static final ModConfigSpec.DoubleValue STARS_EDGE_DASH_FORCE = BUILDER
            .comment("Star's Edge: Dash force that determines dash distance (default: 3.5)")
            .defineInRange("starsEdgeDashForce", 3.5, 0.0, 10.0);

    private static final ModConfigSpec.IntValue STARS_EDGE_RESISTANCE_DURATION = BUILDER
            .comment("Star's Edge: Duration of Damage Resistance effect in seconds (default: 2)")
            .defineInRange("starsEdgeResistanceDuration", 2, 0, 1000);

    private static final ModConfigSpec.IntValue STORMS_EDGE_HIT_COOLDOWN_REDUCTION = BUILDER
            .comment("Storm's Edge: Cooldown reduction per hit in ticks (default: 20 ticks = 1 second)")
            .defineInRange("stormsEdgeHitCooldownReduction", 20, 1, 1000);

    private static final ModConfigSpec.IntValue STORMS_EDGE_COOLDOWN_TICKS = BUILDER
            .comment("Storm's Edge: Cooldown of the ability in ticks (default: 200 ticks = 10 seconds)")
            .defineInRange("stormsEdgeCooldownTicks", 200, 1, 10000);

    private static final ModConfigSpec.DoubleValue STORMS_EDGE_DASH_DISTANCE = BUILDER
            .comment("Storm's Edge: Dash distance in blocks (default: 12)")
            .defineInRange("stormsEdgeDashDistance", 12.0, 0.0, 50.0);

    private static final ModConfigSpec.IntValue STORMS_EDGE_EFFECT_DURATION = BUILDER
            .comment("Storm's Edge: Duration of Speed and Haste effects in seconds (default: 15)")
            .defineInRange("stormsEdgeEffectDuration", 15, 0, 10000);

    private static final ModConfigSpec.IntValue STORMS_EDGE_MAX_HASTE_LEVEL = BUILDER
            .comment("Storm's Edge: Maximum level of Haste effect (default: 5)")
            .defineInRange("stormsEdgeMaxHasteLevel", 5, 1, 100);

    private static final ModConfigSpec.DoubleValue WATCHER_HIT_HEAL_AMOUNT = BUILDER
            .comment("Watcher: Amount of healing per hit (default: 0.5)")
            .defineInRange("watcherHitHealAmount", 0.5, 0.0, 100.0);

    private static final ModConfigSpec.DoubleValue WATCHER_KILL_HEAL_PERCENT = BUILDER
            .comment("Watcher: Percentage of target's max HP healed when the target is killed (default: 0.1, i.e., 10%)")
            .defineInRange("watcherKillHealPercent", 0.1, 0.0, 1.0);

    private static final ModConfigSpec.IntValue WHISPERWIND_COOLDOWN_TICKS = BUILDER
            .comment("Whisper Wind: Cooldown of the dash ability in ticks (default: 400 ticks = 20 seconds)")
            .defineInRange("whisperwindCooldownTicks", 400, 1, 50000);

    private static final ModConfigSpec.IntValue WHISPERWIND_DASH_DISTANCE = BUILDER
            .comment("Whisper Wind: Dash distance in blocks (default: 22)")
            .defineInRange("whisperwindDashDistance", 22, 0, 100);



    public static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean enableEmberlashChanges;
    public static boolean enableMoltenEdgeChanges;
    public static boolean enableShadowstingChanges;
    public static boolean enableSoulPyreChanges;
    public static boolean enableSoulrenderChanges;
    public static boolean enableStarsEdgeChanges;
    public static boolean enableStormsEdgeChanges;
    public static boolean enableWatcherChanges;
    public static boolean enableWhisperwindChanges;
    public static double emberlashSmoulderDamageMultiplier;
    public static int emberlashDashDistance;
    public static int emberlashHealPercentage;
    public static int emberlashMaxSmoulderLevel;
    public static int emberlashCooldownTicks;
    public static boolean emberlashIgnoreDownwardAngleOnGround;
    public static int soulrenderMaxSlownessLevel;
    public static int soulrenderMaxWeaknessLevel;
    public static int soulrenderMaxUnluckLevel;
    public static int soulrenderEffectRadius;
    public static double soulrenderHealMultiplier;
    public static int soulrenderBaseDamage;
    public static int soulrenderStacksPerStage;
    public static double soulrenderMaxHealPercent;
    public static double shadowstingArmorDamageMultiplier;
    public static double shadowstingTeleportDistance;
    public static int shadowstingBlindnessDuration;
    public static int shadowstingCooldownTicks;
    public static double soulPyreMaxAbilityDistance;
    public static double soulPyreTeleportDistance;
    public static int soulPyreCooldownTicks;
    public static int soulPyreWitherDuration;
    public static int soulPyreWitherLevel;
    public static double starsEdgeMagicDamage;
    public static int starsEdgeTeleportDelay;
    public static int starsEdgeCooldownTicks;
    public static int starsEdgeSpeedDuration;
    public static int starsEdgeSpeedLevel;
    public static int starsEdgeHasteDuration;
    public static int starsEdgeHasteLevel;
    public static double starsEdgeDashForce;
    public static int starsEdgeResistanceDuration;
    public static int stormsEdgeHitCooldownReduction;
    public static int stormsEdgeCooldownTicks;
    public static double stormsEdgeDashDistance;
    public static int stormsEdgeEffectDuration;
    public static int stormsEdgeMaxHasteLevel;
    public static double watcherHitHealAmount;
    public static double watcherKillHealPercent;
    public static int whisperwindCooldownTicks;
    public static int whisperwindDashDistance;



    static void onLoad(final ModConfigEvent event) {
        enableEmberlashChanges = ENABLE_EMBERLASH_CHANGES.get();
        enableMoltenEdgeChanges = ENABLE_MOLTEN_EDGE_CHANGES.get();
        enableShadowstingChanges = ENABLE_SHADOWSTING_CHANGES.get();
        enableSoulPyreChanges = ENABLE_SOUL_PYRE_CHANGES.get();
        enableSoulrenderChanges = ENABLE_SOULRENDER_CHANGES.get();
        enableStarsEdgeChanges = ENABLE_STARS_EDGE_CHANGES.get();
        enableStormsEdgeChanges = ENABLE_STORMS_EDGE_CHANGES.get();
        enableWatcherChanges = ENABLE_WATCHER_CHANGES.get();
        enableWhisperwindChanges = ENABLE_WHISPERWIND_CHANGES.get();
        emberlashSmoulderDamageMultiplier = EMBERLASH_SMOULDER_DAMAGE_MULTIPLIER.get();
        emberlashDashDistance = EMBERLASH_DASH_DISTANCE.get();
        emberlashHealPercentage = EMBERLASH_HEAL_PERCENTAGE.get();
        emberlashMaxSmoulderLevel = EMBERLASH_MAX_SMOULDER_LEVEL.get();
        emberlashCooldownTicks = EMBERLASH_COOLDOWN_TICKS.get();
        emberlashIgnoreDownwardAngleOnGround = EMBERLASH_IGNORE_DOWNWARD_ANGLE_ON_GROUND.get();
        soulrenderMaxSlownessLevel = SOULRENDER_MAX_SLOWNESS_LEVEL.get();
        soulrenderMaxWeaknessLevel = SOULRENDER_MAX_WEAKNESS_LEVEL.get();
        soulrenderMaxUnluckLevel = SOULRENDER_MAX_UNLUCK_LEVEL.get();
        soulrenderEffectRadius = SOULRENDER_EFFECT_RADIUS.get();
        soulrenderHealMultiplier = SOULRENDER_HEAL_MULTIPLIER.get();
        soulrenderBaseDamage = SOULRENDER_BASE_DAMAGE.get();
        soulrenderStacksPerStage = SOULRENDER_STACKS_PER_STAGE.get();
        soulrenderMaxHealPercent = SOULRENDER_MAX_HEAL_PERCENT.get();
        shadowstingArmorDamageMultiplier = SHADOWSTING_ARMOR_DAMAGE_MULTIPLIER.get();
        shadowstingTeleportDistance = SHADOWSTING_TELEPORT_DISTANCE.get();
        shadowstingBlindnessDuration = SHADOWSTING_BLINDNESS_DURATION.get();
        shadowstingCooldownTicks = SHADOWSTING_COOLDOWN_TICKS.get();
        soulPyreMaxAbilityDistance = SOUL_PYRE_MAX_ABILITY_DISTANCE.get();
        soulPyreTeleportDistance = SOUL_PYRE_TELEPORT_DISTANCE.get();
        soulPyreCooldownTicks = SOUL_PYRE_COOLDOWN_TICKS.get();
        soulPyreWitherDuration = SOUL_PYRE_WITHER_DURATION.get();
        soulPyreWitherLevel = SOUL_PYRE_WITHER_LEVEL.get();
        starsEdgeMagicDamage = STARS_EDGE_MAGIC_DAMAGE.get();
        starsEdgeTeleportDelay = STARS_EDGE_TELEPORT_DELAY.get();
        starsEdgeCooldownTicks = STARS_EDGE_COOLDOWN_TICKS.get();
        starsEdgeSpeedDuration = STARS_EDGE_SPEED_DURATION.get();
        starsEdgeSpeedLevel = STARS_EDGE_SPEED_LEVEL.get();
        starsEdgeHasteDuration = STARS_EDGE_HASTE_DURATION.get();
        starsEdgeHasteLevel = STARS_EDGE_HASTE_LEVEL.get();
        starsEdgeDashForce = STARS_EDGE_DASH_FORCE.get();
        starsEdgeResistanceDuration = STARS_EDGE_RESISTANCE_DURATION.get();
        stormsEdgeHitCooldownReduction = STORMS_EDGE_HIT_COOLDOWN_REDUCTION.get();
        stormsEdgeCooldownTicks = STORMS_EDGE_COOLDOWN_TICKS.get();
        stormsEdgeDashDistance = STORMS_EDGE_DASH_DISTANCE.get();
        stormsEdgeEffectDuration = STORMS_EDGE_EFFECT_DURATION.get();
        stormsEdgeMaxHasteLevel = STORMS_EDGE_MAX_HASTE_LEVEL.get();
        watcherHitHealAmount = WATCHER_HIT_HEAL_AMOUNT.get();
        watcherKillHealPercent = WATCHER_KILL_HEAL_PERCENT.get();
        whisperwindCooldownTicks = WHISPERWIND_COOLDOWN_TICKS.get();
        whisperwindDashDistance = WHISPERWIND_DASH_DISTANCE.get();
    }
}
