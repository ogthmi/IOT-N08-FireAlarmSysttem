package com.example.IoT.config;

import java.security.Principal;

public class StompUser implements Principal {
    private final String name;

    public StompUser(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
