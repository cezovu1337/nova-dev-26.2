package net.minecraft.server.packs.resources;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;

@FunctionalInterface
public interface ResourceProvider {
    ResourceProvider EMPTY = location -> Optional.empty();

    Optional<Resource> getResource(Identifier location);

    default Resource getResource(net.minecraft.util.ResourceLocation location) throws IOException {
        return this.getResourceOrThrow(location.toIdentifier());
    }

    default Resource getResourceOrThrow(final Identifier location) throws FileNotFoundException {
        Optional<Resource> res = this.getResource(location);
        if (res.isPresent()) {
            return res.get();
        }
        InputStream stream = ResourceProvider.class.getResourceAsStream("/assets/" + location.getNamespace() + "/" + location.getPath());
        if (stream != null) {
            return new Resource(null, () -> stream);
        }
        throw new FileNotFoundException(location.toString());
    }

    default InputStream open(final Identifier location) throws IOException {
        return this.getResourceOrThrow(location).open();
    }

    default BufferedReader openAsReader(final Identifier location) throws IOException {
        return this.getResourceOrThrow(location).openAsReader();
    }

    static ResourceProvider fromMap(final Map<Identifier, Resource> map) {
        return location -> Optional.ofNullable(map.get(location));
    }
}