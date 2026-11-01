package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.GLFWErrorCapture;
import com.mojang.blaze3d.GLFWErrorScope;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import net.optifine.util.VideoModeComparator;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWVidMode.Buffer;
import org.slf4j.Logger;

public record Monitor(String monitorName, long monitor, List<VideoMode> videoModes, VideoMode currentMode, int x, int y) {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();

    public static Monitor tryCreate(long monitor) {
        GLFWErrorCapture glfwErrors = new GLFWErrorCapture();

        try (GLFWErrorScope var3 = new GLFWErrorScope(glfwErrors)) {
            String monitorName = queryMonitorName(monitor);
            List<VideoMode> videoModes = new ArrayList<>();
            Buffer modes = GLFW.glfwGetVideoModes(monitor);
            if (modes == null) {
                LOGGER.warn("Failed to query video modes of monitor {}", monitorName);
                return null;
            }

            GLFWVidMode glCurrentMode = GLFW.glfwGetVideoMode(monitor);
            VideoMode currentVideoMode = new VideoMode(glCurrentMode);
            List<VideoMode> removedModes = new ArrayList<>();

            for (int i = modes.limit() - 1; i >= 0; i--) {
                modes.position(i);
                VideoMode mode = new VideoMode(modes);
                if (mode.getRedBits() >= 8 && mode.getGreenBits() >= 8 && mode.getBlueBits() >= 8) {
                    if (mode.getRefreshRate() < currentVideoMode.getRefreshRate()) {
                        removedModes.add(mode);
                    } else {
                        videoModes.add(mode);
                    }
                }
            }

            removedModes.sort(new VideoModeComparator().reversed());

            for (VideoMode vm : removedModes) {
                if (getVideoMode(videoModes, vm.getWidth(), vm.getHeight()) == null) {
                    videoModes.add(vm);
                }
            }

            videoModes.sort(new VideoModeComparator());
            int[] x = new int[1];
            int[] y = new int[1];
            GLFW.glfwGetMonitorPos(monitor, x, y);
            GLFWVidMode currentMode = GLFW.glfwGetVideoMode(monitor);
            if (currentMode == null) {
                LOGGER.warn("Failed to query current video mode of monitor {}", monitorName);
                return null;
            } else {
                return new Monitor(monitorName, monitor, videoModes, new VideoMode(currentMode), x[0], y[0]);
            }
        } finally {
            for (GLFWErrorCapture.Error error : glfwErrors) {
                LOGGER.error("GLFW error collected during monitor 0x{} query: {}", HEX_FORMAT.toHexDigits(monitor), error);
            }
        }
    }

    private static String queryMonitorName(long monitor) {
        String monitorName = Objects.requireNonNull(GLFW.glfwGetMonitorName(monitor), "unknown");
        return monitorName + "[0x" + HEX_FORMAT.toHexDigits(monitor) + "]";
    }

    public VideoMode getPreferredVidMode(Optional<VideoMode> expectedMode) {
        if (expectedMode.isPresent()) {
            VideoMode videoMode = expectedMode.get();

            for (VideoMode mode : this.videoModes) {
                if (mode.equals(videoMode)) {
                    return mode;
                }
            }
        }

        return this.currentMode;
    }

    public int indexOfMode(VideoMode videoMode) {
        return this.videoModes.indexOf(videoMode);
    }

    public VideoMode mode(int mode) {
        return this.videoModes.get(mode);
    }

    public int modeCount() {
        return this.videoModes.size();
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s(%s at (%d,%d))", this.monitorName, this.currentMode, this.x, this.y);
    }

    public static VideoMode getVideoMode(List<VideoMode> list, int width, int height) {
        for (VideoMode vm : list) {
            if (vm.getWidth() == width && vm.getHeight() == height) {
                return vm;
            }
        }

        return null;
    }
}
