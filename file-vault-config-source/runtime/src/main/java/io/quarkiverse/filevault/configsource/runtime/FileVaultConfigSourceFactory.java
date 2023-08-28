package io.quarkiverse.filevault.configsource.runtime;

import static java.util.Collections.emptyList;

import java.util.List;

import org.eclipse.microprofile.config.spi.ConfigSource;

import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory.ConfigurableConfigSourceFactory;

public class FileVaultConfigSourceFactory implements ConfigurableConfigSourceFactory<FileVaultBootstrapConfig> {
    @Override
    public Iterable<ConfigSource> getConfigSources(final ConfigSourceContext context, final FileVaultBootstrapConfig config) {
        if (config.keystorePath().isPresent()) {
            return List.of(new FileVaultConfigSource(config));
        } else {
            return emptyList();
        }
    }
}
