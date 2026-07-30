package io.github.kingironman2011.orbital_railgun_enhanced.client.impl.v1204;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import io.github.kingironman2011.orbital_railgun_enhanced.client.utils.ModDetector;
import io.github.kingironman2011.orbital_railgun_enhanced.impl.v1204.OrbitalRailgunItem;
import ladysnake.satin.api.managed.uniform.Uniform1f;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class OrbitalRailgunGuiShader extends AbstractOrbitalRailgunShader {
    public static final Identifier ORBITAL_RAILGUN_GUI_SHADER =
            Identifier.of(OrbitalRailgun.MOD_ID, "shaders/post/orbital_railgun_enhanced_gui.json");
    public static final OrbitalRailgunGuiShader INSTANCE = new OrbitalRailgunGuiShader();

    private final Uniform1f uniformIsBlockHit = shaderEffect.findUniform1f("IsBlockHit");

    public HitResult hitResult;

    @Override
    protected Identifier getIdentifier() {
        return ORBITAL_RAILGUN_GUI_SHADER;
    }

    @Override
    protected boolean shouldRender() {
        return client.player != null
                && client.player.getActiveItem().getItem() instanceof OrbitalRailgunItem;
    }

    @Override
    public void onEndTick(MinecraftClient minecraftClient) {
        // is it jank to disable the hud rendering here? yeah kinda
        if (shouldRender()) {
            // The Iris fallback keeps the normal HUD visible and draws its target
            // indicator in-world, rather than replacing the whole screen.
            this.client.options.hudHidden = !ModDetector.isShaderPackActive();
            hitResult = client.player.raycast(300f, 1.0f, false);
        } else if (ticks != 0) {
            this.client.options.hudHidden = false;
            hitResult = null;
        }

        super.onEndTick(minecraftClient);
    }

    @Override
    public void onWorldRendered(Camera camera, float tickDelta, long nanoTime) {
        if (shouldRender()) {
            switch (hitResult.getType()) {
                case BLOCK:
                    uniformIsBlockHit.set(1);
                    uniformBlockPosition.set(
                            ((BlockHitResult) hitResult).getBlockPos().toCenterPos().toVector3f());
                    break;
                case ENTITY:
                    uniformIsBlockHit.set(1);
                    uniformBlockPosition.set(
                            ((EntityHitResult) hitResult).getEntity().getBlockPos().toCenterPos().toVector3f());
                    break;
                case MISS:
                    uniformIsBlockHit.set(0);
                    break;
                default:
                    uniformIsBlockHit.set(0);
                    break;
            }
        }

        super.onWorldRendered(camera, tickDelta, nanoTime);
    }
}
