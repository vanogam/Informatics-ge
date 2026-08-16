package ge.informatics.sandbox;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties properties = new Properties();

    static {
        loadDefaultConfig();
    }

    private static void loadDefaultConfig() {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Default configuration file not found");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load default configuration", e);
        }
    }

    public static void loadCustomConfig(String filePath) {
        try (InputStream input = new FileInputStream(filePath)) {
            properties.clear();
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration from " + filePath, e);
        }
    }

    public static void setProperties(String key, String value) {
        properties.setProperty(key, value);
    }


    /**
     * Configuration value for {@code key}, with an environment variable of the same name in
     * upper snake case taking precedence - "sandbox.image" is overridden by SANDBOX_IMAGE.
     * Workers are started as containers, so environment is how a deployment configures them.
     */
    public static String get(String key) {
        String fromEnvironment = System.getenv(environmentVariableFor(key));
        if (fromEnvironment != null && !fromEnvironment.isBlank()) {
            return fromEnvironment;
        }
        return properties.getProperty(key);
    }

    static String environmentVariableFor(String key) {
        return key.replace('.', '_')
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .toUpperCase(java.util.Locale.ROOT);
    }
}