package dev.menudoll.mixin;

import dev.menudoll.MenuDoll;
import dev.menudoll.MenuDollConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {

    // We draw at the very end, on top of the background and buttons, without altering anything else.
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void menudoll$drawDoll(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   float partialTick, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        MenuDoll.extract(graphics, self.width, self.height, mouseX, mouseY, MenuDollConfig.get().title);
    }
}
