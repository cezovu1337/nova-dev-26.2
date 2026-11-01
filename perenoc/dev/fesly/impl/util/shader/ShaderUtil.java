package dev.fesly.impl.util.shader;

import dev.fesly.Nova;
import dev.fesly.client.notification.Notification;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.file.FileUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.optifine.Config;
import org.lwjgl.opengl.GL11;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class ShaderUtil implements IAccess {
    @Getter
    public final int programID;

    public Framebuffer framebuffer = new Framebuffer(mw.getFramebufferWidth(), mw.getFramebufferHeight(), true);;

    public ShaderUtil(String fragmentShaderLoc, String vertexShaderLoc) {
        int program = glCreateProgram();
        try {
            int fragmentShaderID = switch (fragmentShaderLoc) {
                case "kawaseUpGlow" ->
                        createShader(new ByteArrayInputStream(kawaseUpGlow.getBytes()), GL_FRAGMENT_SHADER);
                case "glow" -> createShader(new ByteArrayInputStream(glowShader.getBytes()), GL_FRAGMENT_SHADER);
                case "chams" -> createShader(new ByteArrayInputStream(chams.getBytes()), GL_FRAGMENT_SHADER);
                case "roundRectTexture" ->
                        createShader(new ByteArrayInputStream(roundRectTexture.getBytes()), GL_FRAGMENT_SHADER);
                case "roundRectOutline" ->
                        createShader(new ByteArrayInputStream(roundRectOutline.getBytes()), GL_FRAGMENT_SHADER);
                case "kawaseUpBloom" ->
                        createShader(new ByteArrayInputStream(kawaseUpBloom.getBytes()), GL_FRAGMENT_SHADER);
                case "kawaseDownBloom" ->
                        createShader(new ByteArrayInputStream(kawaseDownBloom.getBytes()), GL_FRAGMENT_SHADER);
                case "kawaseUp" -> createShader(new ByteArrayInputStream(kawaseUp.getBytes()), GL_FRAGMENT_SHADER);
                case "kawaseDown" -> createShader(new ByteArrayInputStream(kawaseDown.getBytes()), GL_FRAGMENT_SHADER);
                case "gradientMask" ->
                        createShader(new ByteArrayInputStream(gradientMask.getBytes()), GL_FRAGMENT_SHADER);
                case "mask" -> createShader(new ByteArrayInputStream(mask.getBytes()), GL_FRAGMENT_SHADER);
                case "gradient" -> createShader(new ByteArrayInputStream(gradient.getBytes()), GL_FRAGMENT_SHADER);
                case "roundedRect" ->
                        createShader(new ByteArrayInputStream(roundedRect.getBytes()), GL_FRAGMENT_SHADER);
                case "clientbloom" ->
                        createShader(new ByteArrayInputStream(clientbloom.getBytes()), GL_FRAGMENT_SHADER);
                case "clientoutline" ->
                        createShader(new ByteArrayInputStream(clientoutline.getBytes()), GL_FRAGMENT_SHADER);

               /* case "clientKawaseUp" ->
                        createShader(new ByteArrayInputStream(kawaseUpClient.getBytes()),GL_FRAGMENT_SHADER);
                case "clientKawaseDown" ->
                        createShader(new ByteArrayInputStream(kawaseDownClient.getBytes()),GL_FRAGMENT_SHADER);*/

                case "glass" -> createShader(new ByteArrayInputStream(glass.getBytes()), GL_FRAGMENT_SHADER);
                case "outline" -> createShader(new ByteArrayInputStream(outline.getBytes()), GL_FRAGMENT_SHADER);

                default ->
                    createShader(mc.getResourceManager().getResource(new ResourceLocation(/*Nova.getInstance().getClientInfo().getNamespace()*/"nova", "shader/" + fragmentShaderLoc)).getInputStream(), GL_FRAGMENT_SHADER);
                };
            glAttachShader(program, fragmentShaderID);

            int vertexShaderID = createShader(mc.getResourceManager().getResource(new ResourceLocation(Nova.getInstance().getClientInfo().getNamespace(), "shader/" + vertexShaderLoc)).getInputStream(), GL_VERTEX_SHADER);
            glAttachShader(program, vertexShaderID);


        } catch (IOException e) {
            e.printStackTrace();
        }

        glLinkProgram(program);
        int status = glGetProgrami(program, GL_LINK_STATUS);

        if (status == 0) {
            throw new IllegalStateException("Shader link failed ( getter position empty )");
        }
        this.programID = program;
    }

    public ShaderUtil(String fragmentShaderLoc) {
        this(fragmentShaderLoc, "vertex.vsh");
    }


    public void init() {
        glUseProgram(programID);
    }

    public void unload() {
        glUseProgram(0);
    }

    public int getUniform(String name) {
        return glGetUniformLocation(programID, name);
    }


    public void setUniformf(String name, float... args) {
        int loc = glGetUniformLocation(programID, name);
        switch (args.length) {
            case 1 -> glUniform1f(loc, args[0]);
            case 2 -> glUniform2f(loc, args[0], args[1]);
            case 3 -> glUniform3f(loc, args[0], args[1], args[2]);
            case 4 -> glUniform4f(loc, args[0], args[1], args[2], args[3]);
        }
    }

    public void setUniformi(String name, int... args) {
        int loc = glGetUniformLocation(programID, name);
        if (args.length > 1) glUniform2i(loc, args[0], args[1]);
        else glUniform1i(loc, args[0]);
    }

    public static void drawQuad() {
        drawQuad(0.0, 0.0, mw.getScaledWidth(), mw.getScaledHeight());
    }
    public static void drawQuad(final double x, final double y, final double width, final double height) {
        drawQuads();
    }

    public static void drawScaledQuads() {
        drawQuads();
    }


    public static void drawQuadReverse() {
        if (Config.isFastRender()) return;
        float x = 0;
        float y = 0;
        float width = (float) mw.getScaledWidth();
        float height = (float) mw.getScaledHeight();
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0.0F, 1.0F);
        GL11.glVertex2d(x, y + height);
        GL11.glTexCoord2f(1.0F, 1.0F);
        GL11.glVertex2d(x + width, y + height);
        GL11.glTexCoord2f(1.0F, 0.0F);
        GL11.glVertex2d(x + width, y);
        GL11.glTexCoord2f(0.0F, 0.0F);
        GL11.glVertex2d(x, y);
        GL11.glEnd();
    }

    public static void drawQuads(final double x, final double y, final double width, final double height) {
        glBegin(GL_QUADS);
        glTexCoord2f(0.0F, 0.0F);
        if (Minecraft.getInstance().gameSettings.fullscreen || mw.getHeight() < 847) {
            glVertex2d(x, y + height);
            glTexCoord2f(1.0F, 0.0F);
            glVertex2d(x + width, y + height);
        }else {
            glVertex2d(x, y + height - 0.5f);
            glTexCoord2f(1.0F, 0.0F);
            glVertex2d(x + width, y + height - 0.5f);
        }
        glTexCoord2f(1.0F, 1.0F);
        glVertex2d(x + width, y);
        glTexCoord2f(0.0F, 1.0F);
        glVertex2d(x, y);
        glEnd();
    }

    public static void drawIQuads(float x, float y, float width, float height) {
        if (Config.isFastRender()) return;
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2f(x, y);
        glTexCoord2f(0, 1);
        glVertex2f(x, y + height);
        glTexCoord2f(1, 1);
        glVertex2f(x + width, y + height);
        glTexCoord2f(1, 0);
        glVertex2f(x + width, y);
        glEnd();
    }

    public static void drawQuads() {
        drawQuads(0.0, 0.0, mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight());
    }

    public static void drawQuads(float width, float height) {
        glBegin(GL_QUADS);
        glTexCoord2f(0, 1);
        glVertex2f(0, 0);
        glTexCoord2f(0, 0);
        glVertex2f(0, height);
        glTexCoord2f(1, 0);
        glVertex2f(width, height);
        glTexCoord2f(1, 1);
        glVertex2f(width, 0);
        glEnd();
    }

    private int createShader(InputStream inputStream, int shaderType) {
        int shader = glCreateShader(shaderType);
        try {
            glShaderSource(shader, FileUtils.readInputStream(inputStream));
            glCompileShader(shader);


            if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
                System.out.println(glGetShaderInfoLog(shader, 4096));
                throw new IllegalStateException(String.format("Shader (%s) failed to compile!", shaderType));
            }

        } catch (IllegalStateException ex) {
            System.err.println("FAILED LOAD SHADER , ERR:");
            System.out.println(glGetShaderInfoLog(shader, 4096));
            throw new IllegalStateException(String.format("Shader (%s) failed to compile!", shaderType));
        }

        return shader;
    }

    private final String kawaseUpClient = """
            #version 120
                        
            uniform sampler2D inTexture, textureToCheck;
            uniform vec2 halfpixel, offset, iResolution;
            uniform int check;
                        
            uniform float colorSpeed;
            uniform float time;
            uniform float exposure;
            uniform float gradientRange;
            uniform vec3 color1;
            uniform vec3 color2;
            uniform vec3 color3;
            uniform vec3 color4;
            uniform bool blurAgain;
            uniform bool gradient;
            uniform bool rainbow;
            uniform float alphaz;
                        
            vec3 ditheredColor(vec3 color, vec2 coords) {
                float noise = fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453);
                return color + (0.5/255.0) * (-1.0 + 2.0 * noise);
            }
                        
            vec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4) {
                vec3 mixed = color1 + (color2 - color1) * coords.y;
                vec3 mixed2 = color3 + (color4 - color3) * coords.y;
                return mixed2 + (mixed - mixed2) * coords.x;
            }
                        
            vec4 getRainbowColor(float alpha, vec2 uv) {
                vec3 startColor = color1 + (color2 - color1) * (0.7 + 0.5 * sin(time * (colorSpeed / 10.0) + (uv.xyx * gradientRange)));
                vec3 endColor = color3 + (color4 - color2) * (0.7 + 0.55*cos(time * (colorSpeed / 10.0) + ((uv.xyx * 6.0 + uv.x + uv.y))) * (-1.0));
                vec3 startColorB = color3 + (color4 - color3) * (0.7 + 0.5 * sin(time * (colorSpeed / 10.0) + (uv.xyx * gradientRange)));
                vec3 endColorB = color4 + (color3 - color4) * (0.7 + 0.55*cos(time * (colorSpeed / 10.0) + ((uv.xyx * 6.0 + uv.x + uv.y))) * (-1.0));
                        
                vec3 mixed = startColor + (endColor - startColor) * (0.7 + 0.55*sin(time * (colorSpeed / 10.0)+((uv.xyx * 6.0))) * (-1.0));
                vec3 mixedD = startColorB + (endColorB - startColorB) * (0.7 + 0.55*sin(time * (colorSpeed / 10.0)+((uv.xyx * 6.0))) * (-1.0));
                        
                vec3 mixedB = mixed + (mixedD - mixed) * (0.7 + 0.55*sin(time * (colorSpeed / 10.0)+((uv.xyx * 6.0))) * (-1.0));
                        
                float noise = fract(sin(dot(uv, vec2(12.9898, 78.233))) * 43758.5453);
                vec4 color = vec4(ditheredColor(mixed, uv), alpha * exposure);
                if (rainbow) {
                    return vec4(0.5 + 0.5 * sin(time * (colorSpeed / 10.0)+(uv.xyx * gradientRange)+vec3(0.0, 2.0, 4.0)), 1.0);
                }
                return vec4(ditheredColor(mixedB, uv), alpha * exposure);
            }
                        
            void main() {
                vec2 uv = vec2(gl_FragCoord.xy / iResolution);
                vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);
                sum += texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset) * 2.0;
                sum += texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);
                sum += texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset) * 2.0;
                sum += texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);
                sum += texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset) * 2.0;
                sum += texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);
                sum += texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset) * 2.0;
                        
                vec4 overlay = vec4(sum.rgb /12.0, mix(1.0, texture2D(textureToCheck, gl_TexCoord[0].st).a, check));
                        
                vec4 final = vec4(getRainbowColor(1f,uv).rgb + overlay.rgb,sum * getRainbowColor(1f,uv).a);
                gl_FragColor = final;
            }
            """;

    private final String kawaseDownClient = """
                    #version 120
                        
                    uniform sampler2D inTexture;
                    uniform vec2 offset, halfpixel, iResolution;
                        
                    uniform float colorSpeed;
                    uniform float time;
                    uniform float exposure;
                    uniform float gradientRange;
                    uniform vec3 color1;
                    uniform vec3 color2;
                    uniform vec3 color3;
                    uniform vec3 color4;
                    uniform bool blurAgain;
                    uniform bool gradient;
                    uniform bool rainbow;
                    uniform float alphaz;
                        
                    vec3 ditheredColor(vec3 color, vec2 coords) {
                        float noise = fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453);
                        return color + (0.5/255.0) * (-1.0 + 2.0 * noise);
                    }
                        
                    vec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4) {
                        vec3 mixed = color1 + (color2 - color1) * coords.y;
                        vec3 mixed2 = color3 + (color4 - color3) * coords.y;
                        return mixed2 + (mixed - mixed2) * coords.x;
                    }
                        
                    vec4 getRainbowColor(float alpha, vec2 uv) {
                        vec3 startColor = color1 + (color2 - color1) * (0.7 + 0.5 * sin(time * (colorSpeed / 10.0) + (uv.xyx * gradientRange)));
                        vec3 endColor = color3 + (color4 - color2) * (0.7 + 0.55*cos(time * (colorSpeed / 10.0) + ((uv.xyx * 6.0 + uv.x + uv.y))) * (-1.0));
                        vec3 startColorB = color3 + (color4 - color3) * (0.7 + 0.5 * sin(time * (colorSpeed / 10.0) + (uv.xyx * gradientRange)));
                        vec3 endColorB = color4 + (color3 - color4) * (0.7 + 0.55*cos(time * (colorSpeed / 10.0) + ((uv.xyx * 6.0 + uv.x + uv.y))) * (-1.0));
                        
                        vec3 mixed = startColor + (endColor - startColor) * (0.7 + 0.55*sin(time * (colorSpeed / 10.0)+((uv.xyx * 6.0))) * (-1.0));
                        vec3 mixedD = startColorB + (endColorB - startColorB) * (0.7 + 0.55*sin(time * (colorSpeed / 10.0)+((uv.xyx * 6.0))) * (-1.0));
                        
                        vec3 mixedB = mixed + (mixedD - mixed) * (0.7 + 0.55*sin(time * (colorSpeed / 10.0)+((uv.xyx * 6.0))) * (-1.0));
                        
                        float noise = fract(sin(dot(uv, vec2(12.9898, 78.233))) * 43758.5453);
                        vec4 color = vec4(ditheredColor(mixed, uv), alpha * exposure);
                        if (rainbow) {
                            return vec4(0.5 + 0.5 * sin(time * (colorSpeed / 10.0)+(uv.xyx * gradientRange)+vec3(0.0, 2.0, 4.0)), 1.0);
                        }
                        return vec4(ditheredColor(mixedB, uv), alpha * exposure);
                    }
                        
                    void main() {
                        vec2 uv = vec2(gl_FragCoord.xy / iResolution);
                        vec4 sum = texture2D(inTexture, gl_TexCoord[0].st) * 4.0;
                        sum += texture2D(inTexture, uv - halfpixel.xy * offset);
                        sum += texture2D(inTexture, uv + halfpixel.xy * offset);
                        sum += texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);
                        sum += texture2D(inTexture, uv - vec2(halfpixel.x, -halfpixel.y) * offset);
                        vec4 overlayColor = vec4(sum.rgb * .125, 1.0);
                        
                        vec4 finalColor = vec4( getRainbowColor(alpha, uv).rgb + (sum.rgb * .125),1.0)
                        gl_FragColor = finalColor;
                    }
            """;
    private final String kawaseUpGlow = """
            #version 120

            uniform sampler2D inTexture, textureToCheck;
            uniform vec2 halfpixel, offset, iResolution;
            uniform bool check;
            uniform float lastPass;
            uniform float exposure;

            void main() {
                if(check && texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;
                vec2 uv = vec2(gl_FragCoord.xy / iResolution);

                vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);
                sum.rgb *= sum.a;
                vec4 smpl1 =  texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset);
                smpl1.rgb *= smpl1.a;
                sum += smpl1 * 2.0;
                vec4 smp2 = texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);
                smp2.rgb *= smp2.a;
                sum += smp2;
                vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset);
                smp3.rgb *= smp3.a;
                sum += smp3 * 2.0;
                vec4 smp4 = texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);
                smp4.rgb *= smp4.a;
                sum += smp4;
                vec4 smp5 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);
                smp5.rgb *= smp5.a;
                sum += smp5 * 2.0;
                vec4 smp6 = texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);
                smp6.rgb *= smp6.a;
                sum += smp6;
                vec4 smp7 = texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset);
                smp7.rgb *= smp7.a;
                sum += smp7 * 2.0;
                vec4 result = sum / 12.0;
                gl_FragColor = vec4(result.rgb / result.a, mix(result.a, 1.0 - exp(-result.a * exposure), step(0.0, lastPass)));
            }""";

    private final String glowShader = """
            #version 120

            uniform sampler2D textureIn, textureToCheck;
            uniform vec2 texelSize, direction;
            uniform vec3 color;
            uniform bool avoidTexture;
            uniform float exposure, radius;
            uniform float weights[256];

            #define offset direction * texelSize

            void main() {
                if (direction.y == 1 && avoidTexture) {
                    if (texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;
                }
                vec4 innerColor = texture2D(textureIn, gl_TexCoord[0].st);
                innerColor.rgb *= innerColor.a;
                innerColor *= weights[0];
                for (float r = 1.0; r <= radius; r++) {
                    vec4 colorCurrent1 = texture2D(textureIn, gl_TexCoord[0].st + offset * r);
                    vec4 colorCurrent2 = texture2D(textureIn, gl_TexCoord[0].st - offset * r);

                    colorCurrent1.rgb *= colorCurrent1.a;
                    colorCurrent2.rgb *= colorCurrent2.a;

                    innerColor += (colorCurrent1 + colorCurrent2) * weights[int(r)];
                }

                gl_FragColor = vec4(innerColor.rgb / innerColor.a, mix(innerColor.a, 1.0 - exp(-innerColor.a * exposure), step(0.0, direction.y)));
            }
            """;

    private final String chams = """
            #version 120

            uniform sampler2D textureIn;
            uniform vec4 color;
            void main() {
                float alpha = texture2D(textureIn, gl_TexCoord[0].st).a;
                gl_FragColor = vec4(color.rgb, color.a * mix(0.0, alpha, step(0.0, alpha)));
            }
            """;

    private final String roundRectTexture = """
            #version 120

            uniform vec2 location, rectSize;
            uniform sampler2D textureIn;
            uniform float radius, alpha;

            float roundedBoxSDF(vec2 centerPos, vec2 size, float radius) {
                return length(max(abs(centerPos) -size, 0.)) - radius;
            }


            void main() {
                float distance = roundedBoxSDF((rectSize * .5) - (gl_TexCoord[0].st * rectSize), (rectSize * .5) - radius - 1., radius);
                float smoothedAlpha =  (1.0-smoothstep(0.0, 2.0, distance)) * alpha;
                gl_FragColor = vec4(texture2D(textureIn, gl_TexCoord[0].st).rgb, smoothedAlpha);
            }""";

    private final String roundRectOutline = """
            #version 120

            uniform vec2 location, rectSize;
            uniform vec4 color, outlineColor;
            uniform float radius, outlineThickness;

            float roundedSDF(vec2 centerPos, vec2 size, float radius) {
                return length(max(abs(centerPos) - size + radius, 0.0)) - radius;
            }

            void main() {
                float distance = roundedSDF(gl_FragCoord.xy - location - (rectSize * .5), (rectSize * .5) + (outlineThickness *.5) - 1.0, radius);

                float blendAmount = smoothstep(0., 2., abs(distance) - (outlineThickness * .5));

                vec4 insideColor = (distance < 0.) ? color : vec4(outlineColor.rgb,  0.0);
                gl_FragColor = mix(outlineColor, insideColor, blendAmount);

            }""";

    private final String kawaseUpBloom = """
            #version 120

            uniform sampler2D inTexture, textureToCheck;
            uniform vec2 halfpixel, offset, iResolution;
            uniform int check;

            void main() {
                vec2 uv = vec2(gl_FragCoord.xy / iResolution);

                vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);
                sum.rgb *= sum.a;
                vec4 smpl1 =  texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset);
                smpl1.rgb *= smpl1.a;
                sum += smpl1 * 2.0;
                vec4 smp2 = texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);
                smp2.rgb *= smp2.a;
                sum += smp2;
                vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset);
                smp3.rgb *= smp3.a;
                sum += smp3 * 2.0;
                vec4 smp4 = texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);
                smp4.rgb *= smp4.a;
                sum += smp4;
                vec4 smp5 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);
                smp5.rgb *= smp5.a;
                sum += smp5 * 2.0;
                vec4 smp6 = texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);
                smp6.rgb *= smp6.a;
                sum += smp6;
                vec4 smp7 = texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset);
                smp7.rgb *= smp7.a;
                sum += smp7 * 2.0;
                vec4 result = sum / 12.0;
                gl_FragColor = vec4(result.rgb / result.a, mix(result.a, result.a * (1.0 - texture2D(textureToCheck, gl_TexCoord[0].st).a),check));
            }""";

    private final String kawaseDownBloom = """
            #version 120

            uniform sampler2D inTexture;
            uniform vec2 offset, halfpixel, iResolution;

            void main() {
                vec2 uv = vec2(gl_FragCoord.xy / iResolution);
                vec4 sum = texture2D(inTexture, gl_TexCoord[0].st);
                sum.rgb *= sum.a;
                sum *= 4.0;
                vec4 smp1 = texture2D(inTexture, uv - halfpixel.xy * offset);
                smp1.rgb *= smp1.a;
                sum += smp1;
                vec4 smp2 = texture2D(inTexture, uv + halfpixel.xy * offset);
                smp2.rgb *= smp2.a;
                sum += smp2;
                vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);
                smp3.rgb *= smp3.a;
                sum += smp3;
                vec4 smp4 = texture2D(inTexture, uv - vec2(halfpixel.x, -halfpixel.y) * offset);
                smp4.rgb *= smp4.a;
                sum += smp4;
                vec4 result = sum / 8.0;
                gl_FragColor = vec4(result.rgb / result.a, result.a);
            }""";

    private final String kawaseUp = """
            #version 120

            uniform sampler2D inTexture, textureToCheck;
            uniform vec2 halfpixel, offset, iResolution;
            uniform int check;

            void main() {
                vec2 uv = vec2(gl_FragCoord.xy / iResolution);
                vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);
                sum += texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset) * 2.0;
                sum += texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);
                sum += texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset) * 2.0;
                sum += texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);
                sum += texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset) * 2.0;
                sum += texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);
                sum += texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset) * 2.0;

                gl_FragColor = vec4(sum.rgb /12.0, mix(1.0, texture2D(textureToCheck, gl_TexCoord[0].st).a, check));
            }
            """;

    private final String kawaseDown = """
            #version 120

            uniform sampler2D inTexture;
            uniform vec2 offset, halfpixel, iResolution;

            void main() {
                vec2 uv = vec2(gl_FragCoord.xy / iResolution);
                vec4 sum = texture2D(inTexture, gl_TexCoord[0].st) * 4.0;
                sum += texture2D(inTexture, uv - halfpixel.xy * offset);
                sum += texture2D(inTexture, uv + halfpixel.xy * offset);
                sum += texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);
                sum += texture2D(inTexture, uv - vec2(halfpixel.x, -halfpixel.y) * offset);
                gl_FragColor = vec4(sum.rgb * .125, 1.0);
            }
            """;

    private final String gradientMask = """
            #version 120

            uniform vec2 location, rectSize;
            uniform sampler2D tex;
            uniform vec3 color1, color2, color3, color4;
            uniform float alpha;

            #define NOISE .5/255.0

            vec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4){
                vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);
                color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898,78.233))) * 43758.5453));
                return color;
            }

            void main() {
                vec2 coords = (gl_FragCoord.xy - location) / rectSize;
                float texColorAlpha = texture2D(tex, gl_TexCoord[0].st).a;
                gl_FragColor = vec4(createGradient(coords, color1, color2, color3, color4), texColorAlpha * alpha);
            }""";

    private final String mask = """
            #version 120

            uniform vec2 location, rectSize;
            uniform sampler2D u_texture, u_texture2;
            void main() {
                vec2 coords = (gl_FragCoord.xy - location) / rectSize;
                float texColorAlpha = texture2D(u_texture, gl_TexCoord[0].st).a;
                vec3 tex2Color = texture2D(u_texture2, gl_TexCoord[0].st).rgb;
                gl_FragColor = vec4(tex2Color, texColorAlpha);
            }""";


    private final String gradient = """
            #version 120

            uniform vec2 location, rectSize;
            uniform sampler2D tex;
            uniform vec4 color1, color2, color3, color4;
            #define NOISE .5/255.0

            vec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){
                vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);
                color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));
                return color;
            }

            void main() {
                vec2 coords = (gl_FragCoord.xy - location) / rectSize;
                gl_FragColor = createGradient(coords, color1, color2, color3, color4);
            }""";

    private final String roundedRectGradient = """
            #version 120

            uniform vec2 location, rectSize;
            uniform vec4 color1, color2, color3, color4;
            uniform float radius;

            #define NOISE .5/255.0

            float roundSDF(vec2 p, vec2 b, float r) {
                return length(max(abs(p) - b , 0.0)) - r;
            }

            vec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){
                vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);
                color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));
                return color;
            }

            void main() {
                vec2 st = gl_TexCoord[0].st;
                vec2 halfSize = rectSize * .5;
               \s
                float smoothedAlpha =  (1.0-smoothstep(0.0, 2., roundSDF(halfSize - (gl_TexCoord[0].st * rectSize), halfSize - radius - 1., radius)));
                vec4 gradient = createGradient(st, color1, color2, color3, color4);    gl_FragColor = vec4(gradient.rgb, gradient.a * smoothedAlpha);
            }""";


    private final String roundedRect = """
            #version 120

            uniform vec2 location, rectSize;
            uniform vec4 color;
            uniform float radius;
            uniform bool blur;

            float roundSDF(vec2 p, vec2 b, float r) {
                return length(max(abs(p) - b, 0.0)) - r;
            }


            void main() {
                vec2 rectHalf = rectSize * .5;
                float smoothedAlpha =  (1.0-smoothstep(0.0, 1.0, roundSDF(rectHalf - (gl_TexCoord[0].st * rectSize), rectHalf - radius - 1., radius))) * color.a;
                gl_FragColor = vec4(color.rgb, smoothedAlpha);// mix(quadColor, shadowColor, 0.0);

            }""";

    private final String glass = """
            #version 120
                        uniform sampler2D u_diffuse_sampler;
                         uniform sampler2D u_other_sampler;
                         uniform vec2 u_texel_size;
                         uniform vec2 u_direction;
                         uniform float u_radius;
                         uniform float mixAmount;
                         uniform float u_distortion;
                         uniform float u_refractive_index;
                         uniform vec2 u_color_shift;
                         uniform float u_jellyness;
                         uniform vec3 objectColor;
                         
                         void main() {
                             vec2 uv = gl_TexCoord[0].xy;
                             vec2 distortion = texture2D(u_diffuse_sampler, uv).xy * u_distortion;
                             vec2 distortedUV = uv + distortion;
                             vec2 offset = mix(distortion * u_direction, vec2(0.0), mixAmount);
                             vec4 refractionColor = texture2D(u_diffuse_sampler, distortedUV + offset * u_radius);
                             vec4 reflectionColor = texture2D(u_other_sampler, distortedUV);
                             vec3 finalColor = mix(reflectionColor.rgb, refractionColor.rgb, mixAmount);
                             vec3 colorShift = vec3(u_color_shift.x, u_color_shift.y, 0.0);
                             finalColor += colorShift;
                             finalColor *= objectColor;
                             finalColor *= u_jellyness;
                             gl_FragColor = vec4(finalColor, 1.0);
                         }
                        """;
    private final String clientbloom = """
            #version 120

            uniform sampler2D u_diffuse_sampler;
            uniform sampler2D u_other_sampler;
            uniform vec2 u_texel_size;
            uniform vec2 u_direction;
            uniform float u_radius;
            uniform float u_kernel[128];
            uniform float colorSpeed;
            uniform float time;
            uniform float exposure;
            uniform float gradientRange;
            uniform vec3 color1;
            uniform vec3 color2;
            uniform vec3 color3;
            uniform vec3 color4;
            uniform bool blurAgain;
            uniform bool gradient;
            uniform bool rainbow;
            uniform float alphaz;
            
            uniform bool colorful;
             uniform vec3 noColorfulColor;
             
             vec3 ditheredColor(vec3 color, vec2 coords) {
                 float noise = fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453);
                 return color + (0.5/255.0) * (-1.0 + 2.0 * noise);
             }
             
             vec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4) {
                 vec3 mixed = color1 + (color2 - color1) * coords.y;
                 vec3 mixed2 = color3 + (color4 - color3) * coords.y;
                 return mixed2 + (mixed - mixed2) * coords.x;
             }
             
             vec4 getRainbowColor(float alpha, vec2 uv) {
                 vec3 startColor = color1 + (color2 - color1) * (0.7 + 0.5 * sin(time * (colorSpeed / 10.0) + (uv.xyx * gradientRange)));
                 vec3 endColor = color3 + (color4 - color2) * (0.7 + 0.55*cos(time * (colorSpeed / 10.0) + ((uv.xyx * 6.0 + uv.x + uv.y))) * (-1.0));
                 vec3 startColorB = color3 + (color4 - color3) * (0.7 + 0.5 * sin(time * (colorSpeed / 10.0) + (uv.xyx * gradientRange)));
                 vec3 endColorB = color4 + (color3 - color4) * (0.7 + 0.55*cos(time * (colorSpeed / 10.0) + ((uv.xyx * 6.0 + uv.x + uv.y))) * (-1.0));
             
                 vec3 mixed = startColor + (endColor - startColor) * (0.7 + 0.55*sin(time * (colorSpeed / 10.0)+((uv.xyx * 6.0))) * (-1.0));
                 vec3 mixedD = startColorB + (endColorB - startColorB) * (0.7 + 0.55*sin(time * (colorSpeed / 10.0)+((uv.xyx * 6.0))) * (-1.0));
             
                 vec3 mixedB = mixed + (mixedD - mixed) * (0.7 + 0.55*sin(time * (colorSpeed / 10.0)+((uv.xyx * 6.0))) * (-1.0));
             
                 float noise = fract(sin(dot(uv, vec2(12.9898, 78.233))) * 43758.5453);
                 vec4 color = vec4(ditheredColor(mixed, uv), alpha * exposure);
             
                 return vec4(colorful ? ditheredColor(mixedB, uv) : noColorfulColor, alpha * exposure);
             }

            void main() {
                vec2 uv = gl_TexCoord[0].st;
                if (u_direction.x == 0.0) {
                    float alpha = texture2D(u_other_sampler, uv).a;
                    if (alpha > 0.0) discard;
                }

                float half_radius = u_radius / 2.0;
                vec4 pixel_color = texture2D(u_diffuse_sampler, uv);
                pixel_color.rgb *= pixel_color.a;
                pixel_color *= u_kernel[0];
                for (float f = 1; f <= u_radius; f++) {
                    vec2 offset = f * u_texel_size * u_direction;
                    vec4 left = texture2D(u_diffuse_sampler, uv - offset);
                    vec4 right = texture2D(u_diffuse_sampler, uv + offset);
                    left.rgb *= left.a;
                    right.rgb *= right.a;
                    pixel_color += (left + right) * u_kernel[int(f)];
                }

                gl_FragColor = vec4(getRainbowColor(1.0, uv).rgb, pixel_color.a * exposure);
            }
                """;

    private final String clientoutline = """
            #version 120
             uniform sampler2D u_texture;
             uniform sampler2D u_mask;
             uniform vec2 u_texel_size;
             uniform float u_radius;
             uniform float colorSpeed;
             uniform float time;
             uniform float exposure;
             uniform float gradientRange;
             uniform vec3 color1;
             uniform vec3 color2;
             uniform vec3 color3;
             uniform vec3 color4;
             uniform bool blurAgain;
             uniform bool gradient;
             uniform bool rainbow;
             uniform bool colorful;
             uniform vec3 noColorfulColor;
             
             vec3 ditheredColor(vec3 color, vec2 coords) {
                 float noise = fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453);
                 return color + (0.5/255.0) * (-1.0 + 2.0 * noise);
             }
             
             vec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4) {
                 vec3 mixed = color1 + (color2 - color1) * coords.y;
                 vec3 mixed2 = color3 + (color4 - color3) * coords.y;
                 return mixed2 + (mixed - mixed2) * coords.x;
             }
             
             vec4 getRainbowColor(float alpha, vec2 uv) {
                 vec3 startColor = color1 + (color2 - color1) * (0.7 + 0.5 * sin(time * (colorSpeed / 10.0) + (uv.xyx * gradientRange)));
                 vec3 endColor = color3 + (color4 - color2) * (0.7 + 0.55*cos(time * (colorSpeed / 10.0) + ((uv.xyx * 6.0 + uv.x + uv.y))) * (-1.0));
                 vec3 startColorB = color3 + (color4 - color3) * (0.7 + 0.5 * sin(time * (colorSpeed / 10.0) + (uv.xyx * gradientRange)));
                 vec3 endColorB = color4 + (color3 - color4) * (0.7 + 0.55*cos(time * (colorSpeed / 10.0) + ((uv.xyx * 6.0 + uv.x + uv.y))) * (-1.0));
             
                 vec3 mixed = startColor + (endColor - startColor) * (0.7 + 0.55*sin(time * (colorSpeed / 10.0)+((uv.xyx * 6.0))) * (-1.0));
                 vec3 mixedD = startColorB + (endColorB - startColorB) * (0.7 + 0.55*sin(time * (colorSpeed / 10.0)+((uv.xyx * 6.0))) * (-1.0));
             
                 vec3 mixedB = mixed + (mixedD - mixed) * (0.7 + 0.55*sin(time * (colorSpeed / 10.0)+((uv.xyx * 6.0))) * (-1.0));
             
                 float noise = fract(sin(dot(uv, vec2(12.9898, 78.233))) * 43758.5453);
                 vec4 color = vec4(ditheredColor(mixed, uv), alpha * exposure);
             
                 return vec4(colorful ? ditheredColor(mixedB, uv) : noColorfulColor, alpha * exposure);
             }
             
             bool hasColor(vec4 color, sampler2D texture, vec2 texCoord, vec2 radius, vec2 texelSize) {
                 vec4 sampleColor;
                 float i, j;
             
                 for (i = -radius.x; i <= radius.x; ++i) {
                     for (j = -radius.y; j <= radius.y; ++j) {
                         vec2 idx = texCoord + vec2(i, j) * texelSize;
             
                         if (idx.x < 0.0 || idx.x > 1.0 || idx.y < 0.0 || idx.y > 1.0) {
                             continue;
                         }
             
                         vec4 sample = texture2D(u_mask, idx);
                         
                         if (sample.r > 0.5) {
                                         sampleColor = texture2D(texture, idx);
                                         if (sampleColor != color) {
                                             return true;
                                         }
                                     }
                                 }
                             }
                         
                             return false;
                         }
                         
                         
                         void main(void) {
                           vec4 i_color, p_color;
                           vec2 tex_coord = gl_TexCoord[0].st;
                           p_color = texture2D(u_texture, tex_coord);
                           if (p_color.a > 0.0) {
                             discard;
                           }
                           float i, j;
                           for (i = -u_radius; i <= u_radius; ++i) {
                             for (j = -u_radius; j <= u_radius; ++j) {
                               i_color = texture2D(u_texture, tex_coord + vec2(i, j) * u_texel_size);
                               if (i_color.a > 0.0) {
                                 gl_FragColor = getRainbowColor(exposure, tex_coord);
                                 return;
                               }
                             }
                           }
                           discard;
                         }
                """;
    private final String outline = """
                   #version 120
                     
                   uniform vec2 texelSize, direction;
                   uniform sampler2D texture;
                   uniform float radius;
                   uniform vec3 color;
                   const float transparency = 0.0; // добавлена десятичная точка в float
                     
                   #define offset direction * texelSize
                     
                   void main() {
                       vec4 center = texture2D(texture, gl_TexCoord[0].st);
                       center.rgb *= center.a;
                       if (center.a > transparency) discard;
                     
                       vec4 finalColor = vec4(0.0, 0.0, 0.0, 0.0);
                       for (int i=-int(radius); i<=int(radius); i++) {
                           vec2 offsetCoord = gl_TexCoord[0].st + offset * float(i);
                           vec4 alphaCurrent = texture2D(texture, offsetCoord);
                     
                           alphaCurrent.rgb *= alphaCurrent.a;
                           finalColor += mix(center, alphaCurrent, (radius - abs(float(i))) / radius);
                       }
                     
                       gl_FragColor = vec4(finalColor.rgb / finalColor.a * color, finalColor.a);
                   }
            """;
}
