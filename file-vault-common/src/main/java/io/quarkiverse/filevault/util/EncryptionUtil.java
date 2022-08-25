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
     * @param encryptionKey the encryption key in Base64URL encoded format
     * @return the encrypted value
     */
    public static String encrypt(final String strToEncrypt, final String encryptionKey) {
        try {
            String decodedEncryptionKey = new String(Base64.getUrlDecoder().decode(encryptionKey), StandardCharsets.UTF_8);
            SecretKeySpec secretKey = transformEncryptionKey(decodedEncryptionKey);
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

            return encodeToString(message.array());
        } catch (Exception e) {
            LOGGER.error("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static String encodeToString(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    /**
     * @param strToDecrypt the string to decrypt
     * @param encryptionKey the encryption key
     * @return the decrypted value
     */
    public static String decrypt(final String strToDecrypt, final String encryptionKey) {
        try {
            SecretKeySpec secretKey = transformEncryptionKey(encryptionKey);

            Cipher cipher = Cipher.getInstance(EncryptionUtil.ENC_ALGORITHM);
            ByteBuffer byteBuffer = ByteBuffer
                    .wrap(Base64.getUrlDecoder().decode(strToDecrypt.getBytes(StandardCharsets.UTF_8)));
            int ivLength = byteBuffer.get();
            byte[] iv = new byte[ivLength];
            byteBuffer.get(iv);
            byte[] encrypted = new byte[byteBuffer.remaining()];
            byteBuffer.get(encrypted);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(EncryptionUtil.ENC_TAG_LENGTH, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.error("Error while decrypting: " + e.toString());
            throw new RuntimeException(e);
        }
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

    public static String generateAndEncodeEncryptionKey() {
        return encodeToString(generateEncryptionKey().getEncoded());
    }

    public static SecretKey generateEncryptionKey() {
        try {
            return KeyGenerator.getInstance("AES").generateKey();
        } catch (Exception e) {
            LOGGER.error("Error while generating the encryption key: " + e.toString());
        }
        return null;
    }
}
