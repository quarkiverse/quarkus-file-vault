package io.quarkiverse.filevault.configsource.runtime;

import java.util.List;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

public class FileVaultConfigSourceProvider implements ConfigSourceProvider {

    private FileVaultBootstrapConfig fileVaultBootstrapConfig;

    public FileVaultConfigSourceProvider(FileVaultBootstrapConfig fileVaultBootstrapConfig) {
        this.fileVaultBootstrapConfig = fileVaultBootstrapConfig;
    }

    @Override
    public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
        return List.of(new FileVaultConfigSource(fileVaultBootstrapConfig));
    }
}
