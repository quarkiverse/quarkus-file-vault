package io.quarkiverse.filevault.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 *
 * @author pesilva@redhat.com
 *
 */
public class EncryptionUtilTest {

    @Test
    public void testEncryptSecret() throws Exception {
        String masked = EncryptionUtil.encrypt("storepassword",
                EncryptionUtil.encodeToString("somearbitrarycrazystringthatdoesnotmatter".getBytes(StandardCharsets.UTF_8)));
        assertNotEquals("storepassword", masked);

        String decryptedPassword = EncryptionUtil.decrypt(masked, "somearbitrarycrazystringthatdoesnotmatter");
        assertEquals("storepassword", decryptedPassword);
    }
}
