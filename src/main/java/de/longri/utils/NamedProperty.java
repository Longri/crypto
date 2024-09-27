package de.longri.utils;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface NamedProperty {
    NamedProperty setValue(String newValue) throws GeneralSecurityException, IOException;

    String getValue() throws GeneralSecurityException, IOException;

    NamedProperty setComment(String newComment);

    String getComment();

    String getName();
}
