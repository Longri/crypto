package de.longri.utils;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.*;

public class OldSecretTest {

    static final File TEST_INI_FILE = new File("./testFiles/test.ini");
    static final File TEMPLATE_INI_FILE = new File("./testFiles/TEST_TEMPLATE.ini");


    @BeforeAll
    static void init() throws IOException {
        // delete ini file from last test
        if (TEST_INI_FILE.exists()) {
            assertTrue(TEST_INI_FILE.delete());
        }

        //copy template File
        assertTrue(TEMPLATE_INI_FILE.exists());
        UTILS.copyFile(TEMPLATE_INI_FILE, TEST_INI_FILE);
        assertTrue(TEST_INI_FILE.exists());

    }

    @Test
    void testConvertIniFile() throws GeneralSecurityException, IOException, ConfigurationException {
        TestConvertSecret SECRET = new TestConvertSecret(TEST_INI_FILE,false);

        assertTrue(SECRET.exist());

        SECRET.load();
        assertEquals("10.3.1.17", SECRET.get(TestConvertSecret.FireDB_Host));
        assertEquals("56300", SECRET.get(TestConvertSecret.FireDB_Port));
        assertEquals("vario8", SECRET.get(TestConvertSecret.FireDB_Database));
        assertEquals("ODBC", SECRET.get(TestConvertSecret.FireDB_User));
        assertEquals("10.3.1.128", SECRET.get(TestConvertSecret.MariaDB_Host_Node1));
        assertEquals("3306", SECRET.get(TestConvertSecret.MariaDB_Port_Node1));
        assertEquals("", SECRET.get(TestConvertSecret.MariaDB_Host_Node2));
        assertEquals("", SECRET.get(TestConvertSecret.MariaDB_Port_Node2));
        assertEquals("AuthorisationDatabase", SECRET.get(TestConvertSecret.MariaDB_Database));
        assertEquals("botiss-admin", SECRET.get(TestConvertSecret.MariaDB_User));
        assertEquals("389", SECRET.get(TestConvertSecret.LDAP_Port));
        assertEquals("LDAP", SECRET.get(TestConvertSecret.LDAP_ADMIN_DN));
        assertEquals("OU=SBSUsers, OU=Users,OU=MyBusiness,DC=botiss,DC=local", SECRET.get(TestConvertSecret.LDAP_BaseDN));

        SECRET.save();

    }
}
