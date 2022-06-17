/**
 * 
 */
package io.quarkiverse.filevault.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.jboss.logging.Logger;

/**
 * @author pesilva@redhat.com
 *
 */
public class EncryptionUtil {

    static final String ENC_ALGORITHM = "AES/GCM/NoPadding";
    static final int ENC_TAG_LENGTH = 128;

    private static final Logger LOGGER = Logger.getLogger(EncryptionUtil.class.getName());

    /**
     * @param strToEncrypt the string to encrypt
     * @param encryptionKey the encryption key
     * @return the encrypted value
     */
    public static String encrypt(final String strToEncrypt, final String encryptionKey) {
        try {
            SecretKeySpec secretKey = transformEncryptionKey(encryptionKey);
            return encrypt(strToEncrypt, secretKey);
        } catch (Exception e) {
            LOGGER.error("Error while encrypting: " + e.toString());
        }
        return null;
    }

    /**
     * @param strToEncrypt the string to encrypt
     * @param encryptionKey the encryption key
     * @return the encrypted value
     */
    public static String encrypt(final String strToEncrypt, final SecretKey encryptionKey) {
        try {
            Cipher cipher = Cipher.getInstance(ENC_ALGORITHM);
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new GCMParameterSpec(ENC_TAG_LENGTH, iv));

            byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));

            ByteBuffer message = ByteBuffer.allocate(1 + iv.length + encrypted.length);
            message.put((byte) iv.length);
            message.put(iv);
            message.put(encrypted);

            return Base64.getUrlEncoder().withoutPadding().encodeToString(message.array());
        } catch (Exception e) {
            LOGGER.error("Error while encrypting: " + e.toString());
        }
        return null;
    }

    /**
     * @param encryptionKey the encryption key
     * @return the transformed encryption key
     */
    public static SecretKeySpec transformEncryptionKey(final String encryptionKey) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(encryptionKey.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(sha256.digest(), "AES");
        } catch (Exception e) {
            LOGGER.error("Error while transforming the encryption key: " + e.toString());
        }
        return null;
    }

    public static SecretKey generateEncryptionKey() {
        try {
            return KeyGenerator.getInstance("AES").generateKey();
        } catch (Exception e) {
            LOGGER.error("Error while transforming the encryption key: " + e.toString());
        }
        return null;
    }
}
