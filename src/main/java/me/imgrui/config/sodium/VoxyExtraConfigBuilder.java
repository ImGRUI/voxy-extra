package me.imgrui.config.sodium;

import me.imgrui.VoxyExtra;
import me.imgrui.config.VoxyExtraClothConfig;
import me.imgrui.config.VoxyExtraStorage;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class VoxyExtraConfigBuilder implements ConfigEntryPoint {
    private final VoxyExtraStorage storage = new VoxyExtraStorage();
    private final StorageEventHandler handler = this.storage::save;

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        OptionPageBuilder VoxyExtraPage = builder.createOptionPage().setName(Component.translatable("voxy_extra.sodium.config_page"));

        VoxyExtraPage.addOptionGroup(builder.createOptionGroup()
                        .addOption(
                                builder.createBooleanOption(Identifier.parse("voxy-extra:fixnetherfog"))
                                        .setName(Component.translatable("voxy_extra.config.fix_nether_fog"))
                                        .setTooltip(Component.translatable("voxy_extra.config.fix_nether_fog.tooltip"))
                                        .setStorageHandler(this.handler)
                                        .setBinding(this.storage::setFixNetherFog, this.storage::getFixNetherFog)
                                        .setDefaultValue(true)
                                )
                );

        VoxyExtraPage.addOptionGroup(builder.createOptionGroup()
                        .addOption(
                                builder.createBooleanOption(Identifier.parse("voxy-extra:flashbacksaveoldlods"))
                                        .setName(Component.translatable("voxy_extra.config.save_old_lods"))
                                        .setTooltip(
                                                VoxyExtra.IsFlashbackLoaded
                                                        ? Component.translatable("voxy_extra.config.save_old_lods.tooltip")
                                                        : Component.translatable("voxy_extra.config.flashback_not_available.tooltip")
                                        )
                                        .setStorageHandler(this.handler)
                                        .setBinding(this.storage::setSaveOldLods, this.storage::getSaveOldLods)
                                        .setDefaultValue(false)
                                        .setEnabled(VoxyExtra.IsFlashbackLoaded)
                                )
                        .addOption(
                                builder.createBooleanOption(Identifier.parse("voxy-extra:flashbackingest"))
                                        .setName(Component.translatable("voxy_extra.config.flashback_ingest"))
                                        .setTooltip(
                                                VoxyExtra.IsFlashbackLoaded
                                                        ? Component.translatable("voxy_extra.config.flashback_ingest.tooltip")
                                                        : Component.translatable("voxy_extra.config.flashback_not_available.tooltip")
                                        )
                                        .setStorageHandler(this.handler)
                                        .setBinding(this.storage::setFlashbackIngest, this.storage::getFlashbackIngest)
                                        .setDefaultValue(true)
                                        .setEnabled(VoxyExtra.IsFlashbackLoaded)
                        )
                );

        VoxyExtraPage.addOptionGroup(builder.createOptionGroup()
                .addOption(
                        builder.createBooleanOption(Identifier.parse("voxy-extra:serverblacklist"))
                                .setName(Component.translatable("voxy_extra.config.server_blacklist"))
                                .setTooltip(Component.translatable("voxy_extra.config.server_blacklist.tooltip"))
                                .setStorageHandler(this.handler)
                                .setBinding(this.storage::setServerBlacklist, this.storage::getServerBlacklist)
                                .setDefaultValue(false)
                )
                .addOption(
                        builder.createBooleanOption(Identifier.parse("voxy-extra:redirectlod"))
                                .setName(Component.translatable("voxy_extra.config.lod_mirror"))
                                .setTooltip(Component.translatable("voxy_extra.config.lod_mirror.tooltip"))
                                .setStorageHandler(this.handler)
                                .setBinding(this.storage::setLodMirror, this.storage::getLodMirror)
                                .setDefaultValue(false)
                )
        );

//        VoxyExtraPage.addOptionGroup(builder.createOptionGroup()
//                .addOption(
//                        builder.createExternalButtonOption(Identifier.parse("voxy-extra:editconfig"))
//                                .setName(Component.translatable("voxy_extra.config.edit"))
//                                .setTooltip(Component.translatable("voxy_extra.config.edit.tooltip"))
//                                .setScreenConsumer(screen -> AutoConfigClient.getConfigScreen(VoxyExtraClothConfig.class,parent).get())
//                )
//        );

        builder.registerOwnModOptions()
                .setNonTintedIcon(Identifier.parse("voxy-extra:icon.png"))
                .setColorTheme(builder.createColorTheme().setBaseThemeRGB(0xfdff93))
                .addPage(VoxyExtraPage);
    }
}
