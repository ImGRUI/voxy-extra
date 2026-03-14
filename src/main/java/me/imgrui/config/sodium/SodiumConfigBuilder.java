package me.imgrui.config.sodium;

import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ModOptionsBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatterImpls;
import net.caffeinemc.mods.sodium.client.gui.prompt.ScreenPromptable;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static me.imgrui.config.VoxyExtraConfig.CONFIG;

import me.imgrui.config.sodium.modals.EditLinkedServersModal;
import me.imgrui.config.sodium.modals.EditServerBlacklistModal;
import me.imgrui.gui.modal.Modal;

public class SodiumConfigBuilder implements ConfigEntryPoint {
    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        ModOptionsBuilder options = builder.registerOwnModOptions()
            .setNonTintedIcon(Identifier.parse("voxy-extra:icon.png"))
            .setColorTheme(builder.createColorTheme().setBaseThemeRGB(0xfdff93));

        options.addPage(this.createRenderPage(builder));

        if (FabricLoader.getInstance().isModLoaded("flashback")) {
            options.addPage(this.createFlashbackPage(builder));
        }
        
        options.addPage(this.createMultiplayerPage(builder));
    }
    
    private OptionPageBuilder createRenderPage(ConfigBuilder builder) {
        return builder.createOptionPage()
            .setName(Component.translatable("voxy-extra.option.rendering"))
            .addOptionGroup(builder.createOptionGroup()
                .addOption(builder.createBooleanOption(Identifier.parse("voxy-extra:nether_fog"))
                    .setName(Component.translatable("voxy-extra.option.nether_fog"))
                    .setTooltip(Component.translatable("voxy-extra.option.nether_fog.tooltip"))
                    .setDefaultValue(true)
                    .setStorageHandler(CONFIG::save)
                    .setBinding(CONFIG::setNetherFogEnabled, CONFIG::isNetherFogEnabled)
                )
                .addOption(builder.createIntegerOption(Identifier.parse("voxy-extra:start_nether_fog"))
                    .setName(Component.translatable("voxy-extra.option.nether_fog.start"))
                    .setTooltip(Component.translatable("voxy-extra.option.nether_fog.start.tooltip"))
                    .setRange(0, 300, 1)
                    .setDefaultValue(100)
                    .setValueFormatter(ControlValueFormatterImpls.percentage())
                    .setStorageHandler(CONFIG::save)
                    .setBinding(CONFIG::setNetherFogStartMultiplier, CONFIG::getNetherFogStartMultiplier)
                )
                .addOption(builder.createIntegerOption(Identifier.parse("voxy-extra:end_nether_fog"))
                    .setName(Component.translatable("voxy-extra.option.nether_fog.end"))
                    .setTooltip(Component.translatable("voxy-extra.option.nether_fog.end.tooltip"))
                    .setRange(0, 300, 1)
                    .setDefaultValue(100)
                    .setValueFormatter(ControlValueFormatterImpls.percentage())
                    .setStorageHandler(CONFIG::save)
                    .setBinding(CONFIG::setNetherFogEndMultiplier, CONFIG::getNetherFogEndMultiplier)
                ));
    }

    private OptionPageBuilder createFlashbackPage(ConfigBuilder builder) {
        return builder.createOptionPage()
            .setName(Component.literal("Flashback"))
            .addOptionGroup(builder.createOptionGroup()
                .addOption(builder.createBooleanOption(Identifier.parse("voxy-extra:flashback_copy_lods"))
                    .setName(Component.translatable("voxy-extra.option.flashback_copy_lods"))
                    .setTooltip(Component.translatable("voxy-extra.option.flashback_copy_lods.tooltip"))
                    .setDefaultValue(false)
                    .setStorageHandler(CONFIG::save)
                    .setBinding(CONFIG::setFlashbackCopyLodsEnable, CONFIG::isFlashbackCopyLodsEnabled)
                )
                .addOption(builder.createBooleanOption(Identifier.parse("voxy-extra:flashback_ingest"))
                    .setName(Component.translatable("voxy-extra.option.flashback_ingest"))
                    .setTooltip(Component.translatable("voxy-extra.option.flashback_ingest.tooltip"))
                    .setDefaultValue(true)
                    .setStorageHandler(CONFIG::save)
                    .setBinding(CONFIG::setFlashbackIngestEnabled, CONFIG::isFlashbackIngestEnabled)
                ));
    }

    private OptionPageBuilder createMultiplayerPage(ConfigBuilder builder) {
        return builder.createOptionPage()
            .setName(Component.translatable("voxy-extra.option.multiplayer"))
            .addOptionGroup(builder.createOptionGroup()
                .addOption(builder.createBooleanOption(Identifier.parse("voxy-extra:linked_servers"))
                    .setName(Component.translatable("voxy-extra.option.linked_servers"))
                    .setTooltip(Component.translatable("voxy-extra.option.linked_servers.tooltip"))
                    .setDefaultValue(false)
                    .setStorageHandler(CONFIG::save)
                    .setBinding(CONFIG::setLinkedServersEnabled, CONFIG::isLinkedServersEnabled)
                )
                .addOption(builder.createExternalButtonOption(Identifier.parse("voxy-extra:edit_linked_servers"))
                    .setName(Component.translatable("voxy-extra.option.edit_linked_servers"))
                    .setTooltip(Component.translatable("voxy-extra.modal.open_editor_modal.tooltip"))
                    .setScreenConsumer((Screen screen) -> {
                        if (screen instanceof ScreenPromptable promptable) {
                            Modal modal = EditLinkedServersModal.create(promptable);
                            modal.init();
                            promptable.setPrompt(modal);
                        }
                    })
                )
                .addOption(builder.createBooleanOption(Identifier.parse("voxy-extra:server_blacklist"))
                    .setName(Component.translatable("voxy-extra.option.server_blacklist"))
                    .setTooltip(Component.translatable("voxy-extra.option.server_blacklist.tooltip"))
                    .setDefaultValue(false)
                    .setStorageHandler(CONFIG::save)
                    .setBinding(CONFIG::setServerBlacklistEnabled, CONFIG::isServerBlacklistEnabled)
                )
                .addOption(builder.createExternalButtonOption(Identifier.parse("voxy-extra:edit_server_blacklist"))
                    .setName(Component.translatable("voxy-extra.option.edit_server_blacklist"))
                    .setTooltip(Component.translatable("voxy-extra.modal.open_editor_modal.tooltip"))
                    .setScreenConsumer((Screen screen) -> {
                        if (screen instanceof ScreenPromptable promptable) {
                            Modal modal = EditServerBlacklistModal.create(promptable);
                            modal.init();
                            promptable.setPrompt(modal);
                        }
                    })
                ));
    }
}