package de.longri.utils;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SecretTest {
    private SecretInstance secretInstance;
    private static final String dummyName = "SECRETdummyName";
    private static final File dummyFile = new File("./testFiles/" + dummyName + ".ini");
    private static final String dummyValue = "TEST_PASSWORD";
    private static final String dummyPassword = "TEST_VALUE";


    static class SecretInstance extends Secret {

        public static String Config = "<CONFIG>";
        public static String Password = "<PASSWORD>";

        public SecretInstance(File dummyName) throws GeneralSecurityException, IOException {
            super(dummyName);
        }

        public SecretInstance(String name) throws GeneralSecurityException, IOException {
            super(name);
        }

        /**
         * put values with description on secureList.
         * <p>
         * this.put("Secret1", "Secret for Access")
         * this.put("Name", "Comment")
         */
        @Override
        public void init() throws GeneralSecurityException, IOException {
            this.initial(new NamedEncryptedStringProperty(Password, "Secret for Access").setValue(dummyPassword));
            this.initial(new NamedStringProperty(Config, "Config value for Test").setValue(dummyValue));
        }

        @Override
        Secret getNewInstance(String Name) throws GeneralSecurityException, IOException {
            return new SecretInstance(Name);
        }
    }


    @BeforeEach
    public void setUp() throws GeneralSecurityException, IOException {
        if (dummyFile.exists())
            assertTrue(dummyFile.delete());
        secretInstance = new SecretInstance(dummyFile);
    }

    @Test
    public void setAndGetSecretTest() throws GeneralSecurityException, IOException {
        secretInstance.set(SecretInstance.Password, dummyValue);
        assertEquals(dummyValue, secretInstance.get(SecretInstance.Password));
    }

    @Test
    public void saveAndLoadSecretTest() throws GeneralSecurityException, IOException, ConfigurationException {
        secretInstance.set(SecretInstance.Password, dummyPassword);
        secretInstance.save();
        secretInstance.load();
        assertEquals(dummyPassword, secretInstance.get(SecretInstance.Password));
        assertEquals(dummyValue, secretInstance.get(SecretInstance.Config));

        Secret secretCopy = secretInstance.copy();
        assertEquals(dummyPassword, secretCopy.get(SecretInstance.Password));
        assertEquals(dummyValue, secretCopy.get(SecretInstance.Config));
    }
}
