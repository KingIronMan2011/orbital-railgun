package io.github.kingironman2011.orbital_railgun_enhanced.client.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.api.v0.IrisApi;

/** Utilities for selecting render paths that coexist with optional client mods. */
public final class ModDetector {
    private ModDetector() {
    }

    public static boolean isIrisLoaded() {
        return FabricLoader.getInstance().isModLoaded("iris");
    }

    public static boolean isShaderPackActive() {
        if (isIrisLoaded()) {
            return IrisApi.getInstance().isShaderPackInUse();
        }
        return false;
    }
}
