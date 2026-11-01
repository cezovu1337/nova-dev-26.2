package dev.fesly.impl.event.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fesly.impl.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ShaderEvent implements Event {
    private final MatrixStack matrix;

    private final boolean bloom;
}