package com.c12e.cortex.examples.profilesDaemon.requests;

import java.util.Map;

public class Payload {
    private Map<String, String> payload;

    public Map<String, String> getPayload() {
        if(payload == null){
            return Map.of();
        } else {
            return payload;
        }
    }

    public void setPayload(Map<String, String> payload) {
        this.payload = payload;
    }
}

