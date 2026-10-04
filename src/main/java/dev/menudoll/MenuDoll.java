package dev.menudoll;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.player.PlayerSkin;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Renders the player skin model in the main menu and pause menu.
 * Since there is no game world or entity present in the main menu, we manually construct the render state
 * (doing the same thing the InventoryScreen does for the model in the inventory).
 */
public final class MenuDoll {

    private static volatile PlayerSkin skin;
    private static boolean requested;

    private MenuDoll() {}

    public static void extract(GuiGraphicsExtractor g, int width, int height, int mouseX, int mouseY,
                               MenuDollConfig.DollSettings cfg) {
        if (cfg == null || !cfg.enabled) return;

        Minecraft mc = Minecraft.getInstance();
        PlayerSkin playerSkin = resolveSkin(mc);
        if (playerSkin == null) return;

        // Available space to the side of the button column (buttons ~200px wide, centered).
        float regionWidth;
        float cx;
        if (cfg.isLeft()) {
            regionWidth = Math.max(width / 2f - 100f, 40f);
            cx = regionWidth / 2f;
        } else {
            float regionLeft = width / 2f + 100f;
            regionWidth = Math.max(width - regionLeft, 40f);
            cx = regionLeft + regionWidth / 2f;
        }
        float cy = height * 0.55f;

        // The size is in interface units (like for buttons), so it scales along with the GUI scale.
        // Then, the multiplier from the config is applied.
        float size = Math.min(60f, Math.min(height * 0.3f, regionWidth * 0.55f)) * cfg.scale;

        // Offset from the config.
        cx += cfg.offsetX;
        cy += cfg.offsetY;

        // Turning towards the cursor: uses the same formulas as the vanilla inventory doll.
        float dist = Math.max(size * 1.5f, 1f);
        float ax = (float) Math.atan((cx - mouseX) / dist);
        float ay = (float) Math.atan((cy - mouseY) / dist);

        AvatarRenderState state = new AvatarRenderState();
        state.skin = playerSkin;
        state.boundingBoxWidth = 0.6f;
        state.boundingBoxHeight = 1.8f;
        state.eyeHeight = 1.62f;
        state.bodyRot = 180f + ax * 20f;    // body
        state.yRot = ax * 20f;              // head relative to body (totaling ~40°, just like in vanilla)
        state.xRot = -ay * 20f;             // tilting the head up/down
        state.ageInTicks = (System.currentTimeMillis() % 1_000_000L) / 50f;
        state.showHat = true;
        state.showJacket = true;
        state.showLeftSleeve = true;
        state.showRightSleeve = true;
        state.showLeftPants = true;
        state.showRightPants = true;
        state.showCape = true;

        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf camera = new Quaternionf().rotateX(ay * 20f * ((float) Math.PI / 180f));
        rotation.mul(camera);

        Vector3f translation = new Vector3f(0f, state.boundingBoxHeight / 2f + 0.0625f, 0f);

        int x0 = Math.round(cx - size * 1.5f);
        int x1 = Math.round(cx + size * 1.5f);
        int y0 = Math.round(cy - size * 1.8f);
        int y1 = Math.round(cy + size * 1.8f);

        g.entity(state, size, translation, rotation, camera, x0, y0, x1, y1);

        // Nickname above the head: semi-transparent black background + white text.
        if (cfg.showName) {
            var font = mc.font;
            String name = mc.getUser().getName();
            int textW = font.width(name);
            int tx = Math.round(cx - textW / 2f);
            int ty = Math.round(cy - size * 0.9f) - 16;     // above the top of the doll's head
            int alpha = Math.round(cfg.nameBackgroundOpacity * 255f);
            g.fill(tx - 3, ty - 2, tx + textW + 3, ty + font.lineHeight + 1, alpha << 24);
            g.text(font, name, tx, ty, 0xFFFFFFFF);
        }
    }

    private static PlayerSkin resolveSkin(Minecraft mc) {
        PlayerSkin cached = skin;
        if (cached != null) return cached;

        GameProfile profile = mc.getGameProfile();
        if (!requested) {
            requested = true;
            // The actual skin loads asynchronously.
            mc.getSkinManager().get(profile)
                    .thenAccept(opt -> opt.ifPresent(s -> skin = s));
        }
        // While loading — default skin (Steve/Alex based on UUID).
        return DefaultPlayerSkin.get(profile);
    }
}
