package de.longri.utils;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SecretTest {

    static class SecretInstance extends Secret {

        private static final String SECRET1 = "Secret1";

        /**
         * put values with description on secureList.
         * <p>
         * this.put("Secret1", "Secret for Access")
         * this.put("Name", "Comment")
         */
        @Override
        public void init() {
            this.put("Secret1", "Secret for Access");
        }

        @Override
        Secret getNewInstance(String Name) {
            return null;
        }
    }


    @Test
    public void testSecretInstance() throws GeneralSecurityException, IOException {

    }
}
