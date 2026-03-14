package me.imgrui.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.caffeinemc.mods.sodium.client.config.ConfigManager;
import net.caffeinemc.mods.sodium.client.config.structure.OptionPage;
import net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            var page = (OptionPage) ConfigManager.CONFIG.getModOptions().stream().filter(i -> i.configId().equals("voxy-extra")).findFirst().get().pages().get(0);
            var screen = (VideoSettingsScreen)VideoSettingsScreen.createScreen(parent, page);
            return screen;
        };
    }
}