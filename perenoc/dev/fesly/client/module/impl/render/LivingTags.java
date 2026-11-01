package dev.fesly.client.module.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fesly.client.module.api.Category;
import dev.fesly.client.module.api.Module;
import dev.fesly.client.module.api.ModuleInfo;
import dev.fesly.client.module.impl.combat.Aura;
import dev.fesly.font.Fonts;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.impl.render.*;
import dev.fesly.impl.interfaces.client.IAccess;
import dev.fesly.impl.util.render.RenderUtil;
import dev.fesly.impl.util.render.ScaleUtil;
import dev.fesly.impl.util.render.StencilUtil;
import dev.fesly.impl.value.secondValues.BooleanSetting;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.text.*;
import net.optifine.render.RenderUtils;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import dev.fesly.font.Font;

@ModuleInfo(
        name = "Living Tags",
        description = "Отображает На Экране Информацию Об Энтити",
        category = Category.DISPLAY)
public class LivingTags  extends Module {

    public static Map<LivingEntity, double[]> entityPositions = new HashMap();
    Executor threadPool = Executors.newFixedThreadPool(1);
    /*@EventLink
    public Listener<RenderEntityNameEvent> onEntityTag = event -> {
        if (event.getEntity() instanceof PlayerEntity) event.setCancelled();
    };*/

    @Getter
    @Setter
    private boolean background = true;

    @Override
    public void init() {
        super.init();
        addSetting("background",new BooleanSetting("Background",this, this::isBackground,this::setBackground));
    }

    @EventLink
    public Listener<BossHealthRenderEvent> onRender = event -> {
        IAccess.addRunnableWithChecks(() ->draw(true));
        draw(false);
    };

    public static TextFormatting getHealthColor(float health) {
        if (health <= 4)
            return TextFormatting.RED;
        else if (health <= 8)
            return TextFormatting.GOLD;
        else if (health <= 12)
            return TextFormatting.YELLOW;
        else if (health <= 16)
            return TextFormatting.DARK_GREEN;
        else
            return TextFormatting.GREEN;
    }

    private ITextComponent func_238524_a_(Entity entity, ITextComponent displayName) {
        return entity.isSpectator() ? displayName.deepCopy().mergeStyle(TextFormatting.ITALIC) : displayName;
    }

    public ITextComponent getDisplayName(NetworkPlayerInfo p_200262_1_)
    {
        return p_200262_1_.getDisplayName() != null ? this.func_238524_a_(p_200262_1_, p_200262_1_.getDisplayName().deepCopy()) : this.func_238524_a_(p_200262_1_, ScorePlayerTeam.func_237500_a_(p_200262_1_.getPlayerTeam(), new StringTextComponent(p_200262_1_.getGameProfile().getName())));
    }

    private ITextComponent func_238524_a_(NetworkPlayerInfo playerInfo, ITextComponent displayName) {
        // Implement your logic here to modify the display name if needed
        return displayName;
    }

    public void draw(boolean blur) {
        if (Minecraft.getInstance().world == null) return;
        Iterator<Entity> var1 = Minecraft.getInstance().world.getAllEntities().iterator();
        Font font = Fonts.ROBOTO_REGULAR.get(12);
        Font otherFont = Fonts.ROBOTO_REGULAR.get(12);
        while (true) {
            LivingEntity player;
            double[] vec;
            double[] marker = new double[0];

            do {
                Entity e;
                do {
                    do {
                        if (!var1.hasNext()) {
                            return;
                        }

                        e = (Entity) var1.next();
                    } while (!(e instanceof PlayerEntity || e == Aura.target));
                } while ((e == mc.player) );

                player = (LivingEntity) e;
                double x2 =  RenderUtils.interporate((double)mc.getRenderPartialTicks(), player.lastTickPosX, player.getPosX());
                double y2 = RenderUtils.interporate((double)mc.getRenderPartialTicks(), player.lastTickPosY, player.getPosY());
                double z2 = RenderUtils.interporate((double)mc.getRenderPartialTicks(), player.lastTickPosZ, player.getPosZ());

                vec = RenderUtil.worldToScreen(x2, y2 + player.getHeight() + 0.4f, z2);
                marker = RenderUtil.worldToScreen(x2, y2 + 1.5D, z2);
            } while (vec == null || marker == null);
            float dst = Math.min(mc.player.getDistance(player) / 45,0.5f);
            String health = "§a";
            if (player.getHealth() < 13.0F && player.getHealth() > 7.0F) {
                health = "§6";
            } else if (player.getHealth() < 7.0F) {
                health = "§c";
            }
            {
                NetworkPlayerInfo networkPlayerInfo = null;
                if (player instanceof PlayerEntity playerEntity) {
                    networkPlayerInfo = mc.player.connection.getPlayerInfo(playerEntity.getUniqueID());
                }


                String displayName = player.getDisplayName().getString();
                String[] allM = displayName.split(" ");
                String donate = "";
                String name = player.getScoreboardName();
                if (allM.length > 1) {
                    donate = allM[0];
                    name = allM[1];
                    if (donate.toLowerCase().contains("dragon")) {
                        donate = TextFormatting.LIGHT_PURPLE + donate;
                    }if (donate.toLowerCase().contains("imperator")) {
                        donate = TextFormatting.RED + donate;
                    }   if (donate.toLowerCase().contains("magister")) {
                        donate = TextFormatting.YELLOW + donate;
                    }   if (donate.toLowerCase().contains("overlord")) {
                        donate = TextFormatting.BLUE + donate;
                    }   if (donate.toLowerCase().contains("avenger")) {
                        donate = TextFormatting.GREEN + donate;
                    }   if (donate.toLowerCase().contains("titan")) {
                        donate = TextFormatting.YELLOW + donate;
                    }   if (donate.toLowerCase().contains("hero")) {
                        donate = TextFormatting.DARK_BLUE + donate;
                    }
                    if (donate.toLowerCase().contains("helper")) {
                        donate = TextFormatting.GOLD + donate;
                    } else if (donate.toLowerCase().contains("moder")) {
                        donate = TextFormatting.BLUE + donate;
                    } else if (donate.toLowerCase().contains("yt")) {
                        donate = TextFormatting.RED + donate;
                    } else if (donate.toLowerCase().contains("admin")) {
                        donate = TextFormatting.DARK_RED + donate;
                    } else if (donate.toLowerCase().contains("мл.сотрудник")) {
                        donate = TextFormatting.YELLOW + donate;
                    } else if (donate.toLowerCase().contains("сотрудник")) {
                        donate = TextFormatting.DARK_RED + donate;
                    }
                    donate += " ";
                }

                if (displayName == null) return;
                String title = donate + TextFormatting.WHITE + " " + name + " " + TextFormatting.GREEN + String.format("%.1f",player.getHealth());
                if (title == null) return;
                title = ((nova.friendManager.isFriend(player.getName().getString())) || player.getScoreboardName().equals("fireborne") ?
                        TextFormatting.WHITE + "[" + TextFormatting.GREEN + "F" + TextFormatting.WHITE + "]" : "")
                        + title;


               /* if (networkPlayerInfo != null) {
                    displayName = getDisplayName(networkPlayerInfo).getString();
                    title = displayName + " " + (int) player.getHealth();
                }*/
                String titleWithout = ((nova.friendManager.isFriend(name)) || player.getScoreboardName().equals("fireborne") ?
                        TextFormatting.WHITE + "[" + TextFormatting.GREEN + "F" + TextFormatting.WHITE + "]" : "") + " " + donate + name + " " + health;

                int ipb = 0;
                for (ITextComponent s : player.getDisplayName().getSiblings()) {
                    for (ITextComponent s1 : s.getSiblings()) {
                        //font.draw(s1.getString(), vec[0] + ipb,  vec[1], s1.getStyle().getColor() != null ? s1.getStyle().getColor().getColor() : -1);
                        ipb += font.getWidth(s1.getString());
                    }
                    ipb += font.getWidth("i");
                }

                int width = font.getWidth(titleWithout);


                GL11.glPushMatrix();

                GL11.glTranslatef((float) vec[0], (float) vec[1], 0.0F);

                GL11.glScaled(1, 1, 1);

                GL11.glTranslatef((float) (-vec[0]) - (float) (width / 2), (float) (-vec[1]), 0.0F);
                float val = 0.8f;
                if (blur) {
                    if (background)
                        RenderUtil.roundedRectangle(vec[0] - 2, vec[1] - 2, width + 4, font.getHeight() + 4, 5, Color.WHITE);
                } else {
                    float ip = 0;
                    try {
                        for (ITextComponent s : player.getDisplayName().getSiblings()) {
                            for (ITextComponent s1 : s.getSiblings()) {
                                font.draw(s1.getString(), vec[0] + ip,  vec[1], s1.getStyle().getColor() != null ? s1.getStyle().getColor().getColor() : -1);
                                ip += font.getWidth(s1.getString());
                            }
                            ip += font.getWidth("i");
                        }
                        font.draw(new MatrixStack(), title.split(" ").length > 1 ? title.split(" ")[1] + " " + title.split(" ")[2] : "",
                                (float) vec[0] + ip, (float) vec[1], -1);
                    }catch (Exception exception) {
                        font.draw(new MatrixStack(), "ПАМАГИТЕ", (float) vec[0], (float) vec[1], -1);
                    }
                    ScaleUtil.scaleNonMatrix((float) vec[0] + width / 2f, (float) (vec[1] - 2 + font.getHeight() / 2), Math.max(val, 0.1f));
                    float posY = (float) (vec[1] - 3);
                    float posX = (float) (vec[0] + width / 2);
                    float maxOffsetY = (font.getHeight() + 3);
                    final java.util.List<ItemStack> stacks = new java.util.ArrayList<>();

                    stacks.add(player.getHeldItemMainhand());

                    stacks.add(player.getHeldItemOffhand());

                    player.getArmorInventoryList().forEach(stacks::add);

                    stacks.removeIf(w -> w.getItem() instanceof AirItem);
                    int totalSize = stacks.size() * 10;

                    maxOffsetY += 12;
                    int iterable = 0;
                    for (ItemStack stack : stacks) {
                        if (stack != null) {
                            RenderHelper.enableStandardItemLighting();
                            drawItemStack(stack,  posX +  iterable * 20 - totalSize + 2,
                                    posY - maxOffsetY + 1, null, false);
                            RenderHelper.disableStandardItemLighting();
                            iterable++;
                            java.util.ArrayList<String> lines = new java.util.ArrayList<>();
                            buildEnchantments(lines, stack);

                            int i = 0;
                            for (String s : lines) {
                                otherFont.draw(s,
                                        posX +
                                        iterable * 20 - totalSize - 18,
                                        (float) posY - (maxOffsetY) - (7) - (i * 7),
                                        new Color(200, 200, 200, 255).getRGB());
                                i++;
                            }
                        }
                    }
                }
                ScaleUtil.scaleNonMatrix((float) vec[0] + width / 2f, (float) (vec[1] - 2 + font.getHeight() / 2), 1f / (Math.max(val, 0.1f)));
                GL11.glPopMatrix();
            }
        }
    }

    public void drawItemStack(ItemStack stack, double x, double y, String altText, boolean withoutOverlay) {

        GL11.glPushMatrix();
        GL11.glScalef(-0.01f * -200, -0.01f * -200, -0.01f * -200);
        GL11.glPushMatrix();
        GlStateManager.disableLighting();
        GL11.glPopMatrix();
        GlStateManager.scaled(0.5, 0.5, 0.5);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        mc.getItemRenderer().renderItemIntoGUI(stack, (int) x, (int) y);

        String count = "";
        if (stack.getCount() > 1)
        {
            count = stack.getCount() + "";
        }
        mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, stack, (int) x, (int) y, count);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }
    public static void buildEnchantments(java.util.ArrayList<String> list, ItemStack stack) {
        Item item = stack.getItem();
        int protection = EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, stack);
        int thorns = EnchantmentHelper.getEnchantmentLevel(Enchantments.THORNS, stack);
        int unbreaking = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack);
        int mending = EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, stack);
        int feather = EnchantmentHelper.getEnchantmentLevel(Enchantments.FEATHER_FALLING, stack);
        int depth = EnchantmentHelper.getEnchantmentLevel(Enchantments.DEPTH_STRIDER, stack);
        int vanishing_curse = EnchantmentHelper.getEnchantmentLevel(Enchantments.VANISHING_CURSE, stack);
        int binding_curse = EnchantmentHelper.getEnchantmentLevel(Enchantments.BINDING_CURSE, stack);
        int sweeping = EnchantmentHelper.getEnchantmentLevel(Enchantments.SWEEPING, stack);
        int sharpness = EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, stack);
        int looting = EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, stack);
        int infinity = EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack);
        int power = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);
        int punch = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);
        int flame = EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack);
        int knockback = EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, stack);
        int fireAspect = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, stack);
        int efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack);
        int silktouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack);
        int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
        int fireprot = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_PROTECTION, stack);
        int blastprot = EnchantmentHelper.getEnchantmentLevel(Enchantments.BLAST_PROTECTION, stack);

        if (item instanceof AxeItem) {
            if (sharpness > 0) {
                list.add("Ос" + sharpness);
            }
            if (efficiency > 0) {
                list.add("Эф" + efficiency);
            }
            if (unbreaking > 0) {
                list.add("Пр" + unbreaking);
            }
        }
        if (item instanceof ArmorItem) {
            if (vanishing_curse > 0) {
                list.add("§cут ");
            }
            if (binding_curse > 0) {
                list.add("§cне ");
            }
            if (fireprot > 0) {
                list.add("Ог" + fireprot);
            }
            if (blastprot > 0) {
                list.add("Cп" + blastprot);
            }
            if (depth > 0) {
                list.add("Гл" + depth);
            }
            if (feather > 0) {
                list.add("Пе" + feather);
            }
            if (protection > 0) {
                list.add("За" + protection);
            }
            if (thorns > 0) {
                list.add("Ши" + thorns);
            }
            if (mending > 0) {
                list.add("По");
            }
            if (unbreaking > 0) {
                list.add("Пр" + unbreaking);
            }
        }
        if (item instanceof BowItem) {
            if (vanishing_curse > 0) {
                list.add("§cут");
            }
            if (binding_curse > 0) {
                list.add("§cне ");
            }
            if (infinity > 0) {
                list.add("Бе" + infinity);
            }
            if (power > 0) {
                list.add("Си" + power);
            }
            if (punch > 0) {
                list.add("От" + punch);
            }
            if (mending > 0) {
                list.add("По ");
            }
            if (flame > 0) {
                list.add("По" + flame);
            }
            if (unbreaking > 0) {
                list.add("Пр" + unbreaking);
            }
        }
        if (item instanceof SwordItem) {
            if (vanishing_curse > 0) {
                list.add("§cут ");
            }
            if (looting > 0) {
                list.add("До" + looting);
            }
            if (binding_curse > 0) {
                list.add("§cНе");
            }
            if (sweeping > 0) {
                list.add("По" + sweeping);
            }
            if (sharpness > 0) {
                list.add("Ос" + sharpness);
            }
            if (knockback > 0) {
                list.add("От" + knockback);
            }
            if (fireAspect > 0) {
                list.add("За" + fireAspect);
            }
            if (unbreaking > 0) {
                list.add("Пр" + unbreaking);
            }
            if (mending > 0) {
                list.add("Men ");
            }
        }
        if (item instanceof ToolItem) {
            if (unbreaking > 0) {
                list.add("Пр" + unbreaking);
            }
            if (mending > 0) {
                list.add("По ");
            }
            if (vanishing_curse > 0) {
                list.add("Ва ");
            }
            if (binding_curse > 0) {
                list.add("Би ");
            }
            if (efficiency > 0) {
                list.add("Эф" + efficiency);
            }
            if (silktouch > 0) {
                list.add("Ше" + silktouch);
            }
            if (fortune > 0) {
                list.add("Уд" + fortune);
            }
        }
    }

    public static void scale() {
        float n = (Minecraft.getInstance().gameSettings.smoothCamera ? 2.0f : 1F);
        RenderSystem.scaled(n, n, n);
    }

}
