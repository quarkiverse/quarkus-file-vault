package io.quarkiverse.filevault.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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

        String decryptedPassword = decryptPassword(masked);
        assertEquals("storepassword", decryptedPassword);
    }

    private String decryptPassword(String masked) throws Exception {
        SecretKeySpec secretKey = EncryptionUtil.transformEncryptionKey("somearbitrarycrazystringthatdoesnotmatter");

        Cipher cipher = Cipher.getInstance(EncryptionUtil.ENC_ALGORITHM);
        ByteBuffer byteBuffer = ByteBuffer.wrap(Base64.getUrlDecoder().decode(masked.getBytes(StandardCharsets.UTF_8)));
        int ivLength = byteBuffer.get();
        byte[] iv = new byte[ivLength];
        byteBuffer.get(iv);
        byte[] encrypted = new byte[byteBuffer.remaining()];
        byteBuffer.get(encrypted);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(EncryptionUtil.ENC_TAG_LENGTH, iv));
        return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
    }

}
