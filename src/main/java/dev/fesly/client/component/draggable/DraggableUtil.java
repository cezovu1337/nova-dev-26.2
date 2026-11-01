package dev.fesly.client.component.draggable;
import dev.fesly.Nova;
import dev.fesly.client.component.draggable.impl.*;
import dev.fesly.impl.client.clientui.ClientUI;
import dev.fesly.impl.interfaces.client.IAccess;
public final class DraggableUtil implements IAccess {
    public static boolean isValid(Draggable draggable) {
        ClientUI clientUI = nova.getClientUI();
        if (ClientUI.getHudElements().contains(ClientUI.HudElementsEnum.ArrayList) && draggable instanceof FeatureList) return true;
        if (ClientUI.getHudElements().contains(ClientUI.HudElementsEnum.WaterMark) && draggable instanceof WaterMark) return true;
        if (draggable instanceof KeyBinds && ClientUI.getHudElements().contains(ClientUI.HudElementsEnum.KeyBinds)) return true;
        if (draggable instanceof PotionHud && ClientUI.getHudElements().contains(ClientUI.HudElementsEnum.PotionHud)) return true;
        if (draggable instanceof StaffList && ClientUI.getHudElements().contains(ClientUI.HudElementsEnum.StaffList)) return true;
        if (draggable instanceof TargetHud) return true;
        return false;
    }
    @java.lang.SuppressWarnings("all")
    private DraggableUtil() {
        throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
