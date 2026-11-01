package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.GpuFormat;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.util.Mth;
import net.minecraftforge.client.extensions.IForgeVertexFormat;

public class VertexFormat implements IForgeVertexFormat {
    private static final int VERTEX_ALIGNMENT = 4;
    public static final int MAX_VERTEX_ELEMENTS = 16;
    private Map<String, VertexFormatElement> elements = new Object2ObjectArrayMap<>(16);
    private int vertexSize;
    private int stepRate;
    private List<VertexFormatElement> elementValues;
    private String name;
    private VertexFormatElement positionElement;
    private VertexFormatElement normalElement;
    private VertexFormatElement colorElement;
    private VertexFormatElement[] uvElements = new VertexFormatElement[3];
    private boolean extended;
    private ImmutableMap<String, VertexFormatElement> elementMapping;

    private VertexFormat(List<VertexFormatElement> elements, int vertexSize, int stepRate) {
        this.vertexSize = vertexSize;
        this.stepRate = stepRate;

        for (VertexFormatElement element : elements) {
            this.elements.putIfAbsent(element.name(), element);
            if (element.name().equals("Position")) {
                this.positionElement = element;
            } else if (element.name().equals("Normal")) {
                this.normalElement = element;
            } else if (element.name().equals("Color")) {
                this.colorElement = element;
            } else if (element.name().equals("UV0")) {
                this.uvElements[0] = element;
            } else if (element.name().equals("UV1")) {
                this.uvElements[1] = element;
            } else if (element.name().equals("UV2")) {
                this.uvElements[2] = element;
            }
        }

        this.elementValues = elements;
        ImmutableMap.Builder<String, VertexFormatElement> elementMappingBuilder = ImmutableMap.builder();

        for (VertexFormatElement element : elements) {
            elementMappingBuilder.put(element.name(), element);
        }

        this.elementMapping = elementMappingBuilder.buildOrThrow();
    }

    public static VertexFormat.Builder builder(int stepRate) {
        return new VertexFormat.Builder(stepRate);
    }

    @Override
    public String toString() {
        return "VertexFormat "
            + this.name
            + ", "
            + this.vertexSize
            + "B, "
            + this.elementValues.stream().map(VertexFormatElement::name).collect(Collectors.joining(", ", "[", "]"));
    }

    public int getVertexSize() {
        return this.vertexSize;
    }

    public int getStepRate() {
        return this.stepRate;
    }

    public List<VertexFormatElement> getElements() {
        return this.elementValues;
    }

    public VertexFormatElement getElement(String attributeName) {
        return this.elements.get(attributeName);
    }

    public boolean contains(String attributeName) {
        return this.elements.containsKey(attributeName);
    }

    @Override
    public boolean equals(Object o) {
        return this == o ? true : o instanceof VertexFormat format && this.elements.equals(format.elements) && this.vertexSize == format.vertexSize;
    }

    @Override
    public int hashCode() {
        return this.elementValues.hashCode();
    }

    public int getOffset(int index) {
        return this.elementValues.get(index).offset();
    }

    public boolean hasPosition() {
        return this.positionElement != null;
    }

    public int getPositionOffset() {
        return this.positionElement.offset();
    }

    public boolean hasNormal() {
        return this.normalElement != null;
    }

    public int getNormalOffset() {
        return this.normalElement.offset();
    }

    public boolean hasColor() {
        return this.colorElement != null;
    }

    public int getColorOffset() {
        return this.colorElement.offset();
    }

    public boolean hasUV(int id) {
        return this.uvElements[id] != null;
    }

    public int getUvOffsetById(int id) {
        return this.uvElements[id].offset();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void copyFrom(VertexFormat vf) {
        this.elements = vf.elements;
        this.vertexSize = vf.vertexSize;
        this.stepRate = vf.stepRate;
        this.elementValues = vf.elementValues;
        this.name = vf.name;
        this.positionElement = vf.positionElement;
        this.normalElement = vf.normalElement;
        this.colorElement = vf.colorElement;
        this.uvElements = vf.uvElements;
        this.extended = vf.extended;
        this.elementMapping = vf.elementMapping;
    }

    public VertexFormat duplicate() {
        VertexFormat.Builder vfb = builder(this.stepRate);
        vfb.addAll(this);
        return vfb.build();
    }

    public ImmutableMap<String, VertexFormatElement> getElementMapping() {
        return this.elementMapping;
    }

    public int getIntegerSize() {
        return this.getVertexSize() / 4;
    }

    public boolean isExtended() {
        return this.extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public static class Builder {
        private final List<VertexFormatElement> elements = new ArrayList<>(16);
        private int offset = 0;
        private final int stepRate;

        public Builder(int stepRate) {
            this.stepRate = stepRate;
        }

        private void createAttribute(String name, int offset, GpuFormat elementFormat) {
            if (this.elements.size() >= 16) {
                throw new IllegalArgumentException("Having more than 16 attributes are not supported");
            }

            if (!Mth.isMultipleOf(offset, elementFormat.byteAlignment())) {
                throw new IllegalArgumentException(name + " is not aligned to " + elementFormat.byteAlignment() + " as required by " + elementFormat);
            }

            VertexFormatElement element = new VertexFormatElement(name, offset, elementFormat);
            this.elements.add(element);
        }

        private void validateUniqueName(String name) {
            for (VertexFormatElement element : this.elements) {
                if (element.name().equals(name)) {
                    throw new IllegalArgumentException("Another vertex attribute exists with the name " + name);
                }
            }
        }

        public VertexFormat.Builder addAttribute(String name, GpuFormat elementFormat) {
            this.validateUniqueName(name);
            this.createAttribute(name, this.offset, elementFormat);
            this.offset = this.offset + elementFormat.blockSize();
            return this;
        }

        public VertexFormat.Builder addAttribute(String name, int stride, GpuFormat elementFormat) {
            this.validateUniqueName(name);
            this.createAttribute(name, this.offset, elementFormat);
            this.offset += stride;
            return this;
        }

        public VertexFormat.Builder addAttribute(String name, GpuFormat elementFormat, int columnCount) {
            this.validateUniqueName(name);

            for (int i = 0; i < columnCount; i++) {
                this.createAttribute(name, this.offset, elementFormat);
                this.offset = this.offset + elementFormat.blockSize();
            }

            return this;
        }

        public VertexFormat.Builder addAttribute(String name, int offset, int stride, GpuFormat elementFormat, int columnCount) {
            this.validateUniqueName(name);
            int offsetTracker = offset;

            for (int i = 0; i < columnCount; i++) {
                this.createAttribute(name, offsetTracker, elementFormat);
                offsetTracker += stride;
            }

            this.offset = Math.max(this.offset, offsetTracker);
            return this;
        }

        public VertexFormat build() {
            int vertexSize = this.offset;
            if (!Mth.isMultipleOf(vertexSize, 4)) {
                throw new IllegalStateException("Vertex size must be a multiple of 4, was " + vertexSize);
            } else {
                return new VertexFormat(this.elements, vertexSize, this.stepRate);
            }
        }

        public VertexFormat.Builder addAll(VertexFormat vf) {
            for (VertexFormatElement vfe : vf.getElements()) {
                this.addAttribute(vfe.name(), vfe.format());
            }

            while (this.offset < vf.getVertexSize()) {
                this.addAttribute("Padding" + this.offset, GpuFormat.R8_SINT);
            }

            return this;
        }
    }
}
