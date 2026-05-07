package io.github.kingironman2011.orbital_railgun_enhanced.client.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.RangeConstraint;

@Modmenu(modId = "orbital_railgun_enhanced")
@Config(name = "orbital-railgun-enhanced", wrapperName = "EnhancedConfigWrapper")
@SuppressWarnings("unused")
public class EnhancedConfig {

    // Volume settings (0.0 to 1.0)
    @RangeConstraint(min = 0.0, max = 1.0)
    public double scopeVolume = 1.0;

    @RangeConstraint(min = 0.0, max = 1.0)
    public double shootVolume = 0.5;

    @RangeConstraint(min = 0.0, max = 1.0)
    public double equipVolume = 1.0;

    public boolean enableScopeSound = true;
    public boolean enableShootSound = true;
    public boolean enableEquipSound = true;

    // Visual effects settings
    public boolean enableVisualEffects = true;
    public boolean enableShaderEffects = true;
}
