package dev.menudoll;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Config: config/menu_doll.json. Reloaded on the fly (once per second if the file has changed).
 * Two independent sets of settings: "title" (main menu) and "pause" (in-game pause menu).
 */
public final class MenuDollConfig {

    /** One set of doll settings. */
    public static final class DollSettings {
        /** Whether to show the doll. */
        public boolean enabled = true;
        /** On which side of the buttons: "right" or "left". */
        public String side = "right";
        /** Doll size multiplier (1.0 = standard). */
        public float scale = 1.0f;
        /** Horizontal shift in interface units (plus = right). */
        public float offsetX = 0f;
        /** Vertical shift in interface units (plus = down). */
        public float offsetY = 0f;
        /** Show nickname above the avatar. */
        public boolean showName = true;
        /** Opacity of the black bar beneath the username, from 0.0 to 1.0. */
        public float nameBackgroundOpacity = 0.5f;

        void sanitize() {
            if (side == null || !(side.equalsIgnoreCase("left") || side.equalsIgnoreCase("right"))) side = "right";
            scale = Math.max(0.1f, Math.min(scale, 5f));
            nameBackgroundOpacity = Math.max(0f, Math.min(nameBackgroundOpacity, 1f));
        }

        public boolean isLeft() {
            return "left".equalsIgnoreCase(side);
        }
    }

    /** Main menu. */
    public DollSettings title = new DollSettings();
    /** Pause menu (during gameplay). Disabled by default. */
    public DollSettings pause = new DollSettings();

    public MenuDollConfig() {
        pause.enabled = false;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static MenuDollConfig current = new MenuDollConfig();
    private static Path path;
    private static long lastModified = Long.MIN_VALUE;
    private static long lastCheck = 0L;

    /** Saves the current settings to a file (called when exiting the settings screen). */
    public static void save() {
        try {
            if (path == null) {
                path = FabricLoader.getInstance().getConfigDir().resolve("menu_doll.json");
            }
            try (Writer w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(current, w);
            }
            // We store the modification time so that the auto-reload doesn't mistake it for an external edit.
            lastModified = Files.getLastModifiedTime(path).toMillis();
        } catch (Exception e) {
            // If you didn't manage to record it, it's not critical.
        }
    }

    /** Reset all settings to default values ​​(written to the file upon save()). */
    public static void resetToDefaults() {
        current = new MenuDollConfig();
    }

    public static MenuDollConfig get() {
        long now = System.currentTimeMillis();
        if (path == null || now - lastCheck > 1000L) {
            lastCheck = now;
            reloadIfChanged();
        }
        return current;
    }

    private static void reloadIfChanged() {
        try {
            if (path == null) {
                path = FabricLoader.getInstance().getConfigDir().resolve("menu_doll.json");
            }
            if (!Files.exists(path)) {
                try (Writer w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                    GSON.toJson(new MenuDollConfig(), w);
                }
            }
            long modified = Files.getLastModifiedTime(path).toMillis();
            if (modified == lastModified) return;
            lastModified = modified;

            try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                JsonElement root = JsonParser.parseReader(r);
                if (root == null || !root.isJsonObject()) return;
                JsonObject obj = root.getAsJsonObject();

                MenuDollConfig loaded;
                if (obj.has("title") || obj.has("pause")) {
                    loaded = GSON.fromJson(obj, MenuDollConfig.class);
                } else {
                    // Old format (settings located directly at the root) — moving to the "title" group.
                    loaded = new MenuDollConfig();
                    loaded.title = GSON.fromJson(obj, DollSettings.class);
                }
                if (loaded == null) return;
                if (loaded.title == null) loaded.title = new DollSettings();
                if (loaded.pause == null) {
                    loaded.pause = new DollSettings();
                    loaded.pause.enabled = false;
                }
                loaded.title.sanitize();
                loaded.pause.sanitize();
                current = loaded;
            }
        } catch (Exception e) {
            // If the JSON is malformed or the file is inaccessible, we stick with the last working settings.
        }
    }
}
