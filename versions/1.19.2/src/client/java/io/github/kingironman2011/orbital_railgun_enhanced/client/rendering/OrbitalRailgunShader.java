package io.github.kingironman2011.orbital_railgun_enhanced.client.rendering;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.joml.Vector3f;

public class OrbitalRailgunShader extends AbstractOrbitalRailgunShader {
    public static final Identifier ORBITAL_RAILGUN_SHADER =
            Identifier.of(OrbitalRailgun.MOD_ID, "shaders/post/orbital_railgun_enhanced.json");
    public static final OrbitalRailgunShader INSTANCE = new OrbitalRailgunShader();

    public Vector3f BlockPosition = null;
    public RegistryKey<World> Dimension = null;

    @Override
    protected Identifier getIdentifier() {
        return ORBITAL_RAILGUN_SHADER;
    }

    @Override
    protected boolean shouldRender() {
        var world = MinecraftClient.getInstance().world;
        return BlockPosition != null && world != null && world.getRegistryKey() == Dimension;
    }

    /**
     * Stops the animation by clearing the block position and dimension.
     * Called when the player leaves the configured range.
     */
    public void stopAnimation() {
        BlockPosition = null;
        Dimension = null;
        ticks = 0;
    }

    @Override
    public void onEndTick(MinecraftClient minecraftClient) {
        if (ticks >= 1600
                || minecraftClient.world == null
                || minecraftClient.world.getRegistryKey() != Dimension) {
            BlockPosition = null;
            Dimension = null;
        }

        super.onEndTick(minecraftClient);
    }

    @Override
    public void onWorldRendered(Camera camera, float tickDelta, long nanoTime) {
        if (shouldRender()) {
            uniformBlockPosition.set(BlockPosition);
        }

        super.onWorldRendered(camera, tickDelta, nanoTime);
    }
}
