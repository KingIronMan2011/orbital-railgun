package io.github.kingironman2011.orbital_railgun_enhanced.client.impl.v1205;

import ladysnake.satin.api.event.PostWorldRenderCallback;
import ladysnake.satin.api.experimental.ReadableDepthFramebuffer;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import ladysnake.satin.api.managed.uniform.Uniform1f;
import ladysnake.satin.api.managed.uniform.Uniform3f;
import ladysnake.satin.api.managed.uniform.UniformMat4;
import ladysnake.satin.api.util.GlMatrices;
import io.github.kingironman2011.orbital_railgun_enhanced.client.utils.ModDetector;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public abstract class AbstractOrbitalRailgunShader
        implements PostWorldRenderCallback, ClientTickEvents.EndTick {
    protected final MinecraftClient client = MinecraftClient.getInstance();

    private final Matrix4f projectionMatrix = new Matrix4f();

    protected final ManagedShaderEffect shaderEffect =
            ShaderEffectManager.getInstance()
                    .manage(
                            getIdentifier(),
                            shader -> {
                                shader.setSamplerUniform(
                                        "DepthSampler",
                                        ((ReadableDepthFramebuffer) client.getFramebuffer()).getStillDepthMap());
                            });
    private final UniformMat4 uniformInverseTransformMatrix =
            shaderEffect.findUniformMat4("InverseTransformMatrix");
    private final Uniform3f uniformCameraPosition = shaderEffect.findUniform3f("CameraPosition");
    private final Uniform1f uniformiTime = shaderEffect.findUniform1f("iTime");
    protected final Uniform3f uniformBlockPosition = shaderEffect.findUniform3f("BlockPosition");

    protected int ticks = 0;

    protected abstract Identifier getIdentifier();

    protected abstract boolean shouldRender();

    @Override
    public void onEndTick(MinecraftClient minecraftClient) {
        if (shouldRender()) {
            ticks++;
        } else {
            ticks = 0;
        }
    }

    @Override
    public void onWorldRendered(Camera camera, float tickDelta, long nanoTime) {
        // Iris owns the main framebuffer while a shader pack is active. Rendering a
        // Satin post-process pass into it causes visual corruption with many packs.
        // IrisCompatibilityRenderer supplies a world-geometry fallback instead.
        if (ModDetector.isShaderPackActive()) {
            return;
        }

        if (shouldRender()) {
            uniformInverseTransformMatrix.set(GlMatrices.getInverseTransformMatrix(projectionMatrix));
            uniformCameraPosition.set(camera.getPos().toVector3f());
            uniformiTime.set((ticks + tickDelta) / 20f);

            shaderEffect.render(tickDelta);
        }
    }
}
