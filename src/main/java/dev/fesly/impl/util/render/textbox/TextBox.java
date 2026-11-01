package dev.fesly.impl.util.render.textbox;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.font.Font;
import dev.fesly.font.GlyphPage;
import dev.fesly.impl.interfaces.game.IMinecraft;
import dev.fesly.impl.util.render.GUIUtil;
import net.minecraft.client.util.InputMappings;
import org.joml.Vector2d;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
public class TextBox implements IMinecraft {
    public String text = "";
    public Vector2d position;
    public boolean selected;
    public int cursor;
    public double animatedCursorPosition;
    public Font fontRenderer;
    public Color color;
    private double lastTime;
    private double lastBackSpace;
    private TextAlign textAlign;
    private double posX;
    private String emptyText;
    private float width;
    private boolean hideCharacters;
    public TextBox(final Vector2d position, final Font fontRenderer, final Color color, final TextAlign textAlign, final String emptyText, final float width, final boolean hideCharacters) {
        this.position = position;
        this.fontRenderer = fontRenderer;
        this.color = color;
        this.textAlign = textAlign;
        this.emptyText = emptyText;
        this.width = width;
        this.hideCharacters = hideCharacters;
    }
    public TextBox(final Vector2d position, final Font fontRenderer, final Color color, final TextAlign textAlign, final String emptyText, final float width) {
        this.position = position;
        this.fontRenderer = fontRenderer;
        this.color = color;
        this.textAlign = textAlign;
        this.emptyText = emptyText;
        this.width = width;
        this.hideCharacters = false;
    }
    public void drawString(MatrixStack matrix) {
        cursor = Math.min(Math.max(cursor, 0), this.text.length());
        mc.keyboardListener.enableRepeatEvents(true);
        StringBuilder drawnString = new StringBuilder(this.text);
        if (this.hideCharacters && !this.isEmpty()) {
            StringBuilder string = new StringBuilder();
            string.append("*".repeat(Math.max(0, drawnString.length())));
            drawnString = new StringBuilder(string);
        }
        final double time = System.currentTimeMillis();
        double difference = Math.abs(time - lastTime);
        lastTime = time;
        difference = Math.min(difference, 500);
        switch (this.textAlign) {
            case CENTER -> {
                final int speed = 20;
                for (int i = 0; i < difference; ++i) {
                    posX = ((posX * (speed - 1)) + (position.x - this.fontRenderer.getWidth(this.text.isEmpty() || this.text.equals(" ") ? this.emptyText : drawnString.toString()) / 2.0F)) / speed;
                }
            }
            case LEFT -> posX = position.x;
        }
        if (this.isEmpty()) {
            this.fontRenderer.draw(matrix, this.emptyText, posX, position.y, new Color(this.color.getRed(), this.color.getBlue(), this.color.getGreen(), (int) (this.color.getAlpha() * (this.selected ? 0.3F : 0.2F))).hashCode());
        } else {
            this.fontRenderer.draw(matrix, drawnString.toString(), posX, position.y, this.color.hashCode());
        }
        cursor = Math.min(Math.max(cursor, 0), drawnString.length());
        final StringBuilder textBeforeCursor = new StringBuilder();
        for (int i = 0; i < this.cursor; ++i) {
            textBeforeCursor.append(drawnString.charAt(i));
        }
        final float cursorOffset = this.fontRenderer.getWidth(textBeforeCursor.toString());
        final int speed = 20;
        for (int i = 0; i < difference; ++i) {
            animatedCursorPosition = (animatedCursorPosition * (speed - 1) + (cursorOffset - 2)) / speed;
        }
        if (this.selected) {
            this.fontRenderer.draw(matrix, "|", (float) (posX + animatedCursorPosition + 1), position.y - 1, new Color(this.color.getRed(), this.color.getBlue(), this.color.getGreen(), this.color.getAlpha() == 0 ? 0 : (int) ((((Math.sin(System.currentTimeMillis() / 150.0) + 1) / 2) * 255))).hashCode());
        }
    }
    public void click(final int mouseX, final int mouseY, final int mouseButton) {
        final Vector2d position = getPosition();
        this.selected = mouseButton == 0 && GUIUtil.mouseOver(position.x + (textAlign == TextAlign.CENTER ? -width / 2.0F : 0), position.y, width, fontRenderer.getHeight(), mouseX, mouseY);
    }
    public void key(final char typedChar, final int keyCode) {
        if (!this.selected) {
            return;
        }
        final String character = String.valueOf(typedChar);
        cursor = Math.min(Math.max(cursor, 0), this.text.length());
        if (InputMappings.isKeyDown(mc.getMainWindow().getHandle(), 29) && keyCode == 47) {
            try {
                final String clipboard = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                this.addText(clipboard, cursor);
                cursor += clipboard.length();
            } catch (final UnsupportedFlavorException | IOException e) {
                e.printStackTrace();
            }
        } else
        if (GlyphPage.CHARS.contains(character.toLowerCase())) {
            this.addText(character, cursor);
            cursor++;
        } else
        if (keyCode == 211 && !this.text.isEmpty()) {
            this.removeText(cursor);
        } else if (keyCode == 14 && !this.text.isEmpty()) {
            this.removeText(cursor);
            cursor--;
            if (InputMappings.isKeyDown(mc.getMainWindow().getHandle(), 29)) {
                while (!this.text.isEmpty() && 0 < cursor) {
                    this.removeText(cursor);
                    cursor--;
                }
            }
        } else
        if (keyCode == 205) {
            cursor++;
            if (InputMappings.isKeyDown(mc.getMainWindow().getHandle(), 29)) {
                while (this.text.length() > cursor) {
                    cursor++;
                }
            }
        } else if (keyCode == 203) {
            cursor--;
            if (InputMappings.isKeyDown(mc.getMainWindow().getHandle(), 29)) {
                while (0 < cursor) {
                    cursor--;
                }
            }
        }
        cursor = Math.min(Math.max(cursor, 0), this.text.length());
    }
    private void addText(final String text, final int position) {
        if (fontRenderer.getWidth(this.text + text) <= width) {
            final StringBuilder newText = new StringBuilder();
            boolean append = false;
            for (int i = 0; i < this.text.length(); i++) {
                final String character = String.valueOf(this.text.charAt(i));
                if (i == position) {
                    append = true;
                    newText.append(text);
                }
                newText.append(character);
            }
            if (!append) {
                newText.append(text);
            }
            this.text = newText.toString();
        }
    }
    private void removeText(final int position) {
        final StringBuilder newText = new StringBuilder();
        for (int i = 0; i < this.text.length(); ++i) {
            final String character = String.valueOf(this.text.charAt(i));
            if (i != position - 1) {
                newText.append(character);
            }
        }
        this.text = newText.toString();
    }
    public boolean isEmpty() {
        return this.text.isEmpty() || this.text.equals(" ");
    }
    @java.lang.SuppressWarnings("all")
    public String getText() {
        return this.text;
    }
    @java.lang.SuppressWarnings("all")
    public Vector2d getPosition() {
        return this.position;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isSelected() {
        return this.selected;
    }
    @java.lang.SuppressWarnings("all")
    public int getCursor() {
        return this.cursor;
    }
    @java.lang.SuppressWarnings("all")
    public double getAnimatedCursorPosition() {
        return this.animatedCursorPosition;
    }
    @java.lang.SuppressWarnings("all")
    public Font getFontRenderer() {
        return this.fontRenderer;
    }
    @java.lang.SuppressWarnings("all")
    public Color getColor() {
        return this.color;
    }
    @java.lang.SuppressWarnings("all")
    public double getLastTime() {
        return this.lastTime;
    }
    @java.lang.SuppressWarnings("all")
    public double getLastBackSpace() {
        return this.lastBackSpace;
    }
    @java.lang.SuppressWarnings("all")
    public TextAlign getTextAlign() {
        return this.textAlign;
    }
    @java.lang.SuppressWarnings("all")
    public double getPosX() {
        return this.posX;
    }
    @java.lang.SuppressWarnings("all")
    public String getEmptyText() {
        return this.emptyText;
    }
    @java.lang.SuppressWarnings("all")
    public float getWidth() {
        return this.width;
    }
    @java.lang.SuppressWarnings("all")
    public boolean isHideCharacters() {
        return this.hideCharacters;
    }
    @java.lang.SuppressWarnings("all")
    public void setText(final String text) {
        this.text = text;
    }
    @java.lang.SuppressWarnings("all")
    public void setPosition(final Vector2d position) {
        this.position = position;
    }
    @java.lang.SuppressWarnings("all")
    public void setSelected(final boolean selected) {
        this.selected = selected;
    }
    @java.lang.SuppressWarnings("all")
    public void setCursor(final int cursor) {
        this.cursor = cursor;
    }
    @java.lang.SuppressWarnings("all")
    public void setAnimatedCursorPosition(final double animatedCursorPosition) {
        this.animatedCursorPosition = animatedCursorPosition;
    }
    @java.lang.SuppressWarnings("all")
    public void setFontRenderer(final Font fontRenderer) {
        this.fontRenderer = fontRenderer;
    }
    @java.lang.SuppressWarnings("all")
    public void setColor(final Color color) {
        this.color = color;
    }
    @java.lang.SuppressWarnings("all")
    public void setLastTime(final double lastTime) {
        this.lastTime = lastTime;
    }
    @java.lang.SuppressWarnings("all")
    public void setLastBackSpace(final double lastBackSpace) {
        this.lastBackSpace = lastBackSpace;
    }
    @java.lang.SuppressWarnings("all")
    public void setTextAlign(final TextAlign textAlign) {
        this.textAlign = textAlign;
    }
    @java.lang.SuppressWarnings("all")
    public void setPosX(final double posX) {
        this.posX = posX;
    }
    @java.lang.SuppressWarnings("all")
    public void setEmptyText(final String emptyText) {
        this.emptyText = emptyText;
    }
    @java.lang.SuppressWarnings("all")
    public void setWidth(final float width) {
        this.width = width;
    }
    @java.lang.SuppressWarnings("all")
    public void setHideCharacters(final boolean hideCharacters) {
        this.hideCharacters = hideCharacters;
    }
}
