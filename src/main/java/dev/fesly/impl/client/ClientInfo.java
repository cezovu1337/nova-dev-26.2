package dev.fesly.impl.client;
import dev.fesly.Nova;
import dev.fesly.impl.util.shader.ShaderUtil;
public class ClientInfo {
    private final String name;
    private final String namespace;
    private final String version;
    private final String build;
    private final Release release;
    public String getTitle() {
        return getName() + " " + getVersion() + " - " + getRelease().getName() + " / " + Nova.getInstance().getUser().username();
    }
    @java.lang.SuppressWarnings("all")
    public String getName() {
        return this.name;
    }
    @java.lang.SuppressWarnings("all")
    public String getNamespace() {
        return this.namespace;
    }
    @java.lang.SuppressWarnings("all")
    public String getVersion() {
        return this.version;
    }
    @java.lang.SuppressWarnings("all")
    public String getBuild() {
        return this.build;
    }
    @java.lang.SuppressWarnings("all")
    public Release getRelease() {
        return this.release;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof ClientInfo)) return false;
        final ClientInfo other = (ClientInfo) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        final java.lang.Object this$name = this.getName();
        final java.lang.Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final java.lang.Object this$namespace = this.getNamespace();
        final java.lang.Object other$namespace = other.getNamespace();
        if (this$namespace == null ? other$namespace != null : !this$namespace.equals(other$namespace)) return false;
        final java.lang.Object this$version = this.getVersion();
        final java.lang.Object other$version = other.getVersion();
        if (this$version == null ? other$version != null : !this$version.equals(other$version)) return false;
        final java.lang.Object this$build = this.getBuild();
        final java.lang.Object other$build = other.getBuild();
        if (this$build == null ? other$build != null : !this$build.equals(other$build)) return false;
        final java.lang.Object this$release = this.getRelease();
        final java.lang.Object other$release = other.getRelease();
        if (this$release == null ? other$release != null : !this$release.equals(other$release)) return false;
        return true;
    }
    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof ClientInfo;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final java.lang.Object $namespace = this.getNamespace();
        result = result * PRIME + ($namespace == null ? 43 : $namespace.hashCode());
        final java.lang.Object $version = this.getVersion();
        result = result * PRIME + ($version == null ? 43 : $version.hashCode());
        final java.lang.Object $build = this.getBuild();
        result = result * PRIME + ($build == null ? 43 : $build.hashCode());
        final java.lang.Object $release = this.getRelease();
        result = result * PRIME + ($release == null ? 43 : $release.hashCode());
        return result;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "ClientInfo(name=" + this.getName() + ", namespace=" + this.getNamespace() + ", version=" + this.getVersion() + ", build=" + this.getBuild() + ", release=" + this.getRelease() + ")";
    }
    @java.lang.SuppressWarnings("all")
    public ClientInfo(final String name, final String namespace, final String version, final String build, final Release release) {
        this.name = name;
        this.namespace = namespace;
        this.version = version;
        this.build = build;
        this.release = release;
    }
}
