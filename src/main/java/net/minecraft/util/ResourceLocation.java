package net.minecraft.util;

import net.minecraft.resources.Identifier;

public class ResourceLocation {

    private final Identifier identifier;

    public ResourceLocation(String location) {
        if (location.contains(":")) {
            String[] split = location.split(":", 2);
            this.identifier = Identifier.fromNamespaceAndPath(split[0].toLowerCase(java.util.Locale.ROOT), split[1].toLowerCase(java.util.Locale.ROOT));
        } else {
            this.identifier = Identifier.fromNamespaceAndPath("minecraft", location.toLowerCase(java.util.Locale.ROOT));
        }
    }

    public ResourceLocation(String namespace, String path) {
        this.identifier = Identifier.fromNamespaceAndPath(namespace.toLowerCase(java.util.Locale.ROOT), path.toLowerCase(java.util.Locale.ROOT));
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public Identifier toIdentifier() {
        return this.identifier;
    }

    public String getPath() {
        return this.identifier.getPath();
    }

    public String getNamespace() {
        return this.identifier.getNamespace();
    }

    @Override
    public String toString() {
        return this.identifier.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof ResourceLocation rl) return identifier.equals(rl.identifier);
        if (o instanceof Identifier id) return identifier.equals(id);
        return false;
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }
}
