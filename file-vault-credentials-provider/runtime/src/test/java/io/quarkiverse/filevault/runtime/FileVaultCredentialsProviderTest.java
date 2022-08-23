package io.quarkiverse.filevault.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.quarkiverse.filevault.util.EncryptionUtil;
import io.quarkus.credentials.CredentialsProvider;

public class FileVaultCredentialsProviderTest {

    private static final String PROVIDER_NAME = "db1";
    private static final String KEY_ALIAS = "quarkus_alias";
    private static final String CERTIFICATE_ALIAS = "mykey";
    private static final String STORED_PASSWORD = "quarkus_test";

    @Test
    public void testGetUserAndPassword() throws Exception {
        CredentialsProvider cp = createCredentialsProvider(true, true);
        Map<String, String> creds = cp.getCredentials(FileVaultCredentialsProvider.BASE_PROVIDER_NAME + "." + PROVIDER_NAME);
        assertEquals(2, creds.size());
        assertEquals(KEY_ALIAS, creds.get(CredentialsProvider.USER_PROPERTY_NAME));
        assertEquals(STORED_PASSWORD, creds.get(CredentialsProvider.PASSWORD_PROPERTY_NAME));
    }

    @Test
    public void testGetAllCredentials() throws Exception {
        CredentialsProvider cp = createCredentialsProvider(false, false);
        Map<String, String> creds = cp.getCredentials(FileVaultCredentialsProvider.BASE_PROVIDER_NAME + "." + PROVIDER_NAME);
        assertEquals(2, creds.size());
        assertEquals(STORED_PASSWORD, creds.get(KEY_ALIAS));
        verifyCertificate(creds.get(CERTIFICATE_ALIAS));
    }

    @Test
    public void testGetPasswordOnly() throws Exception {
        CredentialsProvider cp = createCredentialsProvider(true, false);
        Map<String, String> creds = cp.getCredentials(FileVaultCredentialsProvider.BASE_PROVIDER_NAME + "." + PROVIDER_NAME);
        assertEquals(1, creds.size());
        assertNull(creds.get(CredentialsProvider.USER_PROPERTY_NAME));
        assertEquals(STORED_PASSWORD, creds.get(CredentialsProvider.PASSWORD_PROPERTY_NAME));
    }

    @Test
    public void testGetMaskedPasswordOnly() throws Exception {
        String masked = EncryptionUtil.encrypt("storepassword", encode(
                "somearbitrarycrazystringthatdoesnotmatter"));
        assertNotEquals("storepassword", masked);
        CredentialsProvider cp = createCredentialsProvider(true, false, true, masked);
        Map<String, String> creds = cp.getCredentials(FileVaultCredentialsProvider.BASE_PROVIDER_NAME + "." + PROVIDER_NAME);
        assertEquals(1, creds.size());
        assertNull(creds.get(CredentialsProvider.USER_PROPERTY_NAME));
        assertEquals(STORED_PASSWORD, creds.get(CredentialsProvider.PASSWORD_PROPERTY_NAME));
    }

    @Test
    public void testGetPasswordOnlyWithAliasInName() throws Exception {
        CredentialsProvider cp = createCredentialsProvider(false, false);
        Map<String, String> creds = cp.getCredentials(
                FileVaultCredentialsProvider.BASE_PROVIDER_NAME + "." + PROVIDER_NAME + "." + KEY_ALIAS);
        assertEquals(1, creds.size());
        assertNull(creds.get(CredentialsProvider.USER_PROPERTY_NAME));
        assertEquals(STORED_PASSWORD, creds.get(CredentialsProvider.PASSWORD_PROPERTY_NAME));
    }

    @Test
    public void testGetCertificate() throws Exception {
        CredentialsProvider cp = createCredentialsProvider(false, false);
        Map<String, String> creds = cp.getCredentials(
                FileVaultCredentialsProvider.BASE_PROVIDER_NAME + "." + PROVIDER_NAME + "." + CERTIFICATE_ALIAS);
        assertEquals(1, creds.size());
        assertNull(creds.get(CredentialsProvider.USER_PROPERTY_NAME));
        assertNull(creds.get(CredentialsProvider.PASSWORD_PROPERTY_NAME));

        verifyCertificate(creds.get(FileVaultCredentialsProvider.CERTIFICATE_PROPERTY));
    }

    private void verifyCertificate(String derEncodedCert) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf
                .generateCertificate(new ByteArrayInputStream(derEncodedCert.getBytes(StandardCharsets.ISO_8859_1)));
        assertTrue(cert.getSubjectX500Principal().getName().startsWith("CN=Quarkus,OU=Quarkus,O=Quarkus"));

    }

    private Map<String, String> createKeystoreProps(boolean includeAlias, boolean isSecretMasked, String secret) {
        Map<String, String> keyStoreProps = new HashMap<>();
        keyStoreProps.put("path", "dbpasswords.p12");
        keyStoreProps.put("secret", secret);

        if (includeAlias) {
            keyStoreProps.put("alias", KEY_ALIAS);
        }

        if (isSecretMasked) {
            keyStoreProps.put("encryption-key", encode("somearbitrarycrazystringthatdoesnotmatter"));
        }

        return keyStoreProps;
    }

    private CredentialsProvider createCredentialsProvider(boolean includeAlias, boolean setAliasAsUser) {
        return createCredentialsProvider(includeAlias, setAliasAsUser, false, "storepassword");
    }

    private CredentialsProvider createCredentialsProvider(boolean includeAlias, boolean setAliasAsUser, boolean isSecretMasked,
            String secret) {
        FileVaultConfig config = new FileVaultConfig();
        config.provider = Map.of(PROVIDER_NAME, createKeystoreProps(includeAlias, isSecretMasked, secret));
        config.setAliasAsUser = setAliasAsUser;
        return new FileVaultCredentialsProvider(config);
    }

    private static String encode(String data) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }
}
