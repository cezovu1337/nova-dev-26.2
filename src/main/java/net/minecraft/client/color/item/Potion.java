package net.minecraft.client.color.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.optifine.Config;
import net.optifine.CustomColors;

public record Potion(int defaultColor) implements ItemTintSource {
    public static final MapCodec<Potion> MAP_CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(Potion::defaultColor)).apply(i, Potion::new)
    );

    public Potion() {
        this(-13083194);
    }

    @Override
    public int calculate(ItemStack itemStack, ClientLevel level, LivingEntity owner) {
        PotionContents contents = itemStack.get(DataComponents.POTION_CONTENTS);
        int customDefaultColor = Config.isCustomColors() ? CustomColors.getPotionColor(0, this.defaultColor) : this.defaultColor;
        return contents != null ? ARGB.opaque(contents.getColorOr(customDefaultColor)) : ARGB.opaque(customDefaultColor);
    }

    @Override
    public MapCodec<Potion> type() {
        return MAP_CODEC;
    }
}
