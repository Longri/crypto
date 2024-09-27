package de.longri.utils;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static de.longri.utils.UTIL.SERIAL_NUMBER;


public class NamedEncryptedStringProperty implements NamedProperty {

    final String NAME;
    String encryptedValue;
    String comment;

    public NamedEncryptedStringProperty(String name) {
        NAME = name;
    }

    public NamedEncryptedStringProperty(String name, String comment) {
        NAME = name;
        this.comment = comment.trim();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public NamedProperty setValue(String newValue) throws GeneralSecurityException, IOException {
        encryptedValue = Crypto.encrypt(newValue.trim(), SERIAL_NUMBER);
        return this;
    }

    @Override
    public String getValue() throws GeneralSecurityException, IOException {
        return Crypto.decrypt(encryptedValue, SERIAL_NUMBER);
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
    public boolean equals(Object obj) {
        if (obj instanceof NamedEncryptedStringProperty other) {
            return other.NAME.equals(NAME);
        }
        return false;
    }

}
