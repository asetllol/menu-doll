package dev.menudoll.mixin;

import dev.menudoll.MenuDoll;
import dev.menudoll.MenuDollConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Pause menu (Esc in-game). The hook is placed on the base `Screen` because `PauseScreen`
 * might not override `extractRenderState`; the dummy is drawn only if the screen is `PauseScreen`.
 */
@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void menudoll$drawPauseDoll(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                        float partialTick, CallbackInfo ci) {
        if ((Object) this instanceof PauseScreen) {
            Screen self = (Screen) (Object) this;
            MenuDoll.extract(graphics, self.width, self.height, mouseX, mouseY, MenuDollConfig.get().pause);
        }
    }
}
