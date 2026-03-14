package me.imgrui.gui.modal;

import net.caffeinemc.mods.sodium.client.gui.prompt.ScreenPrompt;
import net.caffeinemc.mods.sodium.client.gui.prompt.ScreenPromptable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class Modal extends ScreenPrompt {
    private static final List<FormattedText> EMPTY_BODY = List.of(FormattedText.composite(Component.empty()));
    private static final Action DUMMY_ACTION = new Action(Component.empty(), () -> {});

    static final int TITLE_BAR_HEIGHT = 16;

    private final ScreenPromptable parent;
    private final Component title;
    private final int modalWidth;
    private final int modalHeight;

    private int modalX, modalY;

    private final List<ModalWidget> widgets = new ArrayList<>();
    private Runnable onCloseOverride;
    private boolean showBackButton = false;

    public void setShowBackButton(boolean showBackButton) {
        this.showBackButton = showBackButton;
    }

    public void setOnCloseOverride(Runnable onCloseOverride) {
        this.onCloseOverride = onCloseOverride;
    }

    public Modal(ScreenPromptable parent, Component title, int width, int height) {
        super(parent, EMPTY_BODY, 0, 0, DUMMY_ACTION);
        this.parent = parent;
        this.title = title;
        this.modalWidth = width;
        this.modalHeight = height;
    }

    public void addWidget(ModalWidget widget) {
        this.widgets.add(widget);
    }

    @Override
    public void init() {
        super.init();
        var screen = (Screen) this.parent;
        this.modalX = (screen.width - this.modalWidth) / 2;
        this.modalY = (screen.height - this.modalHeight) / 2;

        int currentY = this.modalY + TITLE_BAR_HEIGHT;
        for (ModalWidget widget : widgets) {
            int widgetX = (widget.getWidth() > 0)
                    ? this.modalX + (this.modalWidth - widget.getWidth()) / 2
                    : this.modalX;

            widget.setBounds(widgetX, currentY);
            widget.init(this);
            currentY += widget.getHeight();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        var screen = (Screen) this.parent;
        Minecraft instance = Minecraft.getInstance();

        graphics.fill(0, 0, screen.width, screen.height, 0x90000000);
        graphics.fill(this.modalX, this.modalY + TITLE_BAR_HEIGHT, this.modalX + this.modalWidth, this.modalY + this.modalHeight, 0xFF181818);
        graphics.fill(this.modalX, this.modalY, this.modalX + this.modalWidth, this.modalY + TITLE_BAR_HEIGHT, 0xFF111111);

        int titleX = this.modalX + (this.modalWidth - instance.font.width(title)) / 2;
        int titleY = this.modalY + (TITLE_BAR_HEIGHT - instance.font.lineHeight) / 2 + 1;
        graphics.drawString(instance.font, title, titleX, titleY, 0xFFFFFFFF, false);

        renderCloseButton(graphics, mouseX, mouseY);

        if (showBackButton) {
            renderBackButton(graphics, mouseX, mouseY);
        }

        for (ModalWidget widget : widgets) {
            widget.render(graphics, mouseX, mouseY, delta);
        }
    }

    private void renderCloseButton(GuiGraphics graphics, int mouseX, int mouseY) {
        Minecraft instance = Minecraft.getInstance();
        int x = this.modalX + this.modalWidth - TITLE_BAR_HEIGHT;
        int y = this.modalY;

        boolean hovered = isInsideTitleButton(mouseX, mouseY, x);
        if (hovered) graphics.fill(x, y, x + TITLE_BAR_HEIGHT, y + TITLE_BAR_HEIGHT, 0xFFFF4444);

        String mark = "×";
        graphics.drawString(instance.font, mark,
                x + (TITLE_BAR_HEIGHT - instance.font.width(mark)) / 2 + 1,
                y + (TITLE_BAR_HEIGHT - instance.font.lineHeight) / 2 + 1,
                hovered ? 0xFFFFFFFF : 0xFFAAAAAA);
    }

    private void renderBackButton(GuiGraphics graphics, int mouseX, int mouseY) {
        Minecraft instance = Minecraft.getInstance();
        int x = this.modalX;
        int y = this.modalY;

        boolean hovered = isInsideTitleButton(mouseX, mouseY, x);
        if (hovered) graphics.fill(x, y, x + TITLE_BAR_HEIGHT, y + TITLE_BAR_HEIGHT, 0xFF444444);

        String mark = "<";
        graphics.drawString(instance.font, mark,
                x + (TITLE_BAR_HEIGHT - instance.font.width(mark)) / 2 + 1,
                y + (TITLE_BAR_HEIGHT - instance.font.lineHeight) / 2 + 1,
                hovered ? 0xFFFFFFFF : 0xFFAAAAAA);
    }

    private boolean isInsideTitleButton(int mouseX, int mouseY, int buttonX) {
        return mouseX >= buttonX && mouseX <= buttonX + TITLE_BAR_HEIGHT
                && mouseY >= this.modalY && mouseY <= this.modalY + TITLE_BAR_HEIGHT;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();

        boolean closeClicked = mx >= this.modalX + this.modalWidth - TITLE_BAR_HEIGHT
                && mx <= this.modalX + this.modalWidth
                && my >= this.modalY
                && my <= this.modalY + TITLE_BAR_HEIGHT;

        boolean backClicked = this.showBackButton
                && mx >= this.modalX
                && mx <= this.modalX + TITLE_BAR_HEIGHT
                && my >= this.modalY
                && my <= this.modalY + TITLE_BAR_HEIGHT;

        if (closeClicked || backClicked) {
            this.close();
            return true;
        }

        for (ModalWidget widget : widgets) {
            if (widget.mouseClicked(mx, my, event.button())) return true;
        }

        boolean clickedOutside = event.button() == 0
                && (mx < this.modalX || mx > this.modalX + this.modalWidth
                ||  my < this.modalY || my > this.modalY + this.modalHeight);

        if (clickedOutside) {
            this.close();
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }

        for (ModalWidget widget : widgets) {
            if (widget.keyPressed(event)) return true;
        }

        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        for (ModalWidget widget : widgets) {
            if (widget.charTyped(event)) return true;
        }

        return super.charTyped(event);
    }

    public void close() {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        if (onCloseOverride != null) {
            onCloseOverride.run();
        } else {
            this.parent.setPrompt(null);
        }
    }

    public void setVanillaFocus(GuiEventListener listener) {
        ((Screen) this.parent).setFocused(listener);
    }
}
