package net.minecraft.client.renderer.culling;

import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import net.optifine.util.MathUtils;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

public class Frustum {
    public static final int OFFSET_STEP = 4;
    private final FrustumIntersection intersection = new FrustumIntersection();
    private final Matrix4f matrix = new Matrix4f();
    protected Vector4f viewVector;
    private double camX;
    private double camY;
    private double camZ;
    public boolean disabled;
    private boolean usePlanes;
    protected final Vector4f[] planes = new Vector4f[6];
    protected static final int INSIDE = -2;
    protected static final int OUTSIDE = -3;

    public Frustum(Matrix4fc modelView, Matrix4f projection) {
        this(modelView, projection, false);
    }

    public Frustum(Matrix4fc modelView, Matrix4f projection, boolean usePlanes) {
        this.usePlanes = usePlanes;
        this.calculateFrustum(modelView, projection);
    }

    public Frustum(Frustum frustum) {
        this.set(frustum);
    }

    public void set(Frustum frustum) {
        this.intersection.set(frustum.matrix);
        this.matrix.set(frustum.matrix);
        this.camX = frustum.camX;
        this.camY = frustum.camY;
        this.camZ = frustum.camZ;
        this.viewVector = frustum.viewVector;
        this.disabled = frustum.disabled;
        this.usePlanes = frustum.usePlanes;
        if (frustum.usePlanes) {
            for (int i = 0; i < this.planes.length; i++) {
                this.planes[i] = new Vector4f(frustum.planes[i]);
            }
        }
    }

    public Frustum offset(float offset) {
        this.camX = this.camX + this.viewVector.x * offset;
        this.camY = this.camY + this.viewVector.y * offset;
        this.camZ = this.camZ + this.viewVector.z * offset;
        return this;
    }

    public Frustum offsetToFullyIncludeCameraCube(int cubeSize) {
        double camX1 = Math.floor(this.camX / cubeSize) * cubeSize;
        double camY1 = Math.floor(this.camY / cubeSize) * cubeSize;
        double camZ1 = Math.floor(this.camZ / cubeSize) * cubeSize;
        double camX2 = Math.ceil(this.camX / cubeSize) * cubeSize;
        double camY2 = Math.ceil(this.camY / cubeSize) * cubeSize;
        int count = 0;

        for (double camZ2 = Math.ceil(this.camZ / cubeSize) * cubeSize;
            this.intersection
                    .intersectAab(
                        (float)(camX1 - this.camX),
                        (float)(camY1 - this.camY),
                        (float)(camZ1 - this.camZ),
                        (float)(camX2 - this.camX),
                        (float)(camY2 - this.camY),
                        (float)(camZ2 - this.camZ)
                    )
                != -2;
            this.camZ = this.camZ - this.viewVector.z() * 4.0F
        ) {
            this.camX = this.camX - this.viewVector.x() * 4.0F;
            this.camY = this.camY - this.viewVector.y() * 4.0F;
            if (count++ > 10) {
                break;
            }
        }

        return this;
    }

    public void prepare(double camX, double camY, double camZ) {
        this.camX = camX;
        this.camY = camY;
        this.camZ = camZ;
    }

    private void calculateFrustum(Matrix4fc modelView, Matrix4f projection) {
        projection.mul(modelView, this.matrix);
        this.intersection.set(this.matrix);
        this.viewVector = this.matrix.transformTranspose(new Vector4f(0.0F, 0.0F, 1.0F, 0.0F));
        if (this.usePlanes) {
            Matrix4f matrix4f = new Matrix4f(this.matrix).transpose();
            this.setFrustumPlane(matrix4f, -1, 0, 0, 0);
            this.setFrustumPlane(matrix4f, 1, 0, 0, 1);
            this.setFrustumPlane(matrix4f, 0, -1, 0, 2);
            this.setFrustumPlane(matrix4f, 0, 1, 0, 3);
            this.setFrustumPlane(matrix4f, 0, 0, -1, 4);
            this.setFrustumPlane(matrix4f, 0, 0, 1, 5);
        }
    }

    public boolean isVisible(AABB bb) {
        if (bb == IForgeBlockEntity.INFINITE_EXTENT_AABB) {
            return true;
        }

        int intersectionResult = this.cubeInFrustum(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
        return intersectionResult == -2 || intersectionResult == -1;
    }

    public int cubeInFrustum(BoundingBox bb) {
        return this.cubeInFrustum(bb.minX(), bb.minY(), bb.minZ(), bb.maxX() + 1, bb.maxY() + 1, bb.maxZ() + 1);
    }

    private int cubeInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if (this.disabled) {
            return -2;
        }

        float x1 = (float)(minX - this.camX);
        float y1 = (float)(minY - this.camY);
        float z1 = (float)(minZ - this.camZ);
        float x2 = (float)(maxX - this.camX);
        float y2 = (float)(maxY - this.camY);
        float z2 = (float)(maxZ - this.camZ);
        return this.usePlanes ? this.isBoxInFrustumRaw(x1, y1, z1, x2, y2, z2) : this.intersection.intersectAab(x1, y1, z1, x2, y2, z2);
    }

    public boolean pointInFrustum(double x, double y, double z) {
        return this.intersection.testPoint((float)(x - this.camX), (float)(y - this.camY), (float)(z - this.camZ));
    }

    public Vector4f[] getFrustumPoints() {
        Vector4f[] frustumPoints = new Vector4f[]{
            new Vector4f(-1.0F, -1.0F, -1.0F, 1.0F),
            new Vector4f(1.0F, -1.0F, -1.0F, 1.0F),
            new Vector4f(1.0F, 1.0F, -1.0F, 1.0F),
            new Vector4f(-1.0F, 1.0F, -1.0F, 1.0F),
            new Vector4f(-1.0F, -1.0F, 1.0F, 1.0F),
            new Vector4f(1.0F, -1.0F, 1.0F, 1.0F),
            new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
            new Vector4f(-1.0F, 1.0F, 1.0F, 1.0F)
        };
        Matrix4f clipToWorldMatrix = this.matrix.invert(new Matrix4f());

        for (int i = 0; i < 8; i++) {
            clipToWorldMatrix.transform(frustumPoints[i]);
            frustumPoints[i].div(frustumPoints[i].w());
        }

        return frustumPoints;
    }

    public double getCamX() {
        return this.camX;
    }

    public double getCamY() {
        return this.camY;
    }

    public double getCamZ() {
        return this.camZ;
    }

    private int isBoxInFrustumRaw(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        for (int i = 0; i < 6; i++) {
            Vector4f vector4f = this.planes[i];
            float x = vector4f.x();
            float y = vector4f.y();
            float z = vector4f.z();
            float w = vector4f.w();
            if (x * minX + y * minY + z * minZ + w <= 0.0F
                && x * maxX + y * minY + z * minZ + w <= 0.0F
                && x * minX + y * maxY + z * minZ + w <= 0.0F
                && x * maxX + y * maxY + z * minZ + w <= 0.0F
                && x * minX + y * minY + z * maxZ + w <= 0.0F
                && x * maxX + y * minY + z * maxZ + w <= 0.0F
                && x * minX + y * maxY + z * maxZ + w <= 0.0F
                && x * maxX + y * maxY + z * maxZ + w <= 0.0F) {
                return -3;
            }
        }

        return -2;
    }

    public boolean isBoxInFrustumFully(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if (this.disabled) {
            return true;
        }

        float minXf = (float)minX;
        float minYf = (float)minY;
        float minZf = (float)minZ;
        float maxXf = (float)maxX;
        float maxYf = (float)maxY;
        float maxZf = (float)maxZ;

        for (int i = 0; i < 6; i++) {
            Vector4f frustumi = this.planes[i];
            float x = frustumi.x();
            float y = frustumi.y();
            float z = frustumi.z();
            float w = frustumi.w();
            if (i < 4) {
                if (x * minXf + y * minYf + z * minZf + w <= 0.0F
                    || x * maxXf + y * minYf + z * minZf + w <= 0.0F
                    || x * minXf + y * maxYf + z * minZf + w <= 0.0F
                    || x * maxXf + y * maxYf + z * minZf + w <= 0.0F
                    || x * minXf + y * minYf + z * maxZf + w <= 0.0F
                    || x * maxXf + y * minYf + z * maxZf + w <= 0.0F
                    || x * minXf + y * maxYf + z * maxZf + w <= 0.0F
                    || x * maxXf + y * maxYf + z * maxZf + w <= 0.0F) {
                    return false;
                }
            } else if (x * minXf + y * minYf + z * minZf + w <= 0.0F
                && x * maxXf + y * minYf + z * minZf + w <= 0.0F
                && x * minXf + y * maxYf + z * minZf + w <= 0.0F
                && x * maxXf + y * maxYf + z * minZf + w <= 0.0F
                && x * minXf + y * minYf + z * maxZf + w <= 0.0F
                && x * maxXf + y * minYf + z * maxZf + w <= 0.0F
                && x * minXf + y * maxYf + z * maxZf + w <= 0.0F
                && x * maxXf + y * maxYf + z * maxZf + w <= 0.0F) {
                return false;
            }
        }

        return true;
    }

    public double getCameraX() {
        return this.camX;
    }

    public double getCameraY() {
        return this.camY;
    }

    public double getCameraZ() {
        return this.camZ;
    }

    private void setFrustumPlane(Matrix4f matrixIn, int xIn, int yIn, int zIn, int wIn) {
        Vector4f vector4f = new Vector4f(xIn, yIn, zIn, 1.0F);
        MathUtils.transform(vector4f, matrixIn);
        vector4f.normalize();
        this.planes[wIn] = vector4f;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.usePlanes) {
            sb.append("Planes\n");

            for (int i = 0; i < this.planes.length; i++) {
                Vector4f vec = this.planes[i];
                sb.append(vec.toString());
                sb.append("\n");
            }
        } else {
            sb.append("Intersection\n");
            sb.append(this.matrix.toString());
        }

        return sb.toString();
    }
}
