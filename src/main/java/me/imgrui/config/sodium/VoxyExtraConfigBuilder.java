package me.imgrui.config.sodium;

import me.imgrui.VoxyExtra;
import me.imgrui.config.VoxyExtraConfig;
import me.imgrui.config.VoxyExtraStorage;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class VoxyExtraConfigBuilder implements ConfigEntryPoint {
    private final VoxyExtraStorage storage = new VoxyExtraStorage();
    private final StorageEventHandler handler = this.storage::save;

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        OptionPageBuilder VoxyExtraGeneralPage = builder.createOptionPage().setName(Component.translatable("text.autoconfig.voxy-extra.general"));
        OptionPageBuilder VoxyExtraFlashbackPage = builder.createOptionPage().setName(Component.translatable("text.autoconfig.voxy-extra.flashback"));
        OptionPageBuilder VoxyExtraReplayModPage = builder.createOptionPage().setName(Component.translatable("text.autoconfig.voxy-extra.replaymod"));

        VoxyExtraGeneralPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createBooleanOption(Identifier.parse("voxy-extra:fixnetherfog"))
                                .setName(Component.translatable("text.autoconfig.voxy-extra.option.fixNetherFog"))
                                .setTooltip(Component.translatable("text.autoconfig.voxy-extra.option.fixNetherFog.@Tooltip"))
                                .setStorageHandler(this.handler)
                                .setBinding(this.storage::setFixNetherFog, this.storage::getFixNetherFog)
                                .setDefaultValue(true)
                )
        );

        VoxyExtraGeneralPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createBooleanOption(Identifier.parse("voxy-extra:serverblacklist"))
                                .setName(Component.translatable("text.autoconfig.voxy-extra.option.serverBlacklist"))
                                .setTooltip(Component.translatable("text.autoconfig.voxy-extra.option.serverBlacklist.@Tooltip"))
                                .setStorageHandler(this.handler)
                                .setBinding(this.storage::setServerBlacklist, this.storage::getServerBlacklist)
                                .setDefaultValue(false)
                )
                .addOption(
                        builder.createBooleanOption(Identifier.parse("voxy-extra:serverwhitelist"))
                                .setName(Component.translatable("text.autoconfig.voxy-extra.option.serverWhitelist"))
                                .setTooltip(Component.translatable("text.autoconfig.voxy-extra.option.serverWhitelist.@Tooltip"))
                                .setStorageHandler(this.handler)
                                .setBinding(this.storage::setServerWhitelist, this.storage::getServerWhitelist)
                                .setDefaultValue(false)
                )
                .addOption(
                        builder.createBooleanOption(Identifier.parse("voxy-extra:redirectlod"))
                                .setName(Component.translatable("text.autoconfig.voxy-extra.option.lodMirror"))
                                .setTooltip(Component.translatable("text.autoconfig.voxy-extra.option.lodMirror.@Tooltip"))
                                .setStorageHandler(this.handler)
                                .setBinding(this.storage::setLodMirror, this.storage::getLodMirror)
                                .setDefaultValue(false)
                )
        );

        VoxyExtraGeneralPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createBooleanOption(Identifier.parse("voxy-extra:disableinsingleplayer"))
                                .setName(Component.translatable("text.autoconfig.voxy-extra.option.disableInSingleplayer"))
                                .setTooltip(Component.translatable("text.autoconfig.voxy-extra.option.disableInSingleplayer.@Tooltip"))
                                .setStorageHandler(this.handler)
                                .setBinding(this.storage::setDisableInSingleplayer, this.storage::getDisableInSingleplayer)
                                .setDefaultValue(false)
                )
        );

        VoxyExtraGeneralPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createExternalButtonOption(Identifier.parse("voxy-extra:editconfig"))
                                .setName(Component.translatable("text.autoconfig.voxy-extra.option.edit"))
                                .setTooltip(Component.translatable("text.autoconfig.voxy-extra.option.edit.@Tooltip"))
                                .setScreenConsumer(screen -> Minecraft.getInstance().setScreen(AutoConfigClient.getConfigScreen(VoxyExtraConfig.class,screen).get()))
                )
        );

        VoxyExtraFlashbackPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createBooleanOption(Identifier.parse("voxy-extra:flashbacksaveoldlods"))
                                .setName(Component.translatable("text.autoconfig.voxy-extra.option.flashbackSaveOldLods"))
                                .setTooltip(Component.translatable("text.autoconfig.voxy-extra.option.flashbackSaveOldLods.@Tooltip"))
                                .setStorageHandler(this.handler)
                                .setBinding(this.storage::setSaveOldLods, this.storage::getSaveOldLods)
                                .setDefaultValue(false)
                )
                .addOption(
                        builder.createBooleanOption(Identifier.parse("voxy-extra:flashbackingest"))
                                .setName(Component.translatable("text.autoconfig.voxy-extra.option.flashbackIngest"))
                                .setTooltip(Component.translatable("text.autoconfig.voxy-extra.option.flashbackIngest.@Tooltip"))
                                .setStorageHandler(this.handler)
                                .setBinding(this.storage::setFlashbackIngest, this.storage::getFlashbackIngest)
                                .setDefaultValue(true)
                )
                .addOption(
                        builder.createBooleanOption(Identifier.parse("voxy-extra:flashbackchecklodcache"))
                                .setName(Component.translatable("text.autoconfig.voxy-extra.option.flashbackCheckLodCache"))
                                .setTooltip(Component.translatable("text.autoconfig.voxy-extra.option.flashbackCheckLodCache.@Tooltip"))
                                .setStorageHandler(this.handler)
                                .setBinding(this.storage::setFlashbackCheckLodCache, this.storage::getFlashbackCheckLodCache)
                                .setDefaultValue(true)
                )
        );

        VoxyExtraReplayModPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createBooleanOption(Identifier.parse("voxy-extra:replaymodsavecustommeta"))
                                .setName(Component.translatable("text.autoconfig.voxy-extra.option.replayModSaveCustomMeta"))
                                .setTooltip(Component.translatable("text.autoconfig.voxy-extra.option.replayModSaveCustomMeta.@Tooltip"))
                                .setStorageHandler(this.handler)
                                .setBinding(this.storage::setReplayModSaveCustomMeta, this.storage::getReplayModSaveCustomMeta)
                                .setDefaultValue(true)
                )
                .addOption(
                        builder.createBooleanOption(Identifier.parse("voxy-extra:replaymodloadlods"))
                                .setName(Component.translatable("text.autoconfig.voxy-extra.option.replayModLoadLods"))
                                .setTooltip(Component.translatable("text.autoconfig.voxy-extra.option.replayModLoadLods.@Tooltip"))
                                .setStorageHandler(this.handler)
                                .setBinding(this.storage::setReplayModLoadLods, this.storage::getReplayModLoadLods)
                                .setDefaultValue(true)
                )
        );

        int voxyExtraColor = 0xfdff93;
        if (VoxyExtra.isFlashbackLoaded && VoxyExtra.isReplayModLoaded) {
            builder.registerOwnModOptions()
                    .setNonTintedIcon(Identifier.parse("voxy-extra:icon.png"))
                    .setColorTheme(builder.createColorTheme().setBaseThemeRGB(voxyExtraColor))
                    .addPage(VoxyExtraGeneralPage)
                    .addPage(VoxyExtraFlashbackPage)
                    .addPage(VoxyExtraReplayModPage);
        } else if (VoxyExtra.isFlashbackLoaded) {
            builder.registerOwnModOptions()
                    .setNonTintedIcon(Identifier.parse("voxy-extra:icon.png"))
                    .setColorTheme(builder.createColorTheme().setBaseThemeRGB(voxyExtraColor))
                    .addPage(VoxyExtraGeneralPage)
                    .addPage(VoxyExtraFlashbackPage);
        } else if (VoxyExtra.isReplayModLoaded) {
            builder.registerOwnModOptions()
                    .setNonTintedIcon(Identifier.parse("voxy-extra:icon.png"))
                    .setColorTheme(builder.createColorTheme().setBaseThemeRGB(voxyExtraColor))
                    .addPage(VoxyExtraGeneralPage)
                    .addPage(VoxyExtraReplayModPage);
        } else {
            builder.registerOwnModOptions()
                    .setNonTintedIcon(Identifier.parse("voxy-extra:icon.png"))
                    .setColorTheme(builder.createColorTheme().setBaseThemeRGB(voxyExtraColor))
                    .addPage(VoxyExtraGeneralPage);
        }
    }
}
