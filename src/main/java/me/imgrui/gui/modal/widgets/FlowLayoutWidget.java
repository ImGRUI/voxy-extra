package me.imgrui.gui.modal.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import me.imgrui.gui.modal.ModalWidget;

import java.util.ArrayList;
import java.util.List;

public class FlowLayoutWidget extends ModalWidget {

    private final List<FlowItem> items = new ArrayList<>();

    private double lastMouseX = -1;
    private double lastMouseY = -1;

    private double scrollOffset = 0;
    private int maxScroll = 0;

    public FlowLayoutWidget(int width, int height) {
        super(width, height);
    }

    public void setItems(List<FlowItem> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        recalculateLayout();
    }

    @Override
    public void setBounds(int x, int y) {
        super.setBounds(x, y);
        recalculateLayout();
    }

    private void recalculateLayout() {
        if (width == 0) return;

        Minecraft instance = Minecraft.getInstance();

        int curX = this.x;
        int curY = this.y;

        int maxR = this.x + this.width - 12;
        int lastRowHeight = 0;

        for (FlowItem item : items) {
            item.calculateDimensions(instance.font);

            if (curX + item.totalWidth > maxR && curX != this.x) {
                curX = this.x;
                curY += lastRowHeight + 4;
            }

            item.setBounds(curX, curY);
            curX += item.totalWidth + 4;
            lastRowHeight = item.height;
        }

        int totalContentHeight = items.isEmpty() ? 0 : (curY + lastRowHeight - this.y);
        this.maxScroll = Math.max(0, totalContentHeight - this.height);
        this.scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;

        graphics.enableScissor(x, y, x + width, y + height);

        int scroll = (int) scrollOffset;
        for (FlowItem item : items) {
            int renderY = item.y - scroll;
            if (renderY + item.height < y || renderY > y + height) continue;

            item.render(graphics, mouseX, mouseY, scroll);
        }

        graphics.disableScissor();

        renderScrollbar(graphics);
    }

    private void renderScrollbar(GuiGraphics graphics) {
        if (maxScroll <= 0) return;

        int sbWidth = 3;
        int sbX = x + width - sbWidth - 2;
        int sbHeight = (int) ((height / (float) (height + maxScroll)) * height);
        int sbY = y + (int) ((scrollOffset / (float) maxScroll) * (height - sbHeight));

        graphics.fill(sbX, y, sbX + sbWidth, y + height, 0x55000000);
        graphics.fill(sbX, sbY, sbX + sbWidth, sbY + sbHeight, 0xFF666666);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean isScrollbarClicked = maxScroll > 0
                && mouseX >= x + width - 10 && mouseX <= x + width
                && mouseY >= y && mouseY <= y + height;
        if (isScrollbarClicked) {
            int sbHeight = (int) ((height / (float) (height + maxScroll)) * height);
            double clickY = mouseY - y - (sbHeight / 2.0);
            double trackHeight = height - sbHeight;
            if (trackHeight > 0) {
                scrollOffset = (clickY / trackHeight) * maxScroll;
                scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
            }

            return true;
        }

        boolean isOutside = mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height;
        if (isOutside) return false;

        double adjustedY = mouseY + scrollOffset;
        for (FlowItem item : items) {
            if (item.checkClick(mouseX, adjustedY)) return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (maxScroll <= 0) return false;

        boolean isHovered = lastMouseX >= x && lastMouseX <= x + width
                && lastMouseY >= y && lastMouseY <= y + height;
        if (!isHovered) return false;

        double delta = switch (event.key()) {
            case GLFW.GLFW_KEY_UP -> -15;
            case GLFW.GLFW_KEY_DOWN -> 15;
            case GLFW.GLFW_KEY_PAGE_UP -> -height;
            case GLFW.GLFW_KEY_PAGE_DOWN ->  height;
            default -> 0;
        };

        if (delta == 0) return false;

        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset + delta));
        return true;
    }

    public static class ActionButton {
        public final @NonNull String text;
        public final int hoverColor;
        public final Runnable action;

        int width;

        public ActionButton(@NonNull String text, int hoverColor, Runnable action) {
            this.text = text;
            this.hoverColor = hoverColor;
            this.action = action;
        }

        public ActionButton(@NonNull String text, Runnable action) {
            this(text, 0xFFFF4444, action);
        }
    }

    public static class FlowItem {
        private final @NonNull String text;
        private final List<ActionButton> buttons;

        protected int x, y;
        protected int textWidth, totalWidth;
        protected final int height = 14;

        public FlowItem(@NonNull String text, @NonNull String btnText, Runnable action) {
            this.text = text;
            this.buttons = List.of(new ActionButton(btnText, action));
        }

        public FlowItem(@NonNull String text, List<ActionButton> buttons) {
            this.text = text;
            this.buttons = new ArrayList<>(buttons);
        }

        public void calculateDimensions(Font font) {
            this.textWidth = font.width(text) + 6;
            this.totalWidth = textWidth;
            for (ActionButton btn : buttons) {
                btn.width = Math.max(height, font.width(btn.text) + 6);
                this.totalWidth += btn.width;
            }
        }

        public void setBounds(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void render(GuiGraphics graphics, int mouseX, int mouseY, int scrollOffset) {
            Minecraft instance = Minecraft.getInstance();
            int renderY = y - scrollOffset;

            graphics.fill(x, renderY, x + textWidth, renderY + height, 0xFF303030);
            graphics.drawString(instance.font, text, x + 3, renderY + 3, 0xFFE0E0E0);

            int curX = x + textWidth;
            for (ActionButton btn : buttons) {
                boolean hovered = mouseX >= curX && mouseX <= curX + btn.width
                        && mouseY >= renderY && mouseY <= renderY + height;

                graphics.fill(curX, renderY, curX + btn.width, renderY + height,
                        hovered ? btn.hoverColor : 0xFF252525);
                graphics.drawString(instance.font, btn.text,
                        curX + (btn.width - instance.font.width(btn.text)) / 2,
                        renderY + 3,
                        hovered ? 0xFFFFFFFF : 0xFFAAAAAA);

                curX += btn.width;
            }
        }

        public boolean checkClick(double mouseX, double mouseY) {
            int curX = x + textWidth;
            for (ActionButton btn : buttons) {
                if (mouseX >= curX && mouseX <= curX + btn.width
                        && mouseY >= y && mouseY <= y + height) {
                    btn.action.run();
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
                
                curX += btn.width;
            }

            return false;
        }
    }
}
