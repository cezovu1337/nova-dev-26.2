package dev.fesly.client.friend;

import dev.fesly.Nova;

import java.util.ArrayList;

public class FriendManager extends ArrayList<Friend> {

    public void init() {
        Nova.getInstance().getEventBus().register(this);
    }

    public void addFriend(String name) {
        this.add(new Friend(name));
    }

    public Friend getFriend(String friend) {
        return this.stream().filter(isFriend -> isFriend.getName().equals(friend)).findFirst().orElse(null);
    }

    public boolean isFriend(String friend) {
        return this.stream().anyMatch(isFriend -> isFriend.getName().equals(friend));
    }

    public void removeFriend(String name) {
        this.removeIf(friend -> friend.getName().equalsIgnoreCase(name));
    }

    public void clearFriend() {
        this.clear();
    }

}
