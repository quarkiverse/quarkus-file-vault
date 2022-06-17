package io.quarkiverse.filevault;

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

    @Option(names = { "-e", "--encryption-key" }, description = "(mandatory) Encryption Key", required = true)
    String encryptionKey;

    @Option(names = { "-p", "--keystore-password" }, description = "(mandatory) Keystore password", required = true)
    String keystorePassword;

    @Override
    public void run() {
        String encrypted = EncryptionUtil.encrypt(keystorePassword, encryptionKey);

        System.out.println(
                "######################################################################################################");
        System.out.println(
                "Please, add the following paramenters on your application.properties file, and replace the <name> value!"
                        + "\nThe <name> will be used in the consumer to refer to this provider.\n");

        System.out.println("quarkus.file.vault.provider.<name>.encryption-key=" + encryptionKey);
        System.out.println("quarkus.file.vault.provider.<name>.secret=" + encrypted);

        System.out.println(
                "######################################################################################################");
    }

    @Override
    public int run(String... args) throws Exception {
        return new CommandLine(this, factory).execute(args);
    }

}
