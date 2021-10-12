package com.rncamerakit.editor.manager;

public class ColorFilter {

    private String name;
    private String icon;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public ColorFilter(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }
}
