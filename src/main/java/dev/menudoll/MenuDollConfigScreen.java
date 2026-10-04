package dev.menudoll;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Settings screen for the Mod Menu. Two mode tabs: main menu and pause menu (in-game).
 * Size, shifts, and transparency are sliders.
 */
public final class MenuDollConfigScreen extends Screen {

    private final Screen parent;
    private final boolean pauseMode;

    public MenuDollConfigScreen(Screen parent, boolean pauseMode) {
        super(Component.translatable("menu_doll.config.title"));
        this.parent = parent;
        this.pauseMode = pauseMode;
    }

    private MenuDollConfig.DollSettings s() {
        MenuDollConfig c = MenuDollConfig.get();
        return pauseMode ? c.pause : c.title;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 100;
        int step = Math.max(22, Math.min(26, (this.height - 110) / 8));
        int y = 30;

        // Preset selector.
        addRenderableWidget(Button.builder(
                Component.translatable("menu_doll.config.preset",
                        Component.translatable(pauseMode ? "menu_doll.config.preset.pause" : "menu_doll.config.preset.title")),
                b -> {
                    MenuDollConfig.save();
                    this.minecraft.setScreen(new MenuDollConfigScreen(parent, !pauseMode));
                }).bounds(left, y, 200, 20).build());
        y += step;

        addRenderableWidget(toggle(left, y, "menu_doll.config.enabled",
                () -> s().enabled, v -> s().enabled = v));
        y += step;

        addRenderableWidget(sideButton(left, y));
        y += step;

        addRenderableWidget(toggle(left, y, "menu_doll.config.show_name",
                () -> s().showName, v -> s().showName = v));
        y += step;

        addRenderableWidget(new FloatSlider(left, y, 200, 20, "menu_doll.config.scale", 0.1f, 5f, 0.05f,
                s().scale, v -> s().scale = v, 2));
        y += step;

        addRenderableWidget(new FloatSlider(left, y, 200, 20, "menu_doll.config.offset_x", -400f, 400f, 1f,
                s().offsetX, v -> s().offsetX = v, 0));
        y += step;

        addRenderableWidget(new FloatSlider(left, y, 200, 20, "menu_doll.config.offset_y", -400f, 400f, 1f,
                s().offsetY, v -> s().offsetY = v, 0));
        y += step;

        addRenderableWidget(new FloatSlider(left, y, 200, 20, "menu_doll.config.name_background", 0f, 1f, 0.05f,
                s().nameBackgroundOpacity, v -> s().nameBackgroundOpacity = v, 2));

        int bottom = this.height - 28;
        addRenderableWidget(Button.builder(Component.translatable("menu_doll.config.reset"), b -> {
            MenuDollConfig.resetToDefaults();
            this.minecraft.setScreen(new MenuDollConfigScreen(parent, pauseMode));
        }).bounds(left, bottom - 24, 200, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("menu_doll.config.done"), b -> onClose())
                .bounds(left, bottom, 200, 20).build());
    }

    private Button toggle(int x, int y, String key, Supplier<Boolean> get, Consumer<Boolean> set) {
        return Button.builder(toggleText(key, get.get()), b -> {
            set.accept(!get.get());
            b.setMessage(toggleText(key, get.get()));
        }).bounds(x, y, 200, 20).build();
    }

    private static Component toggleText(String key, boolean value) {
        return Component.translatable("menu_doll.config.toggle", Component.translatable(key),
                Component.translatable(value ? "menu_doll.config.on" : "menu_doll.config.off"));
    }

    private Button sideButton(int x, int y) {
        return Button.builder(sideText(), b -> {
            MenuDollConfig.DollSettings c = s();
            c.side = c.isLeft() ? "right" : "left";
            b.setMessage(sideText());
        }).bounds(x, y, 200, 20).build();
    }

    private Component sideText() {
        return Component.translatable("menu_doll.config.side",
                Component.translatable(s().isLeft() ? "menu_doll.config.side.left" : "menu_doll.config.side.right"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        g.text(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, 12, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        MenuDollConfig.save();
        this.minecraft.setScreen(parent);
    }

    /** Numeric slider: the value is constrained by the step and boundaries. */
    private static final class FloatSlider extends AbstractSliderButton {
        private final String key;
        private final float min;
        private final float max;
        private final float step;
        private final int decimals;
        private final Consumer<Float> setter;

        FloatSlider(int x, int y, int w, int h, String key, float min, float max, float step,
                    float initial, Consumer<Float> setter, int decimals) {
            super(x, y, w, h, Component.empty(), (clamp(initial, min, max) - min) / (max - min));
            this.key = key;
            this.min = min;
            this.max = max;
            this.step = step;
            this.decimals = decimals;
            this.setter = setter;
            updateMessage();
        }

        private float current() {
            float raw = min + (float) this.value * (max - min);
            float snapped = Math.round((raw - min) / step) * step + min;
            return clamp(Math.round(snapped * 1000f) / 1000f, min, max);
        }

        private static float clamp(float v, float min, float max) {
            return Math.max(min, Math.min(max, v));
        }

        @Override
        protected void updateMessage() {
            float v = current();
            String text = decimals == 0 ? String.valueOf(Math.round(v)) : String.format("%." + decimals + "f", v);
            setMessage(Component.translatable("menu_doll.config.slider", Component.translatable(key), text));
        }

        @Override
        protected void applyValue() {
            float v = current();
            setter.accept(v);
            this.value = (v - min) / (max - min);   // sticking to the pace
        }
    }
}
