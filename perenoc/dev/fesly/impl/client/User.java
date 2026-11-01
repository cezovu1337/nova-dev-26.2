package dev.fesly.impl.client;


public record User(String username, String uid, String rank) {
    public String getAvatar() {
        return "https://i.imgur.com/Jka3oEi.png";
    }
}