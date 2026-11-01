package net.minecraft.scoreboard;

import java.util.Collection;
import java.util.Collections;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ScorePlayerTeam {
    private final String name;

    public ScorePlayerTeam(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public ITextComponent getDisplayName() {
        return new StringTextComponent(this.name);
    }

    public ITextComponent getPrefix() {
        return new StringTextComponent("");
    }

    public String getColor() {
        return "";
    }

    public Collection<String> getMembershipCollection() {
        return Collections.emptyList();
    }

    public static ITextComponent func_237500_a_(ScorePlayerTeam team, ITextComponent name) {
        return name;
    }
}
