package io.quarkiverse.filevault.it;

import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;

public class KeyStoreConfigSource implements ConfigSource {

    @Override
    public Set<String> getPropertyNames() {
        return Set.of("db1.storepassword");
    }

    @Override
    public int getOrdinal() {
        return 10;
    }

    @Override
    public String getValue(String propertyName) {
        return "db1.storepassword".equals(propertyName) ? "storepassword" : null;
    }

    @Override
    public String getName() {
        return "custom-config-source";
    }

}
