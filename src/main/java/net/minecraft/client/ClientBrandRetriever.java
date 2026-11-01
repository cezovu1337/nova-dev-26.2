package net.minecraft.client;

import net.optifine.reflect.Reflector;

public class ClientBrandRetriever {
    public static final String VANILLA_NAME = "vanilla";

    public static String getClientModName() {
        return Reflector.BrandingControl_getBranding.exists() ? Reflector.BrandingControl_getBranding.callString() : "optifine";
    }
}
