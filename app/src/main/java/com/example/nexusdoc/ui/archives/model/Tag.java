package com.example.nexusdoc.ui.archives.model;

import com.google.gson.annotations.SerializedName;

public class Tag {
    @SerializedName("name")
    private String name;

    @SerializedName("color")
    private String color;

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color != null ? color : "";
    }

    public void setColor(String color) {
        this.color = color;
    }
}
