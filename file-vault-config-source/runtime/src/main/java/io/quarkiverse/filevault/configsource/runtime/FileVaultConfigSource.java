package io.quarkiverse.filevault.configsource.runtime;

import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;

import io.quarkiverse.filevault.util.KeyStoreUtil;
import io.quarkiverse.filevault.util.KeyStoreUtil.KeyStoreEntry;

public class FileVaultConfigSource implements ConfigSource {
    private final Map<String, KeyStoreEntry> storeProperties;
    private final int ordinal;

    public FileVaultConfigSource(FileVaultBootstrapConfig config) {
        storeProperties = KeyStoreUtil.readKeyStore(
                config.keystorePath().orElse(null),
                config.keystoreSecret().orElse(null),
                config.encryptionKey().orElse(null));
        ordinal = config.configOrdinal();
    }

    @Override
    public String getName() {
        return "file-vault-config-source";
    }

    @Override
    public int getOrdinal() {
        return ordinal;
    }

    @Override
    public Set<String> getPropertyNames() {
        return Set.copyOf(storeProperties.keySet());
    }

    @Override
    public String getValue(String propertyName) {
        return storeProperties.containsKey(propertyName) ? storeProperties.get(propertyName).getValue() : null;
    }
}
