package de.longri.utils;

public class NamedStringProperty {

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

    public void setValue(String newValue) {
        value = newValue.trim();
    }

    public String getValue() {
        return value;
    }

    public void setComment(String newComment) {
        comment = newComment.trim();
    }

    public String getComment() {
        return comment;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NamedStringProperty other) {
            return other.NAME.equals(NAME);
        }
        return false;
    }

}
