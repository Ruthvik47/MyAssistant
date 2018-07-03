package com.abcexample.myassistant;

/**
 * Created by divya on 12-02-2018.
 */

public class Chats {
    private boolean seen;
    private String timestamp;

    public Chats() {
    }

    public Chats(boolean seen, String timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
