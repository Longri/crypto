package de.longri.utils;

public class NamedStringProperty implements NamedProperty {

    private final String NAME;
    private String value;
    private String comment;

    public NamedStringProperty(String name) {
        NAME = name;
    }

    public NamedStringProperty(String name, String comment) {
        NAME = name;
        this.comment = comment.trim();
    }

    @Override
    public NamedProperty setValue(String newValue) {
        value = newValue.trim();
        return this;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public NamedProperty setComment(String newComment) {
        comment = newComment.trim();
        return this;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NamedStringProperty other) {
            return other.NAME.equals(NAME);
        }
        return false;
    }

}
