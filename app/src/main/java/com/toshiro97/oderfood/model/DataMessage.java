package com.toshiro97.oderfood.model;

import java.util.Map;

/**
 * Created by Nicolas on 13/03/2018.
 */

public class DataMessage {

    public String to;
    public Map<String,String> data;

    public DataMessage() {
    }

    public DataMessage(String to, Map<String, String> data) {
        this.to = to;
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
