package net.minecraft.client.renderer.feature;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.optifine.Config;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersRender;

public abstract class RenderTypeFeatureRenderer<Submit extends SubmitNode> implements FeatureRenderer<Submit> {
    private RenderTypeFeatureRenderer.Group currentGroup;
    private final List<RenderTypeFeatureRenderer.Group> groups = new ArrayList<>();

    protected abstract void buildGroup(FeatureFrameContext var1, List<Submit> var2);

    protected final VertexConsumer getVertexBuilder(RenderType renderType) {
        VertexConsumer vb = this.currentGroup().getVertexBuilder(renderType);
        vb.setRenderTypeBuffer(this);
        return vb;
    }

    protected RenderTypeFeatureRenderer.Group currentGroup() {
        return Objects.requireNonNull(this.currentGroup, "Not preparing group");
    }

    @Override
    public final void prepareGroup(FeatureFrameContext context, List<Submit> submits, boolean strictlyOrdered) {
        this.currentGroup = new RenderTypeFeatureRenderer.Group(context.stagedVertexBuffer(), !strictlyOrdered);
        this.buildGroup(context, submits);
        this.groups.add(this.currentGroup);
        this.currentGroup = null;
    }

    @Override
    public void executeGroup(FeatureFrameContext context, int groupIndex, List<Submit> submits, boolean strictlyOrdered) {
        RenderTypeFeatureRenderer.Group group = this.groups.get(groupIndex);
        boolean isShaders = Config.isShaders();

        for (int i = 0; i < group.draws.size(); i++) {
            PreparedRenderType renderType = group.drawRenderTypes.get(i);
            StagedVertexBuffer.Draw draw = group.draws.get(i);
            if (isShaders) {
                if (draw.getEntityRenderState() != null) {
                    EntityRenderState ers = draw.getEntityRenderState();
                    Shaders.nextEntity(ers.entity);
                    if (ers instanceof LivingEntityRenderState lers) {
                        if (lers.hasRedOverlay) {
                            Shaders.setEntityColor(1.0F, 0.0F, 0.0F, 0.3F);
                        }

                        if (lers.overlayProgress > 0.0F) {
                            Shaders.setEntityColor(lers.overlayProgress, lers.overlayProgress, lers.overlayProgress, 0.5F);
                        }
                    }
                }

                if (draw.getBlockEntity() != null) {
                    Shaders.nextBlockEntity(draw.getBlockEntity());
                }
            }

            StagedVertexBuffer.ExecuteInfo info = context.stagedVertexBuffer().getExecuteInfo(draw);
            if (info != null) {
                if (isShaders && renderType.renderType() != null) {
                    ShadersRender.preRender(renderType.renderType());
                }

                renderType.drawFromBuffer(info);
                if (isShaders && renderType.renderType() != null) {
                    ShadersRender.postRender(renderType.renderType());
                }

                if (isShaders) {
                    Shaders.setEntityColor(0.0F, 0.0F, 0.0F, 0.0F);
                }
            }
        }
    }

    @Override
    public void finishExecute(FeatureFrameContext context) {
        this.groups.clear();
    }

    public VertexConsumer getSubVertexBuilder(Identifier textureIn) {
        return this.currentGroup.getSubVertexBuilder(textureIn);
    }

    public void saveRenderState() {
        this.currentGroup.saveRenderState();
    }

    public void restoreRenderState() {
        this.currentGroup.restoreRenderState();
    }

    public static class Group {
        private final StagedVertexBuffer stagedBuffer;
        private final boolean canReorder;
        private final List<StagedVertexBuffer.Draw> draws = new ArrayList<>();
        private final List<PreparedRenderType> drawRenderTypes = new ArrayList<>();
        private RenderType lastRenderType;
        private StagedVertexBuffer.Draw lastDraw;
        private VertexConsumer lastVertexBuilder;
        private Deque<RenderType> prevRenderTypes = new ArrayDeque<>();
        private Deque<StagedVertexBuffer.Draw> prevDraws = new ArrayDeque<>();

        private Group(StagedVertexBuffer stagedBuffer, boolean canReorder) {
            this.stagedBuffer = stagedBuffer;
            this.canReorder = canReorder;
        }

        public VertexConsumer getVertexBuilder(RenderType renderType) {
            if (this.lastDraw == null || this.lastRenderType != renderType || !renderType.canConsolidateConsecutiveGeometry()) {
                this.lastDraw = this.getOrAddDraw(renderType);
                this.lastRenderType = renderType;
            }

            VertexConsumer vb = this.stagedBuffer.getVertexBuilder(this.lastDraw);
            this.lastVertexBuilder = vb;
            return vb;
        }

        private StagedVertexBuffer.Draw getOrAddDraw(RenderType renderType) {
            PreparedRenderType preparedRenderType = renderType.prepare();
            int existingIndex = this.canReorder && renderType.canConsolidateConsecutiveGeometry() ? this.drawRenderTypes.indexOf(preparedRenderType) : -1;
            if (existingIndex != -1) {
                return this.draws.get(existingIndex);
            }

            VertexSorting quadSorting = renderType.sortOnUpload() ? RenderSystem.getProjectionType().vertexSorting() : null;
            StagedVertexBuffer.Draw draw = this.stagedBuffer.appendDraw(renderType.format(), renderType.primitiveTopology(), quadSorting);
            this.draws.add(draw);
            this.drawRenderTypes.add(preparedRenderType);
            return draw;
        }

        public VertexConsumer getSubVertexBuilder(Identifier textureIn) {
            Identifier lastTexture = this.lastRenderType.getTextureLocation();
            if (textureIn.equals(lastTexture)) {
                return this.lastVertexBuilder;
            }

            RenderType renderType2 = this.lastRenderType.getTextured(textureIn);
            return this.getVertexBuilder(renderType2);
        }

        public void saveRenderState() {
            this.prevDraws.push(this.lastDraw);
            this.prevRenderTypes.push(this.lastRenderType);
            this.stagedBuffer.saveRenderState();
        }

        public void restoreRenderState() {
            if (!this.prevDraws.isEmpty()) {
                this.lastDraw = this.prevDraws.pop();
                this.lastRenderType = this.prevRenderTypes.pop();
                this.stagedBuffer.restoreRenderState();
            }
        }

        public StagedVertexBuffer.Draw getLastDraw() {
            return this.lastDraw;
        }
    }
}
