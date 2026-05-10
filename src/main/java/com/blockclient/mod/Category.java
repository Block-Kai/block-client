package com.blockclient.mod;

/**
 * 模块分类
 */
public enum Category {
    Combat("战斗"),
    Misc("杂项"),
    Render("渲染"),
    Movement("移动"),
    Player("玩家"),
    Exploit("漏洞"),
    Client("客户端");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
