package me.imgrui.config.sodium.modals;

import java.util.ArrayList;
import java.util.List;

import me.imgrui.config.VoxyExtraConfig;
import me.imgrui.gui.modal.Modal;
import me.imgrui.gui.modal.ModalBuilder;
import me.imgrui.gui.modal.widgets.FlowLayoutWidget;
import me.imgrui.gui.modal.widgets.InputWidget;
import net.caffeinemc.mods.sodium.client.gui.prompt.ScreenPromptable;
import net.minecraft.network.chat.Component;

public class EditServerBlacklistModal {
 
    public static Modal create(ScreenPromptable parent) {
        FlowLayoutWidget flowLayout = new FlowLayoutWidget(220, 110);
 
        Runnable[] refresher = new Runnable[1];
        refresher[0] = () -> {
            List<FlowLayoutWidget.FlowItem> items = new ArrayList<>();
            for (String host : VoxyExtraConfig.CONFIG.serverBlacklist) {
                items.add(new FlowLayoutWidget.FlowItem(host, "×", () -> {
                    VoxyExtraConfig.CONFIG.serverBlacklist.remove(host);
                    VoxyExtraConfig.CONFIG.save();
                    refresher[0].run();
                }));
            }
            flowLayout.setItems(items);
        };
        refresher[0].run();
 
        InputWidget inputWidget = new InputWidget(
            220, 18,
            Component.translatable("voxy-extra.modal.server_address"),
            "+",
            host -> {
                if (host.isBlank()) return;
                if (VoxyExtraConfig.CONFIG.serverBlacklist.contains(host)) return;
 
                VoxyExtraConfig.CONFIG.serverBlacklist.add(host);
                VoxyExtraConfig.CONFIG.save();
                refresher[0].run();
            }
        );
 
        return new ModalBuilder(parent)
            .title(Component.translatable("voxy-extra.option.server_blacklist"))
            .size(265, 180)
            .spacer(10)
            .add(inputWidget)
            .spacer(10)
            .add(flowLayout)
            .build();
    }
}