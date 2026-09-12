package my.simplehud.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.GuiGraphicsExtractor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Layar "HUD Editor".
 *
 * Cara nganggo (ditampilno ugo nok layar):
 *  - Drag (klik kiri + geser)  -> mindah posisi elemen
 *  - Scroll mouse pas nok duwur elemen -> ngganti ukuran (scale)
 *  - Klik kanan                -> ON/OFF elemen
 *  - Tombol "Rampung" / ESC    -> nutup lan nyimpen
 */
public class HudEditScreen extends Screen {

    // Conto teks kanggo saben elemen, mung kanggo preview pas mode edit.
    private static final Map<String, String> PREVIEW_LABEL = new LinkedHashMap<>();
    static {
        PREVIEW_LABEL.put(HudConfig.FPS, "FPS: 120");
        PREVIEW_LABEL.put(HudConfig.COORDS, "X: 12.3 Y: 64.0 Z: -45.6");
        PREVIEW_LABEL.put(HudConfig.DAY, "Hari: 7");
        PREVIEW_LABEL.put(HudConfig.COMPASS, "Arah: N");
        PREVIEW_LABEL.put(HudConfig.PING, "Ping: 34 ms");
    }

    private String draggingKey = null;
    private double dragOffsetX;
    private double dragOffsetY;

    public HudEditScreen() {
        super(Component.literal("Simple HUD - Editor"));
    }

    @Override
    public boolean isPauseScreen() {
        // Ben ora ngentekno wektu/pause pas edit HUD nok server.
        return false;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // Gambar rada peteng dilit ben tulisan HUD ketok jelas, tapi tetep
        // isok ndelok donya nok mburi.
        graphics.fill(0, 0, this.width, this.height, 0x66000000);

        Minecraft mc = Minecraft.getInstance();

        graphics.drawCenteredString(mc.font,
                "Drag = pindah | Klik kanan = ON/OFF | Scroll = ukuran | ESC = rampung",
                this.width / 2, 10, 0xFFFFFF);

        for (Map.Entry<String, String> preview : PREVIEW_LABEL.entrySet()) {
            String key = preview.getKey();
            HudConfig.Entry cfg = SimpleHudClient.CONFIG.get(key);
            String text = preview.getValue();

            int textWidth = mc.font.width(text);
            int textHeight = mc.font.lineHeight;
            double boxW = textWidth * cfg.scale;
            double boxH = textHeight * cfg.scale;

            boolean hovered = mouseX >= cfg.x && mouseX <= cfg.x + boxW
                    && mouseY >= cfg.y && mouseY <= cfg.y + boxH;

            int boxColor = cfg.enabled ? 0x552266FF : 0x55FF3333;
            if (hovered) {
                boxColor = cfg.enabled ? 0x8833AAFF : 0x88FF5555;
            }

            graphics.fill((int) cfg.x - 2, (int) cfg.y - 2,
                    (int) (cfg.x + boxW) + 2, (int) (cfg.y + boxH) + 2, boxColor);
            graphics.outline((int) cfg.x - 2, (int) cfg.y - 2,
                    (int) boxW + 4, (int) boxH + 4, hovered ? 0xFFFFFFFF : 0xFFAAAAAA);

            graphics.pose().pushMatrix();
            graphics.pose().translate((float) cfg.x, (float) cfg.y);
            graphics.pose().scale((float) cfg.scale, (float) cfg.scale);
            int color = cfg.enabled ? 0xFFFFFF : 0xFF9999;
            graphics.drawString(mc.font, text, 0, 0, color, true);
            graphics.pose().popMatrix();

            if (!cfg.enabled) {
                graphics.drawString(mc.font, "(mati)", (int) (cfg.x + boxW + 6), (int) cfg.y, 0xFF6666, false);
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private String findElementAt(double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        for (Map.Entry<String, String> preview : PREVIEW_LABEL.entrySet()) {
            String key = preview.getKey();
            HudConfig.Entry cfg = SimpleHudClient.CONFIG.get(key);
            int textWidth = mc.font.width(preview.getValue());
            int textHeight = mc.font.lineHeight;
            double boxW = textWidth * cfg.scale;
            double boxH = textHeight * cfg.scale;
            if (mouseX >= cfg.x - 2 && mouseX <= cfg.x + boxW + 2
                    && mouseY >= cfg.y - 2 && mouseY <= cfg.y + boxH + 2) {
                return key;
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        String key = findElementAt(mouseX, mouseY);
        if (key != null) {
            if (button == 1) {
                // Klik kanan = toggle ON/OFF
                HudConfig.Entry cfg = SimpleHudClient.CONFIG.get(key);
                cfg.enabled = !cfg.enabled;
                return true;
            } else if (button == 0) {
                // Klik kiri = wiwit drag
                HudConfig.Entry cfg = SimpleHudClient.CONFIG.get(key);
                draggingKey = key;
                dragOffsetX = mouseX - cfg.x;
                dragOffsetY = mouseY - cfg.y;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingKey != null && button == 0) {
            HudConfig.Entry cfg = SimpleHudClient.CONFIG.get(draggingKey);
            cfg.x = mouseX - dragOffsetX;
            cfg.y = mouseY - dragOffsetY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            draggingKey = null;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        String key = findElementAt(mouseX, mouseY);
        if (key != null) {
            HudConfig.Entry cfg = SimpleHudClient.CONFIG.get(key);
            double newScale = cfg.scale + (verticalAmount > 0 ? 0.1 : -0.1);
            cfg.scale = Math.max(0.5, Math.min(3.0, newScale));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void onClose() {
        SimpleHudClient.CONFIG.save();
        super.onClose();
    }
}
