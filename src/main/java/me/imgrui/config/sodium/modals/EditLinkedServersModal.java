package me.imgrui.config.sodium.modals;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import me.imgrui.config.VoxyExtraConfig;
import me.imgrui.gui.modal.Modal;
import me.imgrui.gui.modal.ModalBuilder;
import me.imgrui.gui.modal.widgets.FlowLayoutWidget;
import me.imgrui.gui.modal.widgets.InputWidget;
import net.caffeinemc.mods.sodium.client.gui.prompt.ScreenPromptable;
import net.minecraft.network.chat.Component;

public class EditLinkedServersModal {
    public static Modal create(ScreenPromptable parent) {
        FlowLayoutWidget flowLayout = new FlowLayoutWidget(220, 110);
 
        Runnable[] refresher = new Runnable[1];
        refresher[0] = () -> {
            List<FlowLayoutWidget.FlowItem> items = new ArrayList<>();
            for (String primaryHost : VoxyExtraConfig.CONFIG.linkedServers.keySet()) {
                if (primaryHost == null) continue;
 
                List<FlowLayoutWidget.ActionButton> buttons = List.of(
                    new FlowLayoutWidget.ActionButton("+", 0xFFFCEE72, () -> openEditLinkedServerServersModal(parent, primaryHost)),
                    new FlowLayoutWidget.ActionButton("×", 0xFFFF4444, () -> {
                        VoxyExtraConfig.CONFIG.linkedServers.remove(primaryHost);
                        VoxyExtraConfig.CONFIG.save();
                        refresher[0].run();
                    })
                );
 
                items.add(new FlowLayoutWidget.FlowItem(primaryHost, buttons));
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
                if (VoxyExtraConfig.CONFIG.linkedServers.containsKey(host)) return;
 
                VoxyExtraConfig.CONFIG.linkedServers.put(host, new LinkedHashSet<>());
                VoxyExtraConfig.CONFIG.save();
                refresher[0].run();
            }
        );
 
        return new ModalBuilder(parent)
            .title(Component.translatable("voxy-extra.option.linked_servers"))
            .size(265, 180)
            .spacer(10)
            .add(inputWidget)
            .spacer(10)
            .add(flowLayout)
            .build();
    }
 
    private static void openEditLinkedServerServersModal(ScreenPromptable parent, String host) {
        Modal next = EditLinkedServerServersModal.create(parent, host);
        next.init();
        parent.setPrompt(next);
    }
}