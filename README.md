# granary

## Table of Contents
- [Build and run](#build-and-run)
- [Docker build and run](#docker-build-and-run)
- [License](#license)

## Build and run
Build the project from the project root:
```
mvn clean package
```

Run the project from the project root:
```
java -jar granary/target/granary-0.0.1-SNAPSHOT.jar
```

## Docker build and run
Raise the full docker setup, including a PostgreSQL instance:
```
docker compose up
```

## License
This project is licensed under the terms of the GNU Affero General Public License.
