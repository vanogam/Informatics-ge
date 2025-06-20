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


    public static String get(String key) {
        return properties.getProperty(key);
    }
}