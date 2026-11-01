package dev.fesly.impl.event.impl.other;
import dev.fesly.impl.event.Event;
public final class ServerJoinEvent implements Event {
    public String ip;
    public int port;
    @java.lang.SuppressWarnings("all")
    public String getIp() {
        return this.ip;
    }
    @java.lang.SuppressWarnings("all")
    public int getPort() {
        return this.port;
    }
    @java.lang.SuppressWarnings("all")
    public ServerJoinEvent(final String ip, final int port) {
        this.ip = ip;
        this.port = port;
    }
}
