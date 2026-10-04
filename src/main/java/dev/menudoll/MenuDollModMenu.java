package dev.menudoll;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Connects the settings screen to the mod's configuration button in Mod Menu. */
public final class MenuDollModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new MenuDollConfigScreen(parent, false);
    }
}
