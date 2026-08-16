package ge.insformatics.sandbox;

import ge.informatics.sandbox.Config;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Deployments configure workers through the environment, so key mapping has to be exact. */
public class ConfigTest {

    private String envVarFor(String key) throws Exception {
        Method m = Config.class.getDeclaredMethod("environmentVariableFor", String.class);
        m.setAccessible(true);
        return (String) m.invoke(null, key);
    }

    @Test
    void mapsConfigKeysToEnvironmentVariableNames() throws Exception {
        assertEquals("SANDBOX_IMAGE", envVarFor("sandbox.image"));
        assertEquals("SANDBOX_CPUSET", envVarFor("sandbox.cpuset"));
        assertEquals("SANDBOX_MEMORY_MB", envVarFor("sandbox.memoryMB"));
        assertEquals("FILE_STORAGE_DIRECTORY_URL", envVarFor("fileStorageDirectory.url"));
    }

    @Test
    void fallsBackToTheBundledProperties() {
        // No SANDBOX_IMAGE set in the test JVM, so the packaged default applies.
        assertEquals("sandbox:latest", Config.get("sandbox.image"));
        assertNotNull(Config.get("fileservice.type"));
    }
}
