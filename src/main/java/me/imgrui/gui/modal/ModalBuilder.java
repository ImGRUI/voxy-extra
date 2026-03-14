package me.imgrui.gui.modal;

import net.caffeinemc.mods.sodium.client.gui.prompt.ScreenPromptable;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

import me.imgrui.gui.modal.widgets.SpacerWidget;

public class ModalBuilder {
    private final ScreenPromptable parent;
    private Component title = Component.empty();
    private int width = 200;
    private int height = 200;
    private final List<ModalWidget> pendingWidgets = new ArrayList<>();
    private Runnable onClose;
    private boolean showBackButton = false;

    public ModalBuilder(ScreenPromptable parent) {
        this.parent = parent;
    }

    public ModalBuilder title(Component title) {
        this.title = title;
        return this;
    }

    public ModalBuilder size(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public ModalBuilder spacer(int pixels) {
        return this.add(new SpacerWidget(pixels));
    }

    public ModalBuilder add(ModalWidget widget) {
        this.pendingWidgets.add(widget);
        return this;
    }

    public ModalBuilder onClose(Runnable onClose) {
        this.onClose = onClose;
        return this;
    }

    public ModalBuilder showBackButton(boolean show) {
        this.showBackButton = show;
        return this;
    }

    public Modal build() {
        Modal modal = new Modal(parent, title, width, height);
        
        if (this.onClose != null) modal.setOnCloseOverride(this.onClose);
        
        modal.setShowBackButton(this.showBackButton);
        for (ModalWidget widget : pendingWidgets) {
            modal.addWidget(widget);
        }

        return modal;
    }
}
