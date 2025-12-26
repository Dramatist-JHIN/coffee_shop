package com.coffee.common;

public enum Role {
    MANAGER("主理人"),
    BARISTA("咖啡师"),
    CUSTOMER("顾客");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
