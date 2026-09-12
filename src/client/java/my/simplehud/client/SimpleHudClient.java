package my.simplehud.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Titik mlebu (entrypoint) client-side kanggo mod Simple HUD.
 *
 * PENTING: mod iki 100% client-side (ndelok "environment": "client" nok
 * fabric.mod.json). Dadi ora ono kode sing mlaku nok server, lan ora
 * butuh dipasang nok server ben isok mlaku - pas kowe konek nok server
 * apa wae (vanilla utawa modded), mod iki tetep nyala normal nok
 * device-mu dhewe, ora isok "dipateni" utawa mogok gara-gara server.
 */
public class SimpleHudClient implements ClientModInitializer {

    public static final String MOD_ID = "simplehud";

    /** Config sing disimpen ning config/simplehud.json */
    public static HudConfig CONFIG;

    /** Keybind kanggo mbukak layar HUD Editor (unbound dhisik, kudu di-bind dhewe). */
    public static KeyMapping OPEN_EDITOR_KEY;

    /** Kategori keybind, ben nongol dadi grup dhewe nok Options > Controls. */
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MOD_ID, "main")
    );

    // --- Kanggo ngitung FPS dhewe (ora gumantung nok field internal game) ---
    private static int frameCounter = 0;
    private static long lastFpsSampleTime = System.currentTimeMillis();
    private static int currentFps = 0;

    @Override
    public void onInitializeClient() {
        CONFIG = HudConfig.load();

        // Daftarno keybind. Default UNKNOWN = ora dibind, user kudu ngatur
        // dhewe nok Options > Controls > Simple HUD, supaya ora tabrakan
        // karo keybind mod/game liyane.
        OPEN_EDITOR_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.simplehud.open_editor",
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                CATEGORY
        ));

        // Cek saben tick klien: nek tombol dipencet lan gak ono layar liyo
        // sing kebuka, bukak layar editor HUD.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_EDITOR_KEY.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new HudEditScreen());
                }
            }
        });

        // Daftarno lapisan render HUD. Kabeh elemen (FPS/koordinat/dll)
        // digambar bareng nok siji layer, posisi diatur miturut CONFIG.
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(MOD_ID, "simple_hud_layer"),
                SimpleHudClient::render
        );
    }

    private static void render(net.fabricmc.fabric.api.client.rendering.v1.hud.GuiGraphicsExtractor graphics,
                                net.minecraft.client.DeltaTracker tickCounter) {
        Minecraft mc = Minecraft.getInstance();

        // Update pancacah FPS: fungsi iki dipanggil pas sedelo pisan (per frame),
        // dadi cukup ngitung piro kaping dipanggil saben detik.
        frameCounter++;
        long now = System.currentTimeMillis();
        if (now - lastFpsSampleTime >= 1000) {
            currentFps = frameCounter;
            frameCounter = 0;
            lastFpsSampleTime = now;
        }

        // Nek gak ono player (durung mlebu donya), gak usah render sing butuh player.
        LocalPlayer player = mc.player;

        drawElement(graphics, mc, HudConfig.FPS, "FPS: " + currentFps);

        if (player != null) {
            drawElement(graphics, mc, HudConfig.COORDS, String.format(
                    "X: %.1f Y: %.1f Z: %.1f",
                    player.getX(), player.getY(), player.getZ()
            ));

            if (mc.level != null) {
                long day = mc.level.getDayTime() / 24000L;
                drawElement(graphics, mc, HudConfig.DAY, "Hari: " + day);
            }

            drawElement(graphics, mc, HudConfig.COMPASS, "Arah: " + compassText(player.getYRot()));

            String pingText = "Ping: -- ms";
            if (mc.getConnection() != null) {
                PlayerInfo info = mc.getConnection().getPlayerInfo(player.getUUID());
                if (info != null) {
                    pingText = "Ping: " + info.getLatency() + " ms";
                }
            }
            drawElement(graphics, mc, HudConfig.PING, pingText);
        }
    }

    /** Gambar siji elemen HUD miturut posisi & ukuran nok config, nek enabled. */
    private static void drawElement(net.fabricmc.fabric.api.client.rendering.v1.hud.GuiGraphicsExtractor graphics,
                                     Minecraft mc, String key, String text) {
        HudConfig.Entry entry = CONFIG.get(key);
        if (!entry.enabled) return;

        graphics.pose().pushMatrix();
        graphics.pose().translate((float) entry.x, (float) entry.y);
        graphics.pose().scale((float) entry.scale, (float) entry.scale);
        graphics.drawString(mc.font, text, 0, 0, 0xFFFFFF, true);
        graphics.pose().popMatrix();
    }

    /** Ngowahi sudut yaw (derajat) dadi teks kompas kayata N, NE, E, dst. */
    public static String compassText(float yawDegrees) {
        // 0 derajat = ngadep Selatan (South) nok Minecraft, mula diowahi.
        float yaw = Mth.wrapDegrees(yawDegrees);
        String[] names = {"S", "SW", "W", "NW", "N", "NE", "E", "SE", "S"};
        int index = Math.round(yaw / 45f) + 4;
        index = Math.max(0, Math.min(names.length - 1, index));
        return names[index];
    }
}
