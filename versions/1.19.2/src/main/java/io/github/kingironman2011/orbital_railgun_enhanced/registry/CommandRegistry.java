package io.github.kingironman2011.orbital_railgun_enhanced.registry;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import io.github.kingironman2011.orbital_railgun_enhanced.config.ServerConfig;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CommandRegistry {
    private static int showHelp(CommandContext<ServerCommandSource> context) {
        ServerConfig config = ServerConfig.INSTANCE;
        if (config.isDebugMode()) {
            OrbitalRailgun.LOGGER.info("Displaying help to player: {}", context.getSource().getName());
        }

        context
                .getSource()
                .sendFeedback(() -> Text.translatable("command.orbital_railgun_enhanced.help"), false);
        return 1;
    }

    public static void registerCommands() {
        ServerConfig.INSTANCE.loadConfig();

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {
                    // Register short form /ore
                    dispatcher.register(
                            CommandManager.literal("ore")
                                    .requires(source -> source.hasPermissionLevel(2))
                                    .executes(CommandRegistry::showHelp)
                                    .then(
                                            CommandManager.literal("debug")
                                                    .executes(CommandRegistry::showDebugMode)
                                                    .then(
                                                            CommandManager.argument("enabled", BoolArgumentType.bool())
                                                                    .executes(
                                                                            context ->
                                                                                    toggleDebugMode(
                                                                                            context,
                                                                                            BoolArgumentType.getBool(context, "enabled")))))
                                    .then(
                                            CommandManager.literal("radius")
                                                    .executes(CommandRegistry::showRadiusValue)
                                                    .then(
                                                            CommandManager.argument("value", DoubleArgumentType.doubleArg(0.0))
                                                                    .executes(
                                                                            context ->
                                                                                    setRadiusValue(
                                                                                            context,
                                                                                            DoubleArgumentType.getDouble(context, "value")))))
                                    .then(
                                            CommandManager.literal("strikeDamage")
                                                    .executes(CommandRegistry::showStrikeDamage)
                                                    .then(
                                                            CommandManager.argument("value", FloatArgumentType.floatArg(0.0f))
                                                                    .executes(
                                                                            context ->
                                                                                    setStrikeDamage(
                                                                                            context,
                                                                                            FloatArgumentType.getFloat(context, "value")))))
                                    .then(
                                            CommandManager.literal("cooldown")
                                                    .executes(CommandRegistry::showCooldown)
                                                    .then(
                                                            CommandManager.argument("ticks", IntegerArgumentType.integer(0))
                                                                    .executes(
                                                                            context ->
                                                                                    setCooldown(
                                                                                            context,
                                                                                            IntegerArgumentType.getInteger(context, "ticks")))))
                                    .then(
                                            CommandManager.literal("maxStrikes")
                                                    .executes(CommandRegistry::showMaxStrikes)
                                                    .then(
                                                            CommandManager.argument("value", IntegerArgumentType.integer(1))
                                                                    .executes(
                                                                            context ->
                                                                                    setMaxStrikes(
                                                                                            context,
                                                                                            IntegerArgumentType.getInteger(context, "value")))))
                                    .then(
                                            CommandManager.literal("particles")
                                                    .executes(CommandRegistry::showParticles)
                                                    .then(
                                                            CommandManager.argument("enabled", BoolArgumentType.bool())
                                                                    .executes(
                                                                            context ->
                                                                                    setParticles(
                                                                                            context,
                                                                                            BoolArgumentType.getBool(context, "enabled")))))
                                    .then(CommandManager.literal("reload").executes(CommandRegistry::reloadConfig))
                                    .then(CommandManager.literal("help").executes(CommandRegistry::showHelp)));

                    // Register long form /orbitalrailgun with same subcommands
                    dispatcher.register(
                            CommandManager.literal("orbitalrailgun")
                                    .requires(source -> source.hasPermissionLevel(2))
                                    .executes(CommandRegistry::showHelp)
                                    .then(
                                            CommandManager.literal("debug")
                                                    .executes(CommandRegistry::showDebugMode)
                                                    .then(
                                                            CommandManager.argument("enabled", BoolArgumentType.bool())
                                                                    .executes(
                                                                            context ->
                                                                                    toggleDebugMode(
                                                                                            context,
                                                                                            BoolArgumentType.getBool(context, "enabled")))))
                                    .then(
                                            CommandManager.literal("radius")
                                                    .executes(CommandRegistry::showRadiusValue)
                                                    .then(
                                                            CommandManager.argument("value", DoubleArgumentType.doubleArg(0.0))
                                                                    .executes(
                                                                            context ->
                                                                                    setRadiusValue(
                                                                                            context,
                                                                                            DoubleArgumentType.getDouble(context, "value")))))
                                    .then(
                                            CommandManager.literal("strikeDamage")
                                                    .executes(CommandRegistry::showStrikeDamage)
                                                    .then(
                                                            CommandManager.argument("value", FloatArgumentType.floatArg(0.0f))
                                                                    .executes(
                                                                            context ->
                                                                                    setStrikeDamage(
                                                                                            context,
                                                                                            FloatArgumentType.getFloat(context, "value")))))
                                    .then(
                                            CommandManager.literal("cooldown")
                                                    .executes(CommandRegistry::showCooldown)
                                                    .then(
                                                            CommandManager.argument("ticks", IntegerArgumentType.integer(0))
                                                                    .executes(
                                                                            context ->
                                                                                    setCooldown(
                                                                                            context,
                                                                                            IntegerArgumentType.getInteger(context, "ticks")))))
                                    .then(
                                            CommandManager.literal("maxStrikes")
                                                    .executes(CommandRegistry::showMaxStrikes)
                                                    .then(
                                                            CommandManager.argument("value", IntegerArgumentType.integer(1))
                                                                    .executes(
                                                                            context ->
                                                                                    setMaxStrikes(
                                                                                            context,
                                                                                            IntegerArgumentType.getInteger(context, "value")))))
                                    .then(
                                            CommandManager.literal("particles")
                                                    .executes(CommandRegistry::showParticles)
                                                    .then(
                                                            CommandManager.argument("enabled", BoolArgumentType.bool())
                                                                    .executes(
                                                                            context ->
                                                                                    setParticles(
                                                                                            context,
                                                                                            BoolArgumentType.getBool(context, "enabled")))))
                                    .then(CommandManager.literal("reload").executes(CommandRegistry::reloadConfig))
                                    .then(CommandManager.literal("help").executes(CommandRegistry::showHelp)));
                });

        OrbitalRailgun.LOGGER.info("Registered commands: /ore and /orbitalrailgun");
    }

    private static int toggleDebugMode(CommandContext<ServerCommandSource> context, boolean enabled) {
        ServerConfig.INSTANCE.setDebugMode(enabled);
        context
                .getSource()
                .sendFeedback(
                        () -> Text.translatable("command.orbital_railgun_enhanced.debug.set", enabled), true);

        if (enabled) {
            OrbitalRailgun.LOGGER.info("Debug mode enabled by {}", context.getSource().getName());
        } else {
            OrbitalRailgun.LOGGER.info("Debug mode disabled by {}", context.getSource().getName());
        }
        return 1;
    }

    private static int setRadiusValue(CommandContext<ServerCommandSource> context, double radius) {
        ServerConfig.INSTANCE.setSoundRange(radius);
        context
                .getSource()
                .sendFeedback(
                        () -> Text.translatable("command.orbital_railgun_enhanced.radius.set", radius), true);

        if (ServerConfig.INSTANCE.isDebugMode()) {
            OrbitalRailgun.LOGGER.info(
                    "Sound radius set to {} by {}", radius, context.getSource().getName());
        }
        return 1;
    }

    private static int setStrikeDamage(CommandContext<ServerCommandSource> context, float damage) {
        ServerConfig.INSTANCE.setStrikeDamage(damage);
        context
                .getSource()
                .sendFeedback(
                        () -> Text.translatable("command.orbital_railgun_enhanced.strikeDamage.set", damage),
                        true);

        if (ServerConfig.INSTANCE.isDebugMode()) {
            OrbitalRailgun.LOGGER.info(
                    "Strike damage set to {} by {}", damage, context.getSource().getName());
        }
        return 1;
    }

    private static int setCooldown(CommandContext<ServerCommandSource> context, int ticks) {
        ServerConfig.INSTANCE.setCooldownTicks(ticks);
        context
                .getSource()
                .sendFeedback(
                        () -> Text.translatable("command.orbital_railgun_enhanced.cooldown.set", ticks), true);

        if (ServerConfig.INSTANCE.isDebugMode()) {
            OrbitalRailgun.LOGGER.info(
                    "Cooldown set to {} ticks by {}", ticks, context.getSource().getName());
        }
        return 1;
    }

    private static int setMaxStrikes(CommandContext<ServerCommandSource> context, int maxStrikes) {
        ServerConfig.INSTANCE.setMaxActiveStrikes(maxStrikes);
        context
                .getSource()
                .sendFeedback(
                        () -> Text.translatable("command.orbital_railgun_enhanced.maxStrikes.set", maxStrikes),
                        true);

        if (ServerConfig.INSTANCE.isDebugMode()) {
            OrbitalRailgun.LOGGER.info(
                    "Max active strikes set to {} by {}", maxStrikes, context.getSource().getName());
        }
        return 1;
    }

    private static int setParticles(CommandContext<ServerCommandSource> context, boolean enabled) {
        ServerConfig.INSTANCE.setEnableParticles(enabled);
        context
                .getSource()
                .sendFeedback(
                        () -> Text.translatable("command.orbital_railgun_enhanced.particles.set", enabled),
                        true);

        if (ServerConfig.INSTANCE.isDebugMode()) {
            OrbitalRailgun.LOGGER.info(
                    "Particles {} by {}", enabled ? "enabled" : "disabled", context.getSource().getName());
        }
        return 1;
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        ServerConfig.INSTANCE.loadConfig();
        context
                .getSource()
                .sendFeedback(
                        () -> Text.translatable("command.orbital_railgun_enhanced.config.reloaded"), true);

        OrbitalRailgun.LOGGER.info(
                "Server configuration reloaded by {}", context.getSource().getName());
        return 1;
    }

    private static int showDebugMode(CommandContext<ServerCommandSource> context) {
        boolean debugMode = ServerConfig.INSTANCE.isDebugMode();
        context
                .getSource()
                .sendFeedback(
                        () -> Text.translatable("command.orbital_railgun_enhanced.debug.current", debugMode),
                        false);
        return 1;
    }

    private static int showRadiusValue(CommandContext<ServerCommandSource> context) {
        double radius = ServerConfig.INSTANCE.getSoundRange();
        context
                .getSource()
                .sendFeedback(
                        () -> Text.translatable("command.orbital_railgun_enhanced.radius.current", radius),
                        false);
        return 1;
    }

    private static int showStrikeDamage(CommandContext<ServerCommandSource> context) {
        float damage = ServerConfig.INSTANCE.getStrikeDamage();
        context
                .getSource()
                .sendFeedback(
                        () ->
                                Text.translatable("command.orbital_railgun_enhanced.strikeDamage.current", damage),
                        false);
        return 1;
    }

    private static int showCooldown(CommandContext<ServerCommandSource> context) {
        int ticks = ServerConfig.INSTANCE.getCooldownTicks();
        context
                .getSource()
                .sendFeedback(
                        () -> Text.translatable("command.orbital_railgun_enhanced.cooldown.current", ticks),
                        false);
        return 1;
    }

    private static int showMaxStrikes(CommandContext<ServerCommandSource> context) {
        int maxStrikes = ServerConfig.INSTANCE.getMaxActiveStrikes();
        context
                .getSource()
                .sendFeedback(
                        () ->
                                Text.translatable(
                                        "command.orbital_railgun_enhanced.maxStrikes.current", maxStrikes),
                        false);
        return 1;
    }

    private static int showParticles(CommandContext<ServerCommandSource> context) {
        boolean enabled = ServerConfig.INSTANCE.isEnableParticles();
        context
                .getSource()
                .sendFeedback(
                        () -> Text.translatable("command.orbital_railgun_enhanced.particles.current", enabled),
                        false);
        return 1;
    }
}
