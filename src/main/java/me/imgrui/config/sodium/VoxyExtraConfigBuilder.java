package me.imgrui.config.sodium;

import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static me.imgrui.config.VoxyExtraConfig.CONFIG;

public class VoxyExtraConfigBuilder implements ConfigEntryPoint {

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        OptionPageBuilder VoxyExtraPage = builder.createOptionPage().setName(Component.translatable("voxy_extra.sodium.config_page"));

        VoxyExtraPage.addOptionGroup(builder.createOptionGroup()
                        .addOption(
                                builder.createBooleanOption(Identifier.parse("voxy-extra:fixnetherfog"))
                                        .setName(Component.translatable("voxy_extra.config.fix_nether_fog"))
                                        .setTooltip(Component.translatable("voxy_extra.config.fix_nether_fog.tooltip"))
                                        .setStorageHandler(CONFIG::save)
                                        .setBinding(CONFIG::setFixNetherFog, CONFIG::getFixNetherFog)
                                        .setDefaultValue(true)
                                )
                );

        VoxyExtraPage.addOptionGroup(builder.createOptionGroup()
                        .addOption(
                                builder.createBooleanOption(Identifier.parse("voxy-extra:flashbacksaveoldlods"))
                                        .setName(Component.translatable("voxy_extra.config.save_old_lods"))
                                        .setTooltip(Component.translatable("voxy_extra.config.save_old_lods.tooltip"))
                                        .setStorageHandler(CONFIG::save)
                                        .setBinding(CONFIG::setSaveOldLods, CONFIG::getSaveOldLods)
                                        .setDefaultValue(false)
                                )
                        .addOption(
                                builder.createBooleanOption(Identifier.parse("voxy-extra:flashbackingest"))
                                        .setName(Component.translatable("voxy_extra.config.flashback_ingest"))
                                        .setTooltip(Component.translatable("voxy_extra.config.flashback_ingest.tooltip"))
                                        .setStorageHandler(CONFIG::save)
                                        .setBinding(CONFIG::setFlashbackIngest, CONFIG::getFlashbackIngest)
                                        .setDefaultValue(true)
                                )
                );

        builder.registerOwnModOptions()
                .setNonTintedIcon(Identifier.parse("voxy-extra:icon.png"))
                .setColorTheme(builder.createColorTheme().setBaseThemeRGB(0xfdff93))
                .addPage(VoxyExtraPage);
    }
}