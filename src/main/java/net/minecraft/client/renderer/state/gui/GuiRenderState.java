package net.minecraft.client.renderer.state.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.optifine.render.GuiRenderListener;
import net.optifine.util.StrUtils;
import org.joml.Matrix3x2f;
import org.joml.Vector4f;

public class GuiRenderState {
    private static final int DEBUG_RECTANGLE_COLOR = 2000962815;
    private final List<GuiRenderState.Node> strata = new ArrayList<>();
    private int firstStratumAfterBlur = Integer.MAX_VALUE;
    private GuiRenderState.Node current;
    private final Set<Object> itemModelIdentities = new HashSet<>();
    private ScreenRectangle lastElementBounds;
    public PanoramaRenderState panoramaRenderState;
    public Vector4f clearColorOverride = new Vector4f(0.0F);
    public boolean isHudHidden;
    public final List<GuiRenderListener> frameListeners = new ArrayList<>();

    public GuiRenderState() {
        this.nextStratum();
    }

    public void nextStratum() {
        this.current = new GuiRenderState.Node(null);
        this.strata.add(this.current);
    }

    public void blurBeforeThisStratum() {
        if (this.firstStratumAfterBlur != Integer.MAX_VALUE) {
            throw new IllegalStateException("Can only blur once per frame");
        }

        this.firstStratumAfterBlur = this.strata.size() - 1;
    }

    public void up() {
        if (this.current.up == null) {
            this.current.up = new GuiRenderState.Node(this.current);
        }

        this.current = this.current.up;
    }

    public void addItem(GuiItemRenderState itemState) {
        if (this.findAppropriateNode(itemState)) {
            this.itemModelIdentities.add(itemState.itemStackRenderState().getModelIdentity());
            this.current.addItem(itemState);
            this.addDebugRectangleIfEnabled(itemState.bounds());
        }
    }

    public void addText(GuiTextRenderState textState) {
        if (this.findAppropriateNode(textState)) {
            this.current.addText(textState);
            this.addDebugRectangleIfEnabled(textState.bounds());
        }
    }

    public void addPicturesInPictureState(PictureInPictureRenderState picturesInPictureState) {
        if (this.findAppropriateNode(picturesInPictureState)) {
            this.current.addPicturesInPictureState(picturesInPictureState);
            this.addDebugRectangleIfEnabled(picturesInPictureState.bounds());
        }
    }

    public void addGuiElement(GuiElementRenderState blitState) {
        if (this.findAppropriateNode(blitState)) {
            this.current.addGuiElement(blitState);
            this.addDebugRectangleIfEnabled(blitState.bounds());
        }
    }

    private void addDebugRectangleIfEnabled(ScreenRectangle bounds) {
        if (SharedConstants.DEBUG_RENDER_UI_LAYERING_RECTANGLES && bounds != null) {
            this.up();
            this.current
                .addGuiElement(
                    new ColoredRectangleRenderState(
                        RenderPipelines.GUI, TextureSetup.noTexture(), new Matrix3x2f(), 0, 0, 10000, 10000, 2000962815, 2000962815, bounds
                    )
                );
        }
    }

    private boolean findAppropriateNode(ScreenArea screenArea) {
        ScreenRectangle bounds = screenArea.bounds();
        if (bounds == null) {
            return false;
        }

        if (this.lastElementBounds != null && this.lastElementBounds.encompasses(bounds)) {
            this.up();
        } else {
            this.navigateToAboveHighestElementWithIntersectingBounds(bounds);
        }

        this.lastElementBounds = bounds;
        return true;
    }

    private void navigateToAboveHighestElementWithIntersectingBounds(ScreenRectangle bounds) {
        GuiRenderState.Node node = this.strata.getLast();

        while (node.up != null) {
            node = node.up;
        }

        boolean found = false;

        while (!found) {
            found = this.hasIntersection(bounds, node.elementStates)
                || this.hasIntersection(bounds, node.itemStates)
                || this.hasIntersection(bounds, node.textStates)
                || this.hasIntersection(bounds, node.picturesInPictureStates);
            if (node.parent == null) {
                break;
            }

            if (!found) {
                node = node.parent;
            }
        }

        this.current = node;
        if (found) {
            this.up();
        }
    }

    private boolean hasIntersection(ScreenRectangle bounds, List<? extends ScreenArea> states) {
        if (states != null) {
            for (ScreenArea area : states) {
                ScreenRectangle existingBounds = area.bounds();
                if (existingBounds != null && existingBounds.intersects(bounds)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void addBlitToCurrentLayer(BlitRenderState blitState) {
        this.current.addGuiElement(blitState);
    }

    public void addGlyphToCurrentLayer(GuiElementRenderState glyphState) {
        this.current.addGlyph(glyphState);
    }

    public Set<Object> getItemModelIdentities() {
        return this.itemModelIdentities;
    }

    public void forEachElement(Consumer<GuiElementRenderState> consumer, GuiRenderState.TraverseRange range) {
        this.traverse(node -> {
            if (node.elementStates != null) {
                for (GuiElementRenderState elementState : node.elementStates) {
                    consumer.accept(elementState);
                }
            }

            if (node.glyphStates != null) {
                for (GuiElementRenderState glyphState : node.glyphStates) {
                    consumer.accept(glyphState);
                }
            }
        }, range);
    }

    public void forEachItem(Consumer<GuiItemRenderState> consumer) {
        GuiRenderState.Node currentBackup = this.current;
        this.traverse(node -> {
            if (node.itemStates != null) {
                this.current = node;

                for (GuiItemRenderState itemState : node.itemStates) {
                    consumer.accept(itemState);
                }
            }
        }, GuiRenderState.TraverseRange.ALL);
        this.current = currentBackup;
    }

    public void forEachText(Consumer<GuiTextRenderState> consumer) {
        GuiRenderState.Node currentBackup = this.current;
        this.traverse(node -> {
            if (node.textStates != null) {
                for (GuiTextRenderState textState : node.textStates) {
                    this.current = node;
                    consumer.accept(textState);
                }
            }
        }, GuiRenderState.TraverseRange.ALL);
        this.current = currentBackup;
    }

    public void forEachPictureInPicture(Consumer<PictureInPictureRenderState> consumer) {
        GuiRenderState.Node currentBackup = this.current;
        this.traverse(node -> {
            if (node.picturesInPictureStates != null) {
                this.current = node;

                for (PictureInPictureRenderState pictureInPictureState : node.picturesInPictureStates) {
                    consumer.accept(pictureInPictureState);
                }
            }
        }, GuiRenderState.TraverseRange.ALL);
        this.current = currentBackup;
    }

    public void sortElements(Comparator<GuiElementRenderState> comparator) {
        this.traverse(node -> {
            if (node.elementStates != null) {
                if (SharedConstants.DEBUG_SHUFFLE_UI_RENDERING_ORDER) {
                    Collections.shuffle(node.elementStates);
                }

                node.elementStates.sort(comparator);
            }
        }, GuiRenderState.TraverseRange.ALL);
    }

    private void traverse(Consumer<GuiRenderState.Node> consumer, GuiRenderState.TraverseRange range) {
        int startIndex = 0;
        int endIndex = this.strata.size();
        if (range == GuiRenderState.TraverseRange.BEFORE_BLUR) {
            endIndex = Math.min(this.firstStratumAfterBlur, this.strata.size());
        } else if (range == GuiRenderState.TraverseRange.AFTER_BLUR) {
            startIndex = this.firstStratumAfterBlur;
        }

        for (int i = startIndex; i < endIndex; i++) {
            for (int ix = 0; ix < this.frameListeners.size(); ix++) {
                this.frameListeners.get(ix).traverseStratumBegin(i);
            }

            GuiRenderState.Node stratum = this.strata.get(i);
            this.traverse(stratum, consumer);

            for (int ix = 0; ix < this.frameListeners.size(); ix++) {
                this.frameListeners.get(i).traverseStratumEnd(i);
            }
        }
    }

    private void traverse(GuiRenderState.Node node, Consumer<GuiRenderState.Node> consumer) {
        consumer.accept(node);
        if (node.up != null) {
            this.traverse(node.up, consumer);
        }
    }

    public void reset() {
        this.itemModelIdentities.clear();
        this.strata.clear();
        this.firstStratumAfterBlur = Integer.MAX_VALUE;
        this.nextStratum();
        this.panoramaRenderState = null;
        this.clearColorOverride.set(0.0F);
    }

    public int getStratum() {
        return this.strata.size() - 1;
    }

    public void addFrameListener(GuiRenderListener listener) {
        this.frameListeners.add(listener);
    }

    public void removeFrameListener(GuiRenderListener listener) {
        this.frameListeners.remove(listener);
    }

    @Override
    public String toString() {
        int preBlur = Math.min(this.firstStratumAfterBlur, this.strata.size());
        return "strata: " + this.strata.size() + ", preBlur: " + preBlur + ", postBlur: " + (this.strata.size() - preBlur);
    }

    private static class Node {
        public final GuiRenderState.Node parent;
        public GuiRenderState.Node up;
        public List<GuiElementRenderState> elementStates;
        public List<GuiElementRenderState> glyphStates;
        public List<GuiItemRenderState> itemStates;
        public List<GuiTextRenderState> textStates;
        public List<PictureInPictureRenderState> picturesInPictureStates;

        private Node(GuiRenderState.Node parent) {
            this.parent = parent;
        }

        public void addItem(GuiItemRenderState itemState) {
            if (this.itemStates == null) {
                this.itemStates = new ArrayList<>();
            }

            this.itemStates.add(itemState);
        }

        public void addText(GuiTextRenderState textState) {
            if (this.textStates == null) {
                this.textStates = new ArrayList<>();
            }

            this.textStates.add(textState);
        }

        public void addPicturesInPictureState(PictureInPictureRenderState picturesInPictureState) {
            if (this.picturesInPictureStates == null) {
                this.picturesInPictureStates = new ArrayList<>();
            }

            this.picturesInPictureStates.add(picturesInPictureState);
        }

        public void addGuiElement(GuiElementRenderState blitState) {
            if (this.elementStates == null) {
                this.elementStates = new ArrayList<>();
            }

            this.elementStates.add(blitState);
        }

        public void addGlyph(GuiElementRenderState glyphState) {
            if (this.glyphStates == null) {
                this.glyphStates = new ArrayList<>();
            }

            this.glyphStates.add(glyphState);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (this.elementStates != null) {
                sb.append("elems: " + this.elementStates.size() + ", ");
            }

            if (this.glyphStates != null) {
                sb.append("glyphs: " + this.glyphStates.size() + ", ");
            }

            if (this.itemStates != null) {
                sb.append("items: " + this.itemStates.size() + ", ");
            }

            if (this.textStates != null) {
                sb.append("texts: " + this.textStates.size() + ", ");
            }

            if (this.picturesInPictureStates != null) {
                sb.append("pips: " + this.picturesInPictureStates.size() + ", ");
            }

            if (this.up != null) {
                sb.append("up: (" + this.up + "), ");
            }

            return StrUtils.removeSuffix(sb.toString(), ", ");
        }
    }

    public enum TraverseRange {
        ALL,
        BEFORE_BLUR,
        AFTER_BLUR;
    }
}
