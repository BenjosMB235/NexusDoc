package com.example.nexusdoc.ui.archives.model;

import com.google.gson.annotations.SerializedName;

public class DocumentTag {
    @SerializedName("tag_id")
    private Tag tag;

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }
}
