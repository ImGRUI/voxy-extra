package me.imgrui.gui.modal.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import me.imgrui.gui.modal.Modal;
import me.imgrui.gui.modal.ModalWidget;

import java.util.function.Consumer;

public class InputWidget extends ModalWidget {
    private final Component hint;
    private final String btnText;
    private final Consumer<String> onSubmit;
    private EditBox editBox;

    public InputWidget(int width, int height, Component hint, String btnText, Consumer<String> onSubmit) {
        super(width, height);
        this.hint = hint;
        this.btnText = btnText;
        this.onSubmit = onSubmit;
    }

    @Override
    public void init(Modal modal) {
        String preservedText = (this.editBox != null) ? this.editBox.getValue() : "";

        super.init(modal);

        Minecraft instance = Minecraft.getInstance();
        this.editBox = new EditBox(instance.font, x + 5, y + 5, width - 26, 12, Component.empty());
        this.editBox.setBordered(false);
        this.editBox.setMaxLength(128);
        this.editBox.setTextColor(0xFFFFFFFF);
        this.editBox.setHint(hint);

        if (!preservedText.isEmpty()) {
            this.editBox.setValue(preservedText);
        }

        this.editBox.setFocused(true);
        modal.setVanillaFocus(this.editBox);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        Minecraft instance = Minecraft.getInstance();

        graphics.fill(x, y, x + width, y + height, 0xFF0A0A0A);

        int borderColor = 0xFF404040;
        graphics.fill(x, y, x + width, y + 1, borderColor);
        graphics.fill(x, y + height - 1, x + width, y + height, borderColor);
        graphics.fill(x, y, x + 1, y + height, borderColor);
        graphics.fill(x + width - 1, y, x + width, y + height, borderColor);

        int btnSize = 18;
        int btnX = x + width - btnSize;
        boolean hovered = mouseX >= btnX && mouseX <= btnX + btnSize
                && mouseY >= y && mouseY <= y + height;

        graphics.fill(btnX, y + 3, btnX + 1, y + height - 3, 0xFF303030);
        graphics.drawString(instance.font, btnText,
                btnX + (btnSize - instance.font.width(btnText)) / 2 + 1,
                y + (height - instance.font.lineHeight) / 2 + 1,
                hovered ? 0xFFE0E0E0 : 0xFF808080);

        editBox.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int btnSize = 18;
        int btnX = x + width - btnSize;

        boolean isButtonClicked = mouseX >= btnX && mouseX <= btnX + btnSize
                && mouseY >= y && mouseY <= y + height;
        if (isButtonClicked) {
            submit();
            return true;
        }

        boolean isEditBoxClicked = mouseX >= x && mouseX <= btnX
                && mouseY >= y && mouseY <= y + height;
        if (isEditBoxClicked) {
            editBox.setFocused(true);
            modal.setVanillaFocus(editBox);
            return true;
        }

        editBox.setFocused(false);
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (editBox.isFocused()) {
            if (event.key() == GLFW.GLFW_KEY_ENTER) {
                submit();
                return true;
            }
            return editBox.keyPressed(event);
        }
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (editBox.isFocused()) return editBox.charTyped(event);
        return false;
    }

    private void submit() {
        onSubmit.accept(editBox.getValue().trim());
        editBox.setValue("");
    }
}
