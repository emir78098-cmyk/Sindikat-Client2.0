package com.sindikat.module.visual;

import com.sindikat.module.*;
import com.sindikat.module.Module;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import java.util.ArrayList;
import java.util.List;

public class Esp extends Module {
    public final ModeSetting mode = add(new ModeSetting("Mode", "Box", "Glow", "Both"));
    public final BoolSetting players = add(new BoolSetting("Players", true));
    public final BoolSetting hostile = add(new BoolSetting("Hostile", true));
    public final BoolSetting passive = add(new BoolSetting("Passive", false));
    public final BoolSetting items = add(new BoolSetting("Items", false));
    public final BoolSetting fill = add(new BoolSetting("Fill", true));
    public final NumSetting thickness = add(new NumSetting("Thickness", 2, 1, 5, 1));
    public final NumSetting opacity = add(new NumSetting("Opacity", 0.9, 0.2, 1.0, 0.05));
    public final NumSetting range = add(new NumSetting("Range", 64, 16, 256, 8));

    /** Готовый к отрисовке бокс (мировые координаты). */
    public record Box3(double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b) {}

    private volatile List<Box3> boxes = List.of();

    public Esp() { super("ESP", Category.VISUALS); }

    public List<Box3> boxes() { return boxes; }

    public boolean accepts(Entity e) {
        if (mc.player == null || e == mc.player || !e.isAlive()) return false;
        double r = range.get();
        if (mc.player.distanceToSqr(e) > r * r) return false;
        if (e instanceof Player) return players.get();
        if (e instanceof Enemy) return hostile.get();
        if (e instanceof Mob) return passive.get();
        if (e instanceof ItemEntity) return items.get();
        return false;
    }

    public int color(Entity e) {
        if (e instanceof Player) return 0x2DB8FF;
        if (e instanceof Enemy) return 0xFF4D4D;
        if (e instanceof Mob) return 0x4DFF88;
        return 0xFFD84D;
    }

    /** Для миксина: обводка (Glow). */
    public boolean shouldGlow(Entity e) {
        return isEnabled() && !mode.is("Box") && e.level().isClientSide() && accepts(e);
    }

    /** Фаза extraction: собираем боксы. */
    public void extract(LevelExtractionContext ctx) {
        if (!isEnabled() || mode.is("Glow") || mc.level == null || mc.player == null) {
            boxes = List.of();
            return;
        }
        float pt = ctx.deltaTracker().getGameTimeDeltaPartialTick(true);
        List<Box3> out = new ArrayList<>();
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!accepts(e)) continue;
            double dx = Mth.lerp(pt, e.xOld, e.getX()) - e.getX();
            double dy = Mth.lerp(pt, e.yOld, e.getY()) - e.getY();
            double dz = Mth.lerp(pt, e.zOld, e.getZ()) - e.getZ();
            AABB bb = e.getBoundingBox();
            int c = color(e);
            out.add(new Box3(bb.minX + dx, bb.minY + dy, bb.minZ + dz, bb.maxX + dx, bb.maxY + dy, bb.maxZ + dz,
                ((c >> 16) & 255) / 255f, ((c >> 8) & 255) / 255f, (c & 255) / 255f));
        }
        boxes = out;
    }
}
