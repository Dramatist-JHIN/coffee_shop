package com.coffee.common;

public enum OrderStatus {
    PENDING("待处理"),
    MAKING("制作中"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
