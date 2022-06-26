# vault-utils Project

This project is an extra layer for the ["quarkus-file-vault"](https://github.com/quarkiverse/quarkus-file-vault).
It can be used to encrypt a keystore password in order to mask it.
All the parameters necessary for the ["quarkus-file-vault"](https://github.com/quarkiverse/quarkus-file-vault)
to work will be printed in the system output.

## Getting Started

1. Fork and clone this project.
2. Package the project:

```
mvn clean install
```

## How to use the tool.

1. Encrypting the keystore password. You can run the `--help` parameter, to see the options:

```
$ java -jar target/quarkus-app/quarkus-run.jar --help
Usage: Encrypt Secret Util [-hV] -e=<encryptionKey>
  -e, --encryption-key=<encryptionKey> Encryption Key
  -h, --help          Show this help message and exit.
  -p, --keystore-password=<keystorePassword> (mandatory) Keystore password
  -V, --version       Print version information and exit.
```

The only mandatory parameter is the keystore password: `-p, --keystore-password`.

If the encryption key is provided then it must be at least 16 characters long.
If the encryption key is not provided then it will be auto-generated instead.

You can encrypt the keystore password like this:

```
$ java -jar target/quarkus-app/quarkus-run.jar -p storedpass -e the_encryption_key
```

You should to see something like that at the output:

```
######################################################################################################
Please add the following parameters on your application.properties file, and replace the <name> value!
The <name> will be used in the consumer to refer to this provider.

quarkus.file.vault.provider.<name>.encryption-key=the_encryption_key
quarkus.file.vault.provider.<name>.secret=4VLLc9bk+WMnQMR3ezJcpw==
######################################################################################################
```
