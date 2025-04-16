# granary

## Table of Contents
- [SOPS and properties](#sops-and-properties)
- [Expected properties](#expected-properties)
- [Build and run](#build-and-run)
- [Docker build and run](#docker-build-and-run)
- [License](#license)

## SOPS and properties

Install AGE and SOPS:
* https://github.com/FiloSottile/age (v1.2.1)
* https://github.com/getsops/sops (v3.10.1)

To generate a passphrase-protected AGE secret key file execute:
```
age-keygen | age -p > /path/to/age/secret/key/file
```

Then add the public key to `.sops.yaml` under `.creation_rules.age`.

See [Expected properties](#expected-properties) for the environment variables, and properties expected to run Granary. You can encrypt the properties and environment files via:
```
sops encrypt --input-type dotenv --output-type dotenv .env > .sops.env
sops encrypt --input-type dotenv --output-type dotenv properties/src/main/resources/granary.prs > properties/src/main/resources/sops.granary.prs
```

To decrypt, you must first specify the AGE secret key file environment variable:
```
export SOPS_AGE_KEY_FILE=/path/to/age/secret/key/file
```

You can decrypt the environment and properties files via:
```
sops decrypt --input-type dotenv --output-type dotenv .sops.env > .env
sops decrypt --input-type dotenv --output-type dotenv properties/src/main/resources/sops.granary.prs > properties/src/main/resources/granary.prs
```

## Expected properties

The decrypted `.env` file should contain the relevant environment variables for raising a Postgres container:
```
HOST_PG_PORT=
HOST_API_PORT=
PG_DB=
PG_USER=
PG_PASS=
PG_PORT=
API_PORT=
DB_CONNECTION_URL=
```

The decrypted `properties/src/main/resources/granary.prs` file should contain the relevant properties for running the Granary application:
```
granary.db.url=
granary.db.user=
granary.db.pass=
granary.db.connection.pool.max.total=
granary.db.connection.pool.max.idle=
granary.db.connection.pool.min.idle=
```

## Build and run
You can build the project from the project root via:
```
mvn clean package
```

You can run the project from the project root via:
```
java -jar granary/target/granary-0.0.1-SNAPSHOT.jar
```

## Docker build and run
To raise the full docker setup, including a PostgreSQL instance, run the following from the project root:
```
docker compose up
```

To decrypt the SOPS-encrypted configuration files and raise the Granary Docker environment, run:
```
bash docker.sh /path/to/age/secret/key/file
```

## License
This project is licensed under the terms of the GNU Affero General Public License.
