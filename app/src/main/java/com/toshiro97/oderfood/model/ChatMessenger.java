package com.toshiro97.oderfood.model;

public class ChatMessenger {
    private String messenger;
    private boolean isStaff;

    public ChatMessenger() {
    }

    public ChatMessenger(String messenger) {
        this.messenger = messenger;
        this.isStaff = false;
    }

    public String getMessenger() {
        return messenger;
    }

    public void setMessenger(String messenger) {
        this.messenger = messenger;
    }

    public boolean isStaff() {
        return isStaff;
    }

    public void setStaff(boolean staff) {
        isStaff = staff;
    }
}
