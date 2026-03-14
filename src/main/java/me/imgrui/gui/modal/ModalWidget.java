package me.imgrui.gui.modal;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

public abstract class ModalWidget {
    protected Modal modal;
    protected int x, y, width, height;

    public ModalWidget(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setBounds(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void init(Modal modal) {
        this.modal = modal;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public abstract void render(GuiGraphics graphics, int mouseX, int mouseY, float delta);

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean keyPressed(KeyEvent event) {
        return false;
    }

    public boolean charTyped(CharacterEvent event) {
        return false;
    }
}
