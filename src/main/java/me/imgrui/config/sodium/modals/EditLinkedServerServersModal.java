package me.imgrui.config.sodium.modals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.imgrui.config.VoxyExtraConfig;
import me.imgrui.gui.modal.Modal;
import me.imgrui.gui.modal.ModalBuilder;
import me.imgrui.gui.modal.widgets.FlowLayoutWidget;
import me.imgrui.gui.modal.widgets.InputWidget;
import net.caffeinemc.mods.sodium.client.gui.prompt.ScreenPromptable;
import net.minecraft.network.chat.Component;

public class EditLinkedServerServersModal {
    public static Modal create(ScreenPromptable parent, String primaryHost) {
        FlowLayoutWidget flowLayout = new FlowLayoutWidget(220, 110);
 
        Runnable[] refresher = new Runnable[1];
        refresher[0] = () -> {
            Set<String> linkedHosts = VoxyExtraConfig.CONFIG.linkedServers.get(primaryHost);
            if (linkedHosts == null) {
                reopenParent(parent);
                return;
            }
 
            List<FlowLayoutWidget.FlowItem> items = new ArrayList<>();
            for (String linkedHost : linkedHosts) {
                items.add(new FlowLayoutWidget.FlowItem(linkedHost, "×", () -> {
                    linkedHosts.remove(linkedHost);
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
 
                Set<String> hosts = VoxyExtraConfig.CONFIG.linkedServers.get(primaryHost);
                if (hosts == null || hosts.contains(host)) return;
 
                hosts.add(host);
                VoxyExtraConfig.CONFIG.save();
                refresher[0].run();
            }
        );
 
        return new ModalBuilder(parent)
            .title(Component.translatable("voxy-extra.modal.linked_servers.primary_server", primaryHost))
            .size(265, 180)
            .showBackButton(true)
            .spacer(10)
            .add(inputWidget)
            .spacer(10)
            .add(flowLayout)
            .onClose(() -> reopenParent(parent))
            .build();
    }
 
    private static void reopenParent(ScreenPromptable parent) {
        Modal prev = EditLinkedServersModal.create(parent);
        prev.init();
        parent.setPrompt(prev);
    }
}