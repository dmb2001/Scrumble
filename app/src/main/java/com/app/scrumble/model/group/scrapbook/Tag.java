package com.app.scrumble.model.group.scrapbook;

public class Tag {

    private final String name;
    private boolean hidden;

    public Tag(String name) {
        this.name = name;
        hidden = false;
    }

    public Tag(String name, boolean hidden) {
        this(name);
        this.hidden = hidden;
    }

    public String getName() {
        return name;
    }
}
