package io.github.kingironman2011.orbital_railgun_enhanced.client.item;

import io.github.kingironman2011.orbital_railgun_enhanced.item.OrbitalRailgunItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderer.geo.GeoItemRenderer;

public class OrbitalRailgunRenderer extends GeoItemRenderer<OrbitalRailgunItem> {
    public OrbitalRailgunRenderer() {
        super(new OrbitalRailgunModel());
    }

    public static class OrbitalRailgunModel extends AnimatedGeoModel<OrbitalRailgunItem> {
        @Override
        public Identifier getModelLocation(OrbitalRailgunItem object) {
            return new Identifier("orbital_railgun_enhanced", "geo/item/orbital_railgun.geo.json");
        }

        @Override
        public Identifier getTextureLocation(OrbitalRailgunItem object) {
            return new Identifier("orbital_railgun_enhanced", "textures/item/orbital_railgun.png");
        }

        @Override
        public Identifier getAnimationFileLocation(OrbitalRailgunItem animatable) {
            return new Identifier("orbital_railgun_enhanced", "animations/orbital_railgun.animation.json");
        }
    }
}

