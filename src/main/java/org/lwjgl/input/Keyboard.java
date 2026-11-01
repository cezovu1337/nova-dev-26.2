package org.lwjgl.input;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Keyboard {
    public static boolean isKeyDown(int key) {
        try {
            long window = Minecraft.getInstance().getWindow().handle();
            return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
        } catch (Exception e) {
            return false;
        }
    }

    public static void enableRepeatEvents(boolean enable) {
    }
    public static final int KEY_NONE = 0;
    public static final int KEY_ESCAPE = GLFW.GLFW_KEY_ESCAPE;
    public static final int KEY_1 = GLFW.GLFW_KEY_1;
    public static final int KEY_2 = GLFW.GLFW_KEY_2;
    public static final int KEY_3 = GLFW.GLFW_KEY_3;
    public static final int KEY_4 = GLFW.GLFW_KEY_4;
    public static final int KEY_5 = GLFW.GLFW_KEY_5;
    public static final int KEY_6 = GLFW.GLFW_KEY_6;
    public static final int KEY_7 = GLFW.GLFW_KEY_7;
    public static final int KEY_8 = GLFW.GLFW_KEY_8;
    public static final int KEY_9 = GLFW.GLFW_KEY_9;
    public static final int KEY_0 = GLFW.GLFW_KEY_0;
    public static final int KEY_MINUS = GLFW.GLFW_KEY_MINUS;
    public static final int KEY_EQUALS = GLFW.GLFW_KEY_EQUAL;
    public static final int KEY_BACK = GLFW.GLFW_KEY_BACKSPACE;
    public static final int KEY_TAB = GLFW.GLFW_KEY_TAB;
    public static final int KEY_Q = GLFW.GLFW_KEY_Q;
    public static final int KEY_W = GLFW.GLFW_KEY_W;
    public static final int KEY_E = GLFW.GLFW_KEY_E;
    public static final int KEY_R = GLFW.GLFW_KEY_R;
    public static final int KEY_T = GLFW.GLFW_KEY_T;
    public static final int KEY_Y = GLFW.GLFW_KEY_Y;
    public static final int KEY_U = GLFW.GLFW_KEY_U;
    public static final int KEY_I = GLFW.GLFW_KEY_I;
    public static final int KEY_O = GLFW.GLFW_KEY_O;
    public static final int KEY_P = GLFW.GLFW_KEY_P;
    public static final int KEY_LBRACKET = GLFW.GLFW_KEY_LEFT_BRACKET;
    public static final int KEY_RBRACKET = GLFW.GLFW_KEY_RIGHT_BRACKET;
    public static final int KEY_RETURN = GLFW.GLFW_KEY_ENTER;
    public static final int KEY_LCONTROL = GLFW.GLFW_KEY_LEFT_CONTROL;
    public static final int KEY_A = GLFW.GLFW_KEY_A;
    public static final int KEY_S = GLFW.GLFW_KEY_S;
    public static final int KEY_D = GLFW.GLFW_KEY_D;
    public static final int KEY_F = GLFW.GLFW_KEY_F;
    public static final int KEY_G = GLFW.GLFW_KEY_G;
    public static final int KEY_H = GLFW.GLFW_KEY_H;
    public static final int KEY_J = GLFW.GLFW_KEY_J;
    public static final int KEY_K = GLFW.GLFW_KEY_K;
    public static final int KEY_L = GLFW.GLFW_KEY_L;
    public static final int KEY_SEMICOLON = GLFW.GLFW_KEY_SEMICOLON;
    public static final int KEY_APOSTROPHE = GLFW.GLFW_KEY_APOSTROPHE;
    public static final int KEY_GRAVE = GLFW.GLFW_KEY_GRAVE_ACCENT;
    public static final int KEY_LSHIFT = GLFW.GLFW_KEY_LEFT_SHIFT;
    public static final int KEY_BACKSLASH = GLFW.GLFW_KEY_BACKSLASH;
    public static final int KEY_Z = GLFW.GLFW_KEY_Z;
    public static final int KEY_X = GLFW.GLFW_KEY_X;
    public static final int KEY_C = GLFW.GLFW_KEY_C;
    public static final int KEY_V = GLFW.GLFW_KEY_V;
    public static final int KEY_B = GLFW.GLFW_KEY_B;
    public static final int KEY_N = GLFW.GLFW_KEY_N;
    public static final int KEY_M = GLFW.GLFW_KEY_M;
    public static final int KEY_COMMA = GLFW.GLFW_KEY_COMMA;
    public static final int KEY_PERIOD = GLFW.GLFW_KEY_PERIOD;
    public static final int KEY_SLASH = GLFW.GLFW_KEY_SLASH;
    public static final int KEY_RSHIFT = GLFW.GLFW_KEY_RIGHT_SHIFT;
    public static final int KEY_MULTIPLY = GLFW.GLFW_KEY_KP_MULTIPLY;
    public static final int KEY_LMENU = GLFW.GLFW_KEY_LEFT_ALT;
    public static final int KEY_SPACE = GLFW.GLFW_KEY_SPACE;
    public static final int KEY_CAPITAL = GLFW.GLFW_KEY_CAPS_LOCK;
    public static final int KEY_F1 = GLFW.GLFW_KEY_F1;
    public static final int KEY_F2 = GLFW.GLFW_KEY_F2;
    public static final int KEY_F3 = GLFW.GLFW_KEY_F3;
    public static final int KEY_F4 = GLFW.GLFW_KEY_F4;
    public static final int KEY_F5 = GLFW.GLFW_KEY_F5;
    public static final int KEY_F6 = GLFW.GLFW_KEY_F6;
    public static final int KEY_F7 = GLFW.GLFW_KEY_F7;
    public static final int KEY_F8 = GLFW.GLFW_KEY_F8;
    public static final int KEY_F9 = GLFW.GLFW_KEY_F9;
    public static final int KEY_F10 = GLFW.GLFW_KEY_F10;
    public static final int KEY_NUMLOCK = GLFW.GLFW_KEY_NUM_LOCK;
    public static final int KEY_SCROLL = GLFW.GLFW_KEY_SCROLL_LOCK;
    public static final int KEY_SUBTRACT = GLFW.GLFW_KEY_KP_SUBTRACT;
    public static final int KEY_ADD = GLFW.GLFW_KEY_KP_ADD;
    public static final int KEY_F11 = GLFW.GLFW_KEY_F11;
    public static final int KEY_F12 = GLFW.GLFW_KEY_F12;
    public static final int KEY_UP = GLFW.GLFW_KEY_UP;
    public static final int KEY_LEFT = GLFW.GLFW_KEY_LEFT;
    public static final int KEY_RIGHT = GLFW.GLFW_KEY_RIGHT;
    public static final int KEY_DOWN = GLFW.GLFW_KEY_DOWN;
    public static final int KEY_DELETE = GLFW.GLFW_KEY_DELETE;
    public static final int KEY_INSERT = GLFW.GLFW_KEY_INSERT;
    public static final int KEY_HOME = GLFW.GLFW_KEY_HOME;
    public static final int KEY_END = GLFW.GLFW_KEY_END;
    public static final int KEY_PAGEUP = GLFW.GLFW_KEY_PAGE_UP;
    public static final int KEY_PAGEDOWN = GLFW.GLFW_KEY_PAGE_DOWN;
    public static final int KEY_RMENU = GLFW.GLFW_KEY_RIGHT_ALT;
    public static final int KEY_RCONTROL = GLFW.GLFW_KEY_RIGHT_CONTROL;

    private static final Map<Integer, String> KEY_NAMES = new HashMap<>();
    private static final Map<String, Integer> KEY_MAP = new HashMap<>();

    static {
        for (Field f : Keyboard.class.getDeclaredFields()) {
            if (f.getName().startsWith("KEY_") && f.getType() == int.class) {
                try {
                    int val = f.getInt(null);
                    String name = f.getName().substring(4);
                    KEY_NAMES.put(val, name);
                    KEY_MAP.put(name.toUpperCase(), val);
                } catch (Exception ignored) {}
            }
        }
    }

    public static String getKeyName(int key) {
        String name = KEY_NAMES.get(key);
        if (name != null) return name;
        String glfwName = GLFW.glfwGetKeyName(key, 0);
        return glfwName != null ? glfwName.toUpperCase() : "NONE";
    }

    public static int getKeyIndex(String keyName) {
        Integer val = KEY_MAP.get(keyName.toUpperCase());
        return val != null ? val : KEY_NONE;
    }
}
