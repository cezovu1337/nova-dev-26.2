package dev.fesly.impl.client;

import dev.fesly.Nova;
import dev.fesly.impl.util.shader.ShaderUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientInfo {
    private final String name, namespace, version, build;
    private final Release release;

    public String getTitle() {
        return getName() + " " + getVersion() + " - " + getRelease().getName() + " / " + Nova.getInstance().getUser().username();
    }

}