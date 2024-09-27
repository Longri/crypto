package de.longri.utils;


import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class TestConvertSecret extends Secret {

    public final static String FireDB_Host = "FireDB_Host";
    public final static String FireDB_Port = "FireDB_Port";
    public final static String FireDB_Database = "FireDB_Database";
    public final static String FireDB_User = "FireDB_User";
    public final static String FireDB_Password = "FireDB_Password";

    // MariaDb Cluster
    public final static String MariaDB_Host_Node1 = "MariaDB_Host_Node1";
    public final static String MariaDB_Port_Node1 = "MariaDB_Port_Node1";
    public final static String MariaDB_Host_Node2 = "MariaDB_Host_Node2";
    public final static String MariaDB_Port_Node2 = "MariaDB_Port_Node2";
    public final static String MariaDB_Database = "MariaDB_Database";
    public final static String MariaDB_User = "MariaDB_User";
    public final static String MariaDB_Password = "MariaDB_Password";

    //LDAP
    public final static String LDAP_Host = "LDAP_Host";
    public final static String LDAP_Port = "LDAP_Port";
    public final static String LDAP_ADMIN_DN = "LDAP_ADMIN_DN";
    public final static String LDAP_ADMIN_PW = "LDAP_ADMIN_PW";
    public final static String LDAP_BaseDN = "LDAP_BaseDN";

    private boolean isLoaded = false;
    private boolean isInit = false;


    public TestConvertSecret(File testIniFile, boolean encryptOld) throws GeneralSecurityException, IOException {
        super(testIniFile, encryptOld);
    }

    public TestConvertSecret(String name) throws GeneralSecurityException, IOException {
        super(getName(name));
        init();
    }

    private static String getName(String name) {
        // remove '.ini'
        if (name.endsWith(".ini")) name = name.substring(0, name.length() - 4);
        return name;
    }

    public TestConvertSecret(File file) throws GeneralSecurityException, IOException {
        super(file);
        init();
    }

    @Override
    public void init() {
        if (isInit) return;
        this.isInit = true;

        this.initial(new NamedStringProperty(MariaDB_User, "MariaDB User"));
        this.initial(new NamedEncryptedStringProperty(MariaDB_Password, "MariaDB Password"));
        this.initial(new NamedStringProperty(MariaDB_Database, "MariaDB Database Name"));
        this.initial(new NamedStringProperty(MariaDB_Host_Node1, "MariaDB Host Node1"));
        this.initial(new NamedStringProperty(MariaDB_Port_Node1, "MariaDB Port Node1"));
        this.initial(new NamedStringProperty(MariaDB_Host_Node2, "MariaDB Host Node2"));
        this.initial(new NamedStringProperty(MariaDB_Port_Node2, "MariaDB Port Node2"));

        this.initial(new NamedStringProperty(LDAP_Host, "LDAP host"));
        this.initial(new NamedStringProperty(LDAP_Port, "LDAP port"));
        this.initial(new NamedStringProperty(LDAP_ADMIN_DN, "LDAP Admin DN"));
        this.initial(new NamedEncryptedStringProperty(LDAP_ADMIN_PW, "LDAP Admin PW"));
        this.initial(new NamedStringProperty(LDAP_BaseDN, "LDAP BaseDN"));

        this.initial(new NamedStringProperty(FireDB_Host, "FireDB Host"));
        this.initial(new NamedStringProperty(FireDB_Port, "FireDB Port"));
        this.initial(new NamedStringProperty(FireDB_Database, "FireDB Database Name"));
        this.initial(new NamedStringProperty(FireDB_User, "FireDB User"));
        this.initial(new NamedEncryptedStringProperty(FireDB_Password, "FireDB Password"));
    }

    @Override
    public void load() throws IOException, GeneralSecurityException, ConfigurationException {

        if (!this.getSecretFile().exists()) {
            //create empty file
            this.getSecretFile().createNewFile();
        }

        super.load();
        isLoaded = true;
    }

    @Override
    public String toString() {
        return "MarketAuthSECRET{" +
                "isLoaded=" + isLoaded +
                ", isInit=" + isInit +
                "} " + super.toString();
    }

    @Override
    Secret getNewInstance(String name) throws GeneralSecurityException, IOException {
        return new TestConvertSecret(name);
    }

    public static void main(String[] args) throws GeneralSecurityException, IOException {
//        args = new String[]{"-f=./SECRET_JUNIT3.ini", "-v=MariaDB_Database"};
//        Secret.main(args);

        Secret.main(args);
    }

    public void setHosts(String[] host, String[] port) throws GeneralSecurityException, IOException {
        if (host == null || host.length == 0) return;
        if (port == null || port.length == 0) return;
        if (host.length >= 1) {
            this.set(MariaDB_Host_Node1, host[0]);
            this.set(MariaDB_Port_Node1, port[0]);
        }

        if (host.length >= 2) {
            this.set(MariaDB_Host_Node2, host[1]);
            this.set(MariaDB_Port_Node2, port[1]);
        }
    }

    public void setDefaultDB() throws GeneralSecurityException, IOException {
        this.set(MariaDB_Database, this.getSecretFile().getName().replace(".ini", ""));
        this.set(MariaDB_Host_Node1, "10.3.1.200");
        this.set(MariaDB_Port_Node1, "3306");
        this.set(MariaDB_Host_Node2, "10.3.1.201");
        this.set(MariaDB_Port_Node2, "3306");
        this.set(MariaDB_User, "botiss-admin");
        this.set(MariaDB_Password, "");
    }

    public void setDefaultLDAP() throws GeneralSecurityException, IOException {
        this.set(LDAP_Host, "botiss.local");
        this.set(LDAP_Port, "389");
        this.set(LDAP_ADMIN_DN, "LDAP");
        this.set(LDAP_ADMIN_PW, "");
        this.set(LDAP_BaseDN, "OU=SBSUsers, OU=Users,OU=MyBusiness,DC=botiss,DC=local");
    }
}
