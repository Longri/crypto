package de.longri.utils;

import org.apache.commons.cli.*;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.reflections.Reflections;

import java.io.*;
import java.lang.reflect.Constructor;
import java.security.GeneralSecurityException;
import java.util.*;

import static de.longri.utils.UTIL.SERIAL_NUMBER;

public abstract class Secret {

    private File secretFile;
    private final HashMap<String, NamedProperty> secretList = new HashMap<>();
    private final String NAME;
    private final boolean ENCRYPT_ALL_OLD_VALUES;

    protected Secret() throws GeneralSecurityException, IOException {
        this(true);
    }

    protected Secret(boolean encryptOld) throws GeneralSecurityException, IOException {
        ENCRYPT_ALL_OLD_VALUES = encryptOld;
        NAME = "SECRET";
        secretFile = new File("./SECRET.ini");
        init();
    }


    public Secret(File file) throws GeneralSecurityException, IOException {
        this(file, true);
    }

    public Secret(File file, boolean encryptOld) throws GeneralSecurityException, IOException {
        ENCRYPT_ALL_OLD_VALUES = encryptOld;
        NAME = file.getName();
        secretFile = file;
        init();
    }

    public Secret(String name) throws GeneralSecurityException, IOException {
        this(name, true);
    }

    public Secret(String name, boolean encryptOld) throws GeneralSecurityException, IOException {
        ENCRYPT_ALL_OLD_VALUES = encryptOld;
        NAME = name;
        secretFile = new File("./" + name + ".ini");
        init();
    }

    /**
     * Diese Methode ist veraltet und sollte nicht mehr verwendet werden.
     * Verwenden Sie stattdessen {@link #initial(NamedProperty property)}.
     *
     * @deprecated seit Version 1.6.0. Verwenden Sie {@link #initial(NamedProperty property)}.
     */
    @Deprecated(since = "1.6.0", forRemoval = true)
    protected void put(String name, String comment) {
        secretList.put(name, new NamedStringProperty(name, comment));
    }

    /**
     * Initializes the given named property with the specified comment.
     *
     * @param property the NamedProperty object to be initialized
     */
    protected void initial(NamedProperty property) {
        secretList.put(property.getName(), property);
    }


    /**
     * put values with description on secureList.
     * <p>
     * this.put("Secret1", "Secret for Access")
     * this.put("Name", "Comment")
     */
    public abstract void init() throws GeneralSecurityException, IOException;

    public void set(String name, String value) throws GeneralSecurityException, IOException {
        NamedProperty propperty = secretList.get(name);
        if (propperty == null) {
            throw new RuntimeException("Cannot find property: " + name);
        }
        propperty.setValue(value);
    }

    public String get(String name) throws GeneralSecurityException, IOException {
        NamedProperty property = secretList.get(name);
        if (property == null) {
            throw new RuntimeException("Cannot find property: " + name);
        }
        return property.getValue();
    }

    public File getSecretFile() {
        return secretFile;
    }

    public boolean exist() {
        return getSecretFile().exists();
    }

    public void load() throws IOException, GeneralSecurityException, ConfigurationException {

        Configurations configs = new Configurations();
        INIConfiguration config = configs.ini(this.getSecretFile());

        Set<String> sections = config.getSections();
        if (sections.isEmpty() || !(sections.contains("SECRET") || sections.contains("CONFIG"))) {
            // read an old version
            Properties prop = new Properties();
            prop.load(new FileInputStream(this.getSecretFile()));
            for (String name : prop.stringPropertyNames()) {
                String value = prop.getProperty(name);
                if (value == null) continue;
                NamedProperty secret = secretList.get(name);
                if (secret != null) {

                    if (secret instanceof NamedEncryptedStringProperty encryptedStringProperty) {
                        encryptedStringProperty.encryptedValue = value;
                    } else {
                        //replace it with Encrypted property
                        if (ENCRYPT_ALL_OLD_VALUES) {
                            NamedEncryptedStringProperty encryptedStringProperty = new NamedEncryptedStringProperty(name);
                            encryptedStringProperty.encryptedValue = value;
                            encryptedStringProperty.setComment(secret.getComment());
                            secretList.remove(name);
                            secretList.put(name, encryptedStringProperty);
                        } else {
                            secret.setValue(Crypto.decrypt(value, SERIAL_NUMBER));
                        }
                    }
                }
            }
        } else {
            for (String sectionName : sections) {
                SubnodeConfiguration section = config.getSection(sectionName);

                Iterator<String> keys = section.getKeys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = section.getString(key);
                    if (value == null) continue;
                    NamedProperty secret = secretList.get(key);
                    if(sectionName.equals("SECRET")){
                        if (secret instanceof NamedEncryptedStringProperty encryptedStringProperty) {
                            encryptedStringProperty.encryptedValue = value;
                        }else{
                            throw new RuntimeException("Can't set a secret section value to a non encrypted property: " + key + " (" + secret.getClass().getSimpleName() + ")");
                        }
                    }else{
                        secret.setValue(value);
                    }

                }
            }
        }
    }

    public void save() throws IOException, GeneralSecurityException, ConfigurationException {
        Configurations configs = new Configurations();

        //if file not exist, create a new one!
        if (!this.getSecretFile().exists()) {
            if (!this.getSecretFile().createNewFile()) {
                throw new IOException("Can't create File: " + this.getSecretFile().getAbsolutePath());
            }
        } else {
            //delete and create a new one for determine file is empty
            if (!this.getSecretFile().delete()) {
                throw new IOException("Can't delete File: " + this.getSecretFile().getAbsolutePath());
            }

            if (!this.getSecretFile().createNewFile()) {
                throw new IOException("Can't create File: " + this.getSecretFile().getAbsolutePath());
            }
        }

        INIConfiguration config = configs.ini(this.getSecretFile());

        for (String name : secretList.keySet()) {
            NamedProperty property = secretList.get(name);
            String value = property.getValue();
            if (value == null) continue;

            if (property instanceof NamedEncryptedStringProperty encryptedStringProperty) {
                config.setProperty("SECRET." + name, encryptedStringProperty.encryptedValue);
            } else {
                config.setProperty("CONFIG." + name, value);
            }
        }
        config.write(new FileWriter(this.getSecretFile()));
    }

    public Secret copy() throws GeneralSecurityException, IOException {
        return copy(this.NAME);
    }

    public Secret copy(String newName) throws GeneralSecurityException, IOException {
        Secret copy = getNewInstance(newName);
        for (String name : secretList.keySet()) {
            NamedProperty property = secretList.get(name);
            NamedProperty copyProperty = copy.secretList.get(name);
            copyProperty.setValue(property.getValue());
        }
        return copy;
    }

    abstract Secret getNewInstance(String Name) throws GeneralSecurityException, IOException;

    // ask for Values and write encrypted to File
    public static void main(String[] args) throws GeneralSecurityException, IOException {

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        Options options = getOptions();

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("LINUX_PRTG_Sensor", options);
            System.exit(1);
        }

        Scanner scanner = new Scanner(System.in);
        // Use reflections to find and register all subclasses of JobType
        Reflections reflections = new Reflections("de.longri.utils");

        Set<Class<? extends Secret>> subclasses = reflections.getSubTypesOf(Secret.class);
        for (Class<? extends Secret> subclass : subclasses) {
            try {
                if (hasDefaultConstructor(subclass)) {
                    Secret instance = subclass.newInstance();

                    if (cmd.hasOption("f")) {
                        //load secret file
                        String path = cmd.getOptionValue("f");
                        instance.secretFile = new File(path);
                        instance.load();
                    }

                    String propertyName = null;
                    if (cmd.hasOption("v")) {
                        propertyName = cmd.getOptionValue("v");
                    }


                    for (String name : instance.secretList.keySet()) {
                        if (propertyName != null && !name.equals(propertyName)) continue;
                        NamedProperty property = instance.secretList.get(name);
                        System.out.println("Enter secret for " + property.getComment() + ":");
                        String input = scanner.nextLine();
                        instance.set(name, input);
                    }
                    instance.save();
                }
            } catch (InstantiationException | IllegalAccessException | ConfigurationException e) {
                e.printStackTrace();
            }
        }
        scanner.close();

    }

    static private Options getOptions() {
        Options options = new Options();
        Option o = new Option("f", "file", true, "Changed File Name");
        o.setRequired(false);
        o.setArgs(2);
        o.setValueSeparator(',');
        options.addOption(o);

        o = new Option("v", "value", true, "Changed Property Name");
        o.setRequired(false);
        o.setArgs(2);
        o.setValueSeparator(',');
        options.addOption(o);

        return options;
    }

    /**
     * Checks if the given class has a default constructor.
     *
     * @param clazz the class to check
     * @return true if the class has a default constructor, false otherwise
     */
    private static boolean hasDefaultConstructor(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            return constructor.isAccessible() || constructor.trySetAccessible();
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
