package dev.fesly.impl.event.impl.input;
import dev.fesly.impl.event.CancellableEvent;
public final class ChatInputEvent extends CancellableEvent {
    private String message;
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof ChatInputEvent)) return false;
        final ChatInputEvent other = (ChatInputEvent) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (!super.equals(o)) return false;
        final java.lang.Object this$message = this.getMessage();
        final java.lang.Object other$message = other.getMessage();
        if (this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
        return true;
    }
    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof ChatInputEvent;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final java.lang.Object $message = this.getMessage();
        result = result * PRIME + ($message == null ? 43 : $message.hashCode());
        return result;
    }
    @java.lang.SuppressWarnings("all")
    public String getMessage() {
        return this.message;
    }
    @java.lang.SuppressWarnings("all")
    public void setMessage(final String message) {
        this.message = message;
    }
    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "ChatInputEvent(message=" + this.getMessage() + ")";
    }
    @java.lang.SuppressWarnings("all")
    public ChatInputEvent(final String message) {
        this.message = message;
    }
}
