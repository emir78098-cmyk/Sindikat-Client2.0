package com.sindikat.gui;

import com.sindikat.module.*;
import com.sindikat.module.Module;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;

public class ClickGuiScreen extends Screen {
    private static final int SB = 112;
    private static final int BG = 0xF20A1222, SIDE = 0xFF0D172B, CARD = 0xCC121E38,
        ACCENT = 0xFF2DB8FF, TEXT = 0xFFE6EEFF, DIM = 0xFF7C8BA8, OFF = 0xFF2A3550;
    private static final String[] TABS = {"Home", "Combat", "Visuals", "Movement", "Player", "World", "Misc"};

    private record Hit(int x, int y, int w, int h, boolean content, boolean drag, DoubleConsumer action) {}
    private record Card(String title, Module toggle, List<Object> rows) {}

    private final List<Hit> hits = new ArrayList<>();
    private Hit dragging;
    private int tab, totalH, viewY, viewH;
    private double scroll;

    public ClickGuiScreen() { super(Component.literal("Sindikat")); }

    @Override public boolean isPauseScreen() { return false; }
    @Override public void removed() { Config.save(); }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) { onClose(); return true; }
        return super.keyPressed(event);
    }

    private static void round(GuiGraphicsExtractor g, int x, int y, int w, int h, int col) {
        if (w <= 1 || h <= 1) return;
        g.fill(x + 1, y, x + w - 1, y + h, col);
        g.fill(x, y + 1, x + w, y + h - 1, col);
    }

    private List<Card> buildCards() {
        List<Card> out = new ArrayList<>();
        if (tab == 0) {
            for (Category c : Category.values()) {
                List<Object> rows = new ArrayList<>(ModuleManager.of(c));
                if (rows.isEmpty()) rows.add("Coming soon");
                out.add(new Card(c.display, null, rows));
            }
        } else {
            Category c = Category.values()[tab - 1];
            for (Module m : ModuleManager.of(c)) out.add(new Card(m.name, m, new ArrayList<>(m.settings)));
            if (out.isEmpty()) out.add(new Card(c.display, null, new ArrayList<>(List.of("Coming soon"))));
        }
        return out;
    }

    private static int rowH(Object o) { return o instanceof NumSetting ? 28 : 18; }

    private static int cardH(Card c) {
        int h = 30;
        for (Object o : c.rows()) h += rowH(o);
        return h;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        hits.clear();
        g.fill(0, 0, width, height, 0xAA040810);
        int w = Math.min(580, width - 20), h = Math.min(340, height - 20);
        int x = (width - w) / 2, y = (height - h) / 2;

        round(g, x - 1, y - 1, w + 2, h + 2, 0xFF1B6FA8);
        round(g, x, y, w, h, BG);
        g.fill(x + 1, y + 1, x + SB, y + h - 1, SIDE);
        g.text(font, Component.literal("SINDIKAT").withStyle(ChatFormatting.BOLD), x + 14, y + 12, ACCENT, false);

        g.text(font, "x", x + w - 14, y + 10, DIM, false);
        hits.add(new Hit(x + w - 24, y + 4, 22, 18, false, false, v -> onClose()));

        for (int i = 0; i < TABS.length; i++) {
            int ty = y + 36 + i * 26;
            boolean sel = i == tab, hov = mx >= x + 8 && mx <= x + SB - 8 && my >= ty && my <= ty + 22;
            if (sel) round(g, x + 8, ty, SB - 16, 22, 0x552DB8FF);
            else if (hov) round(g, x + 8, ty, SB - 16, 22, 0x22FFFFFF);
            g.text(font, TABS[i], x + 20, ty + 7, sel ? TEXT : DIM, false);
            final int idx = i;
            hits.add(new Hit(x + 8, ty, SB - 16, 22, false, false, v -> { tab = idx; scroll = 0; }));
        }

        round(g, x + 8, y + h - 36, SB - 16, 28, CARD);
        g.fill(x + 14, y + h - 26, x + 18, y + h - 22, 0xFF3DFF8A);
        g.text(font, "Client Active", x + 23, y + h - 29, TEXT, false);
        g.text(font, "Version 1.0.0", x + 23, y + h - 18, DIM, false);

        g.text(font, Component.literal(TABS[tab]).withStyle(ChatFormatting.BOLD), x + SB + 12, y + 12, TEXT, false);

        int viewX = x + SB + 10, viewW = w - SB - 20;
        viewY = y + 30; viewH = h - 40;
        int colW = (viewW - 8) / 2;
        int[] col = {0, 0};
        g.enableScissor(viewX, viewY, viewX + viewW, viewY + viewH);
        for (Card c : buildCards()) {
            int ci = col[0] <= col[1] ? 0 : 1;
            drawCard(g, c, viewX + ci * (colW + 8), viewY + col[ci] - (int) scroll, colW);
            col[ci] += cardH(c) + 8;
        }
        g.disableScissor();
        totalH = Math.max(col[0], col[1]);
        scroll = Math.max(0, Math.min(Math.max(0, totalH - viewH), scroll));
    }

    private void toggle(GuiGraphicsExtractor g, int px, int py, boolean on, Runnable r) {
        round(g, px, py, 22, 10, on ? ACCENT : OFF);
        g.fill(on ? px + 13 : px + 1, py + 1, on ? px + 21 : px + 9, py + 9, 0xFFFFFFFF);
        hits.add(new Hit(px - 2, py - 2, 26, 14, true, false, v -> r.run()));
    }

    private static String fmt(double v) { return v == Math.floor(v) ? String.valueOf((int) v) : String.valueOf(Math.round(v * 100) / 100.0); }

    private void drawCard(GuiGraphicsExtractor g, Card c, int x, int y, int w) {
        round(g, x, y, w, cardH(c), CARD);
        g.text(font, Component.literal(c.title()).withStyle(ChatFormatting.BOLD), x + 8, y + 8, TEXT, false);
        if (c.toggle() != null) toggle(g, x + w - 30, y + 7, c.toggle().isEnabled(), c.toggle()::toggle);
        g.fill(x + 8, y + 22, x + w - 8, y + 23, 0x332DB8FF);

        int ry = y + 26;
        for (Object o : c.rows()) {
            int rh = rowH(o);
            if (o instanceof Module m) {
                g.text(font, m.name, x + 8, ry + 4, TEXT, false);
                toggle(g, x + w - 30, ry + 3, m.isEnabled(), m::toggle);
            } else if (o instanceof BoolSetting b) {
                g.text(font, b.name, x + 8, ry + 4, TEXT, false);
                toggle(g, x + w - 30, ry + 3, b.get(), b::toggle);
            } else if (o instanceof ModeSetting ms) {
                g.text(font, ms.name, x + 8, ry + 4, TEXT, false);
                String v = ms.get();
                g.text(font, v, x + w - 10 - font.width(v), ry + 4, ACCENT, false);
                hits.add(new Hit(x + 4, ry, w - 8, rh, true, false, d -> ms.cycle()));
            } else if (o instanceof NumSetting n) {
                g.text(font, n.name, x + 8, ry + 2, TEXT, false);
                String v = fmt(n.get());
                g.text(font, v, x + w - 10 - font.width(v), ry + 2, ACCENT, false);
                int bx = x + 8, bw = w - 16, by = ry + 16;
                double pct = (n.get() - n.min) / (n.max - n.min);
                round(g, bx, by, bw, 4, OFF);
                round(g, bx, by, (int) (bw * pct), 4, ACCENT);
                int kx = bx + (int) (bw * pct);
                g.fill(kx - 2, by - 2, kx + 2, by + 6, 0xFFFFFFFF);
                hits.add(new Hit(bx - 3, ry + 10, bw + 6, 14, true, true,
                    mxv -> n.set(n.min + (mxv - bx) / bw * (n.max - n.min))));
            } else if (o instanceof String s) {
                g.text(font, s, x + 8, ry + 4, DIM, false);
            }
            ry += rh;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            double mx = event.x(), my = event.y();
            for (int i = hits.size() - 1; i >= 0; i--) {
                Hit h = hits.get(i);
                if (mx < h.x() || mx > h.x() + h.w() || my < h.y() || my > h.y() + h.h()) continue;
                if (h.content() && (my < viewY || my > viewY + viewH)) continue;
                h.action().accept(mx);
                if (h.drag()) dragging = h;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (dragging != null) { dragging.action().accept(event.x()); return true; }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = null;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        scroll = Math.max(0, Math.min(Math.max(0, totalH - viewH), scroll - sy * 18));
        return true;
    }
}
