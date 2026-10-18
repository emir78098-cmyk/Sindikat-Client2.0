package com.sindikat.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.sindikat.module.ModuleManager;
import com.sindikat.module.visual.Esp;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class EspRenderer {
    private static final EspRenderer INSTANCE = new EspRenderer();
    public static EspRenderer get() { return INSTANCE; }

    private static final RenderPipeline PIPELINE = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("sindikat", "pipeline/esp_through_walls"))
            .withDepthStencilState(Optional.empty())
            .build());

    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private MappableRingBuffer vertexBuffer;

    private EspRenderer() {}

    public void render(LevelRenderContext ctx) {
        Esp esp = ModuleManager.ESP;
        if (esp == null) return;
        List<Esp.Box3> boxes = esp.boxes();
        if (boxes.isEmpty()) return;

        PoseStack ps = ctx.poseStack();
        Vec3 cam = ctx.levelState().cameraRenderState.pos;
        Matrix4fc m = ps.last().pose();
        BufferBuilder buf = new BufferBuilder(ALLOCATOR, PIPELINE.getVertexFormatMode(), PIPELINE.getVertexFormat());

        float lineA = (float) esp.opacity.get();
        float fillA = lineA * 0.18f;
        float t = (float) (esp.thickness.get() * 0.01);
        boolean fill = esp.fill.get();

        for (Esp.Box3 b : boxes) {
            float x1 = (float) (b.x1() - cam.x), y1 = (float) (b.y1() - cam.y), z1 = (float) (b.z1() - cam.z);
            float x2 = (float) (b.x2() - cam.x), y2 = (float) (b.y2() - cam.y), z2 = (float) (b.z2() - cam.z);
            if (fill) box(m, buf, x1, y1, z1, x2, y2, z2, b.r(), b.g(), b.b(), fillA);
            float[] xs = {x1, x2}, ys = {y1, y2}, zs = {z1, z2};
            for (float y : ys) for (float z : zs) box(m, buf, x1, y - t, z - t, x2, y + t, z + t, b.r(), b.g(), b.b(), lineA);
            for (float x : xs) for (float z : zs) box(m, buf, x - t, y1, z - t, x + t, y2, z + t, b.r(), b.g(), b.b(), lineA);
            for (float x : xs) for (float y : ys) box(m, buf, x - t, y - t, z1, x + t, y + t, z2, b.r(), b.g(), b.b(), lineA);
        }
        draw(Minecraft.getInstance(), buf);
    }

    private static void box(Matrix4fc m, BufferBuilder b, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float bl, float a) {
        b.addVertex(m, minX, minY, maxZ).setColor(r, g, bl, a); b.addVertex(m, maxX, minY, maxZ).setColor(r, g, bl, a);
        b.addVertex(m, maxX, maxY, maxZ).setColor(r, g, bl, a); b.addVertex(m, minX, maxY, maxZ).setColor(r, g, bl, a);

        b.addVertex(m, maxX, minY, minZ).setColor(r, g, bl, a); b.addVertex(m, minX, minY, minZ).setColor(r, g, bl, a);
        b.addVertex(m, minX, maxY, minZ).setColor(r, g, bl, a); b.addVertex(m, maxX, maxY, minZ).setColor(r, g, bl, a);

        b.addVertex(m, minX, minY, minZ).setColor(r, g, bl, a); b.addVertex(m, minX, minY, maxZ).setColor(r, g, bl, a);
        b.addVertex(m, minX, maxY, maxZ).setColor(r, g, bl, a); b.addVertex(m, minX, maxY, minZ).setColor(r, g, bl, a);

        b.addVertex(m, maxX, minY, maxZ).setColor(r, g, bl, a); b.addVertex(m, maxX, minY, minZ).setColor(r, g, bl, a);
        b.addVertex(m, maxX, maxY, minZ).setColor(r, g, bl, a); b.addVertex(m, maxX, maxY, maxZ).setColor(r, g, bl, a);

        b.addVertex(m, minX, maxY, maxZ).setColor(r, g, bl, a); b.addVertex(m, maxX, maxY, maxZ).setColor(r, g, bl, a);
        b.addVertex(m, maxX, maxY, minZ).setColor(r, g, bl, a); b.addVertex(m, minX, maxY, minZ).setColor(r, g, bl, a);

        b.addVertex(m, minX, minY, minZ).setColor(r, g, bl, a); b.addVertex(m, maxX, minY, minZ).setColor(r, g, bl, a);
        b.addVertex(m, maxX, minY, maxZ).setColor(r, g, bl, a); b.addVertex(m, minX, minY, maxZ).setColor(r, g, bl, a);
    }

    private void draw(Minecraft client, BufferBuilder buffer) {
        MeshData built = buffer.buildOrThrow();
        MeshData.DrawState state = built.drawState();
        VertexFormat format = state.format();

        int size = state.vertexCount() * format.getVertexSize();
        if (vertexBuffer == null || vertexBuffer.size() < size) {
            if (vertexBuffer != null) vertexBuffer.close();
            vertexBuffer = new MappableRingBuffer(() -> "sindikat esp", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, size);
        }
        CommandEncoder enc = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer.MappedView view = enc.mapBuffer(vertexBuffer.currentBuffer().slice(0, built.vertexBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(built.vertexBuffer(), view.data());
        }
        GpuBuffer vertices = vertexBuffer.currentBuffer();

        GpuBuffer indices;
        VertexFormat.IndexType indexType;
        built.sortQuads(ALLOCATOR, RenderSystem.getProjectionType().vertexSorting());
        indices = PIPELINE.getVertexFormat().uploadImmediateIndexBuffer(built.indexBuffer());
        indexType = built.drawState().indexType();

        GpuBufferSlice dyn = RenderSystem.getDynamicUniforms()
            .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "sindikat esp rendering",
                client.getMainRenderTarget().getColorTextureView(), OptionalInt.empty(),
                client.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
            pass.setPipeline(PIPELINE);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dyn);
            pass.setVertexBuffer(0, vertices);
            pass.setIndexBuffer(indices, indexType);
            pass.drawIndexed(0, 0, state.indexCount(), 1);
        }
        built.close();
        vertexBuffer.rotate();
    }

    public void close() {
        ALLOCATOR.close();
        if (vertexBuffer != null) { vertexBuffer.close(); vertexBuffer = null; }
    }
}
