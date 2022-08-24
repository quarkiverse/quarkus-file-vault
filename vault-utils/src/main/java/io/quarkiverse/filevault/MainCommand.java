package io.quarkiverse.filevault;

import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import io.quarkiverse.filevault.util.EncryptionUtil;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 *
 * @author pesilva@redhat.com
 *
 */
@QuarkusMain
@Command(name = "Encrypt Secret Util", mixinStandardHelpOptions = true, version = "0.1.0")
public class MainCommand implements Runnable, QuarkusApplication {
    @Inject
    CommandLine.IFactory factory;

    @Option(names = { "-e", "--encryption-key" }, description = "(optional) Encryption Key")
    String encryptionKey;

    @Option(names = { "-p", "--keystore-password" }, description = "(mandatory) Keystore password", required = true)
    String keystorePassword;

    @Override
    public void run() {

        if (encryptionKey == null) {
            encryptionKey = EncryptionUtil.generateAndEncodeEncryptionKey();
        } else {
            encryptionKey = EncryptionUtil.encodeToString(encryptionKey.getBytes(StandardCharsets.UTF_8));
        }

        String encrypted = EncryptionUtil.encrypt(keystorePassword, encryptionKey);

        System.out.println(
                "############################################################################################################################################");
        System.out.println(
                "Please, add the following parameters to application.properties if you use File Vault as CredentialsProvider and replace the <keystore-name>:");
        System.out.println("quarkus.file.vault.provider.<keystore-name>.encryption-key=" + encryptionKey);
        System.out.println("quarkus.file.vault.provider.<keystore-name>.secret=" + encrypted);
        System.out.println();
        System.out.println(
                "Please, add the following parameters to application.properties if you use File Vault as ConfigSource:");
        System.out.println("quarkus.file.vault-config-source.encryption-key=" + encryptionKey);
        System.out.println("quarkus.file.vault-config-source.keystore-secret=" + encrypted);
        System.out.println(
                "############################################################################################################################################");
    }

    @Override
    public int run(String... args) throws Exception {
        return new CommandLine(this, factory).execute(args);
    }

}
