package io.github.kingironman2011.orbital_railgun_enhanced.client.impl.v1206;

import io.github.kingironman2011.orbital_railgun_enhanced.client.utils.ModDetector;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Iris-safe visual fallback for the fullscreen Satin effects.
 *
 * <p>It deliberately uses Fabric's buffered line layer, which Iris and Sodium
 * process as ordinary mod geometry. It never reads or replaces the shader
 * pack's framebuffer or depth texture.</p>
 */
public final class IrisCompatibilityRenderer implements WorldRenderEvents.AfterEntities {
    private static final float TARGET_RED = 0.26f;
    private static final float TARGET_GREEN = 1.0f;
    private static final float TARGET_BLUE = 0.42f;
    private static final float STRIKE_RED = 0.62f;
    private static final float STRIKE_GREEN = 0.93f;
    private static final float STRIKE_BLUE = 0.93f;

    private final OrbitalRailgunGuiShader guiShader;
    private final OrbitalRailgunShader strikeShader;

    public IrisCompatibilityRenderer(
            OrbitalRailgunGuiShader guiShader, OrbitalRailgunShader strikeShader) {
        this.guiShader = guiShader;
        this.strikeShader = strikeShader;
    }

    @Override
    public void afterEntities(WorldRenderContext context) {
        if (!ModDetector.isShaderPackActive()
                || context.matrixStack() == null
                || context.consumers() == null) {
            return;
        }

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        Vec3d camera = context.camera().getPos();

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);
        VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());

        renderTargetIndicator(matrices, lines);
        renderStrikeIndicator(context, matrices, lines);

        matrices.pop();
    }

    private void renderTargetIndicator(MatrixStack matrices, VertexConsumer lines) {
        HitResult hit = guiShader.hitResult;
        if (hit instanceof BlockHitResult blockHit) {
            WorldRenderer.drawBox(
                    matrices, lines, new Box(blockHit.getBlockPos()).expand(0.004),
                    TARGET_RED, TARGET_GREEN, TARGET_BLUE, 0.95f);
        } else if (hit instanceof EntityHitResult entityHit) {
            WorldRenderer.drawBox(
                    matrices, lines, entityHit.getEntity().getBoundingBox().expand(0.02),
                    TARGET_RED, TARGET_GREEN, TARGET_BLUE, 0.95f);
        }
    }

    private void renderStrikeIndicator(
            WorldRenderContext context, MatrixStack matrices, VertexConsumer lines) {
        if (!strikeShader.isStrikeActive()) {
            return;
        }

        double x = strikeShader.blockPosition.x();
        double y = strikeShader.blockPosition.y();
        double z = strikeShader.blockPosition.z();
        float pulse = 0.6f + 0.4f * (float) Math.sin(strikeShader.getTicks() * 0.35f);

        // The original effect starts its strike sequence after four seconds and
        // expands for 32 seconds. Mirror that timing with an expanding cyan ring.
        double elapsedSeconds = strikeShader.getTicks() / 20.0;
        double radius = elapsedSeconds < 4.0
                ? 1.25
                : Math.min(24.0, 2.0 + (elapsedSeconds - 4.0) * 0.75);
        Box ring = new Box(x - radius, y - 0.04, z - radius, x + radius, y + 0.04, z + radius);
        WorldRenderer.drawBox(matrices, lines, ring, STRIKE_RED, STRIKE_GREEN, STRIKE_BLUE, pulse);

        // Keep the impact location unmistakable at all distances.
        WorldRenderer.drawBox(
                matrices, lines, new Box(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5),
                STRIKE_RED, STRIKE_GREEN, STRIKE_BLUE, 1.0f);
    }
}
