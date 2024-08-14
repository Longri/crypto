package de.longri.utils;

import org.reflections.Reflections;

import java.io.*;
import java.lang.reflect.Constructor;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

public abstract class Secret {

    private final File secretFile;
    private final HashMap<String, NamedStringProperty> secretList = new HashMap<>();
    private final String NAME;
    private static final String SERIAL_NUMBER = UTIL.getSystemInfo().get("serialNumber");


    protected Secret() {
        NAME = "SECRET";
        secretFile = new File("./SECRET.ini");
        init();
    }

    public Secret(File file) {
        NAME = file.getName();
        secretFile = file;
        init();
    }

    public Secret(String name) {
        NAME = name;
        secretFile = new File("./" + name + ".ini");
        init();
    }

    protected void put(String name, String comment) {
        secretList.put(name, new NamedStringProperty(name, comment));
    }

    /**
     * put values with description on secureList.
     * <p>
     * this.put("Secret1", "Secret for Access")
     * this.put("Name", "Comment")
     *
     */
    public abstract void init();

    public void set(String name, String value) throws GeneralSecurityException, UnsupportedEncodingException {
        String encryptedValue = Crypto.encrypt(value, SERIAL_NUMBER);
        NamedStringProperty propperty = secretList.get(name);
        if (propperty == null) {
            throw new RuntimeException("Cannot find property: " + name);
        }
        propperty.setValue(encryptedValue);
    }

    public String get(String name) throws GeneralSecurityException, IOException {
        NamedStringProperty property = secretList.get(name);
        if (property == null) {
            throw new RuntimeException("Cannot find property: " + name);
        }
        String encryptedValue = property.getValue();
        return Crypto.decrypt(encryptedValue, SERIAL_NUMBER);
    }

    public File getSecretFile() {
        return secretFile;
    }

    public boolean exist() {
        return getSecretFile().exists();
    }

    public void load() throws IOException, GeneralSecurityException {
        Properties prop = new Properties();
        prop.load(new FileInputStream(this.getSecretFile()));

        for (String name : prop.stringPropertyNames()) {
            String value = prop.getProperty(name);
            NamedStringProperty secret = secretList.get(name);
            if (secret != null) {
                secret.setValue(value);
            }
        }
    }

    public void save() throws IOException, GeneralSecurityException {
        Properties prop = new Properties();
        for (String name : secretList.keySet()) {
            NamedStringProperty property = secretList.get(name);
            String encryptedValue = property.getValue();
            prop.setProperty(name, encryptedValue);
        }
        prop.store(new FileOutputStream(this.getSecretFile()), null);
    }

    public Secret copy() throws GeneralSecurityException, IOException {

        Secret copy = getNewInstance(this.NAME);

        for (String name : secretList.keySet()) {
            NamedStringProperty property = secretList.get(name);
            String encryptedValue = property.getValue();
            copy.set(name, Crypto.decrypt(encryptedValue, SERIAL_NUMBER));
        }
        return copy;
    }

    abstract Secret getNewInstance(String Name);

    // ask for Values and write encrypted to File
    public static void main(String[] args) throws GeneralSecurityException, IOException {
        Scanner scanner = new Scanner(System.in);
        // Use reflections to find and register all subclasses of JobType
        Reflections reflections = new Reflections("de.longri.utils");

        Set<Class<? extends Secret>> subclasses = reflections.getSubTypesOf(Secret.class);
        for (Class<? extends Secret> subclass : subclasses) {
            try {
                if (hasDefaultConstructor(subclass)) {
                    Secret instance = subclass.newInstance();
                    for (String name : instance.secretList.keySet()) {
                        NamedStringProperty property = instance.secretList.get(name);
                        System.out.println("Enter secret for " + property.getComment() + ":");
                        String input = scanner.nextLine();
                        instance.set(name, input);
                    }
                    instance.save();
                }
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        scanner.close();

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