package net.minecraft.server.packs;

import com.mojang.logging.LogUtils;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.FileUtil;
import net.optifine.reflect.ReflectorForge;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class VanillaPackResources implements PackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackLocationInfo location;
    private final ResourceMetadata builtInMetadata;
    private ResourceMetadata resourceMetadata;
    private final Set<String> namespaces;
    private final List<Path> rootPaths;
    private final Map<PackType, List<Path>> pathsForType;

    VanillaPackResources(
        PackLocationInfo location, ResourceMetadata metadata, Set<String> namespaces, List<Path> rootPaths, Map<PackType, List<Path>> pathsForType
    ) {
        this.location = location;
        this.builtInMetadata = metadata;
        this.namespaces = namespaces;
        this.rootPaths = rootPaths;
        this.pathsForType = pathsForType;
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... path) {
        FileUtil.validatePath(path);
        List<String> pathList = List.of(path);

        for (Path rootPath : this.rootPaths) {
            Path pathInRoot = FileUtil.resolvePath(rootPath, pathList);
            if (Files.exists(pathInRoot) && PathPackResources.validatePath(pathInRoot)) {
                return IoSupplier.create(pathInRoot);
            }
        }

        return null;
    }

    public void listRawPaths(PackType type, Identifier resource, Consumer<Path> output) {
        FileUtil.decomposePath(resource.getPath()).ifSuccess(decomposedPath -> {
            String namespace = resource.getNamespace();

            for (Path typePath : this.pathsForType.get(type)) {
                Path namespacedPath = typePath.resolve(namespace);
                output.accept(FileUtil.resolvePath(namespacedPath, (List<String>)decomposedPath));
            }
        }).ifError(error -> LOGGER.error("Invalid path {}: {}", resource, error.message()));
    }

    @Override
    public void listResources(PackType type, String namespace, String directory, PackResources.ResourceOutput output) {
        PackResources.ResourceOutput outputOF = (locIn, suppIn) -> {
            if (locIn.getPath().startsWith("models/block/template_glass_pane")) {
                IoSupplier<InputStream> suppOF = this.getResourceOF(type, locIn);
                if (suppOF != null) {
                    suppIn = suppOF;
                }
            }

            output.accept(locIn, suppIn);
        };
        FileUtil.decomposePath(directory).ifSuccess(decomposedPath -> {
            List<Path> paths = this.pathsForType.get(type);
            int pathsSize = paths.size();
            if (pathsSize == 1) {
                getResources(outputOF, namespace, paths.get(0), (List<String>)decomposedPath);
            } else if (pathsSize > 1) {
                Map<Identifier, IoSupplier<InputStream>> resources = new HashMap<>();

                for (int i = 0; i < pathsSize - 1; i++) {
                    getResources(resources::putIfAbsent, namespace, paths.get(i), (List<String>)decomposedPath);
                }

                Path lastPath = paths.get(pathsSize - 1);
                if (resources.isEmpty()) {
                    getResources(outputOF, namespace, lastPath, (List<String>)decomposedPath);
                } else {
                    getResources(resources::putIfAbsent, namespace, lastPath, (List<String>)decomposedPath);
                    resources.forEach(outputOF);
                }
            }
        }).ifError(error -> LOGGER.error("Invalid path {}: {}", directory, error.message()));
    }

    private static void getResources(PackResources.ResourceOutput result, String namespace, Path root, List<String> directory) {
        Path namespaceDir = root.resolve(namespace);
        PathPackResources.listPath(namespace, namespaceDir, directory, result);
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, Identifier location) {
        IoSupplier<InputStream> res = this.getResourcesImpl(type, location);
        if (res != null) {
            return res;
        }
        IoSupplier<InputStream> resOF = this.getResourceOF(type, location);
        if (resOF != null) {
            return resOF;
        }
        String cpPath = "/assets/" + location.getNamespace() + "/" + location.getPath();
        if (VanillaPackResources.class.getResource(cpPath) != null) {
            return () -> VanillaPackResources.class.getResourceAsStream(cpPath);
        }
        return null;
    }

    public @Nullable IoSupplier<InputStream> getResourcesImpl(PackType type, Identifier location) {
        return FileUtil.decomposePath(location.getPath()).mapOrElse(decomposedPath -> {
            String namespace = location.getNamespace();

            for (Path typePath : this.pathsForType.get(type)) {
                Path resource = FileUtil.resolvePath(typePath.resolve(namespace), (List<String>)decomposedPath);
                if (Files.exists(resource) && PathPackResources.validatePath(resource)) {
                    return IoSupplier.create(resource);
                }
            }

            return null;
        }, error -> {
            LOGGER.error("Invalid path {}: {}", location, error.message());
            return null;
        });
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        Set<String> set = new java.util.HashSet<>(this.namespaces);
        set.add("nova");
        return set;
    }

    @Override
    public <T> T getMetadataSection(MetadataSectionType<T> metadataSerializer) {
        try {
            if (this.resourceMetadata == null) {
                this.resourceMetadata = AbstractPackResources.loadMetadata(this);
            }

            Optional<T> section = this.resourceMetadata.getSection(metadataSerializer);
            if (section.isPresent()) {
                return section.get();
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to parse vanilla pack metadata", e);
        }

        return this.builtInMetadata.getSection(metadataSerializer).orElse(null);
    }

    @Override
    public PackLocationInfo location() {
        return this.location;
    }

    @Override
    public void close() {
    }

    public ResourceProvider asProvider() {
        return location -> Optional.ofNullable(this.getResource(PackType.CLIENT_RESOURCES, location)).map(s -> new Resource(this, (IoSupplier<InputStream>)s));
    }

    public IoSupplier<InputStream> getResourceOF(PackType type, Identifier locationIn) {
        String pathStr = "/" + type.getDirectory() + "/" + locationIn.getNamespace() + "/" + locationIn.getPath();
        InputStream is = ReflectorForge.getOptiFineResourceStream(pathStr);
        if (is != null) {
            return () -> is;
        }

        URL url = VanillaPackResources.class.getResource(pathStr);
        return url != null ? () -> url.openStream() : null;
    }
}
