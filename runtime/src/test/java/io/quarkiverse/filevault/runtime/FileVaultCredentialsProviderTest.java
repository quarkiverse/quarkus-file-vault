package io.quarkiverse.filevault.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

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
    public void testGetPasswordOnly() throws Exception {
        CredentialsProvider cp = createCredentialsProvider(true, false);
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

        String derEncodedCert = creds.get(FileVaultCredentialsProvider.CERTIFICATE_PROPERTY);
        CertificateFactory cf = java.security.cert.CertificateFactory
                .getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf
                .generateCertificate(new ByteArrayInputStream(derEncodedCert.getBytes(StandardCharsets.ISO_8859_1)));
        assertTrue(cert.getSubjectX500Principal().getName().startsWith("CN=Quarkus,OU=Quarkus,O=Quarkus"));
    }

    private CredentialsProvider createCredentialsProvider(boolean includeAlias, boolean setAliasAsUser) {
        FileVaultConfig config = new FileVaultConfig();
        Map<String, String> keyStoreProps = new HashMap<>();
        keyStoreProps.put("path", "dbpasswords.p12");
        keyStoreProps.put("secret", "storepassword");
        if (includeAlias) {
            keyStoreProps.put("alias", KEY_ALIAS);
        }
        config.provider = Map.of(PROVIDER_NAME, keyStoreProps);
        config.setAliasAsUser = setAliasAsUser;
        return new FileVaultCredentialsProvider(config);
    }
}
