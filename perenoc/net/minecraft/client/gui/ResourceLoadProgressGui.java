package net.minecraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

import dev.fesly.font.Font;
import dev.fesly.font.Fonts;
import dev.fesly.impl.util.Status;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.ScaleUtil;
import dev.fesly.impl.util.render.StencilUtil;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.resources.IAsyncReloader;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.VanillaPack;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.optifine.Config;
import net.optifine.reflect.Reflector;
import net.optifine.render.GlBlendState;
import net.optifine.shaders.config.ShaderPackParser;
import net.optifine.util.PropertiesOrdered;

import static dev.fesly.impl.util.render.HudEffects.detime;

public class ResourceLoadProgressGui extends LoadingGui
{
    private static final ResourceLocation MOJANG_LOGO_TEXTURE = new ResourceLocation("nova","icons/loadscreen/girl2.png");
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("nova","icons/loadscreen/background.png");
    private static final ResourceLocation ROUND_TEXTURE = new ResourceLocation("nova","icons/loadscreen/round.png");
    private static final ResourceLocation SMILE_FACE_TEXTURE = new ResourceLocation("nova","icons/loadscreen/smile.png");
    private static final ResourceLocation SMILE_FACE2_TEXTURE = new ResourceLocation("nova","icons/loadscreen/smileface2.png");

    private static final ResourceLocation SMILE_FACE_BG = new ResourceLocation("nova","icons/loadscreen/applybg.png");

    private static final ResourceLocation NOVA_ICON = new ResourceLocation("nova","icons/loadscreen/icon.png");


    public static List<Status> statusList = new ArrayList<>();





    private static final int field_238627_b_ = ColorHelper.PackedColor.packColor(255, 0,0,0);
    private static final int field_238628_c_ = field_238627_b_ & 16777215;
    private final Minecraft mc;
    private final IAsyncReloader asyncReloader;
    private final Consumer<Optional<Throwable>> completedCallback;
    private final boolean reloading;
    private float progress;
    private long fadeOutStart = -1L;
    private long fadeInStart = -1L;
    private int colorBackground = field_238628_c_;
    private int colorBar = field_238628_c_;
    private int colorOutline = 16777215;
    private int colorProgress = 16777215;
    private GlBlendState blendState = null;
    private boolean fadeOut = false;

    public ResourceLoadProgressGui(Minecraft p_i225928_1_, IAsyncReloader p_i225928_2_, Consumer<Optional<Throwable>> p_i225928_3_, boolean p_i225928_4_)
    {
        this.mc = p_i225928_1_;
        this.asyncReloader = p_i225928_2_;
        this.completedCallback = p_i225928_3_;
        this.reloading = false;
    }

    public static void loadLogoTexture(Minecraft mc)
    {
        mc.getTextureManager().loadTexture(MOJANG_LOGO_TEXTURE, new ResourceLoadProgressGui.MojangLogoTexture());
    }

    private boolean yadroLoaded = false;
    private boolean minecraftLoaded = false;
    private boolean novaLoaded = false;
    private boolean priyatnoyIngri = false;

    public static boolean startGame = false;

    public static double random = Math.random();
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        int i = this.mc.getMainWindow().getScaledWidth();
        int j = this.mc.getMainWindow().getScaledHeight();
        long k = Util.milliTime();

        if (this.reloading && (this.asyncReloader.asyncPartDone() || this.mc.currentScreen != null) && this.fadeInStart == -1L)
        {
            this.fadeInStart = k;
        }

        float f = this.fadeOutStart > -1L ? (float)(k - this.fadeOutStart) / 1000.0F : -1.0F;
        float f1 = this.fadeInStart > -1L ? (float)(k - this.fadeInStart) / 500.0F : -1.0F;
        float f2;

        if (f >= 1.0F)
        {
            this.fadeOut = true;

            if (this.mc.currentScreen != null)
            {
                this.mc.currentScreen.render(matrixStack, 0, 0, partialTicks);
            }

            int l = MathHelper.ceil((1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);
            fill(matrixStack, 0, 0, i, j, this.colorBackground | l << 24);
            f2 = 1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F);
        }
        else if (this.reloading)
        {
            if (this.mc.currentScreen != null && f1 < 1.0F)
            {
                this.mc.currentScreen.render(matrixStack, mouseX, mouseY, partialTicks);
            }

            int i2 = MathHelper.ceil(MathHelper.clamp((double)f1, 0.15D, 1.0D) * 255.0D);
            fill(matrixStack, 0, 0, i, j, this.colorBackground | i2 << 24);
            f2 = MathHelper.clamp(f1, 0.0F, 1.0F);
        }
        else
        {
            fill(matrixStack, 0, 0, i, j, this.colorBackground | -16777216);
            f2 = 1.0F;
        }

        int j2 = (int)((double)this.mc.getMainWindow().getScaledWidth() * 0.5D);
        int i1 = (int)((double)this.mc.getMainWindow().getScaledHeight() * 0.5D);
        double d0 = Math.min((double)this.mc.getMainWindow().getScaledWidth() * 0.75D, (double)this.mc.getMainWindow().getScaledHeight()) * 0.25D;
        int j1 = (int)(d0);
        double d1 = d0 * 4.0D;
        int k1 = (int) (mc.getMainWindow().getScaledWidth() /2 - 15);
        boolean flag = true;

        if (this.blendState != null)
        {
            this.blendState.apply();

            if (!this.blendState.isEnabled() && this.fadeOut)
            {
                flag = false;
            }
        }
        boolean reload = startGame;


        String zagr = reload ? "Перезагружаем " : "Загружаем ";
        String zagrM = reload ? "Подгружаем Текстуры" : "Загружаем Майнкрафт";

        if (progress < 0.1f && !yadroLoaded) {
            statusList.add(new Status(zagr + "Ядро"));
            yadroLoaded = true;
        }
        if (progress > 0.4f && !minecraftLoaded) {
            statusList.add(new Status(zagrM));
            minecraftLoaded = true;
        }
        if (progress > 0.65f && !novaLoaded) {
            if (!reload) {
                statusList.add(new Status(zagr + "Нову"));
            }else{
                statusList.add(new Status("Прогружаем Пак ["));
            }
            novaLoaded = true;
        }
        if (progress > 0.945f && !priyatnoyIngri) {
            statusList.add(new Status("Приятной игры!"));
            if (!reload) startGame = true;
            priyatnoyIngri = true;
        }
        if (progress >= 0.97) statusList.clear();

        MainWindow mw = mc.getMainWindow();
        if (flag) {
            if (progress < 0.995) {
                RenderUtil.drawImage(BACKGROUND_TEXTURE, 0, 0, mw.getScaledWidth(), mw.getScaledHeight(), -1);

                float xOffer = 0.33125f;
                float yOffer = 0.1787037f;
                float wOffer = 0.3375f;
                float hOffer = 0.64166f;

                float w = 144;
                float h = 153;

                float x = mw.getScaledWidth() / 2f - w / 2f;
                float y = mw.getScaledHeight() / 2f - h / 2f;

                RenderUtil.roundedRectangle(x,y,w,h,10,Color.decode("#D9D9D9"));

                StencilUtil.initStencil();
                RenderUtil.roundedRectangle(x,y,w,h,10,Color.decode("#D9D9D9"));
                StencilUtil.bindReadStencilBuffer(1);
                RenderUtil.drawImage(SMILE_FACE_BG,x - 10,y - 10,w + 20,h + 20,-1);
                StencilUtil.uninitStencilBuffer();
                {
                    double sin = Math.cos(((System.currentTimeMillis() - detime) / 1200f));
                    float fWOffer = 127 / 648f;
                    float smileW = w * fWOffer;
                    RenderUtil.drawImage(random > 0.5 ? SMILE_FACE_TEXTURE : SMILE_FACE2_TEXTURE, x + (w / 2f) - smileW / 2f,
                            y + (h * 0.1f) - (sin * 1.5f),
                            smileW, smileW, -1);
                    Font loadFont = Fonts.ROBOTO_REGULAR.get(17f);
                    Font statusFont = Fonts.ROBOTO_REGULAR.get(14f);


                    String t = ".";
                    float alpha = 1f;
                    if (!statusList.isEmpty())
                        for (int b = 0 ; b < statusList.size() ; b++) {
                        alpha-=0.2f;
                        alpha = Math.max(alpha,0);

                        Status status = statusList.get(b);

                        float offset = 12;
                        status.animationY.run(b == 0 ? 0 : offset);
                        float BBAH = offset + 2;
                        float BBAHO = Math.max(b -1,0);
                        float xValue = x + w * 0.13f;
                        float yValue = y + (h * 0.45f) + (BBAHO * BBAH) + status.animationY.getNumberValue().floatValue();


                        status.setAlpha(alpha);
                        float bAlpha = 1f -  Math.min(status.animationAlpha.getNumberValue().floatValue(),1f);
                        float size = statusList.size();

                        Runnable round = () ->
                                RenderUtil.roundedRectangle(xValue,yValue,2,h * 0.0577f,3,Color.CYAN);
                        RenderUtil.applyBloom(3,1,round);
                        Color c = Color.CYAN;
                        RenderUtil.roundedRectangle(xValue,yValue,2,h * 0.0577f,3,new Color(c.getRed(),c.getGreen(),c.getBlue(),
                                ((int)(bAlpha * 255f))));
                        statusFont.draw(status.getName().equalsIgnoreCase( "Прогружаем Пак [") ?
                                        status.getName() + (String.format("%.2f",this.progress * 100) + "%]")
                                        : status.getName()
                                ,xValue + 10, yValue + statusFont.getMiddleOfBox(h * 0.0577f) * 2f,
                                new Color(0.11f,0.11f,0.11f,bAlpha).getRGB());
                    }

                    boolean bo = sin > 0;

                    if (!bo) {
                        if (sin > -0.33) {
                            t = ".";
                        } else if (sin > -0.66) {
                            t = "..";
                        } else {
                            t = "...";
                        }
                    } else {
                        if (sin < 0.33) {
                            t = ".";
                        } else if (sin < 0.66) {
                            t = "..";
                        } else {
                            t = "...";
                        }
                    }

                    loadFont.drawCenterXY("Загружаемся" + t,x + (w / 2f), y + (h * 0.35f),Color.decode("#222222").getRGB());
                }
                //RenderUtil.drawImage(SMILE_FACE_TEXTURE, x, y, w, h, -1);
            }
        }

      /*  RenderSystem.defaultBlendFunc();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.disableBlend();*/
        int l1 = (int)((double)this.mc.getMainWindow().getScaledHeight() * 0.9325D);
        float f3 = this.asyncReloader.estimateExecutionSpeed();
        this.progress = MathHelper.clamp(this.progress * 0.95F + f3 * 0.050000012F, 0.0F, 1.0F);
        Reflector.ClientModLoader_renderProgressText.call();

        if (f < 1.0F)
        {
            //this.func_238629_a_(matrixStack, i / 2 - k1, l1 - 5, i / 2 + k1, l1 + 5, 1.0F - MathHelper.clamp(f, 0.0F, 1.0F));
        }

        if (f >= 2.0F)
        {
            this.mc.setLoadingGui((LoadingGui)null);
        }

        if (this.fadeOutStart == -1L && this.asyncReloader.fullyDone() && (!this.reloading || f1 >= 2.0F))
        {
            this.fadeOutStart = Util.milliTime();

            try
            {
                this.asyncReloader.join();
                this.completedCallback.accept(Optional.empty());
            }
            catch (Throwable throwable)
            {
                this.completedCallback.accept(Optional.of(throwable));
            }

            if (this.mc.currentScreen != null)
            {
                this.mc.currentScreen.init(this.mc, this.mc.getMainWindow().getScaledWidth(), this.mc.getMainWindow().getScaledHeight());
            }
        }
    }

    private void func_238629_a_(MatrixStack p_238629_1_, int p_238629_2_, int p_238629_3_, int p_238629_4_, int p_238629_5_, float p_238629_6_)
    {
        int i = MathHelper.ceil((float)(p_238629_4_ - p_238629_2_ - 2) * this.progress);
        int j = Math.round(p_238629_6_ * 255.0F);

        if (this.colorBar != this.colorBackground)
        {
            int k = this.colorBar >> 16 & 255;
            int l = this.colorBar >> 8 & 255;
            int i1 = this.colorBar & 255;
            int j1 = ColorHelper.PackedColor.packColor(j, k, l, i1);
            fillRectBG(p_238629_2_, p_238629_3_, p_238629_4_, p_238629_5_, j1);
            fillRect(p_238629_2_, p_238629_3_, p_238629_4_, p_238629_5_, j1);
        }

        int j2 = this.colorOutline >> 16 & 255;
        int k2 = this.colorOutline >> 8 & 255;
        int l2 = this.colorOutline & 255;
        int i3 = ColorHelper.PackedColor.packColor(j, j2, k2, l2);
        //fillRect( p_238629_2_ + 1, p_238629_3_, p_238629_4_ - 1, p_238629_3_ + 1, i3);
        //fillRect( p_238629_2_ + 1, p_238629_5_, p_238629_4_ - 1, p_238629_5_ - 1, i3);
        //fillRect( p_238629_2_, p_238629_3_, p_238629_2_ + 1, p_238629_5_, i3);
        //fillRect( p_238629_4_, p_238629_3_, p_238629_4_ - 1, p_238629_5_, i3);
        int k1 = this.colorProgress >> 16 & 255;
        int l1 = this.colorProgress >> 8 & 255;
        int i2 = this.colorProgress & 255;
        i3 = ColorHelper.PackedColor.packColor(j, k1, l1, i2);


        Font font = Fonts.COMFORTAA_REGULAR.get(35);
        Font otherFont = Fonts.ROBOTO_REGULAR.get(18);

        String text = "Получи преимущество вместе с Nova client!";
        int center = p_238629_2_ + 2 + ((p_238629_4_- p_238629_2_ + 2 ) / 2);

        int posY = (int) (mc.getMainWindow().getScaledHeight() * 0.75f);
        ScaleUtil.scaleStart(center,posY + font.getHeight() / 2f,1f);
        StencilUtil.initStencil();
        fillRect(p_238629_2_ + 2, 0, p_238629_4_, posY + 10.5f, i3);
        StencilUtil.bindReadStencilBuffer(1);
        font.drawCenterXY(text,center, posY,-1);
        StencilUtil.uninitStencilBuffer();
        ScaleUtil.scaleEnd();


/*
 int finalI = i3;
        Runnable shadered = () ->
                fillRect(p_238629_2_ + 2, p_238629_3_ + 2, p_238629_2_ + i + 2, p_238629_5_, finalI);
        RenderUtil.applyBloom(3,1,shadered);
*/

        fillRectBG(p_238629_2_ + 2, p_238629_3_ + 2,  p_238629_4_, p_238629_5_ + 1, i3);

        fillRect(p_238629_2_ + 2, p_238629_3_ + 2, p_238629_2_ + i, p_238629_5_ + 1, i3);

        float height =  p_238629_5_ - (p_238629_3_ + 2);
        boolean variant = false;
        String precent = "" + String.format("%.0f", progress * 100) + "%";
        if (variant) {
            otherFont.draw(precent, p_238629_2_ + i - otherFont.getWidth(precent) - 2, p_238629_3_ - otherFont.getHeight() + 1, -1);
        }
        else {
            otherFont.drawCenterXY(precent,
                    p_238629_2_ + 2 +
                            (p_238629_4_ - (p_238629_2_ + 2))
                                    / 2f,
                    p_238629_3_ + 2 + otherFont.getHeight() / 2.39f, -1);
        }
    }

    public void fillRect(float minX,float minY,float maxX,float maxY,int cA) {
        int a = new Color(cA).getAlpha();
        Color c = new Color(255, 129, 129,a);
        RenderUtil.roundedRectangle(minX,minY,maxX - minX,maxY - minY, 2.5f,c);
    }
    public void fillRectBG(float minX,float minY,float maxX,float maxY,int cA) {
        int a = new Color(cA).getAlpha();
        Color c = new Color(255, 129, 129,Math.max(a - 120,0));
        RenderUtil.roundedRectangle(minX,minY,maxX - minX,maxY - minY, 2.5f,c);
    }

    public boolean isPauseScreen()
    {
        return true;
    }

    public void update()
    {
        this.colorBackground = field_238628_c_;
        this.colorBar = field_238628_c_;
        this.colorOutline = 16777215;
        this.colorProgress = 16777215;

        if (Config.isCustomColors())
        {
            try
            {
                String s = "optifine/color.properties";
                ResourceLocation resourcelocation = new ResourceLocation(s);

                if (!Config.hasResource(resourcelocation))
                {
                    return;
                }

                InputStream inputstream = Config.getResourceStream(resourcelocation);
                Config.dbg("Loading " + s);
                Properties properties = new PropertiesOrdered();
                properties.load(inputstream);
                inputstream.close();
                this.colorBackground = readColor(properties, "screen.loading", this.colorBackground);
                this.colorOutline = readColor(properties, "screen.loading.outline", this.colorOutline);
                this.colorBar = readColor(properties, "screen.loading.bar", this.colorBar);
                this.colorProgress = readColor(properties, "screen.loading.progress", this.colorProgress);
                this.blendState = ShaderPackParser.parseBlendState(properties.getProperty("screen.loading.blend"));
            }
            catch (Exception exception)
            {
                Config.warn("" + exception.getClass().getName() + ": " + exception.getMessage());
            }
        }
    }

    private static int readColor(Properties p_readColor_0_, String p_readColor_1_, int p_readColor_2_)
    {
        String s = p_readColor_0_.getProperty(p_readColor_1_);

        if (s == null)
        {
            return p_readColor_2_;
        }
        else
        {
            s = s.trim();
            int i = parseColor(s, p_readColor_2_);

            if (i < 0)
            {
                Config.warn("Invalid color: " + p_readColor_1_ + " = " + s);
                return i;
            }
            else
            {
                Config.dbg(p_readColor_1_ + " = " + s);
                return i;
            }
        }
    }

    private static int parseColor(String p_parseColor_0_, int p_parseColor_1_)
    {
        if (p_parseColor_0_ == null)
        {
            return p_parseColor_1_;
        }
        else
        {
            p_parseColor_0_ = p_parseColor_0_.trim();

            try
            {
                return Integer.parseInt(p_parseColor_0_, 16) & 16777215;
            }
            catch (NumberFormatException numberformatexception)
            {
                return p_parseColor_1_;
            }
        }
    }

    public boolean isFadeOut()
    {
        return this.fadeOut;
    }

    static class MojangLogoTexture extends SimpleTexture
    {
        public MojangLogoTexture()
        {
            super(ResourceLoadProgressGui.MOJANG_LOGO_TEXTURE);
        }

        protected SimpleTexture.TextureData getTextureData(IResourceManager resourceManager)
        {
            Minecraft minecraft = Minecraft.getInstance();
            VanillaPack vanillapack = minecraft.getPackFinder().getVanillaPack();

            try (InputStream inputstream = getLogoInputStream(resourceManager, vanillapack))
            {
                return new SimpleTexture.TextureData(new TextureMetadataSection(true, true), NativeImage.read(inputstream));
            }
            catch (IOException ioexception1)
            {
                return new SimpleTexture.TextureData(ioexception1);
            }
        }

        private static InputStream getLogoInputStream(IResourceManager p_getLogoInputStream_0_, VanillaPack p_getLogoInputStream_1_) throws IOException
        {
            return p_getLogoInputStream_0_.hasResource(ResourceLoadProgressGui.MOJANG_LOGO_TEXTURE) ? p_getLogoInputStream_0_.getResource(ResourceLoadProgressGui.MOJANG_LOGO_TEXTURE).getInputStream() : p_getLogoInputStream_1_.getResourceStream(ResourcePackType.CLIENT_RESOURCES, ResourceLoadProgressGui.MOJANG_LOGO_TEXTURE);
        }
    }
}
