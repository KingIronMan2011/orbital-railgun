package io.github.kingironman2011.orbital_railgun_enhanced.universal;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin configuration plugin that conditionally enables version-specific mixins.
 *
 * Each per-version mixin configuration JSON references this plugin.  The plugin
 * inspects its own package name (passed via {@link #onLoad}) to determine which
 * Minecraft version the mixin set targets, then allows or rejects individual mixin
 * classes based on the version that is actually running.
 */
public class VersionMixinPlugin implements IMixinConfigPlugin {

    /** The Minecraft version group that this mixin set targets ("1.19", "1.20.4", "1.20.6"). */
    private String targetVersionGroup;

    @Override
    public void onLoad(String mixinPackage) {
        if (mixinPackage.contains(".impl.v1_19_2.")) {
            targetVersionGroup = "1.19";
        } else if (mixinPackage.contains(".impl.v1_20_6.")) {
            targetVersionGroup = "1.20.6";
        } else {
            targetVersionGroup = "1.20.4";
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String mcVersion = FabricLoader.getInstance()
                .getModContainer("minecraft")
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");

        switch (targetVersionGroup) {
            case "1.19":
                return mcVersion.startsWith("1.19");
            case "1.20.6":
                return mcVersion.startsWith("1.20.5") || mcVersion.startsWith("1.20.6");
            default: // 1.20.4 covers 1.20 through 1.20.4
                return !mcVersion.startsWith("1.19")
                        && !mcVersion.startsWith("1.20.5")
                        && !mcVersion.startsWith("1.20.6");
        }
    }

    // -----------------------------------------------------------------------
    // Unused IMixinConfigPlugin lifecycle methods – must be present
    // -----------------------------------------------------------------------

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // no-op
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(
            String targetClassName,
            ClassNode targetClass,
            String mixinClassName,
            IMixinInfo mixinInfo) {
        // no-op
    }

    @Override
    public void postApply(
            String targetClassName,
            ClassNode targetClass,
            String mixinClassName,
            IMixinInfo mixinInfo) {
        // no-op
    }
}
