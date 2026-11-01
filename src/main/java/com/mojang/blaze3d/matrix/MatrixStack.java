package com.mojang.blaze3d.matrix;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;

public class MatrixStack extends PoseStack {
    public static class Entry {
        private final Matrix4f matrix;

        public Entry(Matrix4f matrix) {
            this.matrix = matrix;
        }

        public Matrix4f getMatrix() {
            return matrix;
        }
    }

    public MatrixStack() {
        super();
    }

    public void push() {
        this.pushPose();
    }

    public void pop() {
        this.popPose();
    }

    public Entry getLast() {
        return new Entry(this.last().pose());
    }

    public void rotate(net.minecraft.util.math.vector.Quaternion quaternion) {
    }
}
