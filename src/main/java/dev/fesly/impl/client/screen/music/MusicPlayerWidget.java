package dev.fesly.impl.client.screen.music;
import dev.fesly.Nova;
import dev.fesly.client.notification.Notification;
import dev.fesly.client.notification.NotificationManager;
import dev.fesly.client.notification.NotificationType;
import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.client.screen.mainmenu.AltManager;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.animation.compact.Animation;
import dev.fesly.impl.util.animation.compact.Easing;
import dev.fesly.impl.util.animation.extended.Direction;
import dev.fesly.impl.util.animation.extended.impl.EaseBackIn;
import dev.fesly.impl.util.color.ClientUtil;
import dev.fesly.impl.util.math.MathUtil;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.ScaleUtil;
import dev.fesly.impl.util.render.StencilUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;
import java.awt.*;
import java.util.ArrayList;
import static dev.fesly.impl.util.render.HudEffects.detime;
public  class MusicPlayerWidget {
    public class AltManager implements IAccess {
        private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("nova","icons/loadscreen/background.png");
        private static final ResourceLocation SMILE_FACE_BG = new ResourceLocation("nova","icons/loadscreen/applybg.png");
        private static final ResourceLocation ALT_TEXTURE = new ResourceLocation("nova","icons/loadscreen/alt.png");
        public final EaseBackIn animation = new EaseBackIn(350, 1,1, Direction.BACKWARDS);
        private boolean closed = false;
        private boolean started = false;
        private int dragRelativeX;
        private int dragRelativeY;
        private boolean dragged = false;
        private int x = -1;
        private int y = -1;
        private int w = (int) (144 * 0.95f);
        private int h = (int) (153 * 0.95f);
        private int SW = (int) (144);
        private int SH = (int) (153);
        private int getX() {
            return (int) MathUtil.clamp(0,mw.getScaledWidth() - w,x);
        }
        private int getY() {
            return (int) MathUtil.clamp(0,mw.getScaledHeight() - h,y);
        }
        public void mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (MathUtil.isHovered(x,y,w,h,mouseX,mouseY)) {
                if (dragged) {
                    x = ((int) (mouseX - dragRelativeX));
                    y = ((int) (mouseY - dragRelativeY));
                }
            }
        }
        private SelectionType selected = null;
        private ArrayList<String> login = new ArrayList<String>();
        private ArrayList<String> password = new ArrayList<String>();
        private enum SelectionType {
            Login,
            Password,
        }
        public void keyPressed(int keyCode, int scanCode, int modifiers) {
            if (selected != null) {
                String buffer = mc.keyboardListener.getClipboardString();
                boolean isShiftPressed = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
                boolean isControlPressed = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
                boolean isCapsLockOn = false;
                if (GLFW.GLFW_KEY_BACKSPACE == keyCode) {
                    if (!(selected.equals(SelectionType.Login) ? login : password).isEmpty())
                        (selected.equals(SelectionType.Login) ? login : password).remove((selected.equals(SelectionType.Login) ? login : password).size() - 1);
                } else {
                    String keyName = GLFW.glfwGetKeyName(keyCode, scanCode);
                    if (keyName != null) {
                        if (isShiftPressed || isCapsLockOn)
                            keyName = keyName.toUpperCase();
                        else
                            keyName = keyName.toLowerCase();
                        if (keyName.equals("-") && isShiftPressed) keyName = "_";
                        if ((keyName.equalsIgnoreCase("v") || keyName.equalsIgnoreCase("м")) && isControlPressed) {
                            ArrayList<String> textToPaste = new ArrayList<>();
                            char[] chars = buffer.toCharArray();
                            for (char c : chars) {
                                if (!String.valueOf(c).equals(" ")) {
                                    textToPaste.add(String.valueOf(c));
                                }
                            }
                            (selected.equals(SelectionType.Login) ? login : password).addAll(textToPaste);
                        }else {
                            (selected.equals(SelectionType.Login) ? login : password).add(keyName);
                        }
                    }
                }
            }
        }
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            {
                float bw = w * 0.8f;
                float by = h * 0.515f;
                int height = 17;
                if (MathUtil.isHovered((int) (x + w / 2f - bw / 2f),
                        (int) (y + by),
                        (int) bw,height,mouseX,mouseY)) {
                    selected = SelectionType.Login;
                    return true;
                }
                else if (MathUtil.isHovered( (int) (x + w / 2f - bw / 2f),
                        (int) (y + by + height + 5),
                        (int) bw, height,mouseX,mouseY)) {
                    selected = SelectionType.Password;
                    return true;
                }else if(MathUtil.isHovered( (int) (x + w / 2f - bw / 2f),
                        (int) (y + by + (height * 2) + 10),
                        (int) bw, height,mouseX,mouseY)) {
                    if (login.isEmpty()) {
                        NotificationManager.sendNotification(new Notification("Логин не введен.", NotificationType.INFO));
                        return true;
                    }else {
                        StringBuilder loginString = new StringBuilder();
                        for (int i = 0 ; i < login.size() ; i++) {
                            loginString.append(login.get(i));
                        }
                        mc.session = new net.minecraft.util.Session(loginString.toString(), "", "", "mojang");
                        NotificationManager.sendNotification(new Notification("Успешная авторизация под ником " + TextFormatting.fromColorIndex(ClientUtil.getTwoClientColor().getRGB()) + loginString.toString(), NotificationType.INFO));
                        onClose();
                        return true;
                    }
                }else {
                    selected = null;
                }
            }
            if (MathUtil.isHovered(x,y,w,h,mouseX,mouseY)) {
                mouseX = (float) (mc.mouseHelper.getMouseX() / mw.getScaleFactor());
                mouseY = (float) (mc.mouseHelper.getMouseY() / mw.getScaleFactor());
                dragRelativeX = (int) (mouseX - x);
                dragRelativeY = (int) ((mouseY) - y);
                dragged = true;
                return  true;
            }
            return false;
        }
        public void mouseReleased(double mouseX, double mouseY, int button) {
            dragged = false;
        }
        public void check() {
            if (!started) start();
            else onClose();
        }
        public void start() {
            closed = false;
            started =true;
            if (x == -1 || y == -1) {
                x = 2;
                y = 46;
            }
            animation.setDirection(true);
        }
        public void onClose() {
            login.clear();
            password.clear();
            closed = true;
            started = false;
            animation.setDirection(false);
        }
        private final Animation loginAnim = new Animation(Easing.EASE_OUT_CUBIC, 350);
        private final Animation  passwordAnim = new Animation(Easing.EASE_OUT_CUBIC, 350);
        private final Animation  saveAnim = new Animation(Easing.EASE_OUT_CUBIC, 350);
        public void drawRound(String text,float x,float y,float width,float height,boolean save, double mouseX,double mouseY) {
            boolean hovered = MathUtil.isHovered(x,y,width,height,mouseX,mouseY);
            float bw = w * 0.8f;
            float by = h * 0.515f;
            int heightA = 17;
            boolean selectedLogin = false;
            boolean selectedPassword = false;
            if (selected != null) {
                selectedLogin =  selected.equals(SelectionType.Login);
            }
            if (selected != null) {
                selectedPassword =  selected.equals(SelectionType.Password);
            }
            boolean loginHovered = MathUtil.isHovered((int) (this.x + w / 2f - bw / 2f),
                    (int) (this.y + by),
                    (int) width,height,mouseX,mouseY) || selectedLogin;
            boolean passwordHovered = MathUtil.isHovered( (int) (this.x + w / 2f - bw / 2f),
                    (int) (this.y + by + heightA + 5),width,height,mouseX,mouseY) || selectedPassword;
            boolean saveHovered = MathUtil.isHovered( (int) (x + w / 2f - bw / 2f),
                    (int) (this.y + by + height * 2 + 10),width,height,mouseX,mouseY);
            {
                if (loginHovered) {
                    loginAnim.run(200);
                }
                else if (passwordHovered) {
                    passwordAnim.run(200);
                }
                else   if (saveHovered) {
                    saveAnim.run(200);
                }
            }{
                if (!loginHovered) {
                    loginAnim.run(248);
                }
                if (!passwordHovered) {
                    passwordAnim.run(248);
                }
                if (!saveHovered) {
                    saveAnim.run(248);
                }
            }
            int r = 0;
            if (text.contains("Логин") || text.contains(!login.isEmpty() ? login.get(0) : "")) r = loginAnim.getNumberValue().intValue();
            if (text.contains("Пароль") || text.contains(!password.isEmpty() ? password.get(0) : "")) r = passwordAnim.getNumberValue().intValue();
            if (text.contains("Сохранить")) r = saveAnim.getNumberValue().intValue();
            if (text.contains("Пароль") || text.contains((!password.isEmpty() ? password.get(0) : "ZALUPER1337$12__"))) r = passwordAnim.getNumberValue().intValue();
            if (text.contains("Логин") || text.contains((!login.isEmpty() ? login.get(0) : "ZALUPER1337$12__"))) r = loginAnim.getNumberValue().intValue();
            RenderUtil.applyBloom(3,1,() -> {
                ScaleUtil.scaleStart(getX() + w / 2f,getY() + h / 2f,animation.getOutput().floatValue());
                RenderUtil.roundedRectangle(x + 1.5f,y + 4,width - 3,height-6,5, new Color(31,31,31));
                ScaleUtil.scaleEnd();
            });
            ScaleUtil.scaleStart(getX() + w / 2f,getY() + h / 2f,animation.getOutput().floatValue());
            RenderUtil.roundedRectangle(x,y,width,height,5, new Color(r, r, r));
            dev.fesly.font.Font font =   Fonts.ROBOTO_REGULAR.get(18);
            font.drawCenterXY(text, x + width / 2f, y +
                    height / 2f,new Color(34, 34, 34,save ? 255 : 125).getRGB());
            ScaleUtil.scaleEnd();
        }
        public void draw(double mouseX, double mouseY) {
            float x = getX();
            float y = getY();
            float SX = mw.getScaledWidth() / 2f - SW / 2f;
            float SY = mw.getScaledHeight() / 2f - SH / 2f;
            ScaleUtil.scaleStart(getX() + w / 2f,getY() + h / 2f,animation.getOutput().floatValue());
            StencilUtil.initStencil();
            RenderUtil.roundedRectangle(SX,SY,SW,SH,10, Color.decode("#D9D9D9"));
            StencilUtil.bindReadStencilBuffer(2);
            RenderUtil.roundedRectangle(x,y,w,h,10, Color.decode("#D9D9D9"));
            StencilUtil.uninitStencilBuffer();
            StencilUtil.initStencil();
            RenderUtil.roundedRectangle(SX,SY,SW,SH,10, Color.decode("#D9D9D9"));
            StencilUtil.bindReadStencilBuffer(1);
            RenderUtil.roundedRectangle(x,y,w,h,10, new Color(0x3AD9D9D9, true));
            StencilUtil.uninitStencilBuffer();
            ScaleUtil.scaleEnd();
            {
                float bw = w * 0.8f;
                float by = h * 0.515f;
                int height = 17;
                StringBuilder loginString = new StringBuilder();
                for (int i = 0 ; i < login.size() ; i++) {
                    loginString.append(login.get(i));
                }
                StringBuilder passwordString = new StringBuilder();
                for (int i = 0 ; i < password.size() ; i++) {
                    passwordString.append(password.get(i));
                }
                drawRound(login.isEmpty() ? "Логин" : loginString.toString(),(int) (x + w / 2f - bw / 2f),
                        (int) (y + by),
                        (int) bw,
                        height,false,mouseX,mouseY);
                drawRound(password.isEmpty() ? "Пароль(необяз.)" : passwordString.toString(),
                        (int) (x + w / 2f - bw / 2f),
                        (int) (y + by + height + 5),
                        (int) bw,
                        height,false,mouseX,mouseY);
                drawRound("Сохранить",(int) (x + w / 2f - bw / 2f),
                        (int) (y + by + height * 2 + 10),
                        (int) bw,
                        height,true,mouseX,mouseY);
            }
            ScaleUtil.scaleStart(getX() + w / 2f,getY() + h / 2f,animation.getOutput().floatValue());
            StencilUtil.initStencil();
            RenderUtil.roundedRectangle(x,y,w,h,10,Color.decode("#D9D9D9"));
            StencilUtil.bindReadStencilBuffer(1);
            RenderUtil.drawImage(SMILE_FACE_BG,x - 10,y - 10,w + 20,h + 20,-1);
            StencilUtil.uninitStencilBuffer();
            {
                double sin = Math.cos(((System.currentTimeMillis() - detime) / 1200f));
                float fWOffer = 127 / 648f;
                float smileW = 22;
                dev.fesly.font.Font loadFont = Fonts.ROBOTO_REGULAR.get(19f);
                dev.fesly.font.Font loadFont2 = Fonts.ROBOTO_REGULAR.get(18f);
                Font descriptionFont = Fonts.ROBOTO_LIGHT.get(22f);
                RenderUtil.drawImage(ALT_TEXTURE, x + (w / 2f) - smileW / 2f,
                        (y + (h * 0.1f) - 1.5f + (sin)),
                        smileW, smileW, -1);
                loadFont.drawCenterXY("Введите данные", x + (w / 2f), y + (h * 0.35f), Color.decode("#222222").getRGB());
            }
            ScaleUtil.scaleEnd();
            Nova.getInstance().getNotificationManager().drawMethod();
            IAccess.renderHudRunnables();
        }
    }
}
