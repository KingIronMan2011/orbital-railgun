package io.github.kingironman2011.orbital_railgun_enhanced.client.impl.v1192;

import io.github.kingironman2011.orbital_railgun_enhanced.impl.v1192.OrbitalRailgunItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class OrbitalRailgunRenderer extends GeoItemRenderer<OrbitalRailgunItem> {
    public OrbitalRailgunRenderer() {
        super(new OrbitalRailgunModel());
    }

    public static class OrbitalRailgunModel extends AnimatedGeoModel<OrbitalRailgunItem> {
        @Override
        public Identifier getModelResource(OrbitalRailgunItem object) {
            return new Identifier("orbital_railgun_enhanced", "geo/item/orbital_railgun.geo.json");
        }

        @Override
        public Identifier getTextureResource(OrbitalRailgunItem object) {
            return new Identifier("orbital_railgun_enhanced", "textures/item/orbital_railgun.png");
        }

        @Override
        public Identifier getAnimationResource(OrbitalRailgunItem animatable) {
            return new Identifier("orbital_railgun_enhanced", "animations/orbital_railgun.animation.json");
        }
    }
}

