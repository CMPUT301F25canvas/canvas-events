package com.example.lotteryeventsystem;

public class Entrant {
    private String name;
    private String id;

    public Entrant(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
