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
        OptionPageBuilder VoxyExtraPage = builder.createOptionPage().setName(Component.translatable("text.autoconfig.voxy-extra.title"));

        VoxyExtraPage.addOptionGroup(builder.createOptionGroup()
                        .addOption(
                                builder.createBooleanOption(Identifier.parse("voxy-extra:fixnetherfog"))
                                        .setName(Component.translatable("text.autoconfig.voxy-extra.option.fixNetherFog"))
                                        .setTooltip(Component.translatable("text.autoconfig.voxy-extra.option.fixNetherFog.@Tooltip"))
                                        .setStorageHandler(this.handler)
                                        .setBinding(this.storage::setFixNetherFog, this.storage::getFixNetherFog)
                                        .setDefaultValue(true)
                                )
                );

        VoxyExtraPage.addOptionGroup(builder.createOptionGroup()
                        .addOption(
                                builder.createBooleanOption(Identifier.parse("voxy-extra:flashbacksaveoldlods"))
                                        .setName(Component.translatable("text.autoconfig.voxy-extra.option.saveOldLods"))
                                        .setTooltip(
                                                VoxyExtra.IsFlashbackLoaded
                                                        ? Component.translatable("text.autoconfig.voxy-extra.option.saveOldLods.@Tooltip")
                                                        : Component.translatable("voxy_extra.config.flashback_not_available.@Tooltip")
                                        )
                                        .setStorageHandler(this.handler)
                                        .setBinding(this.storage::setSaveOldLods, this.storage::getSaveOldLods)
                                        .setDefaultValue(false)
                                        .setEnabled(VoxyExtra.IsFlashbackLoaded)
                                )
                        .addOption(
                                builder.createBooleanOption(Identifier.parse("voxy-extra:flashbackingest"))
                                        .setName(Component.translatable("text.autoconfig.voxy-extra.option.flashbackIngest"))
                                        .setTooltip(
                                                VoxyExtra.IsFlashbackLoaded
                                                        ? Component.translatable("text.autoconfig.voxy-extra.option.flashbackIngest.@Tooltip")
                                                        : Component.translatable("voxy_extra.config.flashback_not_available.@Tooltip")
                                        )
                                        .setStorageHandler(this.handler)
                                        .setBinding(this.storage::setFlashbackIngest, this.storage::getFlashbackIngest)
                                        .setDefaultValue(true)
                                        .setEnabled(VoxyExtra.IsFlashbackLoaded)
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

        VoxyExtraPage.addOptionGroup(builder.createOptionGroup()
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

        VoxyExtraPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createExternalButtonOption(Identifier.parse("voxy-extra:editconfig"))
                                .setName(Component.translatable("text.autoconfig.voxy-extra.option.edit"))
                                .setTooltip(Component.translatable("text.autoconfig.voxy-extra.option.edit.@Tooltip"))
                                .setScreenConsumer(screen -> Minecraft.getInstance().gui.setScreen(AutoConfigClient.getConfigScreen(VoxyExtraConfig.class,screen).get()))
                )
        );

        builder.registerOwnModOptions()
                .setNonTintedIcon(Identifier.parse("voxy-extra:icon.png"))
                .setColorTheme(builder.createColorTheme().setBaseThemeRGB(0xfdff93))
                .addPage(VoxyExtraPage);
    }
}
