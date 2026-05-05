package io.github.kingironman2011.orbital_railgun_enhanced.client.rendering;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import io.github.kingironman2011.orbital_railgun_enhanced.item.OrbitalRailgunItem;
import ladysnake.satin.api.managed.uniform.Uniform1f;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

public class OrbitalRailgunGuiShader extends AbstractOrbitalRailgunShader {
    public static final Identifier ORBITAL_RAILGUN_GUI_SHADER =
            new Identifier(OrbitalRailgun.MOD_ID, "shaders/post/orbital_railgun_enhanced_gui.json");
    public static final OrbitalRailgunGuiShader INSTANCE = new OrbitalRailgunGuiShader();

    private final Uniform1f uniformIsBlockHit = SHADER.findUniform1f("IsBlockHit");

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
        if (shouldRender()) {
            this.client.options.hudHidden = true;
        } else if (ticks != 0) {
            this.client.options.hudHidden = false;
        }

        super.onEndTick(minecraftClient);
    }

    @Override
    public void onWorldRendered(Camera camera, float tickDelta, long nanoTime) {
        if (shouldRender()) {
            hitResult = client.player.raycast(300f, tickDelta, false);
            switch (hitResult.getType()) {
                case BLOCK:
                    uniformIsBlockHit.set(1);
                    Vec3d blockCenter = Vec3d.ofCenter(((BlockHitResult) hitResult).getBlockPos());
                    uniformBlockPosition.set((float) blockCenter.x, (float) blockCenter.y, (float) blockCenter.z);
                    break;
                case ENTITY:
                    uniformIsBlockHit.set(1);
                    Vec3d entityCenter = Vec3d.ofCenter(((EntityHitResult) hitResult).getEntity().getBlockPos());
                    uniformBlockPosition.set((float) entityCenter.x, (float) entityCenter.y, (float) entityCenter.z);
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
