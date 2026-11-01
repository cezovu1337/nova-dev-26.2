package net.minecraft.util;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.User;

public class Session extends User {
    public Session(String username, String uuid, String token, String type) {
        super(username, UUID.randomUUID(), token, Optional.empty(), Optional.empty());
    }
}
