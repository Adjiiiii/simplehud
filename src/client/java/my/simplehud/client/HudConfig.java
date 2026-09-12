package my.simplehud.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Nyimpen setelan HUD: saben elemen (FPS, COORDS, DAY, COMPASS, PING)
 * duwe: enabled (ON/OFF), x, y, lan scale (ukuran).
 * Disimpen dadi file JSON nok config/simplehud.json, dadi setelan
 * gak ilang pas metu/mlebu game maneh.
 */
public class HudConfig {

    /** Setelan siji elemen HUD. */
    public static class Entry {
        public boolean enabled = true;
        public double x;
        public double y;
        public double scale = 1.0;

        public Entry(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    // Kunci kanggo saben elemen HUD sing ono.
    public static final String FPS = "fps";
    public static final String COORDS = "coords";
    public static final String DAY = "day";
    public static final String COMPASS = "compass";
    public static final String PING = "ping";

    public Map<String, Entry> elements = new LinkedHashMap<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("simplehud.json");

    public static HudConfig createDefault() {
        HudConfig cfg = new HudConfig();
        cfg.elements.put(FPS, new Entry(4, 4));
        cfg.elements.put(COORDS, new Entry(4, 16));
        cfg.elements.put(DAY, new Entry(4, 28));
        cfg.elements.put(COMPASS, new Entry(4, 40));
        cfg.elements.put(PING, new Entry(4, 52));
        return cfg;
    }

    public Entry get(String key) {
        return elements.computeIfAbsent(key, k -> new Entry(4, 4));
    }

    public static HudConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                HudConfig loaded = GSON.fromJson(reader, HudConfig.class);
                if (loaded != null && loaded.elements != null) {
                    // Pastekno kabeh elemen default ono, seumpomo ana elemen anyar sing
                    // ditambahno nok versi sabanjure.
                    HudConfig def = createDefault();
                    for (String key : def.elements.keySet()) {
                        loaded.elements.putIfAbsent(key, def.elements.get(key));
                    }
                    return loaded;
                }
            } catch (IOException | com.google.gson.JsonSyntaxException e) {
                System.err.println("[SimpleHUD] Gagal moco config, nganggo default. " + e);
            }
        }
        return createDefault();
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            System.err.println("[SimpleHUD] Gagal nyimpen config. " + e);
        }
    }
}
